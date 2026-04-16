from fastapi import Depends

from app.core.auth import get_active_user
from app.security.role_guard import RoleGuard


def get_role_guard(user: dict = Depends(get_active_user)) -> RoleGuard:
    return RoleGuard(user["role"])
