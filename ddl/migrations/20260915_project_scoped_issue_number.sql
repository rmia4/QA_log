-- issues.issue_number를 서비스 전역 번호에서 프로젝트별 번호로 변경한다.
-- ALTER TABLE이 issues에 ACCESS EXCLUSIVE 잠금을 잡으므로 재번호 처리 중 새 오류 등록은 대기한다.
BEGIN;

ALTER TABLE issues
    DROP CONSTRAINT IF EXISTS uk_issues_issue_number;

-- 재실행하거나 일부 적용된 환경에서도 최종 제약조건을 동일하게 다시 구성한다.
ALTER TABLE issues
    DROP CONSTRAINT IF EXISTS uk_issues_project_issue_number;

-- 기존 데이터는 각 프로젝트 안에서 오래된 PK(id)부터 1, 2, 3...으로 다시 번호를 부여한다.
-- 재번호가 기존 오류의 최근 수정 시각을 바꾸지 않도록 updated_at 트리거만 잠시 멈춘다.
ALTER TABLE issues DISABLE TRIGGER trg_issues_updated_at;
WITH numbered AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY project_id
            ORDER BY id
        ) AS new_issue_number
    FROM issues
)
UPDATE issues AS i
SET issue_number = numbered.new_issue_number
FROM numbered
WHERE i.id = numbered.id;

ALTER TABLE issues ENABLE TRIGGER trg_issues_updated_at;

ALTER TABLE issues
    ADD CONSTRAINT uk_issues_project_issue_number
    UNIQUE (project_id, issue_number);

DROP TRIGGER IF EXISTS trg_issues_set_issue_number ON issues;
DROP FUNCTION IF EXISTS set_issue_number();

-- 같은 프로젝트의 INSERT끼리는 부모 projects 행 잠금에서 직렬화된다.
-- 서로 다른 프로젝트는 서로 다른 행을 잠그므로 번호 채번 때문에 상호 대기하지 않는다.
CREATE OR REPLACE FUNCTION set_issue_number()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM 1
    FROM projects
    WHERE id = NEW.project_id
    FOR UPDATE;

    SELECT COALESCE(MAX(issue_number), 0) + 1
    INTO NEW.issue_number
    FROM issues
    WHERE project_id = NEW.project_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER trg_issues_set_issue_number
BEFORE INSERT ON issues
FOR EACH ROW
EXECUTE FUNCTION set_issue_number();

COMMIT;