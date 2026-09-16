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
| Connection pool | HikariCP (tuned for a pooled free-tier connection in prod) |
| Migrations | Flyway (`flyway-core` + `flyway-database-postgresql`) |
| External API | BoardGameGeek XML API v2 (`RestClient`) |
| Real-time | Spring WebSocket + STOMP (location chat) |
| Caching | Spring Cache + Caffeine (BGG search/import, games list) |
| Rate limiting | Bucket4j (per-IP, auth + BGG import routes) |
| Health checks | Spring Boot Actuator (`/actuator/health`) |
| Local DB | Docker Compose (Postgres + Adminer) |
| Production DB | Supabase (managed Postgres via Supavisor session pooler, `sslmode=require`) |
| Production host | Northflank (Docker web service, native GitHub build/deploy) |
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
| Google Sign-In | `GoogleTokenVerifier` (`google-api-client`) | Verifies ID token signature/expiry/issuer/audience + manual `email_verified` check |
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

### Google Sign-In stack (ID token → JWT)

```
HTTP POST /api/auth/google { idToken }
    ↓
GoogleTokenVerifier.verify(idToken)
    ├─ checks signature, expiry, issuer, audience (app.google.client-id)
    └─ checks email_verified manually
    ↓
AuthServiceImpl resolves user:
    ├─ by stored google_sub, else
    ├─ by email (links the Google account to an existing user), else
    └─ creates a new passwordless account
    ↓
JwtUtil.generateToken(username) → AuthResponse { token }
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
| `/api/auth/**`, `/error`, `/actuator/health/**` | Public |
| `/ws/**` | Public at the HTTP layer; STOMP `CONNECT` frames are authenticated separately by `StompAuthChannelInterceptor` (JWT) |
| `/api/telegram/**` | Public at the Spring Security layer; authenticated per-request via a shared `X-Telegram-Bot-Secret` header checked in `TelegramBotController` |
| `DELETE /api/games/**` | `ROLE_ADMIN` |
| `POST /api/games/import/**` | `ROLE_ADMIN` |
| `POST /api/games/*/expansions/import/**` | `ROLE_ADMIN` |
| `POST /api/locations/{id}/games/import/**`, `POST /api/locations/{id}/games/*/expansions/import/**` | Authenticated + location owner or `ROLE_ADMIN` (checked in service layer via `LocationAccessGuard`, not `SecurityConfig`) |
| `/api/games/**`, `/api/locations/**` | Authenticated |
| Everything else | Authenticated |

---

## Data stack

| Piece | Technology | Notes |
|-------|------------|-------|
| Driver | `org.postgresql:postgresql` | JDBC to Postgres (runtime scope) |
| Pool | HikariCP | Tuned for Supabase's Supavisor pooler in prod: `maximum-pool-size=5`, `minimum-idle=1`, `max-lifetime=180000`, `idle-timeout=150000` — retires connections proactively before the pooler closes them server-side |
| ORM | Hibernate | `ddl-auto=validate` — schema owned entirely by Flyway |
| Repositories | Spring Data JPA | `JpaRepository` interfaces per entity |
| Migrations | Flyway | `src/main/resources/db/migration/V*.sql` |
| Cascades | DB-level `ON DELETE CASCADE` | Set via `@OnDelete` on entities, enforced by Postgres, not application code |

Domain model: `User` / `Role`, `Game` / `GameExpansion`, `Location`, `LocationGame` / `LocationGameExpansion` (per-location copies), `LocationHistory` / `HistoryPlayer` / `HistoryExpansion` / `HistoryState` (play sessions), `LocationShare` (sharing a location with another user), `LocationChatMessage` (real-time location chat).

See [Game deletion cascade](game-deletion-cascade.md) for how a single `DELETE /api/games/{id}` ripples through expansions, location copies, and history via DB-enforced cascades.

---

## BoardGameGeek integration

| Piece | Technology |
|-------|------------|
| Client | Spring 6 `RestClient` (`BggConfig` → `app.bgg.base-url`, default `https://boardgamegeek.com/xmlapi2`) |
| Auth | Bearer token (`app.bgg.token` / `BGG_TOKEN`) required per request; 503 if unset |
| Rate limiting | `BggClient` enforces a 5s minimum interval between requests in-process (`synchronized`) |
| Parsing | `BggXmlParser` — hand-rolled XML parsing into `BggSearchItem` / `BggGameDetails` / `BggExpansionLink` |
| Use cases | `GET /api/games/search-external`, `POST /api/games/import/{bggId}` (ADMIN), same pattern for expansions; `POST /api/locations/{id}/games/import/{bggId}` lets a location owner import-or-reuse a catalog game and attach it in one call, same pattern for expansions |
| Errors | Upstream failures mapped to `502 Bad Gateway` via `ResponseStatusException` |
| Caching | Search results and game details are Caffeine-cached (`CacheConfig`), TTLs configurable via `app.cache.*` / `*_CACHE_TTL_MINUTES` |
| Rate limiting | `RateLimitFilter` (Bucket4j) additionally throttles the BGG-facing import routes per IP |

---

## Location history photos

Photo storage for location history entries lives outside this backend, in Netlify
Blobs accessed via Netlify Functions in the frontend repo. Those functions
authorize each request by calling this backend's `GET /api/locations/{locationId}`
with the caller's bearer token and checking for a 200 response. This backend has
no photo upload/storage code of its own.

The backend previously stored photos itself; that code was removed when
real-time chat (below) replaced it as the way to share moments from a session.

At current volume — one re-encoded JPEG per history entry, typically well under
1MB — this is nowhere near Netlify Blobs' free-tier limits, so cost/capacity is
not a reason to move providers. The actual cost of the current design is the
extra HTTP round-trip on every photo operation (the Netlify Function has no way
to verify this backend's JWT itself, so it calls back into
`GET /api/locations/{locationId}` just to check access) and having photo logic
split across two repos. If that overhead is ever worth removing, the cheap fix
is having the Netlify Function verify the JWT locally (shared secret / public
key) instead of calling back into this API — no need to migrate the storage
provider itself.

---

## Real-time location chat

| Piece | Technology / class |
|-------|---------------------|
| Transport | Spring WebSocket + STOMP (`WebSocketConfig`) over `/ws/**` |
| Auth | `StompAuthChannelInterceptor` validates the JWT on the STOMP `CONNECT` frame (the raw `/ws/**` HTTP upgrade route is `permitAll` in `SecurityConfig`, since STOMP auth happens at the frame level instead) |
| Controllers | `LocationChatController` (REST, e.g. history), `LocationChatWebSocketController` (`@MessageMapping`) |
| Service | `LocationChatServiceImpl` |
| Storage | `LocationChatMessage` entity, persisted per location |

---

## Telegram integration

| Piece | Technology / class |
|-------|---------------------|
| Outbound notifications | `TelegramNotifier` (`integration.telegram`) calls the Telegram Bot API `sendMessage` on a location's `telegramChatId` whenever a history entry becomes finished; no-ops silently if `TELEGRAM_BOT_TOKEN` or the location's chat id isn't set |
| Inbound (bot commands) | `GET /api/telegram/history?chatId=...` (`TelegramBotController`) returns a location's 5 most recent history entries for the bot's `/history` command |
| Auth | Shared `X-Telegram-Bot-Secret` header checked against `TELEGRAM_BOT_SECRET` — there is no per-user JWT auth for the bot |
| Constraints | `telegramChatId` is unique across locations (DB constraint + service-level 409) so one chat can't be linked to more than one location |

---

## Health checks, caching & rate limiting

Added so Northflank has a health probe to restart against, repeated BGG
searches don't re-hit the rate-limited BGG API, and nothing throttles a
single client hammering auth or BGG-facing endpoints.

| Piece | Technology / class |
|-------|---------------------|
| Health | Spring Boot Actuator, `/actuator/health` only (DB check included; everything else closed via `management.endpoints.web.exposure.include=health`) |
| Caching | Spring Cache + Caffeine (`CacheConfig`) — TTL caches for BGG search, BGG game details, and the games list |
| Rate limiting | Bucket4j (`RateLimitFilter`, per-IP token bucket) on `/api/auth/{login,register}` and the BGG-facing game/expansion import routes |

---

## Configuration & secrets

Loaded from environment (`.env.local` for dev, Northflank env vars for prod), via Spring profiles (`SPRING_PROFILES_ACTIVE=local|prod`):

| Variable | Used by |
|----------|---------|
| `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | JDBC datasource (Supabase Session pooler in prod) |
| `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS` | JWT signing / expiry |
| `GOOGLE_CLIENT_ID` | Google Sign-In (`app.google.client-id`) |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origin(s) |
| `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | `AdminUserSeeder` bootstrap |
| `BGG_TOKEN`, `BGG_API_BASE_URL` | BoardGameGeek integration |
| `BGG_SEARCH_CACHE_TTL_MINUTES`, `BGG_GAME_DETAILS_CACHE_TTL_MINUTES`, `GAMES_LIST_CACHE_TTL_MINUTES` | Caffeine cache TTLs (all default if unset) |
| `RATE_LIMIT_AUTH_CAPACITY`, `RATE_LIMIT_AUTH_REFILL_TOKENS`, `RATE_LIMIT_AUTH_REFILL_SECONDS` | Bucket4j limiter on `/api/auth/**` (defaults: 10 / 10 / 60s) |
| `RATE_LIMIT_BGG_CAPACITY`, `RATE_LIMIT_BGG_REFILL_TOKENS`, `RATE_LIMIT_BGG_REFILL_SECONDS` | Bucket4j limiter on BGG import routes (defaults: 5 / 5 / 60s) |
| `TELEGRAM_BOT_TOKEN`, `TELEGRAM_BOT_SECRET` | Telegram integration (unset = feature no-ops) |
| `SERVER_PORT` (local) / `PORT` (prod, Northflank-injected) | HTTP port |

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
Northflank service, linked directly to the GitHub repo
    → builds the repo's Dockerfile natively (no GitHub Actions build/push step)
    → redeploys automatically on every push to main
    → Dockerfile: maven:3.9-eclipse-temurin-21 build → eclipse-temurin:21-jre-alpine runtime
    → SPRING_PROFILES_ACTIVE=prod
    → Flyway migrates on startup (baseline-on-migrate=true)
    → Supabase PostgreSQL via Session pooler (POSTGRES_* env vars, sslmode=require)
    → APP_JWT_SECRET set as a Northflank secret
```

Runs on Northflank's free 512MB plan, which is why HikariCP's pool is capped
small (see [Data stack](#data-stack)). `.github/workflows/ci.yml` only
compile-checks PRs — it doesn't build images or deploy; Northflank owns that
end to end. This replaced an earlier Render deployment (which slept after
~15 min idle) and a brief Oracle Cloud Always Free VM attempt (abandoned
when free ARM capacity ran out).

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
| `spring-boot-starter-websocket` | STOMP over WebSocket for location chat |
| `spring-boot-starter-cache` + `caffeine` | TTL caching for BGG/games responses |
| `bucket4j_jdk17-core` | Per-IP rate limiting |
| `spring-boot-starter-actuator` | `/actuator/health` |
| `google-api-client` | Google ID token verification for Google Sign-In |
| `spring-boot-devtools` | Hot reload (dev, optional) |
| `spring-boot-starter-test`, `spring-security-test` | Tests only |

---

## Related docs

- [Game deletion cascade](game-deletion-cascade.md) — how DB-level `ON DELETE CASCADE` propagates from `games`
