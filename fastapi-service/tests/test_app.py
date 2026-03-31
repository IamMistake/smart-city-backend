from __future__ import annotations

from fastapi import HTTPException
from fastapi.testclient import TestClient

from app.api.routes import health
from app.core.auth import get_active_user
from app.main import app, root


def test_root_endpoint() -> None:
	assert root() == {"message": "fastapi-service is running"}


def test_health_endpoint_reports_up(monkeypatch) -> None:
	monkeypatch.setattr(health, "check_db_connection", lambda: (True, None))

	body = health.get_health()

	assert body["status"] == "UP"
	assert body["db_status"] == "UP"
	assert body["service"] == "fastapi-service"
	assert body["message"] == "Database connected"
	assert body["error"] == ""


def test_health_endpoint_reports_down(monkeypatch) -> None:
	monkeypatch.setattr(health, "check_db_connection", lambda: (False, "database offline"))

	body = health.get_health()

	assert body["status"] == "DOWN"
	assert body["db_status"] == "DOWN"
	assert body["message"] == "Database unavailable"
	assert body["error"] == "database offline"


def test_auth_me_returns_active_user() -> None:
	def override_active_user() -> dict[str, str | bool]:
		return {
			"id": "95e9e14c-8e98-4ad3-818f-2f1bcc7dd4ab",
			"clerk_user_id": "user_test_123",
			"email": "user@example.com",
			"full_name": "Test User",
			"role": "CITIZEN",
			"avatar_url": "https://example.com/avatar.png",
			"is_active": True,
		}

	app.dependency_overrides[get_active_user] = override_active_user
	client = TestClient(app)
	response = client.get("/api/auth/me")
	app.dependency_overrides.clear()

	assert response.status_code == 200
	body = response.json()
	assert body["clerk_user_id"] == "user_test_123"
	assert body["is_active"] is True


def test_auth_me_rejects_inactive_user() -> None:
	def override_active_user() -> None:
		raise HTTPException(status_code=403, detail="User is not active")

	app.dependency_overrides[get_active_user] = override_active_user
	client = TestClient(app)
	response = client.get("/api/auth/me")
	app.dependency_overrides.clear()

	assert response.status_code == 403
	assert response.json()["detail"] == "User is not active"
