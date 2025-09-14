ALTER TABLE wf_task
  ADD COLUMN IF NOT EXISTS form_key TEXT;

-- (optional but handy) index if you’ll query by form_key
CREATE INDEX IF NOT EXISTS idx_wf_task_form_key ON wf_task(form_key);

-- Add direct assignee on a task
ALTER TABLE wf_task
  ADD COLUMN IF NOT EXISTS assignee TEXT,
  ADD COLUMN IF NOT EXISTS priority INT,
  ADD COLUMN IF NOT EXISTS due_date timestamptz;

-- Candidates: normalized table (avoids Postgres array mapping hassles)
CREATE TABLE IF NOT EXISTS wf_task_candidate (
  task_id   uuid NOT NULL REFERENCES wf_task(id) ON DELETE CASCADE,
  type      varchar(1) NOT NULL CHECK (type IN ('U','G')),  -- U=user, G=group
  candidate TEXT NOT NULL,
  PRIMARY KEY (task_id, type, candidate)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS wf_task_assignee_idx ON wf_task(assignee);
CREATE INDEX IF NOT EXISTS wf_task_candidate_user_idx  ON wf_task_candidate(type, candidate) WHERE type='U';
CREATE INDEX IF NOT EXISTS wf_task_candidate_group_idx ON wf_task_candidate(type, candidate) WHERE type='G';
