from __future__ import annotations

from fastapi import APIRouter, HTTPException, Query

from app.services.pollution_service import PulseEcoError, pollution_service

router = APIRouter(prefix="/api/pollution", tags=["pollution"])


@router.get("/current")
def get_current_pollution(
	metric: str = Query(default="pm10", description="Pollution metric"),
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
