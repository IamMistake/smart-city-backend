# Smart City Backend

Monorepo-style backend workspace for two microservices:

- `spring-service` (Spring Boot + Maven)
- `fastapi-service` (FastAPI + Python)

PostgreSQL is provided via Docker Compose from the repo root.

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

3. CORS is controlled by `CORS_ALLOWED_ORIGINS` (comma-separated), defaulting to:

- `http://localhost:5173` (Vite dev)
- `http://localhost:5000` (frontend preview)

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

## Run FastAPI Service (venv)

From `smart-city-backend/fastapi-service/`:

### Windows (PowerShell)

```powershell
py -3.11 -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

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
- Recommended next step: add dedicated schemas or separate databases per service as the architecture evolves.
- If you change `CORS_ALLOWED_ORIGINS`, restart both backend services.
