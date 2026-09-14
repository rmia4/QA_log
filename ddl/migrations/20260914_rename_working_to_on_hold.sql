ALTER TABLE projects
    DROP CONSTRAINT IF EXISTS ck_projects_status;

UPDATE projects
SET status = 'on_hold'
WHERE status = 'working';

ALTER TABLE projects
    ALTER COLUMN status SET DEFAULT 'in_progress';

ALTER TABLE projects
    ADD CONSTRAINT ck_projects_status
    CHECK (status IN ('in_progress', 'maintenance', 'on_hold', 'archived'));
