from __future__ import annotations

from datetime import datetime, timezone

from fastapi import APIRouter

from app.core.config import settings

router = APIRouter(prefix="/api/health", tags=["health"])


@router.get("/")
def get_health() -> dict[str, str]:
	return {
		"status": "UP",
		"service": settings.service_name,
		"timestamp": datetime.now(timezone.utc).isoformat(),
	}
