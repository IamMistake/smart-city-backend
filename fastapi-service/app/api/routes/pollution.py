from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Query

from app.security.role_guard import RoleGuard
from app.security.role_guard_dep import require_any_role
from app.services.pollution_service import PulseEcoError, pollution_service

router = APIRouter(prefix="/api/pollution", tags=["pollution"])
ALLOWED_ROLES = ("CITIZEN", "OPERATOR", "AUTHORITY", "ADMIN")


@router.get("/current")
def get_current_pollution(
    metric: str = Query(default="pm10", description="Pollution metric"),
    _role_guard: RoleGuard = Depends(require_any_role(*ALLOWED_ROLES)),
) -> dict:
    try:
        return pollution_service.get_current_snapshot(metric=metric)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except PulseEcoError as exc:
        raise HTTPException(
            status_code=503,
            detail={
                "code": "POLLUTION_DATA_UNAVAILABLE",
                "message": str(exc),
                "retryable": True,
            },
        ) from exc


@router.get("/history")
def get_pollution_history(
    metric: str = Query(default="pm10", description="Pollution metric"),
    window_hours: int = Query(default=24, alias="windowHours"),
    bucket_minutes: int = Query(default=60, alias="bucketMinutes"),
    sensor_id: str | None = Query(default=None, alias="sensorId"),
    _role_guard: RoleGuard = Depends(require_any_role(*ALLOWED_ROLES)),
) -> dict:
    try:
        return pollution_service.get_history_snapshot(
            metric=metric,
            window_hours=window_hours,
            bucket_minutes=bucket_minutes,
            sensor_id=sensor_id,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except PulseEcoError as exc:
        raise HTTPException(
            status_code=503,
            detail={
                "code": "POLLUTION_DATA_UNAVAILABLE",
                "message": str(exc),
                "retryable": True,
            },
        ) from exc
