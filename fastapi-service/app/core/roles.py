from typing import Any

from fastapi import Depends, HTTPException, status

from app.core.auth import get_active_user


class RoleChecker:
    def __init__(self, allowed_roles: list[str]):
        self.allowed_roles = allowed_roles

    def __call__(self, user: dict[str, Any] = Depends(get_active_user)):
        if user["role"] not in self.allowed_roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Operation not permitted for role: {user['role']}",
            )
        return user
