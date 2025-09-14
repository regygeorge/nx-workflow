CREATE TABLE wf_process (
  process_id   text PRIMARY KEY,
  name         text NOT NULL,
  bpmn_xml     text,
  deployed_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE wf_instance (
  id           uuid PRIMARY KEY,
  process_id   text NOT NULL REFERENCES wf_process(process_id) ON DELETE RESTRICT,
  business_key text,
  status       text NOT NULL CHECK (status IN ('RUNNING','COMPLETED')),
  variables    jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX wf_instance_process_id_idx ON wf_instance(process_id);
CREATE INDEX wf_instance_status_idx     ON wf_instance(status);

CREATE TABLE wf_token (
  id           uuid PRIMARY KEY,
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  node_id      text NOT NULL,
  active       boolean NOT NULL DEFAULT true,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX wf_token_instance_active_idx ON wf_token(instance_id, active);

CREATE TABLE wf_join (
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  node_id      text NOT NULL,
  arrivals     int  NOT NULL,
  PRIMARY KEY (instance_id, node_id)
);

CREATE TABLE wf_task (
  id           uuid PRIMARY KEY,
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  token_id     uuid REFERENCES wf_token(id) ON DELETE SET NULL,
  node_id      text NOT NULL,
  name         text NOT NULL,
  state        text NOT NULL CHECK (state IN ('OPEN','COMPLETED','CANCELLED')),
  created_at   timestamptz NOT NULL DEFAULT now(),
  completed_at timestamptz,
  due_date_time timestamptz
);
CREATE INDEX wf_task_instance_state_idx ON wf_task(instance_id, state);

-- Postgres
CREATE TABLE IF NOT EXISTS wf_variable (
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  "key"        text NOT NULL,
  value_text   text NOT NULL,                                -- store JSON as text
  value_jsonb  jsonb GENERATED ALWAYS AS (value_text::jsonb) STORED,
  updated_at   timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (instance_id, "key")
);

-- Optional: JSONB index for fast JSON queries
CREATE INDEX IF NOT EXISTS idx_wf_variable_json ON wf_variable USING gin (value_jsonb);

-- Optional: instance filter index
CREATE INDEX IF NOT EXISTS idx_wf_variable_instance ON wf_variable (instance_id);


CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_instance_mtime BEFORE UPDATE ON wf_instance
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_token_mtime BEFORE UPDATE ON wf_token
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
