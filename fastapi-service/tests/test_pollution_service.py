from __future__ import annotations

import pytest

from app.services.pollution_service import PollutionService
from app.services.pulse_eco_client import PulseEcoError


class FakePulseClient:
    def __init__(self) -> None:
        self.should_fail = False

    def fetch_overall(self) -> dict:
        if self.should_fail:
            raise PulseEcoError("upstream unavailable")

        return {
            "cityName": "skopje",
            "values": {
                "pm10": "6",
                "pm25": "4",
                "humidity": "74",
            },
        }

    def fetch_current(self) -> list[dict]:
        if self.should_fail:
            raise PulseEcoError("upstream unavailable")

        return [
            {
                "sensorId": "1003",
                "stamp": "2026-04-02T08:15:00Z",
                "type": "pm10",
                "position": "41.9981,21.4254",
                "value": "7",
            },
            {
                "sensorId": "invalid-position",
                "stamp": "2026-04-02T08:15:00Z",
                "type": "pm10",
                "position": "bad-position",
                "value": "5",
            },
        ]

    def fetch_data_raw(
        self,
        *,
        metric: str,
        from_iso: str,
        to_iso: str,
        sensor_id: str = "-1",
    ) -> list[dict]:
        if self.should_fail:
            raise PulseEcoError("upstream unavailable")

        return [
            {
                "sensorId": sensor_id,
                "type": metric,
                "stamp": "2026-04-02T08:05:00Z",
                "value": "7",
            },
            {
                "sensorId": sensor_id,
                "type": metric,
                "stamp": "2026-04-02T08:35:00Z",
                "value": "9",
            },
        ]


def test_refresh_and_snapshot_map_payload() -> None:
    service = PollutionService(
        client=FakePulseClient(),
        city="skopje",
        stale_after_minutes=120,
        history_cache_ttl_minutes=15,
    )

    service.refresh()
    snapshot = service.get_current_snapshot(metric="pm10")

    assert snapshot["city"] == "skopje"
    assert snapshot["metric"] == "pm10"
    assert snapshot["summary"]["cityValue"] == 6.0
    assert snapshot["coverage"]["activeStationCount"] == 1
    assert snapshot["coverage"]["interpolationEnabled"] is False
    assert snapshot["stations"][0]["stationId"] == "1003"
    assert snapshot["stations"][0]["position"]["lat"] == 41.9981


def test_refresh_failure_keeps_cache_and_marks_error() -> None:
    client = FakePulseClient()
    service = PollutionService(
        client=client,
        city="skopje",
        stale_after_minutes=0,
        history_cache_ttl_minutes=15,
    )

    service.refresh()
    client.should_fail = True

    with pytest.raises(PulseEcoError):
        service.refresh()

    snapshot = service.get_current_snapshot(metric="pm10")

    assert snapshot["stale"] is True
    assert snapshot["errors"][0]["code"] == "POLLUTION_SOURCE_STALE"


def test_get_current_snapshot_requires_fetch_first() -> None:
    service = PollutionService(
        client=FakePulseClient(),
        city="skopje",
        stale_after_minutes=120,
        history_cache_ttl_minutes=15,
    )

    with pytest.raises(PulseEcoError):
        service.get_current_snapshot(metric="pm10")


def test_get_current_snapshot_rejects_unknown_metric() -> None:
    service = PollutionService(
        client=FakePulseClient(),
        city="skopje",
        stale_after_minutes=120,
        history_cache_ttl_minutes=15,
    )

    service.refresh()

    with pytest.raises(ValueError):
        service.get_current_snapshot(metric="aqi")


def test_history_snapshot_returns_bucketed_series() -> None:
    service = PollutionService(
        client=FakePulseClient(),
        city="skopje",
        stale_after_minutes=120,
        history_cache_ttl_minutes=15,
    )

    history = service.get_history_snapshot(
        metric="pm10",
        window_hours=24,
        bucket_minutes=60,
        sensor_id=None,
    )

    assert history["metric"] == "pm10"
    assert history["bucketMinutes"] == 60
    assert len(history["series"]) > 1
    assert all("at" in point and "value" in point for point in history["series"])


def test_history_snapshot_uses_cached_payload_on_failure() -> None:
    client = FakePulseClient()
    service = PollutionService(
        client=client,
        city="skopje",
        stale_after_minutes=120,
        history_cache_ttl_minutes=0,
    )

    service.get_history_snapshot(
        metric="pm10",
        window_hours=24,
        bucket_minutes=60,
        sensor_id="1003",
    )
    client.should_fail = True

    history = service.get_history_snapshot(
        metric="pm10",
        window_hours=24,
        bucket_minutes=60,
        sensor_id="1003",
    )

    assert history["stale"] is True
    assert history["errors"][0]["code"] == "POLLUTION_SOURCE_STALE"
