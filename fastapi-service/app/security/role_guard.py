class RoleGuard:
    def __init__(self, role: str):
        self.role = role.upper()

    def user_has_role(self, role: str) -> bool:
        return self.role == role.upper()

    def user_has_any_role(self, *roles: str) -> bool:
        return self.role in [r.upper() for r in roles]

    def user_has_none_role(self, *roles: str) -> bool:
        return self.role not in [r.upper() for r in roles]