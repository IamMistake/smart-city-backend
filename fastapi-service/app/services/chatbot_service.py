from __future__ import annotations

from dataclasses import asdict
from typing import Any

from app.core.config import settings
from app.db.session import SessionLocal
from app.services.pollution_service import PulseEcoError, pollution_service
from app.services.chatbot_providers import (
    ChatbotMessage,
    ChatbotModelOption,
    ChatbotProvider,
    ChatbotProviderError,
    OpenAIProvider,
    OllamaProvider,
    OpenRouterProvider,
    build_model_id,
)
from sqlalchemy import text

SYSTEM_PROMPT = """You are the Smart City Skopje assistant.

Stay focused on Skopje-related smart city topics such as air quality, pollution, active incidents, emergencies, public safety, and critical city events.

Prefer concise, practical answers. If the user asks for live or exact data that is not present in the conversation context, say that you do not have verified live data in the chatbot context and direct the user to the platform's pollution, map, or emergencies views for confirmation.

Do not invent measurements, incidents, or official emergency instructions. When information is uncertain, be explicit about the limitation and provide safe next steps.
"""


class ChatbotService:
    def __init__(self) -> None:
        self.system_prompt = SYSTEM_PROMPT.replace("Skopje", settings.chatbot_focus_city)
        self.providers: dict[str, ChatbotProvider] = {
            "openai": OpenAIProvider(
                base_url=settings.chatbot_openai_base_url,
                api_key=settings.chatbot_openai_api_key,
                timeout_seconds=settings.chatbot_timeout_seconds,
            ),
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
        configured_options = self._configured_model_options()
        active_option = selected_option or (configured_options[0] if configured_options else None)

        if active_option is not None and self._is_model_question(cleaned_messages[-1].text):
            return {
                "reply": (
                    f"I am currently using {active_option.provider} with the "
                    f"`{active_option.model}` model."
                ),
                "provider": active_option.provider,
                "model": active_option.model,
            }

        direct_reply = self._build_direct_reply(cleaned_messages[-1].text)
        if active_option is not None and direct_reply is not None:
            return {
                "reply": direct_reply,
                "provider": active_option.provider,
                "model": active_option.model,
            }

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
        for option in configured_options:
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

        if not provider.is_configured():
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

        system_prompt = self._build_system_prompt(messages)

        return provider.generate_reply(
            model=model,
            messages=messages,
            system_prompt=system_prompt,
        )

    def _build_system_prompt(self, messages: list[ChatbotMessage]) -> str:
        context_parts = [self._build_pollution_context(), self._build_incident_context()]
        context = "\n\n".join(part for part in context_parts if part)
        if not context:
            return self.system_prompt

        return (
            f"{self.system_prompt}\n\n"
            "Use the platform context below when it is relevant to the user's question. "
            "If the context does not answer the question, say what is missing instead of inventing details.\n\n"
            f"{context}"
        )

    def _build_pollution_context(self) -> str:
        try:
            snapshot = pollution_service.get_current_snapshot(metric="pm10")
        except (PulseEcoError, ValueError):
            return ""

        summary = snapshot.get("summary") or {}
        stations = snapshot.get("stations") or []
        city_value = summary.get("cityValue")
        fetched_at = snapshot.get("fetchedAt", "")
        top_station = stations[0] if stations else None

        lines = [
            "Platform pollution context:",
            f"- City: {snapshot.get('city', settings.chatbot_focus_city)}",
            f"- Metric: {snapshot.get('metric', 'pm10')}",
            f"- City value: {city_value if city_value is not None else 'unavailable'} {snapshot.get('unit', '')}".strip(),
            f"- Status: {summary.get('statusText', 'unknown')}",
            f"- Active station count: {snapshot.get('coverage', {}).get('activeStationCount', 0)}",
            f"- Fetched at: {fetched_at}",
        ]

        if isinstance(top_station, dict):
            current = top_station.get("current") or {}
            lines.append(
                "- Example station: "
                f"{top_station.get('name', 'unknown')} = {current.get('value', 'n/a')} "
                f"{snapshot.get('unit', '')}".strip()
            )

        return "\n".join(lines)

    def _build_incident_context(self) -> str:
        query = text(
            """
            SELECT
                title,
                incident_type,
                priority,
                status,
                address,
                occurred_at
            FROM core.incidents
            WHERE deleted_at IS NULL
            ORDER BY
                CASE
                    WHEN status = 'ACTIVE' THEN 0
                    WHEN status = 'REPORTED' THEN 1
                    ELSE 2
                END,
                occurred_at DESC NULLS LAST,
                created_at DESC
            LIMIT 5
            """
        )

        with SessionLocal() as db:
            rows = db.execute(query).mappings().all()

        if not rows:
            return ""

        lines = ["Platform incident context:"]
        for row in rows:
            item = dict(row)
            lines.append(
                "- "
                f"{item.get('title', 'Unknown incident')} | "
                f"status={item.get('status', 'unknown')} | "
                f"priority={item.get('priority', 'unknown')} | "
                f"type={item.get('incident_type', 'unknown')} | "
                f"address={item.get('address', 'n/a')} | "
                f"occurred_at={item.get('occurred_at', 'n/a')}"
            )

        return "\n".join(lines)

    def _is_model_question(self, text_value: str) -> bool:
        normalized = text_value.strip().lower()
        triggers = (
            "what model",
            "which model",
            "what ai model",
            "what llm",
            "what are you running",
            "what model are you",
        )
        return any(trigger in normalized for trigger in triggers)

    def _build_direct_reply(self, text_value: str) -> str | None:
        normalized = text_value.strip().lower()

        if any(trigger in normalized for trigger in ("air quality", "pollution right now", "pm10")):
            return self._direct_pollution_reply()

        if any(
            trigger in normalized
            for trigger in (
                "active emergencies",
                "active incidents",
                "critical happening",
                "anything critical",
                "emergencies",
            )
        ):
            return self._direct_incident_reply()

        if "high pollution" in normalized or "areas have high pollution" in normalized:
            return self._direct_hotspot_reply()

        return None

    def _direct_pollution_reply(self) -> str:
        try:
            snapshot = pollution_service.get_current_snapshot(metric="pm10")
        except (PulseEcoError, ValueError):
            return (
                "I do not have verified live PM10 data available right now. "
                "Please check the pollution map or monitoring view for confirmation."
            )

        summary = snapshot.get("summary") or {}
        city_value = summary.get("cityValue")
        status_text = summary.get("statusText", "unknown")
        unit = snapshot.get("unit", "")
        station_count = snapshot.get("coverage", {}).get("activeStationCount", 0)
        fetched_at = snapshot.get("fetchedAt", "")

        return (
            f"The latest PM10 reading for {snapshot.get('city', settings.chatbot_focus_city)} is "
            f"{city_value if city_value is not None else 'unavailable'} {unit}. "
            f"Overall status is {status_text}. "
            f"This snapshot covers {station_count} active stations and was fetched at {fetched_at}."
        )

    def _direct_hotspot_reply(self) -> str:
        try:
            snapshot = pollution_service.get_current_snapshot(metric="pm10")
        except (PulseEcoError, ValueError):
            return (
                "I do not have station-level pollution data available right now. "
                "Please check the pollution map for the latest hotspots."
            )

        stations = snapshot.get("stations") or []
        if not stations:
            return "I do not have station-level pollution hotspots available right now."

        ranked = sorted(
            stations,
            key=lambda station: (station.get("current") or {}).get("value") or -1,
            reverse=True,
        )[:3]
        unit = snapshot.get("unit", "")
        hotspot_text = "; ".join(
            f"{station.get('name', 'unknown')} at {(station.get('current') or {}).get('value', 'n/a')} {unit}".strip()
            for station in ranked
        )

        return f"The highest PM10 areas in the latest snapshot are: {hotspot_text}."

    def _direct_incident_reply(self) -> str:
        rows = self._recent_incident_rows(limit=5)
        if not rows:
            return "I do not have any recent incident data available right now."

        active_rows = [row for row in rows if row.get("status") in {"ACTIVE", "REPORTED"}]
        critical_rows = [row for row in active_rows if row.get("priority") == "CRITICAL"]

        if not active_rows:
            return "There are no active or reported incidents in the latest platform incident snapshot."

        summary = "; ".join(
            f"{row.get('title')} ({row.get('status')}, {row.get('priority')})"
            for row in active_rows[:3]
        )

        critical_note = (
            f" There are {len(critical_rows)} critical incidents right now."
            if critical_rows
            else " There are no critical incidents in the current top results."
        )

        return (
            f"I found {len(active_rows)} active or reported incidents in the latest snapshot. "
            f"Top incidents: {summary}.{critical_note}"
        )

    def _recent_incident_rows(self, limit: int = 5) -> list[dict[str, Any]]:
        query = text(
            """
            SELECT
                title,
                incident_type,
                priority,
                status,
                address,
                occurred_at
            FROM core.incidents
            WHERE deleted_at IS NULL
            ORDER BY
                CASE
                    WHEN status = 'ACTIVE' THEN 0
                    WHEN status = 'REPORTED' THEN 1
                    ELSE 2
                END,
                occurred_at DESC NULLS LAST,
                created_at DESC
            LIMIT :limit
            """
        )

        with SessionLocal() as db:
            rows = db.execute(query, {"limit": limit}).mappings().all()

        return [dict(row) for row in rows]


chatbot_service = ChatbotService()
