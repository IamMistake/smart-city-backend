from collections.abc import Callable

from fastapi import Depends, HTTPException, status

from app.core.auth import get_active_user
from app.security.role_guard import RoleGuard


def get_role_guard(user: dict = Depends(get_active_user)) -> RoleGuard:
    return RoleGuard(user["role"])


def require_any_role(*allowed_roles: str) -> Callable[[RoleGuard], RoleGuard]:
    if not allowed_roles:
        raise ValueError("At least one allowed role must be provided")

    normalized_roles = tuple(role.upper() for role in allowed_roles)

    def _require_role(role_guard: RoleGuard = Depends(get_role_guard)) -> RoleGuard:
        if not role_guard.user_has_any_role(*normalized_roles):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not allowed",
            )
        return role_guard

    return _require_role
