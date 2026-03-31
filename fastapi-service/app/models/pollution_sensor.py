from __future__ import annotations

from datetime import datetime

from sqlalchemy import Boolean, DateTime, Index, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import SoftDeleteModel


class PollutionSensor(SoftDeleteModel):
	__tablename__ = "pollution_sensors"
	__table_args__ = (
		UniqueConstraint(
			"provider",
			"city_name",
			"external_sensor_id",
			name="uq_pollution_sensors_provider_city_external",
		),
		Index("idx_pollution_sensors_deleted_at", "deleted_at"),
		{"schema": "pollution"},
	)

	provider: Mapped[str] = mapped_column(String(64), nullable=False, default="pulse_eco")
	city_name: Mapped[str] = mapped_column(String(100), nullable=False)
	external_sensor_id: Mapped[str] = mapped_column(String(100), nullable=False)
	sensor_type_code: Mapped[str | None] = mapped_column(String(32))
	description: Mapped[str | None] = mapped_column(String(255))
	comments: Mapped[str | None] = mapped_column(String(500))
	status: Mapped[str | None] = mapped_column(String(64))
	latitude: Mapped[float | None] = mapped_column(nullable=True)
	longitude: Mapped[float | None] = mapped_column(nullable=True)
	is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
	last_synced_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))

	measurements = relationship("PollutionMeasurement", back_populates="sensor")
