-- QA로그 DB 스키마 (PostgreSQL / Supabase)
-- writing-block.md 원문 합의 내용을 반영. 컬럼 실제 자료형/길이는 이 파일이 기준.
--
-- 2026-09-11 MySQL -> PostgreSQL(Supabase) 포팅하면서 발견해 같이 고친 것 (동기에게 전달 완료 필요):
--   1) issue_histories.old_value/new_value: 원안은 JSON 타입이었는데, 실제 코드(IssueServiceImpl)는
--      "확인 중" 같은 화면 표시용 순수 문자열을 그대로 저장한다 - JSON 타입 컬럼은 값이 유효한 JSON
--      이어야 해서(따옴표 없는 문자열은 무효) 이대로면 INSERT 자체가 실패한다. TEXT로 변경.
--   2) issue_histories.change_group_id: 원안은 ID(숫자) 타입이었는데, 실제 코드는
--      UUID.randomUUID().toString()로 문자열을 쓴다. VARCHAR(36)으로 변경.
--   3) MySQL의 "ON UPDATE CURRENT_TIMESTAMP"는 PostgreSQL에 없어 트리거로 대체(파일 하단).

CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id      VARCHAR(50)     NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    display_name  VARCHAR(50)     NOT NULL,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_login_id UNIQUE (login_id)
);

-- 팀 공용 가입 코드. 단일 행만 유지.
CREATE TABLE team_settings (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invite_code_hash  VARCHAR(255)    NOT NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(200)    NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'working',
    created_by  BIGINT          NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_projects_status CHECK (status IN ('working', 'in_progress', 'maintenance', 'archived'))
);

CREATE TABLE issues (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_number        BIGINT          NOT NULL,
    project_id          BIGINT          NOT NULL REFERENCES projects (id),
    title               VARCHAR(200)    NULL,
    location            VARCHAR(300)    NULL,
    location_url        VARCHAR(500)    NULL,
    description         TEXT            NULL,
    steps_to_reproduce  TEXT            NULL,
    expected_result     TEXT            NULL,
    actual_result       TEXT            NULL,
    test_version        VARCHAR(50)     NULL,
    test_environment    TEXT            NULL,
    suggested_fix       TEXT            NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'new',
    severity            VARCHAR(20)     NOT NULL DEFAULT 'unspecified',
    priority            VARCHAR(20)     NOT NULL DEFAULT 'unspecified',
    assignee_id         BIGINT          NULL REFERENCES users (id),
    created_by          BIGINT          NOT NULL REFERENCES users (id),
    updated_by          BIGINT          NOT NULL REFERENCES users (id),
    closed_by           BIGINT          NULL REFERENCES users (id),
    closed_at           TIMESTAMP       NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_issues_issue_number UNIQUE (issue_number)
);

CREATE INDEX idx_issues_project_status_updated ON issues (project_id, status, updated_at);
CREATE INDEX idx_issues_project_assignee_status ON issues (project_id, assignee_id, status);

CREATE TABLE issue_attachments (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_id      BIGINT          NOT NULL REFERENCES issues (id),
    original_name VARCHAR(255)    NOT NULL,
    storage_key   VARCHAR(255)    NOT NULL,
    mime_type     VARCHAR(100)    NOT NULL,
    size_bytes    BIGINT          NOT NULL,
    uploaded_by   BIGINT          NOT NULL REFERENCES users (id),
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- NULL=오류 본문 전체에 딸린 일반 첨부, 'expected_result'/'actual_result'=해당 필드 전용 첨부
    -- (2026-09-11 실사용 피드백으로 추가 - 기대결과/실제결과 입력란에도 이미지 첨부 가능하게)
    context       VARCHAR(20)     NULL
);

CREATE INDEX idx_issue_attachments_issue_created ON issue_attachments (issue_id, created_at);

CREATE TABLE issue_comments (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_id    BIGINT          NOT NULL REFERENCES issues (id),
    content     TEXT            NOT NULL,
    created_by  BIGINT          NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_issue_comments_issue_created ON issue_comments (issue_id, created_at);

CREATE TABLE issue_histories (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_id           BIGINT          NOT NULL REFERENCES issues (id),
    change_group_id    VARCHAR(36)     NOT NULL,
    event_type         VARCHAR(20)     NOT NULL,
    field_name         VARCHAR(50)     NULL,
    old_value          TEXT            NULL,
    new_value          TEXT            NULL,
    related_record_id  BIGINT          NULL,
    actor_id           BIGINT          NOT NULL REFERENCES users (id),
    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_issue_histories_issue_created ON issue_histories (issue_id, created_at);

-- issue_number는 NOT NULL인데 id(GENERATED ALWAYS AS IDENTITY)와 같은 값을 쓰기로 했다(오류등록_기능명세서.md
-- 2.2). "INSERT 후 별도 UPDATE로 채운다"는 애플리케이션 레벨 2단계 방식은 NOT NULL 제약이 트랜잭션 커밋까지
-- 유예되지 않아 INSERT 시점에 즉시 위반되므로 실제로는 동작하지 않는다(Supabase 연결 테스트 중 실제로
-- 이 오류를 재현해서 발견함) - DB가 INSERT 시점에 트리거로 직접 채우도록 수정한다. Postgres는 BEFORE ROW
-- 트리거가 실행되기 전에 identity 컬럼 기본값을 이미 채워두므로 트리거 안에서 NEW.id를 바로 쓸 수 있다.
CREATE OR REPLACE FUNCTION set_issue_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.issue_number = NEW.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_issues_set_issue_number BEFORE INSERT ON issues FOR EACH ROW EXECUTE FUNCTION set_issue_number();

-- MySQL의 "ON UPDATE CURRENT_TIMESTAMP" 대체 - updated_at 있는 테이블마다 UPDATE 시 자동 갱신
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at    BEFORE UPDATE ON users    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_team_settings_updated_at BEFORE UPDATE ON team_settings FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_projects_updated_at BEFORE UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_issues_updated_at   BEFORE UPDATE ON issues   FOR EACH ROW EXECUTE FUNCTION set_updated_at();
