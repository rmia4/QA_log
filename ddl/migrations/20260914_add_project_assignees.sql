-- 기존 프로젝트에 다중 담당자와 담당자 전용 권한을 추가한다.
CREATE TABLE IF NOT EXISTS project_assignees (
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_project_assignees_user_project
    ON project_assignees (user_id, project_id);

-- 기능 적용 전에 존재하던 프로젝트는 생성자를 최초 담당자로 지정해 관리 불능 상태를 막는다.
INSERT INTO project_assignees (project_id, user_id)
SELECT id, created_by
FROM projects
ON CONFLICT (project_id, user_id) DO NOTHING;
