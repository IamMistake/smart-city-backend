from __future__ import annotations

import os


class Settings:
	service_name: str = "fastapi-service"
	env: str = os.getenv("FASTAPI_ENV", "development")
	port: int = int(os.getenv("FASTAPI_SERVICE_PORT", "8000"))
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


settings = Settings()
