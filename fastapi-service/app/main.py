from __future__ import annotations

import logging
import os

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes.auth import router as auth_router
from app.api.routes.health import router as health_router
from app.api.routes.pollution import router as pollution_router
from app.core.config import settings
from app.db.session import SessionLocal, check_db_connection      #SessionLocal
from app.seeder.demo_pollution_seeder import seed_pollution      #seed_polution
from app.db.session import check_db_connection
from app.services.pollution_service import pollution_service
from app.db.base import Base
from app.db.session import engine

logger = logging.getLogger(__name__)

app = FastAPI(title=settings.service_name)

app.add_middleware(
	CORSMiddleware,
	allow_origins=settings.cors_allowed_origins,
	allow_credentials=True,
	allow_methods=["*"],
	allow_headers=["*"],
)

app.include_router(health_router)
app.include_router(auth_router)
app.include_router(pollution_router)


@app.on_event("startup")
def startup_db_check() -> None:
	is_connected, error = check_db_connection()

	if not is_connected:
		logger.warning("Database unavailable on startup: %s", error)
		return

	if settings.demo_mode:                                          #block for demo
            logger.info(">>> [DEMO] Running pollution seeder...")
            db = SessionLocal()
            try:
                seed_pollution(db)
            except Exception as exc:
                logger.error(">>> [DEMO] Seeder failed: %s", exc)
            finally:
                db.close()

	if settings.pollution_startup_fetch and "PYTEST_CURRENT_TEST" not in os.environ:
		pollution_service.warm_cache()

@app.on_event("startup")
def init_db():
    Base.metadata.create_all(bind=engine)

@app.get("/")
def root() -> dict[str, str]:
	return {"message": "fastapi-service is running"}
