from __future__ import annotations

from collections.abc import Generator

from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session, sessionmaker

from app.core.config import settings

engine = create_engine(
	settings.database_url,
	pool_pre_ping=True,
	future=True,
)

SessionLocal = sessionmaker(
	bind=engine,
	autocommit=False,
	autoflush=False,
	class_=Session,
)


def get_db() -> Generator[Session, None, None]:
	db = SessionLocal()
	try:
		yield db
	finally:
		db.close()


def check_db_connection() -> tuple[bool, str | None]:
	try:
		with engine.connect() as connection:
			connection.execute(text("SELECT 1"))
		return True, None
	except SQLAlchemyError as error:
		return False, str(error)
