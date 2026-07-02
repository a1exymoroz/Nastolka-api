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

Credentials live in `.env.local` (gitignored). Create it with your database and JWT settings (see existing `.env.local` in the repo if present, or ask a teammate).

### 2. Start PostgreSQL

```bash
chmod +x scripts/start-db.sh
./scripts/start-db.sh
```

Or manually:

```bash
docker-compose --env-file .env.local up -d
```

Wait until the container is healthy:

```bash
docker-compose ps
```

### 3. Run the API

Option A — script (loads `.env.local` automatically):

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

| Method | Path               | Auth     | Description          |
|--------|--------------------|----------|----------------------|
| POST   | /api/auth/register | Public   | Register a new user  |
| POST   | /api/auth/login    | Public   | Login and get JWT    |
| GET    | /api/games         | Required | List available games |

## Postman

Import the files from `postman/` into Postman:

1. **Nastolka-API.postman_collection.json** — auth, games, and JWT failure tests
2. **Nastolka-Local.postman_environment.json** — `baseUrl` (8090), credentials, `token`

Select the **Nastolka Local** environment, then:

1. **Register** — creates a unique user and saves `token`
2. **Get All Games** — uses Bearer `{{token}}`
3. **Get All Games - No Token** / **Invalid Token** — expect 403 / 401

## Stop PostgreSQL

```bash
docker-compose down
```

To remove persisted data:

```bash
docker-compose down -v
```
