from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from threading import Lock
from typing import Any

from app.core.config import settings
from app.services.pulse_eco_client import PulseEcoClient, PulseEcoError

SUPPORTED_METRICS = {
	"pm10",
	"pm25",
	"pm1",
	"no2",
	"o3",
	"temperature",
	"humidity",
	"pressure",
	"noise_dba",
}


@dataclass
class PollutionCache:
	overall: dict[str, float] = field(default_factory=dict)
	stations_by_metric: dict[str, list[dict[str, Any]]] = field(default_factory=dict)
	fetched_at: datetime | None = None
	last_attempt_at: datetime | None = None
	last_error: str = ""


class PollutionService:
	def __init__(
		self,
		client: PulseEcoClient,
		city: str,
		stale_after_minutes: int,
	) -> None:
		self.client = client
		self.city = city
		self.stale_after = timedelta(minutes=stale_after_minutes)
		self._cache = PollutionCache()
		self._lock = Lock()

	def warm_cache(self) -> None:
		try:
			self.refresh()
		except PulseEcoError:
			return

	def refresh(self) -> None:
		with self._lock:
			self._cache.last_attempt_at = datetime.now(timezone.utc)

		try:
			overall_payload = self.client.fetch_overall()
			current_payload = self.client.fetch_current()
		except PulseEcoError as exc:
			with self._lock:
				self._cache.last_error = str(exc)
			raise

		normalized_overall = self._normalize_overall_values(overall_payload.get("values", {}))
		normalized_stations = self._normalize_stations(current_payload)

		with self._lock:
			self._cache.overall = normalized_overall
			self._cache.stations_by_metric = normalized_stations
			self._cache.fetched_at = datetime.now(timezone.utc)
			self._cache.last_error = ""

	def get_current_snapshot(self, metric: str) -> dict[str, Any]:
		normalized_metric = metric.strip().lower()
		if normalized_metric not in SUPPORTED_METRICS:
			raise ValueError(f"Unsupported metric: {normalized_metric}")

		with self._lock:
			if self._cache.fetched_at is None:
				raise PulseEcoError("Pollution data not fetched yet")

			fetched_at = self._cache.fetched_at
			overall = dict(self._cache.overall)
			stations_by_metric = dict(self._cache.stations_by_metric)
			last_error = self._cache.last_error

		now = datetime.now(timezone.utc)
		stale = now - fetched_at > self.stale_after
		stations = stations_by_metric.get(normalized_metric, [])
		city_value = overall.get(normalized_metric)

		return {
			"source": "pulse-eco",
			"city": self.city,
			"metric": normalized_metric,
			"unit": self._unit_for_metric(normalized_metric),
			"fetchedAt": fetched_at.isoformat(),
			"stale": stale,
			"summary": {
				"cityValue": city_value,
				"statusText": self._status_text(city_value),
				"scaleMin": 0,
				"scaleMax": 200,
			},
			"coverage": {
				"mode": "multi-station" if len(stations) > 1 else "single-station",
				"activeStationCount": len(stations),
				"interpolationEnabled": len(stations) >= 3,
			},
			"legend": self._legend(),
			"stations": stations,
			"errors": self._errors(stale, last_error),
		}

	def _normalize_overall_values(self, values: dict[str, Any]) -> dict[str, float]:
		normalized: dict[str, float] = {}

		for metric, raw_value in values.items():
			metric_key = str(metric).strip().lower()
			if metric_key not in SUPPORTED_METRICS:
				continue

			parsed = self._parse_float(raw_value)
			if parsed is not None:
				normalized[metric_key] = parsed

		return normalized

	def _normalize_stations(
		self,
		current_payload: list[dict[str, Any]],
	) -> dict[str, list[dict[str, Any]]]:
		stations_by_metric: dict[str, list[dict[str, Any]]] = {}

		for item in current_payload:
			metric = str(item.get("type", "")).strip().lower()
			if metric not in SUPPORTED_METRICS:
				continue

			value = self._parse_float(item.get("value"))
			if value is None:
				continue

			position = self._parse_position(str(item.get("position", "")))
			if position is None:
				continue

			measured_at = self._parse_iso_datetime(item.get("stamp"))

			stations_by_metric.setdefault(metric, []).append(
				{
					"stationId": str(item.get("sensorId", "")).strip(),
					"name": str(item.get("sensorId", "")).strip(),
					"position": {"lat": position[0], "lng": position[1]},
					"isConfirmed": True,
					"isActive": measured_at is not None,
					"current": {
						"value": value,
						"measuredAt": measured_at,
					},
					"last24h": [],
				}
			)

		return stations_by_metric

	def _parse_position(self, value: str) -> tuple[float, float] | None:
		parts = value.split(",")
		if len(parts) != 2:
			return None

		lat = self._parse_float(parts[0])
		lng = self._parse_float(parts[1])
		if lat is None or lng is None:
			return None

		return (lat, lng)

	def _parse_iso_datetime(self, value: Any) -> str | None:
		if not value:
			return None

		value_str = str(value).strip()
		if not value_str:
			return None

		if value_str.endswith("Z"):
			value_str = value_str.replace("Z", "+00:00")

		try:
			parsed = datetime.fromisoformat(value_str)
		except ValueError:
			return None

		if parsed.tzinfo is None:
			parsed = parsed.replace(tzinfo=timezone.utc)

		return parsed.astimezone(timezone.utc).isoformat()

	def _parse_float(self, value: Any) -> float | None:
		try:
			return float(value)
		except (TypeError, ValueError):
			return None

	def _unit_for_metric(self, metric: str) -> str:
		if metric in {"temperature"}:
			return "C"
		if metric in {"humidity"}:
			return "%"
		if metric in {"pressure"}:
			return "hPa"
		if metric in {"noise_dba"}:
			return "dBA"

		return "ug/m3"

	def _status_text(self, city_value: float | None) -> str:
		if city_value is None:
			return "No current reading available"

		if city_value <= 25:
			return "Air quality is good"
		if city_value <= 50:
			return "Air quality is fair"
		if city_value <= 100:
			return "Air quality is moderate"

		return "Air quality is poor"

	def _legend(self) -> list[dict[str, Any]]:
		return [
			{"from": 0, "to": 25, "color": "#1FA34A", "label": "Good"},
			{"from": 26, "to": 50, "color": "#A3C73A", "label": "Fair"},
			{"from": 51, "to": 100, "color": "#F4C430", "label": "Moderate"},
			{"from": 101, "to": 200, "color": "#D94841", "label": "Poor"},
		]

	def _errors(self, stale: bool, last_error: str) -> list[dict[str, Any]]:
		if not stale or not last_error:
			return []

		return [
			{
				"code": "POLLUTION_SOURCE_STALE",
				"message": last_error,
				"retryable": True,
			}
		]


def _resolve_pulse_base_url() -> str:
	if settings.pulse_base_url:
		return settings.pulse_base_url

	return f"https://{settings.pulse_city}.pulse.eco/rest"


pollution_service = PollutionService(
	client=PulseEcoClient(
		base_url=_resolve_pulse_base_url(),
		timeout_seconds=settings.pulse_timeout_seconds,
		username=settings.pulse_username,
		password=settings.pulse_password,
	),
	city=settings.pulse_city,
	stale_after_minutes=settings.pollution_stale_after_minutes,
)
