from __future__ import annotations

import os


class Settings:
	service_name: str = "fastapi-service"
	env: str = os.getenv("FASTAPI_ENV", "development")
	port: int = int(os.getenv("FASTAPI_SERVICE_PORT", "8000"))
	cors_allowed_origins: list[str] = [
		origin.strip()
		for origin in os.getenv(
			"CORS_ALLOWED_ORIGINS",
			"http://localhost:5173,http://localhost:5000",
		).split(",")
		if origin.strip()
	]


settings = Settings()
