from __future__ import annotations

import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, UniqueConstraint
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import CreationAwareModel


class PollutionMeasurement(CreationAwareModel):
	__tablename__ = "pollution_measurements"
	__table_args__ = (
		UniqueConstraint(
			"sensor_id",
			"measured_at",
			"metric_type",
			name="uq_pollution_measurements_sensor_stamp_metric",
		),
		Index(
			"idx_pollution_measurements_sensor_measured_at",
			"sensor_id",
			"measured_at",
		),
		{"schema": "pollution"},
	)

	sensor_id: Mapped[uuid.UUID] = mapped_column(
		UUID(as_uuid=True),
		ForeignKey("pollution.pollution_sensors.id"),
		nullable=False,
	)
	measured_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
	metric_type: Mapped[str] = mapped_column(String(32), nullable=False)
	metric_value: Mapped[str] = mapped_column(String(64), nullable=False)
	source_year: Mapped[int | None] = mapped_column(nullable=True)
	raw_payload: Mapped[dict | None] = mapped_column(JSONB)

	sensor = relationship("PollutionSensor", back_populates="measurements")
