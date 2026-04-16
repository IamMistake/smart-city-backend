from __future__ import annotations

from datetime import datetime, timezone

from fastapi import APIRouter, Depends

from app.core.config import settings
from app.db.session import check_db_connection
from app.security.role_guard import RoleGuard
from app.security.role_guard_dep import require_any_role

router = APIRouter(prefix="/api/health", tags=["health"])
ALLOWED_ROLES = ("CITIZEN", "OPERATOR", "AUTHORITY", "ADMIN")


@router.get("/")
def get_health(
    role_guard: RoleGuard = Depends(require_any_role(*ALLOWED_ROLES)),
) -> dict[str, str]:
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
