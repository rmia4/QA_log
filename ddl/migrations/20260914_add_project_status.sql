ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE projects
SET status = 'working'
WHERE status IS NULL;

ALTER TABLE projects
    ALTER COLUMN status SET DEFAULT 'working',
    ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_projects_status'
    ) THEN
        ALTER TABLE projects
            ADD CONSTRAINT ck_projects_status
            CHECK (status IN ('working', 'in_progress', 'maintenance', 'archived'));
    END IF;
END
$$;
