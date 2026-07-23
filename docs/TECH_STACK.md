# Technology Stack

What this backend is built with — runtime, frameworks, data layer, auth, external integrations, and hosting.

For the game deletion cascade behavior, see [Game deletion cascade](game-deletion-cascade.md).

---

## At a glance

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Build | Maven |
| Framework | Spring Boot 3.4.0 |
| API | Spring Web (REST + JSON) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Security | Spring Security 6 (stateless JWT) |
| Password hashing | BCrypt (`BCryptPasswordEncoder`) |
| Tokens | JJWT 0.12.6 (HS256 signed JWT) |
| Database | PostgreSQL 16 |
| ORM | Hibernate via Spring Data JPA |
| Connection pool | HikariCP (Spring Boot default) |
| Migrations | Flyway (`flyway-core` + `flyway-database-postgresql`) |
| External API | BoardGameGeek XML API v2 (`RestClient`) |
| File storage | Local filesystem (history photo uploads) |
| Local DB | Docker Compose (Postgres + Adminer) |
| Production host | Render (Docker web service) |
| Dev productivity | Spring Boot DevTools (hot reload) |

---

## Application layers

How a typical authenticated request moves through the system:

```
Client (Bearer JWT in Authorization header)
    ↓
CORS filter (CorsConfig — allowed origins from app.cors.allowed-origins)
    ↓
JwtAuthFilter (parse JWT, load user, set SecurityContext)
    ↓
SecurityFilterChain (authorize: public vs authenticated vs ADMIN-only routes)
    ↓
@RestController (AuthController, GameController, LocationController, …)
    ↓
@Service (AuthServiceImpl, GameServiceImpl, LocationServiceImpl, …)
    ↓
Repository interface (Spring Data JPA — UserRepository, GameRepository, …)
    ↓
Hibernate / JPA
    ↓
HikariCP connection pool
    ↓
PostgreSQL JDBC driver
    ↓
PostgreSQL
```

Register/login hit `AuthController` → `AuthServiceImpl` → `UserRepository` directly — there is no separate Spring Security `AuthenticationManager` in play; password checking happens by hand with `PasswordEncoder.matches()`.

---

## Authentication stack

Auth is **stateless**: no server-side HTTP sessions. After login, the client sends a JWT on each request.

### Libraries

| Piece | Library / class | Role |
|-------|-----------------|------|
| Security framework | `spring-boot-starter-security` | Filter chain, method security |
| Password encoder | `BCryptPasswordEncoder` | Hash on register; `matches()` on login |
| JWT create/verify | `JwtUtil` (JJWT) | Sign with `app.jwt.secret`; HS256; expiry via `app.jwt.expiration-ms` (default 24h) |
| Per-request auth | `JwtAuthFilter` | `OncePerRequestFilter`, skips `/api/auth/**` and `OPTIONS`, runs before `UsernamePasswordAuthenticationFilter` |
| User loading | `UserService.findByUsername()` | Looked up per-request inside `JwtAuthFilter` (no `UserDetailsService`) |
| Roles | `Role` enum (`USER`, `ADMIN`) | Mapped to `ROLE_*` `SimpleGrantedAuthority` |
| Central config | `SecurityConfig` + `CorsConfig` | Route rules, stateless sessions, CORS, `PasswordEncoder` bean |
| Admin bootstrap | `AdminUserSeeder` (`CommandLineRunner`) | Creates an admin user from `ADMIN_USERNAME`/`ADMIN_EMAIL`/`ADMIN_PASSWORD` on startup if it doesn't exist |

### Register stack (password → database)

```
HTTP POST /api/auth/register
    ↓
Jackson deserializes JSON → RegisterRequest
    ↓
Jakarta Validation (@Valid on controller)
    ↓
AuthServiceImpl.register()
    ↓
BCryptPasswordEncoder.encode(plainPassword)
    ↓
User entity (role = USER, password field = hash)
    ↓
UserRepository.save() → JPA INSERT
    ↓
JwtUtil.generateToken(username) → AuthResponse JSON
```

### Login stack (password check → JWT)

```
HTTP POST /api/auth/login
    ↓
LoginRequest validated
    ↓
AuthServiceImpl.login()
    ↓
UserRepository.findByUsername()
    ↓
PasswordEncoder.matches(typedPassword, storedHash)
    ↓
JwtUtil.generateToken(username)
    ↓
AuthResponse { token }
```

### Protected request stack (JWT, no password)

```
HTTP request + Authorization: Bearer <jwt>
    ↓
JwtAuthFilter
    ├─ JwtUtil.validateAndExtractUsername(jwt)
    ├─ UserService.findByUsername(subject)
    └─ builds ROLE_<Role> authority from user.getRole()
    ↓
SecurityContextHolder populated (cleared again in a finally block after the request)
    ↓
Controller method runs (e.g. POST /api/locations)
```

The JWT only carries the username (`sub` claim), issued-at, and expiry — signed with `app.jwt.secret` (`APP_JWT_SECRET`), not encrypted.

### Authorization rules (`SecurityConfig`)

| Route | Access |
|-------|--------|
| `/api/auth/**`, `/error` | Public |
| `DELETE /api/games/**` | `ROLE_ADMIN` |
| `POST /api/games/import/**` | `ROLE_ADMIN` |
| `POST /api/games/*/expansions/import/**` | `ROLE_ADMIN` |
| `/api/games/**`, `/api/locations/**` | Authenticated |
| Everything else | Authenticated |

