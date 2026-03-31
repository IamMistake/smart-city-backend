from __future__ import annotations

import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


def utc_now() -> datetime:
	return datetime.now(UTC)


class Base(DeclarativeBase):
	pass


class BaseModel(Base):
	__abstract__ = True

	id: Mapped[uuid.UUID] = mapped_column(
		UUID(as_uuid=True),
		primary_key=True,
		default=uuid.uuid4,
	)


class CreationAwareModel(BaseModel):
	__abstract__ = True

	created_at: Mapped[datetime] = mapped_column(
		DateTime(timezone=True),
		nullable=False,
		default=utc_now,
	)
	updated_at: Mapped[datetime] = mapped_column(
		DateTime(timezone=True),
		nullable=False,
		default=utc_now,
		onupdate=utc_now,
	)


class SoftDeleteModel(CreationAwareModel):
	__abstract__ = True

	deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
