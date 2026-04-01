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
