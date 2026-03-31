CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS core;

CREATE TYPE core.user_role AS ENUM (
	'CITIZEN',
	'OPERATOR',
	'AUTHORITY',
	'ADMIN'
);

CREATE TYPE core.incident_type AS ENUM (
	'FIRE',
	'ACCIDENT',
	'PROTEST',
	'POLLUTION',
	'POLICE_ACTIVITY',
	'OTHER'
);

CREATE TYPE core.priority_level AS ENUM (
	'LOW',
	'MEDIUM',
	'HIGH',
	'CRITICAL'
);

CREATE TYPE core.incident_status AS ENUM (
	'REPORTED',
	'ACTIVE',
	'RESOLVED',
	'REJECTED'
);

CREATE TYPE core.event_type AS ENUM (
	'PROTEST',
	'PUBLIC_EVENT',
	'POLICE_ACTIVITY',
	'ROAD_CLOSURE',
	'OTHER'
);

CREATE TYPE core.event_status AS ENUM (
	'PLANNED',
	'ACTIVE',
	'RESOLVED',
	'CANCELLED'
);

CREATE TYPE core.camera_status AS ENUM (
	'ONLINE',
	'OFFLINE',
	'MAINTENANCE'
);

CREATE TYPE core.police_unit_status AS ENUM (
	'AVAILABLE',
	'DISPATCHED',
	'ACTIVE',
	'OFFLINE'
);

CREATE OR REPLACE FUNCTION core.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
	NEW.updated_at = NOW();
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS core.user_profiles (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	deleted_at TIMESTAMPTZ,
	clerk_user_id VARCHAR(255) NOT NULL UNIQUE,
	email VARCHAR(320) NOT NULL UNIQUE,
	full_name VARCHAR(255),
	role core.user_role NOT NULL,
	avatar_url TEXT,
	is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS core.incidents (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	deleted_at TIMESTAMPTZ,
	reported_by_user_id UUID NOT NULL REFERENCES core.user_profiles(id),
	title VARCHAR(255) NOT NULL,
	description TEXT,
	incident_type core.incident_type NOT NULL,
	priority core.priority_level NOT NULL,
	status core.incident_status NOT NULL,
	latitude DECIMAL(9, 6) NOT NULL,
	longitude DECIMAL(9, 6) NOT NULL,
	address VARCHAR(255),
	occurred_at TIMESTAMPTZ,
	resolved_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS core.incident_status_history (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	incident_id UUID NOT NULL REFERENCES core.incidents(id),
	changed_by_user_id UUID REFERENCES core.user_profiles(id),
	old_status core.incident_status,
	new_status core.incident_status NOT NULL,
	note TEXT
);

CREATE TABLE IF NOT EXISTS core.events (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	deleted_at TIMESTAMPTZ,
	created_by_user_id UUID REFERENCES core.user_profiles(id),
	title VARCHAR(255) NOT NULL,
	description TEXT,
	event_type core.event_type NOT NULL,
	status core.event_status NOT NULL,
	latitude DECIMAL(9, 6) NOT NULL,
	longitude DECIMAL(9, 6) NOT NULL,
	address VARCHAR(255),
	start_time TIMESTAMPTZ,
	end_time TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS core.event_status_history (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	event_id UUID NOT NULL REFERENCES core.events(id),
	changed_by_user_id UUID REFERENCES core.user_profiles(id),
	old_status core.event_status,
	new_status core.event_status NOT NULL,
	note TEXT
);

CREATE TABLE IF NOT EXISTS core.cameras (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	deleted_at TIMESTAMPTZ,
	name VARCHAR(255) NOT NULL,
	provider VARCHAR(100),
	external_camera_id VARCHAR(100),
	latitude DECIMAL(9, 6) NOT NULL,
	longitude DECIMAL(9, 6) NOT NULL,
	status core.camera_status NOT NULL,
	stream_url TEXT,
	CONSTRAINT uq_camera_provider_external UNIQUE (provider, external_camera_id)
);

CREATE TABLE IF NOT EXISTS core.police_units (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	deleted_at TIMESTAMPTZ,
	unit_code VARCHAR(64) NOT NULL UNIQUE,
	display_name VARCHAR(255),
	status core.police_unit_status NOT NULL,
	current_latitude DECIMAL(9, 6),
	current_longitude DECIMAL(9, 6),
	last_reported_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_user_profiles_clerk_user_id ON core.user_profiles(clerk_user_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_deleted_at ON core.user_profiles(deleted_at);

CREATE INDEX IF NOT EXISTS idx_incidents_status_type ON core.incidents(status, incident_type);
CREATE INDEX IF NOT EXISTS idx_incidents_reported_by_user_id ON core.incidents(reported_by_user_id);
CREATE INDEX IF NOT EXISTS idx_incidents_deleted_at ON core.incidents(deleted_at);
CREATE INDEX IF NOT EXISTS idx_incidents_occurred_at ON core.incidents(occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_incident_status_history_incident ON core.incident_status_history(incident_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_events_status_type ON core.events(status, event_type);
CREATE INDEX IF NOT EXISTS idx_events_deleted_at ON core.events(deleted_at);
CREATE INDEX IF NOT EXISTS idx_event_status_history_event ON core.event_status_history(event_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_cameras_deleted_at ON core.cameras(deleted_at);

CREATE INDEX IF NOT EXISTS idx_police_units_status ON core.police_units(status);
CREATE INDEX IF NOT EXISTS idx_police_units_deleted_at ON core.police_units(deleted_at);

CREATE TRIGGER trg_user_profiles_set_updated_at
BEFORE UPDATE ON core.user_profiles
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();

CREATE TRIGGER trg_incidents_set_updated_at
BEFORE UPDATE ON core.incidents
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();

CREATE TRIGGER trg_incident_status_history_set_updated_at
BEFORE UPDATE ON core.incident_status_history
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();

CREATE TRIGGER trg_events_set_updated_at
BEFORE UPDATE ON core.events
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();

CREATE TRIGGER trg_event_status_history_set_updated_at
BEFORE UPDATE ON core.event_status_history
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();

CREATE TRIGGER trg_cameras_set_updated_at
BEFORE UPDATE ON core.cameras
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();

CREATE TRIGGER trg_police_units_set_updated_at
BEFORE UPDATE ON core.police_units
FOR EACH ROW
EXECUTE FUNCTION core.set_updated_at();
