from __future__ import annotations

import os
from pathlib import Path

from dotenv import load_dotenv

ROOT_DIR = Path(__file__).resolve().parents[3]
SERVICE_DIR = Path(__file__).resolve().parents[2]

load_dotenv(ROOT_DIR / ".env", override=False)
load_dotenv(SERVICE_DIR / ".env", override=False)


class Settings:
    service_name: str = "fastapi-service"
    env: str = os.getenv("FASTAPI_ENV", "development")
    port: int = int(os.getenv("FASTAPI_SERVICE_PORT", "8000"))
    chatbot_focus_city: str = os.getenv("CHATBOT_FOCUS_CITY", "Skopje").strip() or "Skopje"
    chatbot_timeout_seconds: float = float(
        os.getenv("CHATBOT_TIMEOUT_SECONDS", "30")
    )
    chatbot_primary_provider: str = os.getenv(
        "CHATBOT_PRIMARY_PROVIDER", "ollama"
    ).strip().lower()
    chatbot_primary_model: str = os.getenv(
        "CHATBOT_PRIMARY_MODEL", "llama3.1:8b"
    ).strip()
    chatbot_fallback_provider: str = os.getenv(
        "CHATBOT_FALLBACK_PROVIDER", ""
    ).strip().lower()
    chatbot_fallback_model: str = os.getenv(
        "CHATBOT_FALLBACK_MODEL", ""
    ).strip()
    chatbot_openai_base_url: str = os.getenv(
        "CHATBOT_OPENAI_BASE_URL", "https://api.openai.com/v1"
    ).strip()
    chatbot_openai_api_key: str = (
        os.getenv("CHATBOT_OPENAI_API_KEY", "").strip()
        or os.getenv("OPENAI_API_KEY", "").strip()
    )
    chatbot_openrouter_base_url: str = os.getenv(
        "CHATBOT_OPENROUTER_BASE_URL", "https://openrouter.ai/api/v1"
    ).strip()
    chatbot_openrouter_api_key: str = os.getenv(
        "CHATBOT_OPENROUTER_API_KEY", ""
    ).strip()
    chatbot_openrouter_site_url: str = os.getenv(
        "CHATBOT_OPENROUTER_SITE_URL", ""
    ).strip()
    chatbot_openrouter_site_name: str = os.getenv(
        "CHATBOT_OPENROUTER_SITE_NAME", "Smart City Platform"
    ).strip()
    chatbot_ollama_base_url: str = os.getenv(
        "CHATBOT_OLLAMA_BASE_URL", "http://localhost:11434"
    ).strip()
    pulse_city: str = os.getenv("PULSE_CITY", "skopje").strip().lower()
    pulse_base_url: str = os.getenv("PULSE_BASE_URL", "").strip()
    pulse_timeout_seconds: float = float(os.getenv("PULSE_TIMEOUT_SECONDS", "10"))
    pulse_username: str = os.getenv("PULSE_USERNAME", "").strip()
    pulse_password: str = os.getenv("PULSE_PASSWORD", "").strip()
    pollution_startup_fetch: bool = (
        os.getenv("POLLUTION_STARTUP_FETCH", "true").strip().lower() == "true"
    )
    pollution_stale_after_minutes: int = int(os.getenv("POLLUTION_STALE_AFTER_MINUTES", "120"))
    pollution_history_cache_ttl_minutes: int = int(
        os.getenv("POLLUTION_HISTORY_CACHE_TTL_MINUTES", "15")
    )
    clerk_issuer_url: str = os.getenv("CLERK_ISSUER_URL", "").strip()
    clerk_audience: str = os.getenv("CLERK_AUDIENCE", "").strip()
    clerk_jwks_url: str = os.getenv("CLERK_JWKS_URL", "").strip()
    postgres_host: str = os.getenv("POSTGRES_HOST", "localhost")
    postgres_port: int = int(os.getenv("POSTGRES_PORT", "5432"))
    postgres_db: str = os.getenv("POSTGRES_DB", "smart_city")
    postgres_user: str = os.getenv("POSTGRES_USER", "smart_city_user")
    postgres_password: str = os.getenv("POSTGRES_PASSWORD", "smart_city_pass")
    cors_allowed_origins: list[str] = [
        origin.strip()
        for origin in os.getenv(
            "CORS_ALLOWED_ORIGINS",
            "http://localhost:5173,http://localhost:5000",
        ).split(",")
        if origin.strip()
    ]

    @property
    def database_url(self) -> str:
        return (
            "postgresql+psycopg://"
            f"{self.postgres_user}:{self.postgres_password}"
            f"@{self.postgres_host}:{self.postgres_port}/{self.postgres_db}"
        )

    @property
    def resolved_clerk_jwks_url(self) -> str:
        if self.clerk_jwks_url:
            return self.clerk_jwks_url

        if not self.clerk_issuer_url:
            return ""

        return f"{self.clerk_issuer_url.rstrip('/')}/.well-known/jwks.json"


settings = Settings()
