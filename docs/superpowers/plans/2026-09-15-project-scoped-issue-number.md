# Project-Scoped Issue Number Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Change `issues.issue_number` from a service-wide sequence to a sequence that starts at 1 inside each project.

**Architecture:** PostgreSQL remains responsible for assigning display numbers in a `BEFORE INSERT` trigger. The trigger locks the parent `projects` row, then calculates `MAX(issue_number) + 1` inside that project; a composite unique constraint provides the final integrity check. Existing rows are renumbered deterministically by `project_id` and `id ASC` in one migration transaction.

**Tech Stack:** PostgreSQL/Supabase, PL/pgSQL, Maven, Java 8, MyBatis

**Spec:** `docs/오류등록_기능명세서.md` section 2.2 and `docs/프로젝트_테이블_명세서.md` sections 6 and 12

## Global Constraints

- Keep `issues.id` as the global primary key.
- Assign `issue_number` from 1 independently for each `project_id`.
- Enforce uniqueness with `UNIQUE (project_id, issue_number)`.
- Do not add a counter table.
- Serialize concurrent inserts for the same project by locking its `projects` row.
- Renumber existing rows with `ROW_NUMBER() OVER (PARTITION BY project_id ORDER BY id)`.
- Do not add issue deletion or number reuse behavior.
- Keep MyBatis `insertIssue` free of `issue_number`; the database trigger owns numbering.

---

### Task 1: Update canonical schema and create migration

**Files:**
- Modify: `ddl/schema.sql`
- Create: `ddl/migrations/20260915_project_scoped_issue_number.sql`

**Interfaces:**
- Consumes: existing `projects(id)` and `issues(project_id, issue_number, id)` columns
- Produces: `uk_issues_project_issue_number` and `trg_issues_set_issue_number`

- [ ] **Step 1: Confirm the old policy is present**

Run:

```powershell
rg -n "uk_issues_issue_number|NEW.issue_number = NEW.id" ddl/schema.sql
```

Expected: both old global-numbering definitions are found.

- [ ] **Step 2: Create the migration**

Create `ddl/migrations/20260915_project_scoped_issue_number.sql` with one transaction that:

```sql
ALTER TABLE issues DROP CONSTRAINT IF EXISTS uk_issues_issue_number;
ALTER TABLE issues DROP CONSTRAINT IF EXISTS uk_issues_project_issue_number;

WITH numbered AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY project_id ORDER BY id) AS new_issue_number
    FROM issues
)
UPDATE issues AS i
SET issue_number = numbered.new_issue_number
FROM numbered
WHERE i.id = numbered.id;

ALTER TABLE issues
    ADD CONSTRAINT uk_issues_project_issue_number UNIQUE (project_id, issue_number);
```

Disable `trg_issues_updated_at` only for the renumbering UPDATE and enable it immediately afterward so existing modification timestamps remain unchanged. Then replace `set_issue_number()` so it locks `projects.id = NEW.project_id`, selects the next number only from that project, and returns `NEW`.

- [ ] **Step 3: Apply the same final-state policy to `ddl/schema.sql`**

Replace the global unique constraint and the `NEW.id` trigger assignment with the composite constraint and project-row-locking trigger used by the migration.

- [ ] **Step 4: Review SQL invariants**

Confirm that the migration drops the global constraint before renumbering, preserves existing `updated_at` values, adds the composite constraint after renumbering, and recreates the trigger before commit. Confirm the trigger filters `MAX(issue_number)` by `NEW.project_id` and uses `FOR UPDATE` on the matching project row first.

### Task 2: Synchronize application comments and specifications

**Files:**
- Modify: `src/main/java/egovframework/issue/service/impl/IssueServiceImpl.java`
- Modify: `docs/오류등록_기능명세서.md`
- Modify: `docs/프로젝트_테이블_명세서.md`

**Interfaces:**
- Consumes: the database-owned numbering behavior from Task 1
- Produces: documentation that describes the same uniqueness and concurrency policy

- [ ] **Step 1: Update the Java comment**

State that `trg_issues_set_issue_number` assigns the next number inside the selected project. Do not change MyBatis or Java behavior.

- [ ] **Step 2: Rewrite registration specification section 2.2**

Document project-scoped numbering, parent-row locking, `MAX()+1`, the composite unique constraint, deterministic migration order, and the default `READ COMMITTED` concurrency assumption.

- [ ] **Step 3: Update the table specification**

Change the `issue_number` description and recommended unique index from global uniqueness to `(project_id, issue_number)` uniqueness.

### Task 3: Verify the change

**Files:**
- Verify all modified files

**Interfaces:**
- Consumes: Tasks 1 and 2
- Produces: a reviewable branch commit

- [ ] **Step 1: Search for stale policy text**

Run:

```powershell
rg -n "서비스 전체에서 고유|NEW.issue_number = NEW.id|issues.issue_number.: 고유|uk_issues_issue_number" ddl docs src/main/java -g "!docs/superpowers/plans/**"
```

Expected: no active documentation or source comment describes the old global policy; the old constraint name may appear only in the migration drop statement.

- [ ] **Step 2: Check formatting and run regression tests**

Run:

```powershell
git diff --check
mvn -B -ntp test
```

Expected: no whitespace errors and all tests pass.

- [ ] **Step 3: Review the final diff and commit**

Confirm that MyBatis insert SQL is unchanged, then commit the schema, migration, documentation, comment, and plan together.