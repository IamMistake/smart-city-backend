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

    def get_history_snapshot(
        self,
        *,
        metric: str,
        window_hours: int,
        bucket_minutes: int,
        sensor_id: str | None,
    ) -> dict:
        if self.error is not None:
            raise self.error

        return {
            "metric": metric,
            "windowHours": window_hours,
            "bucketMinutes": bucket_minutes,
            "sensorId": sensor_id,
            "series": [],
        }


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


def test_get_pollution_history_returns_payload(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(pollution, "pollution_service", FakeRouteService())

    body = pollution.get_pollution_history(
        metric="pm10",
        window_hours=24,
        bucket_minutes=60,
        sensor_id="1003",
    )

    assert body["metric"] == "pm10"
    assert body["sensorId"] == "1003"


def test_get_pollution_history_returns_bad_request(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(
        pollution,
        "pollution_service",
        FakeRouteService(error=ValueError("windowHours must be between 1 and 168")),
    )

    with pytest.raises(HTTPException) as exc:
        pollution.get_pollution_history(
            metric="pm10",
            window_hours=0,
            bucket_minutes=60,
            sensor_id=None,
        )

    assert exc.value.status_code == 400


def test_get_pollution_history_returns_unavailable(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(
        pollution,
        "pollution_service",
        FakeRouteService(error=PulseEcoError("upstream unavailable")),
    )

    with pytest.raises(HTTPException) as exc:
        pollution.get_pollution_history(
            metric="pm10",
            window_hours=24,
            bucket_minutes=60,
            sensor_id=None,
        )

    assert exc.value.status_code == 503
    assert exc.value.detail["code"] == "POLLUTION_DATA_UNAVAILABLE"
