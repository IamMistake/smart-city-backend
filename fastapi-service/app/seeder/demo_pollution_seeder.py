from __future__ import annotations

from datetime import datetime, timezone, timedelta

from sqlalchemy.orm import Session
from sqlalchemy import func, select

from app.models.pollution_sensor import PollutionSensor
from app.models.pollution_measurement import PollutionMeasurement


DEMO_SENSORS = [
    {
        "provider": "pulse_eco",
        "city_name": "skopje",
        "external_sensor_id": "1001",
        "sensor_type_code": "AQI",
        "description": "Центар - Плоштад Македонија",
        "comments": "Demo sensor – city center",
        "status": "ACTIVE",
        "latitude": 41.9964,
        "longitude": 21.4314,
        "is_active": True,
    },
    {
        "provider": "pulse_eco",
        "city_name": "skopje",
        "external_sensor_id": "1002",
        "sensor_type_code": "AQI",
        "description": "Аеродром - Јане Сандански",
        "comments": "Demo sensor – Aerodrom district",
        "status": "ACTIVE",
        "latitude": 41.9756,
        "longitude": 21.4643,
        "is_active": True,
    },
    {
        "provider": "pulse_eco",
        "city_name": "skopje",
        "external_sensor_id": "1003",
        "sensor_type_code": "AQI",
        "description": "Железара - Индустриска зона",
        "comments": "Demo sensor – heavy industry zone, high readings",
        "status": "ACTIVE",
        "latitude": 41.9856,
        "longitude": 21.4637,
        "is_active": True,
    },
    {
        "provider": "pulse_eco",
        "city_name": "skopje",
        "external_sensor_id": "1004",
        "sensor_type_code": "AQI",
        "description": "Карпош - Градски Парк",
        "comments": "Demo sensor – residential/park area, low readings",
        "status": "ACTIVE",
        "latitude": 41.9933,
        "longitude": 21.4094,
        "is_active": True,
    },
    {
        "provider": "pulse_eco",
        "city_name": "skopje",
        "external_sensor_id": "1005",
        "sensor_type_code": "AQI",
        "description": "Чаир - бул. 1 Мај",
        "comments": "Demo sensor – Chair district",
        "status": "ACTIVE",
        "latitude": 41.9998,
        "longitude": 21.4345,
        "is_active": True,
    },
    {
        "provider": "pulse_eco",
        "city_name": "skopje",
        "external_sensor_id": "1006",
        "sensor_type_code": "AQI",
        "description": "Ѓорче Петров - Западен дел",
        "comments": "Demo sensor – western suburb",
        "status": "INACTIVE",
        "latitude": 41.9921,
        "longitude": 21.3876,
        "is_active": False,
    },
]

SENSOR_READINGS: dict[str, list[tuple[str, str]]] = {
    "1001": [("pm10", "48.2"),  ("pm25", "31.4"), ("no2", "42.1"), ("temperature", "18.5"), ("humidity", "61.0")],
    "1002": [("pm10", "62.1"),  ("pm25", "41.3"), ("no2", "55.2"), ("temperature", "17.9"), ("humidity", "58.0")],
    "1003": [("pm10", "118.7"), ("pm25", "87.6"), ("no2", "98.3"), ("temperature", "19.1"), ("humidity", "54.0")],
    "1004": [("pm10", "22.3"),  ("pm25", "14.1"), ("no2", "18.7"), ("temperature", "17.2"), ("humidity", "67.0")],
    "1005": [("pm10", "55.9"),  ("pm25", "36.8"), ("no2", "49.5"), ("temperature", "18.0"), ("humidity", "60.0")],
    "1006": [("pm10", "31.4"),  ("pm25", "20.2"), ("no2", "27.8"), ("temperature", "16.8"), ("humidity", "65.0")],
}

HISTORY_HOURS = 24


def seed_pollution(db: Session) -> None:
    """Idempotent: skips if sensors already exist."""
    existing = db.scalar(select(func.count()).select_from(PollutionSensor))
    if existing and existing > 0:
        print(">>> [DEMO] Pollution sensors already seeded, skipping.")
        return

    now = datetime.now(timezone.utc)
    sensor_objects: list[PollutionSensor] = []

    for sensor_data in DEMO_SENSORS:
        sensor = PollutionSensor(**sensor_data)
        sensor.last_synced_at = now
        db.add(sensor)
        sensor_objects.append(sensor)

    # flush so sensors get their UUIDs before measurements reference them
    db.flush()

    measurements: list[PollutionMeasurement] = []

    for sensor in sensor_objects:
        ext_id = sensor.external_sensor_id
        readings = SENSOR_READINGS.get(ext_id, [])

        for hours_ago in range(HISTORY_HOURS, -1, -1):
            measured_at = now - timedelta(hours=hours_ago)

            for metric_type, base_value in readings:
                variance = _jitter(hours_ago, metric_type)
                value = round(float(base_value) + variance, 2)

                measurements.append(PollutionMeasurement(
                    sensor_id=sensor.id,
                    measured_at=measured_at,
                    metric_type=metric_type,
                    metric_value=str(value),
                    source_year=measured_at.year,
                    raw_payload={
                        "demo": True,
                        "sensor_id": ext_id,
                        "metric": metric_type,
                        "value": value,
                        "unit": _unit_for(metric_type),
                        "timestamp": measured_at.isoformat(),
                    },
                ))

    db.add_all(measurements)
    db.commit()

    print(f">>> [DEMO] Seeded {len(sensor_objects)} sensors "
          f"and {len(measurements)} measurements.")


def _jitter(hours_ago: int, metric: str) -> float:
    """Deterministic variance so charts look realistic but resets are identical."""
    seed = (hours_ago * 7 + hash(metric)) % 20
    return (seed - 10) * 0.3


def _unit_for(metric: str) -> str:
    return {
        "pm10": "μg/m³",
        "pm25": "μg/m³",
        "no2":  "μg/m³",
        "temperature": "°C",
        "humidity": "%",
    }.get(metric, "")