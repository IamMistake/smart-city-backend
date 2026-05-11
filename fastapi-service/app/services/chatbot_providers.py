from __future__ import annotations

from collections.abc import Sequence
from dataclasses import dataclass
from typing import Any, Protocol

import httpx


@dataclass(frozen=True)
class ChatbotMessage:
    role: str
    text: str


@dataclass(frozen=True)
class ChatbotModelOption:
    id: str
    label: str
    provider: str
    model: str
    is_default: bool = False


class ChatbotProviderError(RuntimeError):
    def __init__(
        self,
        message: str,
        *,
        code: str = "CHATBOT_PROVIDER_ERROR",
        provider: str = "unknown",
        model: str = "",
        status_code: int = 503,
        retryable: bool = False,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.provider = provider
        self.model = model
        self.status_code = status_code
        self.retryable = retryable


class ChatbotProvider(Protocol):
    name: str

    def is_configured(self) -> bool: ...

    def generate_reply(
        self,
        *,
        model: str,
        messages: Sequence[ChatbotMessage],
        system_prompt: str,
    ) -> str: ...


def build_model_id(provider: str, model: str) -> str:
    return f"{provider}:{model}"


class OpenRouterProvider:
    name = "openrouter"

    def __init__(
        self,
        *,
        base_url: str,
        api_key: str,
        timeout_seconds: float,
        site_url: str = "",
        site_name: str = "",
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout_seconds = timeout_seconds
        self.site_url = site_url
        self.site_name = site_name

    def is_configured(self) -> bool:
        return bool(self.api_key and self.base_url)

    def generate_reply(
        self,
        *,
        model: str,
        messages: Sequence[ChatbotMessage],
        system_prompt: str,
    ) -> str:
        if not self.is_configured():
            raise ChatbotProviderError(
                "OpenRouter is not configured",
                code="MODEL_UNAVAILABLE",
                provider=self.name,
                model=model,
                retryable=False,
            )

        payload = {
            "model": model,
            "messages": [
                {"role": "system", "content": system_prompt},
                *[
                    {
                        "role": self._normalize_role(message.role),
                        "content": message.text,
                    }
                    for message in messages
                ],
            ],
            "temperature": 0.2,
        }

        headers = {"Authorization": f"Bearer {self.api_key}"}
        if self.site_url:
            headers["HTTP-Referer"] = self.site_url
        if self.site_name:
            headers["X-Title"] = self.site_name

        body = self._post_json("/chat/completions", payload=payload, headers=headers)
        choices = body.get("choices")
        if not isinstance(choices, list) or not choices:
            raise ChatbotProviderError(
                "OpenRouter returned no completion choices",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                model=model,
                retryable=True,
            )

        message = choices[0].get("message")
        content = message.get("content") if isinstance(message, dict) else None
        if not isinstance(content, str) or not content.strip():
            raise ChatbotProviderError(
                "OpenRouter returned an empty response",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                model=model,
                retryable=True,
            )

        return content.strip()

    def _post_json(
        self,
        path: str,
        *,
        payload: dict[str, Any],
        headers: dict[str, str],
    ) -> dict[str, Any]:
        url = f"{self.base_url}{path}"

        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.post(url, json=payload, headers=headers)
                response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise ChatbotProviderError(
                "OpenRouter request timed out",
                code="MODEL_TIMEOUT",
                provider=self.name,
                retryable=True,
            ) from exc
        except httpx.HTTPStatusError as exc:
            raise self._map_http_error(exc, path=path)
        except httpx.HTTPError as exc:
            raise ChatbotProviderError(
                "OpenRouter request failed",
                code="MODEL_UNAVAILABLE",
                provider=self.name,
                retryable=True,
            ) from exc

        try:
            body = response.json()
        except ValueError as exc:
            raise ChatbotProviderError(
                "OpenRouter returned invalid JSON",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                retryable=True,
            ) from exc

        if not isinstance(body, dict):
            raise ChatbotProviderError(
                "OpenRouter returned an unexpected response payload",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                retryable=True,
            )

        return body

    def _map_http_error(
        self,
        exc: httpx.HTTPStatusError,
        *,
        path: str,
    ) -> ChatbotProviderError:
        status_code = exc.response.status_code
        message = f"OpenRouter returned {status_code} at {path}"

        try:
            body = exc.response.json()
        except ValueError:
            body = None

        if isinstance(body, dict):
            error = body.get("error")
            if isinstance(error, dict) and isinstance(error.get("message"), str):
                message = error["message"].strip() or message
            elif isinstance(error, str) and error.strip():
                message = error.strip()
            elif isinstance(body.get("message"), str) and body["message"].strip():
                message = body["message"].strip()

        code = "MODEL_UNAVAILABLE"
        retryable = status_code >= 500

        if status_code == 429:
            code = "MODEL_RATE_LIMITED"
            retryable = True
        elif status_code in (400, 404):
            code = "MODEL_UNAVAILABLE"
        elif status_code in (401, 403):
            code = "MODEL_CONFIGURATION_ERROR"

        return ChatbotProviderError(
            message,
            code=code,
            provider=self.name,
            status_code=503 if status_code >= 500 or status_code == 429 else status_code,
            retryable=retryable,
        )

    @staticmethod
    def _normalize_role(role: str) -> str:
        return "assistant" if role == "bot" else role


class OpenAIProvider:
    name = "openai"

    def __init__(
        self,
        *,
        base_url: str,
        api_key: str,
        timeout_seconds: float,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout_seconds = timeout_seconds

    def is_configured(self) -> bool:
        return bool(self.api_key and self.base_url)

    def generate_reply(
        self,
        *,
        model: str,
        messages: Sequence[ChatbotMessage],
        system_prompt: str,
    ) -> str:
        if not self.is_configured():
            raise ChatbotProviderError(
                "OpenAI is not configured",
                code="MODEL_UNAVAILABLE",
                provider=self.name,
                model=model,
                retryable=False,
            )

        payload = {
            "model": model,
            "messages": [
                {"role": "system", "content": system_prompt},
                *[
                    {
                        "role": self._normalize_role(message.role),
                        "content": message.text,
                    }
                    for message in messages
                ],
            ],
            "temperature": 0.2,
        }

        body = self._post_json(
            "/chat/completions",
            payload=payload,
            headers={"Authorization": f"Bearer {self.api_key}"},
        )
        choices = body.get("choices")
        if not isinstance(choices, list) or not choices:
            raise ChatbotProviderError(
                "OpenAI returned no completion choices",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                model=model,
                retryable=True,
            )

        message = choices[0].get("message")
        content = message.get("content") if isinstance(message, dict) else None
        if not isinstance(content, str) or not content.strip():
            raise ChatbotProviderError(
                "OpenAI returned an empty response",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                model=model,
                retryable=True,
            )

        return content.strip()

    def _post_json(
        self,
        path: str,
        *,
        payload: dict[str, Any],
        headers: dict[str, str],
    ) -> dict[str, Any]:
        url = f"{self.base_url}{path}"

        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.post(url, json=payload, headers=headers)
                response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise ChatbotProviderError(
                "OpenAI request timed out",
                code="MODEL_TIMEOUT",
                provider=self.name,
                retryable=True,
            ) from exc
        except httpx.HTTPStatusError as exc:
            raise self._map_http_error(exc, path=path)
        except httpx.HTTPError as exc:
            raise ChatbotProviderError(
                "OpenAI request failed",
                code="MODEL_UNAVAILABLE",
                provider=self.name,
                retryable=True,
            ) from exc

        try:
            body = response.json()
        except ValueError as exc:
            raise ChatbotProviderError(
                "OpenAI returned invalid JSON",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                retryable=True,
            ) from exc

        if not isinstance(body, dict):
            raise ChatbotProviderError(
                "OpenAI returned an unexpected response payload",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                retryable=True,
            )

        return body

    def _map_http_error(
        self,
        exc: httpx.HTTPStatusError,
        *,
        path: str,
    ) -> ChatbotProviderError:
        status_code = exc.response.status_code
        message = f"OpenAI returned {status_code} at {path}"

        try:
            body = exc.response.json()
        except ValueError:
            body = None

        if isinstance(body, dict):
            error = body.get("error")
            if isinstance(error, dict) and isinstance(error.get("message"), str):
                message = error["message"].strip() or message
            elif isinstance(error, str) and error.strip():
                message = error.strip()
            elif isinstance(body.get("message"), str) and body["message"].strip():
                message = body["message"].strip()

        code = "MODEL_UNAVAILABLE"
        retryable = status_code >= 500

        if status_code == 429:
            code = "MODEL_RATE_LIMITED"
            retryable = True
        elif status_code in (400, 404):
            code = "MODEL_UNAVAILABLE"
        elif status_code in (401, 403):
            code = "MODEL_CONFIGURATION_ERROR"

        return ChatbotProviderError(
            message,
            code=code,
            provider=self.name,
            status_code=503 if status_code >= 500 or status_code == 429 else status_code,
            retryable=retryable,
        )

    @staticmethod
    def _normalize_role(role: str) -> str:
        return "assistant" if role == "bot" else role


class OllamaProvider:
    name = "ollama"

    def __init__(self, *, base_url: str, timeout_seconds: float) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout_seconds = timeout_seconds

    def is_configured(self) -> bool:
        return bool(self.base_url)

    def generate_reply(
        self,
        *,
        model: str,
        messages: Sequence[ChatbotMessage],
        system_prompt: str,
    ) -> str:
        payload = {
            "model": model,
            "stream": False,
            "messages": [
                {"role": "system", "content": system_prompt},
                *[
                    {
                        "role": self._normalize_role(message.role),
                        "content": message.text,
                    }
                    for message in messages
                ],
            ],
        }

        body = self._post_json("/api/chat", payload=payload)
        message = body.get("message")
        content = message.get("content") if isinstance(message, dict) else None
        if not isinstance(content, str) or not content.strip():
            raise ChatbotProviderError(
                "Ollama returned an empty response",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                model=model,
                retryable=True,
            )

        return content.strip()

    def _post_json(self, path: str, *, payload: dict[str, Any]) -> dict[str, Any]:
        url = f"{self.base_url}{path}"

        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.post(url, json=payload)
                response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise ChatbotProviderError(
                "Ollama request timed out",
                code="MODEL_TIMEOUT",
                provider=self.name,
                retryable=True,
            ) from exc
        except httpx.HTTPStatusError as exc:
            raise self._map_http_error(exc)
        except httpx.HTTPError as exc:
            raise ChatbotProviderError(
                "Ollama is unavailable",
                code="MODEL_UNAVAILABLE",
                provider=self.name,
                retryable=True,
            ) from exc

        try:
            body = response.json()
        except ValueError as exc:
            raise ChatbotProviderError(
                "Ollama returned invalid JSON",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                retryable=True,
            ) from exc

        if not isinstance(body, dict):
            raise ChatbotProviderError(
                "Ollama returned an unexpected response payload",
                code="INVALID_PROVIDER_RESPONSE",
                provider=self.name,
                retryable=True,
            )

        return body

    def _map_http_error(self, exc: httpx.HTTPStatusError) -> ChatbotProviderError:
        status_code = exc.response.status_code
        message = f"Ollama returned {status_code}"

        try:
            body = exc.response.json()
        except ValueError:
            body = None

        if isinstance(body, dict) and isinstance(body.get("error"), str):
            message = body["error"].strip() or message

        return ChatbotProviderError(
            message,
            code="MODEL_UNAVAILABLE" if status_code != 429 else "MODEL_RATE_LIMITED",
            provider=self.name,
            status_code=503 if status_code >= 500 or status_code == 429 else status_code,
            retryable=status_code >= 500 or status_code == 429,
        )

    @staticmethod
    def _normalize_role(role: str) -> str:
        return "assistant" if role == "bot" else role
