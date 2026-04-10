#!/bin/bash
set -e

echo "========================================="
echo " Smart City – Demo Environment Reset"
echo "========================================="

export $(grep -v '^#' .env | xargs)

DB="${POSTGRES_DB:-smart_city}"
USER="${POSTGRES_USER:-smart_city_user}"

echo "[1/2] Resetting database..."
docker compose down -v
docker compose up -d postgres

echo "Waiting for postgres to be ready..."
until docker compose exec postgres pg_isready -U "$USER" -d "$DB" > /dev/null 2>&1; do
    sleep 2
done

echo ""
echo "✓ Database reset complete."
echo "  Now start your services in demo mode:"
echo "  Spring Boot: SPRING_PROFILES_ACTIVE=demo ./mvnw spring-boot:run"
echo "  FastAPI:     FASTAPI_ENV=demo uvicorn app.main:app --reload"