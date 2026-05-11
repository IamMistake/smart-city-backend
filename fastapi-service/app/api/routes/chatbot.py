from __future__ import annotations

from typing import Literal

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from app.security.role_guard import RoleGuard
from app.security.role_guard_dep import require_any_role
from app.services.chatbot_providers import ChatbotMessage, ChatbotProviderError
from app.services.chatbot_service import chatbot_service

router = APIRouter(prefix="/api/chatbot", tags=["chatbot"])
ALLOWED_ROLES = ("CITIZEN", "OPERATOR", "AUTHORITY", "ADMIN")


class ChatbotMessagePayload(BaseModel):
    role: Literal["user", "bot", "assistant"]
    text: str = Field(min_length=1)


class ChatbotRequest(BaseModel):
    messages: list[ChatbotMessagePayload] = Field(min_length=1)
    model: str | None = None


@router.get("/models")
def get_chatbot_models(
    _role_guard: RoleGuard = Depends(require_any_role(*ALLOWED_ROLES)),
) -> dict[str, object]:
    return chatbot_service.list_models()


@router.post("/messages")
def create_chatbot_message(
    payload: ChatbotRequest,
    _role_guard: RoleGuard = Depends(require_any_role(*ALLOWED_ROLES)),
) -> dict[str, str]:
    try:
        return chatbot_service.generate_reply(
            messages=[
                ChatbotMessage(role=message.role, text=message.text)
                for message in payload.messages
            ],
            selected_model_id=payload.model,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except ChatbotProviderError as exc:
        raise HTTPException(
            status_code=exc.status_code,
            detail={
                "code": exc.code,
                "message": str(exc),
                "provider": exc.provider,
                "model": exc.model,
                "retryable": exc.retryable,
            },
        ) from exc
