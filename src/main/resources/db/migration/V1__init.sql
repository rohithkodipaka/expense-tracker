CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS expenses (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id uuid NOT NULL,
  amount numeric(19,2) NOT NULL,
  currency varchar(10) NOT NULL,
  category varchar(100) NOT NULL,
  description text,
  occurred_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL
);

CREATE TABLE IF NOT EXISTS monthly_aggregates (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id uuid NOT NULL,
  year int NOT NULL,
  month int NOT NULL,
  category varchar(100) NOT NULL,
  total_amount numeric(19,2) NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL,
  CONSTRAINT monthly_uniq UNIQUE (user_id, year, month, category)
);

CREATE TABLE IF NOT EXISTS outbox_events (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  aggregate_type varchar(50) NOT NULL,
  aggregate_id uuid NOT NULL,
  event_type varchar(100) NOT NULL,
  payload_json jsonb NOT NULL,
  created_at timestamptz NOT NULL,
  published boolean NOT NULL DEFAULT false,
  published_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_outbox_published_created
ON outbox_events(published, created_at);
