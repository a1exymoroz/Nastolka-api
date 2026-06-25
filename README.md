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

The app connects to `jdbc:postgresql://localhost:5432/nastolka` using credentials from `.env.local`.

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

1. **Nastolka-API.postman_collection.json** — requests for auth and games
2. **Nastolka-Local.postman_environment.json** — local variables (`baseUrl`, test user, `token`)

Select the **Nastolka Local** environment, then run **Register** or **Login** — the JWT is saved automatically to `token` for **Get All Games**.

## Stop PostgreSQL

```bash
docker-compose down
```

To remove persisted data:

```bash
docker-compose down -v
```
