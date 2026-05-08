from __future__ import annotations

import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes.health import router as health_router
from app.api.routes.chat import router as chat_router
from app.core.config import settings
from app.db.session import check_db_connection

from dotenv import load_dotenv
load_dotenv()

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
app.include_router(chat_router, prefix="/api")


@app.on_event("startup")
def startup_db_check() -> None:
	is_connected, error = check_db_connection()

	if not is_connected:
		logger.warning("Database unavailable on startup: %s", error)


@app.get("/")
def root() -> dict[str, str]:
	return {"message": "fastapi-service is running"}