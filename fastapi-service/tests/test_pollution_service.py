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


def test_refresh_and_snapshot_map_payload() -> None:
	service = PollutionService(
		client=FakePulseClient(),
		city="skopje",
		stale_after_minutes=120,
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
	)

	with pytest.raises(PulseEcoError):
		service.get_current_snapshot(metric="pm10")


def test_get_current_snapshot_rejects_unknown_metric() -> None:
	service = PollutionService(
		client=FakePulseClient(),
		city="skopje",
		stale_after_minutes=120,
	)

	service.refresh()

	with pytest.raises(ValueError):
		service.get_current_snapshot(metric="aqi")
