# Smart City Backend

Monorepo-style backend workspace for two microservices:

- `spring-service` (Spring Boot + Maven)
- `fastapi-service` (FastAPI + Python)

PostgreSQL is provided via Docker Compose from the repo root.

## Full-stack Docker Compose

The repository root contains `compose.yaml` for the full application stack:

- `frontend`
- `spring-service`
- `fastapi-service`
- `postgres`

Environment files used by the stack:

- `smart-city-frontend/.env` for the frontend build
- `smart-city-backend/.env` for backend runtime configuration

From the repository root:

```bash
docker compose up --build
```

Published ports:

- Frontend: `http://localhost:3000`
- Spring service: `http://localhost:8080`
- FastAPI service: `http://localhost:8000`
- Postgres: `localhost:5432`

Container notes:

- Spring connects to Postgres through `POSTGRES_HOST=postgres` inside Compose.
- FastAPI runs `alembic upgrade head` on container startup before serving requests.
- Frontend is built as a static Vite bundle and served by Nginx with SPA route fallback.

## Project Structure

```text
smart-city-backend/
  spring-service/
    pom.xml
    src/main/java/com/smartcity/springservice/
    src/main/resources/application.yml
  fastapi-service/
    requirements.txt
    app/main.py
    app/api/routes/health.py
    app/core/config.py
  docker-compose.yml
  .env.example
  .gitignore
  README.md
```

## Prerequisites

- Docker + Docker Compose
- Java 17+ (recommended: Java 21)
- Maven 3.9+
- Python 3.11 (for FastAPI)

## Install Java 17+/21 and Maven (Windows/macOS/Linux)

Recommended (Java 21):

### Windows (PowerShell, using winget)

```powershell
winget install -e --id EclipseAdoptium.Temurin.21.JDK
winget install -e --id Apache.Maven
java -version
mvn -v
```

Alternative (Java 17):

```powershell
winget install -e --id EclipseAdoptium.Temurin.17.JDK
winget install -e --id Apache.Maven
java -version
mvn -v
```

### macOS (Homebrew)

```bash
brew install --cask temurin
brew install maven
java -version
mvn -v
```

### Linux

Install OpenJDK 21 (or 17) and Maven using your distro package manager, then verify:

```bash
java -version
mvn -v
```

## Environment Setup

1. Copy env template:

```powershell
# PowerShell (Windows)
Copy-Item .env.example .env

# Command Prompt (Windows)
copy .env.example .env

# macOS/Linux
cp .env.example .env
```

2. Values in `.env` are used by Docker Compose and can also be reused by services.

   - Spring imports `../.env` through `spring.config.import`.
   - FastAPI loads `../.env` (and `fastapi-service/.env` if present) via `python-dotenv` in `app/core/config.py`.

3. CORS is controlled by `CORS_ALLOWED_ORIGINS` (comma-separated), defaulting to:

- `http://localhost:5173` (Vite dev)
- `http://localhost:5000` (frontend preview)
- `http://localhost:3000` (Dockerized frontend)

## Run PostgreSQL

From `smart-city-backend/`:

```bash
docker compose up -d
docker compose ps
```

Postgres defaults:

- Host: `localhost`
- Port: `${POSTGRES_PORT}` (default `5432`)
- DB: `${POSTGRES_DB}`
- User: `${POSTGRES_USER}`

## Run Spring Service

From `smart-city-backend/spring-service/`:

```bash
mvn spring-boot:run
```

Health endpoint:

```bash
# Windows PowerShell
curl.exe http://localhost:8080/api/health/

# macOS/Linux
curl http://localhost:8080/api/health/
```

Build and test:

```bash
mvn clean test
mvn clean package
```

Database migrations (Flyway):

- Migrations are in `spring-service/src/main/resources/db/migration`.
- `spring.jpa.hibernate.ddl-auto` is set to `validate`.
- Start the app and Flyway applies pending migrations automatically.

Authentication endpoints (Spring):

- `GET /api/auth/me` requires a valid Clerk bearer token.
- On first successful call, Spring auto-provisions `core.user_profiles` if the `clerk_user_id` does not exist.
- Clerk config env vars used by Spring:
  - `CLERK_ISSUER_URL`
  - `CLERK_JWKS_URL`
  - `CLERK_AUDIENCE` (optional)

## Run FastAPI Service (venv)

From `smart-city-backend/fastapi-service/`:

### Windows (PowerShell)

