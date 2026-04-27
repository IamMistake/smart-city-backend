from __future__ import annotations

from datetime import datetime, timezone

from fastapi import APIRouter

from app.core.config import settings
from app.db.session import check_db_connection

router = APIRouter(prefix="/api/health", tags=["health"])


@router.get("/")
def get_health() -> dict[str, str]:
    is_connected, error = check_db_connection()
    status_value = "UP" if is_connected else "DOWN"
    db_status = "UP" if is_connected else "DOWN"
    message = "Database connected" if is_connected else "Database unavailable"

    return {
        "status": status_value,
        "db_status": db_status,
        "service": settings.service_name,
        "message": message,
        "error": error or "",
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }
