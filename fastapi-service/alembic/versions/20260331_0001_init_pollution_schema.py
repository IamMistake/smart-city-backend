"""init pollution schema

Revision ID: 20260331_0001
Revises:
Create Date: 2026-03-31 00:00:00
"""

from __future__ import annotations

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision = "20260331_0001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
	op.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto")
	op.execute("CREATE SCHEMA IF NOT EXISTS pollution")

	op.create_table(
		"pollution_sensors",
		sa.Column(
			"id",
			postgresql.UUID(as_uuid=True),
			nullable=False,
			server_default=sa.text("gen_random_uuid()"),
		),
		sa.Column(
			"created_at",
			sa.DateTime(timezone=True),
			nullable=False,
			server_default=sa.text("now()"),
		),
		sa.Column(
			"updated_at",
			sa.DateTime(timezone=True),
			nullable=False,
			server_default=sa.text("now()"),
		),
		sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
		sa.Column("provider", sa.String(length=64), nullable=False, server_default="pulse_eco"),
		sa.Column("city_name", sa.String(length=100), nullable=False),
		sa.Column("external_sensor_id", sa.String(length=100), nullable=False),
		sa.Column("sensor_type_code", sa.String(length=32), nullable=True),
		sa.Column("description", sa.String(length=255), nullable=True),
		sa.Column("comments", sa.String(length=500), nullable=True),
		sa.Column("status", sa.String(length=64), nullable=True),
		sa.Column("latitude", sa.Float(), nullable=True),
		sa.Column("longitude", sa.Float(), nullable=True),
		sa.Column("is_active", sa.Boolean(), nullable=False, server_default=sa.text("true")),
		sa.Column("last_synced_at", sa.DateTime(timezone=True), nullable=True),
		sa.PrimaryKeyConstraint("id"),
		sa.UniqueConstraint(
			"provider",
			"city_name",
			"external_sensor_id",
			name="uq_pollution_sensors_provider_city_external",
		),
		schema="pollution",
	)

	op.create_table(
		"pollution_measurements",
		sa.Column(
			"id",
			postgresql.UUID(as_uuid=True),
			nullable=False,
			server_default=sa.text("gen_random_uuid()"),
		),
		sa.Column(
			"created_at",
			sa.DateTime(timezone=True),
			nullable=False,
			server_default=sa.text("now()"),
		),
		sa.Column(
			"updated_at",
			sa.DateTime(timezone=True),
			nullable=False,
			server_default=sa.text("now()"),
		),
		sa.Column("sensor_id", postgresql.UUID(as_uuid=True), nullable=False),
		sa.Column("measured_at", sa.DateTime(timezone=True), nullable=False),
		sa.Column("metric_type", sa.String(length=32), nullable=False),
		sa.Column("metric_value", sa.String(length=64), nullable=False),
		sa.Column("source_year", sa.Integer(), nullable=True),
		sa.Column("raw_payload", postgresql.JSONB(astext_type=sa.Text()), nullable=True),
		sa.ForeignKeyConstraint(["sensor_id"], ["pollution.pollution_sensors.id"]),
		sa.PrimaryKeyConstraint("id"),
		sa.UniqueConstraint(
			"sensor_id",
			"measured_at",
			"metric_type",
			name="uq_pollution_measurements_sensor_stamp_metric",
		),
		schema="pollution",
	)

	op.create_index(
		"idx_pollution_sensors_deleted_at",
		"pollution_sensors",
		["deleted_at"],
		unique=False,
		schema="pollution",
	)
	op.create_index(
		"idx_pollution_measurements_sensor_measured_at",
		"pollution_measurements",
		["sensor_id", "measured_at"],
		unique=False,
		schema="pollution",
	)


	op.drop_index(
		"idx_pollution_measurements_sensor_measured_at",
		table_name="pollution_measurements",
		schema="pollution",
	)
	op.drop_index(
		"idx_pollution_sensors_deleted_at",
		table_name="pollution_sensors",
		schema="pollution",
	)
	op.drop_table("pollution_measurements", schema="pollution")
	op.drop_table("pollution_sensors", schema="pollution")
	op.execute("DROP SCHEMA IF EXISTS pollution CASCADE")
