from __future__ import annotations

from app.api.routes import health
from app.main import root


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
