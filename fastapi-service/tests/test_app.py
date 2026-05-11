from __future__ import annotations

import inspect

import pytest
from fastapi import HTTPException

from app.api.routes import auth, chatbot, health, pollution
from app.core.auth import get_clerk_claims
from app.main import root
from app.security.role_guard import RoleGuard
from app.security.role_guard_dep import require_any_role
from app.services.chatbot_providers import ChatbotProviderError


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


def test_health_route_is_public(monkeypatch) -> None:
	monkeypatch.setattr(health, "check_db_connection", lambda: (True, None))

	assert inspect.signature(health.get_health).parameters == {}
	assert health.get_health()["status"] == "UP"


def test_auth_me_returns_active_user() -> None:
	active_user = {
		"id": "95e9e14c-8e98-4ad3-818f-2f1bcc7dd4ab",
		"clerk_user_id": "user_test_123",
		"email": "user@example.com",
		"full_name": "Test User",
		"role": "CITIZEN",
		"avatar_url": "https://example.com/avatar.png",
		"is_active": True,
	}

	body = auth.get_authenticated_user(active_user=active_user)
	assert body["clerk_user_id"] == "user_test_123"
	assert body["is_active"] is True


def test_protected_routes_require_bearer_token() -> None:
	with pytest.raises(HTTPException) as exc_info:
		get_clerk_claims(credentials=None)

	assert exc_info.value.status_code == 401
	assert exc_info.value.detail == "Missing bearer token"


def test_pollution_route_rejects_wrong_role() -> None:
	role_dependency = require_any_role(*pollution.ALLOWED_ROLES)

	with pytest.raises(HTTPException) as exc_info:
		role_dependency(role_guard=RoleGuard("GUEST"))

	assert exc_info.value.status_code == 403
	assert exc_info.value.detail == "Not allowed"


def test_pollution_route_allows_citizen_role(monkeypatch) -> None:
	monkeypatch.setattr(
		pollution.pollution_service,
		"get_current_snapshot",
		lambda metric: {"metric": metric, "status": "ok"},
	)
	role_guard = require_any_role(*pollution.ALLOWED_ROLES)(role_guard=RoleGuard("CITIZEN"))
	response = pollution.get_current_pollution(metric="pm10", _role_guard=role_guard)

	assert response == {"metric": "pm10", "status": "ok"}


def test_chatbot_models_route_allows_citizen_role(monkeypatch) -> None:
	monkeypatch.setattr(
		chatbot.chatbot_service,
		"list_models",
		lambda: {"models": [{"id": "ollama:llama3.1:8b"}], "defaultModel": "ollama:llama3.1:8b"},
	)
	role_guard = require_any_role(*chatbot.ALLOWED_ROLES)(role_guard=RoleGuard("CITIZEN"))

	response = chatbot.get_chatbot_models(_role_guard=role_guard)

	assert response["defaultModel"] == "ollama:llama3.1:8b"


def test_chatbot_message_route_returns_reply(monkeypatch) -> None:
	monkeypatch.setattr(
		chatbot.chatbot_service,
		"generate_reply",
		lambda **_: {
			"reply": "Skopje air quality is currently unavailable in chatbot context.",
			"provider": "ollama",
			"model": "llama3.1:8b",
		},
	)
	role_guard = require_any_role(*chatbot.ALLOWED_ROLES)(role_guard=RoleGuard("CITIZEN"))
	request = chatbot.ChatbotRequest(
		messages=[chatbot.ChatbotMessagePayload(role="user", text="How is the air quality?")],
	)

	response = chatbot.create_chatbot_message(request, _role_guard=role_guard)

	assert response["provider"] == "ollama"
	assert response["model"] == "llama3.1:8b"


def test_chatbot_message_route_maps_provider_errors(monkeypatch) -> None:
	def raise_error(**_: object) -> dict[str, str]:
		raise ChatbotProviderError(
			"Model rate limited",
			code="MODEL_RATE_LIMITED",
			provider="openrouter",
			model="meta-llama/llama-3.3-8b-instruct:free",
			status_code=503,
			retryable=True,
		)

	monkeypatch.setattr(chatbot.chatbot_service, "generate_reply", raise_error)
	role_guard = require_any_role(*chatbot.ALLOWED_ROLES)(role_guard=RoleGuard("CITIZEN"))
	request = chatbot.ChatbotRequest(
		messages=[chatbot.ChatbotMessagePayload(role="user", text="Any emergencies?")],
	)

	with pytest.raises(HTTPException) as exc_info:
		chatbot.create_chatbot_message(request, _role_guard=role_guard)

	assert exc_info.value.status_code == 503
	assert exc_info.value.detail["code"] == "MODEL_RATE_LIMITED"
