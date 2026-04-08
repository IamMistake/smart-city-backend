from __future__ import annotations

import pytest
from fastapi import HTTPException

from app.api.routes import pollution
from app.services.pulse_eco_client import PulseEcoError


class FakeRouteService:
	def __init__(self, *, error: Exception | None = None) -> None:
		self.error = error

	def get_current_snapshot(self, metric: str) -> dict:
		if self.error is not None:
			raise self.error

		return {"metric": metric, "stations": []}


def test_get_current_pollution_returns_payload(monkeypatch: pytest.MonkeyPatch) -> None:
	monkeypatch.setattr(pollution, "pollution_service", FakeRouteService())

	body = pollution.get_current_pollution(metric="pm10")

	assert body["metric"] == "pm10"


def test_get_current_pollution_returns_bad_request(monkeypatch: pytest.MonkeyPatch) -> None:
	monkeypatch.setattr(
		pollution,
		"pollution_service",
		FakeRouteService(error=ValueError("Unsupported metric: aqi")),
	)

	with pytest.raises(HTTPException) as exc:
		pollution.get_current_pollution(metric="aqi")

	assert exc.value.status_code == 400


def test_get_current_pollution_returns_unavailable(monkeypatch: pytest.MonkeyPatch) -> None:
	monkeypatch.setattr(
		pollution,
		"pollution_service",
		FakeRouteService(error=PulseEcoError("Pollution data not fetched yet")),
	)

	with pytest.raises(HTTPException) as exc:
		pollution.get_current_pollution(metric="pm10")

	assert exc.value.status_code == 503
	assert exc.value.detail["code"] == "POLLUTION_DATA_UNAVAILABLE"
