from fastapi import HTTPException, status
import pytest

from app.core.auth import RoleChecker

def test_role_checker_allows_correct_role():
    checker = RoleChecker(allowed_roles=["ADMIN", "OPERATOR"])
    user = {"role": "ADMIN"}
    
    # Should not raise exception
    result = checker(user=user)
    assert result == user

def test_role_checker_denies_incorrect_role():
    checker = RoleChecker(allowed_roles=["ADMIN", "OPERATOR"])
    user = {"role": "CITIZEN"}
    
    with pytest.raises(HTTPException) as exc:
        checker(user=user)
    
    assert exc.value.status_code == status.HTTP_403_FORBIDDEN
    assert "Operation not permitted" in exc.value.detail

def test_role_checker_allows_multiple_roles():
    checker = RoleChecker(allowed_roles=["CITIZEN", "ADMIN"])
    
    user1 = {"role": "CITIZEN"}
    assert checker(user=user1) == user1
    
    user2 = {"role": "ADMIN"}
    assert checker(user=user2) == user2