---

## Data stack

| Piece | Technology | Notes |
|-------|------------|-------|
| Driver | `org.postgresql:postgresql` | JDBC to Postgres (runtime scope) |
| Pool | HikariCP | Spring Boot default, no custom tuning |
| ORM | Hibernate | `ddl-auto=validate` — schema owned entirely by Flyway |
| Repositories | Spring Data JPA | `JpaRepository` interfaces per entity |
| Migrations | Flyway | `src/main/resources/db/migration/V*.sql` |
| Cascades | DB-level `ON DELETE CASCADE` | Set via `@OnDelete` on entities, enforced by Postgres, not application code |

Domain model: `User` / `Role`, `Game` / `GameExpansion`, `Location`, `LocationGame` / `LocationGameExpansion` (per-location copies), `LocationHistory` / `HistoryPlayer` / `HistoryExpansion` / `HistoryState` (play sessions), `LocationShare` (sharing a location with another user).

See [Game deletion cascade](game-deletion-cascade.md) for how a single `DELETE /api/games/{id}` ripples through expansions, location copies, and history via DB-enforced cascades.

---

## BoardGameGeek integration

| Piece | Technology |
|-------|------------|
| Client | Spring 6 `RestClient` (`BggConfig` → `app.bgg.base-url`, default `https://boardgamegeek.com/xmlapi2`) |
| Auth | Bearer token (`app.bgg.token` / `BGG_TOKEN`) required per request; 503 if unset |
| Rate limiting | `BggClient` enforces a 5s minimum interval between requests in-process (`synchronized`) |
| Parsing | `BggXmlParser` — hand-rolled XML parsing into `BggSearchItem` / `BggGameDetails` / `BggExpansionLink` |
| Use cases | `GET /api/games/search-external`, `POST /api/games/import/{bggId}` (ADMIN), same pattern for expansions |
| Errors | Upstream failures mapped to `502 Bad Gateway` via `ResponseStatusException` |

---

## File storage stack

| Piece | Technology |
|-------|------------|
| Storage | Local filesystem, directory from `app.upload.dir` / `UPLOAD_DIR` (default `uploads/history`) |
| Component | `HistoryPhotoStorage` |
| Constraints | Image content-types only; filenames are randomly generated (`UUID`); path traversal guarded by resolving against and checking `startsWith(root)` |
| Limits | `spring.servlet.multipart.max-file-size` / `max-request-size` = 10MB |
| Use case | One photo per `LocationHistory` play session (`POST/GET/DELETE /api/locations/{id}/history/{historyId}/photo`) |

Uploaded files are **not** committed to the repo and are not backed by object storage — on Render's ephemeral filesystem this means photos don't survive a redeploy.

---

## Configuration & secrets

Loaded from environment (`.env.local` for dev, Render env vars for prod), via Spring profiles (`SPRING_PROFILES_ACTIVE=local|prod`):

| Variable | Used by |
|----------|---------|
| `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | JDBC datasource |
| `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS` | JWT signing / expiry |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origin(s) |
| `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | `AdminUserSeeder` bootstrap |
| `BGG_TOKEN`, `BGG_API_BASE_URL` | BoardGameGeek integration |
| `UPLOAD_DIR` | History photo storage directory |
| `SERVER_PORT` (local) / `PORT` (prod, Render-injected) | HTTP port |

`application.properties` just selects the active profile; `application-local.properties` and `application-prod.properties` hold the actual values. Local uses a plain JDBC URL; prod appends `?sslmode=require`.

---

## Local development stack

```
./scripts/run-local.sh
    → loads .env.local
    → starts Postgres + Adminer (docker-compose, if not already running)
    → mvn spring-boot:run -Dspring-boot.run.profiles=local
    → API:      http://localhost:8090  (SERVER_PORT)
    → Adminer:  http://localhost:8091  (ADMINER_PORT)
    → Postgres: localhost:5433 (host) → 5432 (container)
```

Dependencies declared in `pom.xml`; `spring-boot-devtools` gives hot reload; tests use `spring-boot-starter-test` + `spring-security-test`.

---

## Production stack

```
Render Web Service (Docker, render.yaml)
    → Dockerfile: maven:3.9-eclipse-temurin-21 build → eclipse-temurin:21-jre-alpine runtime
    → SPRING_PROFILES_ACTIVE=prod
    → Flyway migrates on startup (baseline-on-migrate=true)
    → PostgreSQL (POSTGRES_* env vars, sslmode=require)
    → APP_JWT_SECRET generated by Render
```

---

## Maven dependencies (summary)

From `pom.xml`:

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST API, embedded Tomcat, Jackson |
| `spring-boot-starter-data-jpa` | JPA, Hibernate, transactions |
| `spring-boot-starter-security` | Auth filter chain, BCrypt |
| `spring-boot-starter-validation` | Request DTO validation |
| `postgresql` | JDBC driver (runtime) |
| `flyway-core` + `flyway-database-postgresql` | Schema migrations |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | JWT |
| `spring-boot-devtools` | Hot reload (dev, optional) |
| `spring-boot-starter-test`, `spring-security-test` | Tests only |

---

## Related docs

- [Game deletion cascade](game-deletion-cascade.md) — how DB-level `ON DELETE CASCADE` propagates from `games`
