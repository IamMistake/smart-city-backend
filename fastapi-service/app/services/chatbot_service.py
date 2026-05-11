from __future__ import annotations

from dataclasses import asdict

from app.core.config import settings
from app.services.chatbot_providers import (
    ChatbotMessage,
    ChatbotModelOption,
    ChatbotProvider,
    ChatbotProviderError,
    OllamaProvider,
    OpenRouterProvider,
    build_model_id,
)

SYSTEM_PROMPT = """You are the Smart City Skopje assistant.

Stay focused on Skopje-related smart city topics such as air quality, pollution, active incidents, emergencies, public safety, and critical city events.

Prefer concise, practical answers. If the user asks for live or exact data that is not present in the conversation context, say that you do not have verified live data in the chatbot context and direct the user to the platform's pollution, map, or emergencies views for confirmation.

Do not invent measurements, incidents, or official emergency instructions. When information is uncertain, be explicit about the limitation and provide safe next steps.
"""


class ChatbotService:
    def __init__(self) -> None:
        self.system_prompt = SYSTEM_PROMPT.replace("Skopje", settings.chatbot_focus_city)
        self.providers: dict[str, ChatbotProvider] = {
            "openrouter": OpenRouterProvider(
                base_url=settings.chatbot_openrouter_base_url,
                api_key=settings.chatbot_openrouter_api_key,
                timeout_seconds=settings.chatbot_timeout_seconds,
                site_url=settings.chatbot_openrouter_site_url,
                site_name=settings.chatbot_openrouter_site_name,
            ),
            "ollama": OllamaProvider(
                base_url=settings.chatbot_ollama_base_url,
                timeout_seconds=settings.chatbot_timeout_seconds,
            ),
        }

    def list_models(self) -> dict[str, object]:
        options = self._configured_model_options()
        default_model_id = options[0].id if options else ""
        return {
            "models": [asdict(option) for option in options],
            "defaultModel": default_model_id,
        }

    def generate_reply(
        self,
        *,
        messages: list[ChatbotMessage],
        selected_model_id: str | None = None,
    ) -> dict[str, str]:
        cleaned_messages = [
            ChatbotMessage(role=message.role, text=message.text.strip())
            for message in messages
            if message.text.strip()
        ]

        if not cleaned_messages:
            raise ValueError("At least one non-empty message is required")

        selected_option = self._resolve_selected_option(selected_model_id)
        if selected_option is not None:
            reply = self._run_provider(
                provider_name=selected_option.provider,
                model=selected_option.model,
                messages=cleaned_messages,
            )
            return {
                "reply": reply,
                "provider": selected_option.provider,
                "model": selected_option.model,
            }

        last_error: ChatbotProviderError | None = None
        for option in self._configured_model_options():
            try:
                reply = self._run_provider(
                    provider_name=option.provider,
                    model=option.model,
                    messages=cleaned_messages,
                )
                return {
                    "reply": reply,
                    "provider": option.provider,
                    "model": option.model,
                }
            except ChatbotProviderError as exc:
                last_error = exc

        if last_error is not None:
            raise last_error

        raise ChatbotProviderError(
            "No chatbot providers are configured",
            code="MODEL_UNAVAILABLE",
            retryable=False,
        )

    def _configured_model_options(self) -> list[ChatbotModelOption]:
        options: list[ChatbotModelOption] = []

        primary = self._build_option(
            provider_name=settings.chatbot_primary_provider,
            model=settings.chatbot_primary_model,
            is_default=True,
        )
        if primary is not None:
            options.append(primary)

        fallback = self._build_option(
            provider_name=settings.chatbot_fallback_provider,
            model=settings.chatbot_fallback_model,
            is_default=False,
        )
        if fallback is not None and fallback.id not in {option.id for option in options}:
            options.append(fallback)

        return options

    def _build_option(
        self,
        *,
        provider_name: str,
        model: str,
        is_default: bool,
    ) -> ChatbotModelOption | None:
        if not provider_name or not model:
            return None

        provider = self.providers.get(provider_name)
        if provider is None:
            return None

        return ChatbotModelOption(
            id=build_model_id(provider_name, model),
            label=f"{provider_name.capitalize()} · {model}",
            provider=provider_name,
            model=model,
            is_default=is_default,
        )

    def _resolve_selected_option(
        self,
        selected_model_id: str | None,
    ) -> ChatbotModelOption | None:
        if not selected_model_id:
            return None

        for option in self._configured_model_options():
            if option.id == selected_model_id:
                return option

        raise ValueError("Unsupported chatbot model")

    def _run_provider(
        self,
        *,
        provider_name: str,
        model: str,
        messages: list[ChatbotMessage],
    ) -> str:
        provider = self.providers.get(provider_name)
        if provider is None:
            raise ChatbotProviderError(
                f"Unsupported chatbot provider: {provider_name}",
                code="MODEL_CONFIGURATION_ERROR",
                provider=provider_name,
                model=model,
                status_code=500,
                retryable=False,
            )

        return provider.generate_reply(
            model=model,
            messages=messages,
            system_prompt=self.system_prompt,
        )


chatbot_service = ChatbotService()