```powershell
py -3.11 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Database migrations (Alembic):

```bash
alembic upgrade head
```

Create a new revision:

```bash
alembic revision -m "describe change"
```

Authentication endpoints (FastAPI):

- `GET /api/auth/me` requires a valid Clerk bearer token.
- FastAPI validates JWT signature/issuer and reads active users from `core.user_profiles`.
- FastAPI does not auto-provision users; it expects the user to already exist in `core.user_profiles`.
- Clerk config env vars used by FastAPI:
  - `CLERK_ISSUER_URL`
  - `CLERK_JWKS_URL`
  - `CLERK_AUDIENCE` (optional)

Pollution endpoint (FastAPI):

- `GET /api/pollution/current?metric=pm10`
- `GET /api/pollution/history?metric=pm10&windowHours=24&bucketMinutes=60&sensorId=1003`
- Pulls latest data from Pulse Eco (`/overall` + `/current`) and serves cached data.
- History uses Pulse Eco `dataRaw` and returns backend-bucketed time series.
- By default, FastAPI warms the cache on startup (`POLLUTION_STARTUP_FETCH=true`).
- Supported metric values: `pm10`, `pm25`, `pm1`, `no2`, `o3`, `temperature`, `humidity`, `pressure`, `noise_dba`.

Chatbot endpoint (FastAPI):

- `GET /api/chatbot/models`
- `POST /api/chatbot/messages`
- Uses free-model providers configured through env vars.
- Supported providers in this repo:
- `ollama` for local models
- `openrouter` for free hosted models
- `openai` for OpenAI-hosted models
- Default selection comes from:
  - `CHATBOT_PRIMARY_PROVIDER`
  - `CHATBOT_PRIMARY_MODEL`
- Optional fallback comes from:
  - `CHATBOT_FALLBACK_PROVIDER`
  - `CHATBOT_FALLBACK_MODEL`
- Provider-specific env vars:
  - `CHATBOT_OPENAI_BASE_URL`
  - `CHATBOT_OPENAI_API_KEY`
  - `CHATBOT_OLLAMA_BASE_URL`
  - `CHATBOT_OPENROUTER_BASE_URL`
  - `CHATBOT_OPENROUTER_API_KEY`
  - `CHATBOT_OPENROUTER_SITE_URL`
  - `CHATBOT_OPENROUTER_SITE_NAME`

Demo data seeding:

- Set `SPRING_PROFILES_ACTIVE=demo` to enable demo seeders.
- The demo profile seeds user profiles, incidents, cameras, police units, and city events.
- The current incident seeder inserts 10 Skopje incidents when the incidents table is empty.
- The demo profile in this repo is safe for shared Postgres and keeps Flyway + JPA validation enabled.

### Windows (Command Prompt)

```bat
py -3.11 -m venv .venv
.venv\Scripts\activate.bat
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### macOS/Linux

```bash
python3.11 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Health endpoint:

```bash
# Windows PowerShell
curl.exe http://localhost:8000/api/health/

# macOS/Linux
curl http://localhost:8000/api/health/
```

FastAPI now includes a PostgreSQL connection check.
If the DB is unavailable, FastAPI still starts (graceful mode), and health returns:

- `status: "DOWN"`
- `db_status: "DOWN"`
- an `error` message with connection details

## Run FastAPI Service (Conda)

From `smart-city-backend/fastapi-service/`:

```bash
conda create -n smart-city-fastapi python=3.11 -y
conda activate smart-city-fastapi
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## Notes

- `application-local.yml` is intentionally ignored by git for local Spring overrides.
- Current setup is scaffold-level and ready for feature modules.
- Database ownership:
  - `core` schema is owned by Spring service (Flyway + JPA entities)
  - `pollution` schema is owned by FastAPI service (Alembic + SQLAlchemy models)
  - FastAPI auth lookup reads from `core.user_profiles`, so both services must point to the same PostgreSQL instance.
- If you change `CORS_ALLOWED_ORIGINS`, restart both backend services.

## CI Expectations

GitHub Actions now provides baseline PR quality gates for this backend repo through
[`backend-ci.yml`](./.github/workflows/backend-ci.yml).

Required checks for pull requests:

- `spring-boot`: runs `mvn -B test` and `mvn -B package -DskipTests`
- `fastapi`: runs `ruff`, `pytest`, and `python -m compileall app tests`

Merge blocking policy:

- In GitHub, add a branch protection rule or ruleset for your protected branches (`main`, and `dev` if used).
- Enable `Require a pull request before merging`.
- Enable `Require status checks to pass before merging`.
- Mark the `spring-boot` and `fastapi` jobs from the `Backend CI` workflow as required checks.

Status expectations:

- A PR is merge-ready only when all required CI checks are green.
- Any failed or skipped required check must be fixed or rerun before merge.
