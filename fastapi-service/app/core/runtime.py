from __future__ import annotations

from app.core.config import settings


def is_dev() -> bool:
	return settings.env.lower() in {"dev", "development", "local"}
