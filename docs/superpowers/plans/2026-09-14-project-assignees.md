# 프로젝트 담당자 권한 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** 프로젝트마다 여러 담당자를 지정하고 담당자만 프로젝트 편집·보관과 오류 댓글 작성을 할 수 있게 한다.

**Architecture:** `project_assignees` 연결 테이블로 프로젝트와 사용자를 다대다로 연결한다. 프로젝트 서비스가 담당자 저장과 권한 판정을 맡고, 프로젝트 컨트롤러와 댓글 서비스가 서버에서 권한을 강제한다. 화면은 동일한 사용자 목록으로 생성·편집 체크박스를 그리고 좌측 프로젝트 항목에 담당자 표시명을 보여준다.

**Tech Stack:** Java 8, Spring MVC, MyBatis, JSP/JSTL, PostgreSQL/Supabase, JUnit 4

**Spec:** `docs/프로젝트_담당자_기능명세서.md`

## Global Constraints

- 프로젝트 생성 시 한 명 이상의 담당자가 필요하며 생성자는 기본 선택한다.
- 프로젝트는 여러 담당자를 가질 수 있다.
- 프로젝트 편집·보관과 오류 댓글 작성은 해당 프로젝트 담당자만 가능하다.
- 프로젝트 조회와 오류·댓글 조회 권한은 기존처럼 모든 로그인 사용자에게 유지한다.
- 향후 `+A` 권한은 같은 프로젝트 담당자 판정을 재사용하되 이번 구현에는 버튼이나 API를 추가하지 않는다.
- 권한은 버튼 숨김만으로 처리하지 않고 서버에서도 검사한다.

---

### Task 1: 데이터 모델과 명세

**Files:**
- Create: `docs/프로젝트_담당자_기능명세서.md`
- Modify: `docs/프로젝트_테이블_명세서.md`
- Modify: `docs/오류상세_기능명세서.md`
- Modify: `ddl/schema.sql`
- Create: `ddl/migrations/20260914_add_project_assignees.sql`

**Interfaces:**
- Produces: `project_assignees(project_id, user_id)`와 프로젝트별 담당자 조회·권한 판정에 필요한 인덱스

- [x] **Step 1:** 신규·기존 DB용 DDL과 권한 명세를 작성한다.
- [x] **Step 2:** 연결 테이블의 복합 기본키와 두 외래키를 XML/DDL 검증으로 확인한다.

### Task 2: 프로젝트 담당자 저장과 조회

**Files:**
- Modify: `ProjectVO.java`, `ProjectMapper.java`, `projects.xml`
- Modify: `ProjectService.java`, `ProjectServiceImpl.java`
- Test: `ProjectServiceImplTest.java`

**Interfaces:**
- Consumes: `project_assignees`
- Produces: `createProject(name, status, createdBy, assigneeIds)`, `updateProject(id, name, status, assigneeIds, actorId)`, `isProjectAssignee(projectId, userId)`

- [x] **Step 1:** 생성·편집 시 담당자 교체와 권한 거부를 검증하는 실패 테스트를 작성한다.
- [x] **Step 2:** 테스트가 컴파일 또는 단언 실패하는지 실행한다.
- [x] **Step 3:** 담당자 ID 정규화, 최소 1명 검증, 트랜잭션 저장, 목록 표시명을 구현한다.
- [x] **Step 4:** 프로젝트 서비스 테스트를 통과시킨다.

### Task 3: 프로젝트 화면과 컨트롤러 권한

**Files:**
- Modify: `MainController.java`, `ProjectController.java`
- Modify: `main.jsp`, `main.css`
- Test: `MainControllerTest.java`, `ProjectControllerTest.java`

**Interfaces:**
- Consumes: 사용자 선택 목록, 선택 프로젝트의 `assigneeIds`, `isProjectAssignee`
- Produces: 생성·편집 체크박스, 담당자 전용 편집 버튼, 좌측 목록 담당자 3번째 줄

- [x] **Step 1:** 다중 `assigneeIds` 전달과 비담당자 편집·보관 거부 테스트를 작성한다.
- [x] **Step 2:** 실패를 확인한다.
- [x] **Step 3:** 컨트롤러 모델과 폼·목록 UI를 구현한다.
- [x] **Step 4:** 관련 테스트를 통과시킨다.

### Task 4: 댓글 작성 권한

**Files:**
- Modify: `IssueCommentServiceImpl.java`
- Modify: `IssueViewController.java`, `detail.jsp`
- Test: `IssueCommentServiceImplTest.java`

**Interfaces:**
- Consumes: 오류의 `projectId`, `ProjectService.isProjectAssignee`
- Produces: 담당자만 댓글 저장 가능, 비담당자에게 읽기 전용 댓글 탭 표시

- [x] **Step 1:** 담당자는 댓글을 저장하고 비담당자는 `IssueForbiddenException`을 받는 실패 테스트를 작성한다.
- [x] **Step 2:** 실패를 확인한다.
- [x] **Step 3:** 서비스 권한 검사와 댓글 작성 폼 조건부 표시를 구현한다.
- [x] **Step 4:** 관련 테스트를 통과시킨다.

### Task 5: 통합 검증과 커밋

**Files:**
- Verify: 모든 변경 파일

**Interfaces:**
- Consumes: Tasks 1-4 결과
- Produces: 빌드 가능한 기능 브랜치

- [x] **Step 1:** 전체 Maven 테스트를 실행한다.
- [x] **Step 2:** MyBatis XML과 SQL 파일을 정적 검증한다.
- [x] **Step 3:** `git diff --check`와 변경 범위를 확인한다.
- [x] **Step 4:** 한 기능 커밋으로 저장한다.
