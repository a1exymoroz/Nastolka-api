# Nastolka API

Backend API for a board game application — a Spring Boot learning project.

## Stack

- Java 21
- Spring Boot 3.4
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL (Docker, local dev)

## Local setup

### 1. Environment

Credentials live in `.env.local` (gitignored). Create it with your database, JWT, and BGG settings.

For BoardGameGeek import/search, register an app at [boardgamegeek.com/applications](https://boardgamegeek.com/applications) and set `BGG_TOKEN` in `.env.local`.

### 2. Start PostgreSQL

```bash
chmod +x scripts/start-db.sh scripts/run-local.sh
./scripts/start-db.sh
```

`run-local.sh` starts PostgreSQL automatically if it is not running yet.

Or manually:

```bash
docker-compose --env-file .env.local up -d
```

Wait until the container is healthy:

```bash
docker-compose ps
```

### 3. Run the API

**Requires Docker to be running** (Docker Desktop or Colima).

Option A — script (loads `.env.local`, starts DB, runs API):

```bash
chmod +x scripts/run-local.sh
./scripts/run-local.sh
```

Option B — manual:

```bash
set -a && source .env.local && set +a
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The app connects to PostgreSQL on `localhost:${POSTGRES_PORT}` using credentials from `.env.local`.

## Local ports

| Service | URL / connection | Notes |
|---------|------------------|-------|
| API | http://localhost:8090 | Spring Boot (`SERVER_PORT`) |
| Adminer | http://localhost:8091 | Browser DB UI (`ADMINER_PORT`) |
| PostgreSQL | localhost:5433 | Host port (`POSTGRES_PORT`), avoids conflict with other apps on 5432 |

**Adminer login** (from browser at http://localhost:8091):

- System: PostgreSQL
- Server: `postgres`
- Username / password / database: from `.env.local`
- Port: `5432` (internal Docker port, not 5433)

## Project structure

```
src/main/java/com/nastolka/
├── NastolkaApplication.java
├── config/SecurityConfig.java
├── controller/
│   ├── AuthController.java      # POST /api/auth/register, /api/auth/login
│   └── GameController.java      # GET  /api/games
├── dto/
├── entity/
├── repository/
├── security/
└── service/
```

## Your TODOs

1. **`JwtUtil`** — Implement token generation, parsing, and validation.
2. **`JwtAuthFilter`** — Extract Bearer token, validate, and populate `SecurityContextHolder`.
3. **`AuthServiceImpl`** — Hash passwords with `PasswordEncoder`, validate credentials, handle duplicate registration.
4. **`SecurityConfig`** — Fine-tune the filter chain and add 401/403 exception handlers.
5. **`GameServiceImpl`** — Add filtering, pagination, or sorting as needed.

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | Public | Register a new user |
| POST | /api/auth/login | Public | Login and get JWT |
| GET | /api/games | Required | List all games |
| POST | /api/games | Required | Create a game manually |
| GET | /api/games/search-external?query=catan | Required | Search BoardGameGeek |
| POST | /api/games/import/{bggId} | Required | Import game from BGG into DB |

## BoardGameGeek import

1. Get a token at [boardgamegeek.com/applications](https://boardgamegeek.com/applications)
2. Add to `.env.local`: `BGG_TOKEN=your-token`
3. Restart the API
4. In Postman: **Login** → **Search BGG** → **Import Game from BGG**

BGG rate limit: ~1 request per 5 seconds (handled automatically).

## Postman

Import the files from `postman/` into Postman:

1. **Nastolka-API.postman_collection.json** — auth, games, and JWT failure tests
2. **Nastolka-Local.postman_environment.json** — `baseUrl` (8090), credentials, `token`

Select the **Nastolka Local** environment, then:

1. **Register** — creates a unique user and saves `token`
2. **Search BGG** → **Import Game from BGG** — fetch real game data (needs `BGG_TOKEN`)
3. **Get All Games** — uses Bearer `{{token}}`
4. **Get All Games - No Token** / **Invalid Token** — expect 403 / 401

## Stop PostgreSQL

```bash
docker-compose down
```

To remove persisted data:

```bash
docker-compose down -v
```

## Deployment

The API runs on [Northflank](https://northflank.com/)'s free tier (always-on, no idle sleep, automatic HTTPS on a `*.northflank.app` domain). Northflank is linked directly to this GitHub repo and builds/deploys the existing [Dockerfile](Dockerfile) itself on every push to `main` — [.github/workflows/ci.yml](.github/workflows/ci.yml) only runs a compile check on PRs and pushes, it doesn't perform the deploy. The database (Neon Postgres) is unaffected — only the API container moves.

### One-time Northflank setup

1. Sign up at [northflank.com](https://northflank.com/) (no card required) and create a **Project** (e.g. `nastolka`).
2. In that project, create a **Service**, source **Deployment** (build a Docker image from this repo, not "Combined"/git-store-image), link the GitHub account and select the `Nastolka-api` repo, branch `main`. Build type **Dockerfile**, context `/`, Dockerfile path `/Dockerfile` (matches this repo's layout).
3. **Resources**: pick the largest compute plan available without an upgrade prompt — Spring Boot needs more than the default 256MB tier to avoid OOM at startup.
4. **Networking**: add a public port mapping `8080 → HTTP` so Northflank issues a public URL with automatic TLS.
5. **Environment variables** (Runtime variables — never committed to git):
   ```
   SPRING_PROFILES_ACTIVE=prod
   POSTGRES_HOST=...
   POSTGRES_PORT=5432
   POSTGRES_DB=...
   POSTGRES_USER=...
   POSTGRES_PASSWORD=...
   APP_JWT_SECRET=...
   APP_JWT_EXPIRATION_MS=86400000
   GOOGLE_CLIENT_ID=...
   CORS_ALLOWED_ORIGINS=https://nastolka.netlify.app
   ADMIN_USERNAME=admin
   ADMIN_EMAIL=...
   ADMIN_PASSWORD=...
   BGG_TOKEN=...
   BGG_API_BASE_URL=https://boardgamegeek.com/xmlapi2
   ```

After that, every merge to `main` triggers Northflank to rebuild the Dockerfile and redeploy automatically.
