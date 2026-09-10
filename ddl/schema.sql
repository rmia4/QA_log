-- QA로그 DB 스키마 (MySQL/MariaDB)
-- writing-block.md 원문 합의 내용을 그대로 반영. 컬럼 실제 자료형/길이는 이 파일이 기준.

CREATE TABLE users (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    login_id      VARCHAR(50)     NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    display_name  VARCHAR(50)     NOT NULL,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 팀 공용 가입 코드. 단일 행만 유지.
CREATE TABLE team_settings (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    invite_code_hash  VARCHAR(255)    NOT NULL,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE projects (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(200)    NOT NULL,
    created_by  BIGINT UNSIGNED NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE issues (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    issue_number        BIGINT UNSIGNED NOT NULL,
    project_id          BIGINT UNSIGNED NOT NULL,
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
    assignee_id         BIGINT UNSIGNED NULL,
    created_by          BIGINT UNSIGNED NOT NULL,
    updated_by          BIGINT UNSIGNED NOT NULL,
    closed_by           BIGINT UNSIGNED NULL,
    closed_at           DATETIME        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_issues_issue_number (issue_number),
    KEY idx_issues_project_status_updated (project_id, status, updated_at),
    KEY idx_issues_project_assignee_status (project_id, assignee_id, status),
    CONSTRAINT fk_issues_project     FOREIGN KEY (project_id)  REFERENCES projects (id),
    CONSTRAINT fk_issues_assignee    FOREIGN KEY (assignee_id) REFERENCES users (id),
    CONSTRAINT fk_issues_created_by  FOREIGN KEY (created_by)  REFERENCES users (id),
    CONSTRAINT fk_issues_updated_by  FOREIGN KEY (updated_by)  REFERENCES users (id),
    CONSTRAINT fk_issues_closed_by   FOREIGN KEY (closed_by)   REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE issue_attachments (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    issue_id      BIGINT UNSIGNED NOT NULL,
    original_name VARCHAR(255)    NOT NULL,
    storage_key   VARCHAR(255)    NOT NULL,
    mime_type     VARCHAR(100)    NOT NULL,
    size_bytes    BIGINT UNSIGNED NOT NULL,
    uploaded_by   BIGINT UNSIGNED NOT NULL,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_issue_attachments_issue_created (issue_id, created_at),
    CONSTRAINT fk_issue_attachments_issue       FOREIGN KEY (issue_id)    REFERENCES issues (id),
    CONSTRAINT fk_issue_attachments_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE issue_comments (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    issue_id    BIGINT UNSIGNED NOT NULL,
    content     TEXT            NOT NULL,
    created_by  BIGINT UNSIGNED NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_issue_comments_issue_created (issue_id, created_at),
    CONSTRAINT fk_issue_comments_issue      FOREIGN KEY (issue_id)   REFERENCES issues (id),
    CONSTRAINT fk_issue_comments_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE issue_histories (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    issue_id           BIGINT UNSIGNED NOT NULL,
    change_group_id    BIGINT UNSIGNED NOT NULL,
    event_type         VARCHAR(20)     NOT NULL,
    field_name         VARCHAR(50)     NULL,
    old_value          JSON            NULL,
    new_value          JSON            NULL,
    related_record_id  BIGINT UNSIGNED NULL,
    actor_id           BIGINT UNSIGNED NOT NULL,
    created_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_issue_histories_issue_created (issue_id, created_at),
    CONSTRAINT fk_issue_histories_issue    FOREIGN KEY (issue_id) REFERENCES issues (id),
    CONSTRAINT fk_issue_histories_actor_id FOREIGN KEY (actor_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
