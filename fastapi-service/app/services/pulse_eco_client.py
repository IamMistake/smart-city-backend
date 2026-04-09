from __future__ import annotations

from collections.abc import Mapping
from typing import Any

import httpx


class PulseEcoError(RuntimeError):
    pass


class PulseEcoClient:
    def __init__(
        self,
        base_url: str,
        timeout_seconds: float,
        username: str = "",
        password: str = "",
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout_seconds = timeout_seconds
        self.username = username
        self.password = password

    def fetch_overall(self) -> dict[str, Any]:
        payload = self._get_json("/overall")

        if not isinstance(payload, Mapping):
            raise PulseEcoError("Pulse overall payload is not an object")

        values = payload.get("values") or payload.get("value")
        if not isinstance(values, Mapping):
            raise PulseEcoError("Pulse overall payload is missing values map")

        return {
            "cityName": str(payload.get("cityName", "")),
            "values": dict(values),
        }

    def fetch_current(self) -> list[dict[str, Any]]:
        payload = self._get_json("/current")

        if not isinstance(payload, list):
            raise PulseEcoError("Pulse current payload is not a list")

        current_items: list[dict[str, Any]] = []
        for item in payload:
            if isinstance(item, Mapping):
                current_items.append(dict(item))

        return current_items

    def fetch_data_raw(
        self,
        *,
        metric: str,
        from_iso: str,
        to_iso: str,
        sensor_id: str = "-1",
    ) -> list[dict[str, Any]]:
        payload = self._get_json(
            "/dataRaw",
            params={
                "sensorId": sensor_id,
                "type": metric,
                "from": from_iso,
                "to": to_iso,
            },
        )

        if not isinstance(payload, list):
            raise PulseEcoError("Pulse dataRaw payload is not a list")

        raw_items: list[dict[str, Any]] = []
        for item in payload:
            if isinstance(item, Mapping):
                raw_items.append(dict(item))

        return raw_items

    def _get_json(self, path: str, params: Mapping[str, str] | None = None) -> Any:
        url = f"{self.base_url}{path}"

        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.get(url, params=params, auth=self._auth())
                response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise PulseEcoError(f"Pulse API timeout at {path}") from exc
        except httpx.HTTPStatusError as exc:
            raise PulseEcoError(f"Pulse API returned {exc.response.status_code} at {path}") from exc
        except httpx.HTTPError as exc:
            raise PulseEcoError(f"Pulse API request failed at {path}") from exc

        try:
            return response.json()
        except ValueError as exc:
            raise PulseEcoError(f"Pulse API returned invalid JSON at {path}") from exc

    def _auth(self) -> tuple[str, str] | None:
        if not self.username:
            return None

        return (self.username, self.password)
