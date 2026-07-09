# Smart City Backend

Backend monorepo for the Smart City Monitoring Platform — powers emergency incident tracking, real-time air pollution monitoring, and a city-information chatbot.

Contains two microservices sharing a PostgreSQL database:

| Service | Stack |
|---|---|
| `spring-service/` | Spring Boot 3 + Maven + Flyway |
| `fastapi-service/` | FastAPI + SQLAlchemy + Alembic |

## Getting started

```bash
cp .env.example .env
```

### Start PostgreSQL

```bash
docker compose up -d
```

### Run Spring Service

```bash
cd spring-service
mvn spring-boot:run
```

Health: `curl http://localhost:8080/api/health/`

### Run FastAPI Service

```bash
cd fastapi-service
python3.11 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Health: `curl http://localhost:8000/api/health/`

> [!TIP]
> FastAPI starts gracefully even if the DB is unavailable, returning `status: "DOWN"` with details.

## Features

- **Incident management** — Spring Boot with Flyway migrations, Clerk JWT auth, demo data seeding
- **Pollution monitoring** — FastAPI ingests Pulse Eco data, caches it, serves current/history endpoints
- **City chatbot** — FastAPI with multi-provider support (Ollama, OpenRouter, OpenAI), primary + fallback model config
- **Demo mode** — set `SPRING_PROFILES_ACTIVE=demo` for seed data (10 Skopje incidents)

## Environment

| Variable | Default | Description |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | localhost ports | Comma-separated allowed origins |
| `SPRING_PROFILES_ACTIVE` | — | Set to `demo` for seed data |
| `CLERK_ISSUER_URL` | — | Clerk JWT issuer |
| `CLERK_JWKS_URL` | — | Clerk JWKS endpoint |

CORS defaults to `http://localhost:5173` (Vite dev) and `http://localhost:5000` (preview).

## Project structure

```text
smart-city-backend/
  spring-service/          # Spring Boot 3 + Maven
    src/main/java/.../     # Java controllers, services, entities
    src/main/resources/    # Flyway migrations, application.yml
  fastapi-service/         # FastAPI + Python 3.11
    app/
      api/routes/          # Health, pollution, chatbot, auth
      core/config.py       # Env-based config
  docker-compose.yml       # PostgreSQL service
  .env.example
```

> [!NOTE]
> Database ownership: `core` schema is owned by Spring (Flyway + JPA), `pollution` schema by FastAPI (Alembic + SQLAlchemy). FastAPI reads `core.user_profiles` for auth — both services must point to the same PostgreSQL instance.
