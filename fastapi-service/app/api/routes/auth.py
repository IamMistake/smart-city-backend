from __future__ import annotations

from typing import Any

from fastapi import APIRouter, Depends

from app.core.auth import get_active_user

router = APIRouter(prefix="/api/auth", tags=["auth"])


@router.get("/me")
def get_authenticated_user(
	active_user: dict[str, Any] = Depends(get_active_user),
) -> dict[str, Any]:
	return active_user
