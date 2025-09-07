-- Create tenant table for multi-tenant support
CREATE TABLE tenant (
  id                VARCHAR(50) PRIMARY KEY,
  name              VARCHAR(100) NOT NULL,
  db_url            VARCHAR(255) NOT NULL,
  db_username       VARCHAR(100) NOT NULL,
  db_password       VARCHAR(100) NOT NULL,
  driver_class_name VARCHAR(255) NOT NULL DEFAULT 'org.postgresql.Driver',
  active            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create trigger to update the updated_at timestamp
CREATE TRIGGER trg_tenant_mtime BEFORE UPDATE ON tenant
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Insert default tenant
INSERT INTO tenant (id, name, db_url, db_username, db_password, created_at, updated_at)
VALUES ('default', 'Default Tenant', 'jdbc:postgresql://localhost:5432/miniflow', 'postgres', 'postgres', now(), now());

-- Made with Bob
