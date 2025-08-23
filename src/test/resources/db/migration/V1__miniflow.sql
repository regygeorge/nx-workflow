CREATE TABLE wf_process (
  process_id   varchar(255) PRIMARY KEY,
  name         varchar(255) NOT NULL,
  bpmn_xml     text,
  deployed_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE wf_instance (
  id           uuid PRIMARY KEY,
  process_id   varchar(255) NOT NULL REFERENCES wf_process(process_id) ON DELETE RESTRICT,
  business_key varchar(255),
  status       varchar(20) NOT NULL CHECK (status IN ('RUNNING','COMPLETED')),
  variables    varchar(4000) NOT NULL DEFAULT '{}',
  created_at   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP(),
  updated_at   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP()
);
CREATE INDEX wf_instance_process_id_idx ON wf_instance(process_id);
CREATE INDEX wf_instance_status_idx     ON wf_instance(status);

CREATE TABLE wf_token (
  id           uuid PRIMARY KEY,
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  node_id      varchar(255) NOT NULL,
  active       boolean NOT NULL DEFAULT true,
  created_at   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP(),
  updated_at   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP()
);
CREATE INDEX wf_token_instance_active_idx ON wf_token(instance_id, active);

CREATE TABLE wf_join (
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  node_id      varchar(255) NOT NULL,
  arrivals     int  NOT NULL,
  PRIMARY KEY (instance_id, node_id)
);

CREATE TABLE wf_task (
  id           uuid PRIMARY KEY,
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  token_id     uuid REFERENCES wf_token(id) ON DELETE SET NULL,
  node_id      varchar(255) NOT NULL,
  name         varchar(255) NOT NULL,
  state        varchar(20) NOT NULL CHECK (state IN ('OPEN','COMPLETED','CANCELLED')),
  created_at   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP(),
  completed_at timestamp,
  due_date_time timestamp
);
CREATE INDEX wf_task_instance_state_idx ON wf_task(instance_id, state);

CREATE TABLE wf_variable (
  instance_id  uuid NOT NULL REFERENCES wf_instance(id) ON DELETE CASCADE,
  key          varchar(255) NOT NULL,
  value        varchar(4000) NOT NULL,
  PRIMARY KEY (instance_id, key)
);

-- H2 doesn't support GIN index, so we'll skip it
-- CREATE INDEX wf_variable_gin ON wf_variable USING gin (value);

-- H2 trigger syntax is different, so we'll use H2-specific syntax
CREATE TRIGGER trg_instance_mtime BEFORE UPDATE ON wf_instance
FOR EACH ROW CALL "org.h2.api.Trigger.UPDATE_TIMESTAMP";

CREATE TRIGGER trg_token_mtime BEFORE UPDATE ON wf_token
FOR EACH ROW CALL "org.h2.api.Trigger.UPDATE_TIMESTAMP";

-- Made with Bob
