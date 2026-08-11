# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

> The project didn't tag releases from the start. Entries up to `0.9.1` were
> reconstructed retroactively from git history, grouped by feature/PR
> boundary rather than by exact commit date.

## [Unreleased]

## [0.9.3] - 2026-08-11

### Fixed

- API error responses (400/404/409/etc.) returned Spring Boot's default
  whitelabel-style body with no `message` field, so callers couldn't tell
  *why* a request failed (e.g. importing a BoardGameGeek expansion id via
  the game-import endpoint just returned a bare `400` with no explanation).
  Added a `GlobalExceptionHandler` (`@RestControllerAdvice`) that surfaces
  the actual reason for `ResponseStatusException`, `IllegalArgumentException`,
  and `@Valid` validation failures, and also turns an unhandled BGG
  "game not found" case into a proper `400` instead of a `500`.

## [0.9.2] - 2026-08-10

### Fixed

- Telegram "session finished" notification showed a redundant midnight
  time (e.g. "Aug 9, 2026, 12:00 AM") because `playedAt` has no meaningful
  time-of-day; the notification date is now date-only. Also fixes the
  formatter crashing on the now-`Instant` `playedAt` field, which had no
  zone attached.

## [0.9.1] - 2026-08-10

### Fixed

- Timezone-naive session date/times: `LocalDateTime` fields on location
  history replaced with `Instant`, with a migration to convert existing
  columns to `timestamptz`.

## [0.9.0] - 2026-08-07

### Added

- Telegram bot integration: `TelegramNotifier` posts to a location's
  Telegram chat whenever a history entry is added, and later refined to
  only notify once an entry becomes finished.
- `GET /api/telegram/history` returning a location's 5 most recent history
  entries for the bot's `/history` command, authenticated via a shared
  `X-Telegram-Bot-Secret` header.
- Unique constraint on `telegramChatId` so the same chat can't be linked to
  more than one location.

## [0.8.0] - 2026-08-07

### Added

- `/actuator/health` endpoint (DB check included, everything else closed)
  so Northflank has something to restart against.
- Caffeine-backed TTL caching for BGG search/import and the games list.
- Bucket4j per-IP rate limiting on `/api/auth/{login,register}` and the
  BGG-facing game/expansion import routes.

## [0.7.1] - 2026-08-07

### Fixed

- HikariCP was reusing connections Neon had already closed server-side;
  shortened `max-lifetime`/`idle-timeout` and capped pool size so
  connections are retired proactively.
- `idle-timeout` had no effect because `minimum-idle` defaulted to the
  fixed pool size; set `minimum-idle=1` so the pool can actually shrink.

## [0.7.0] - 2026-08-07

### Changed

- Deploy target switched from Render to an Oracle Cloud Always Free VM
  (Docker + Caddy, GitHub Actions build/push/SSH-deploy), then from Oracle
  to Northflank after Oracle's free ARM capacity ran out and the free x86
  shape proved too tight (1GB RAM, different OS).
- Northflank now builds the existing `Dockerfile` directly from GitHub and
  redeploys on every push to `main`; GitHub Actions was simplified to a
  compile-check gate on PRs (`deploy.yml` → `ci.yml`).
- Removed `render.yaml` once the Northflank deploy was verified working
  end-to-end.

## [0.6.0] - 2026-07-31

### Added

- `POST /api/auth/google` for Google Sign-In: verifies the Google ID token
  server-side, resolves the user by stored `google_sub` or by email, and
  creates a passwordless account if neither match.

### Fixed

- Location sharing search no longer returns the admin user or the
  requesting user as share candidates.

## [0.5.0] - 2026-07-27 to 2026-07-29

### Added

- WebSocket/STOMP chat for location messages (`LocationChatController`,
  `LocationChatWebSocketController`, JWT-authenticated STOMP handshake).
- Batch fetching of players and expansions when retrieving location
  history.

### Removed

- Photo upload support on location history (superseded by chat; photo
  storage had lived outside this backend regardless).

### Changed

- Service/controller structure reorganized for clarity.

## [0.4.0] - 2026-07-23

### Added

- Player and playtime details on games.
- `BggExpansionLink` record for BoardGameGeek expansion links.
- Production Docker configuration.
- Flyway-managed schema migrations (previously Hibernate `ddl-auto`).
- Initial technology stack documentation (`docs/TECH_STACK.md`).

## [0.3.0] - 2026-07-21

### Added

- `AdminUserSeeder` for automatic admin user creation on startup.
- `GET /api/games/{id}` and `DELETE /api/games/{id}`.
- BoardGameGeek link included in game responses.
- Admin-only restriction on game delete and BGG import.

### Fixed

- Admin user seeding startup crash; role now returned in auth responses.
- Error responses no longer masked as 403.

### Changed

- `games.description` widened to unbounded `TEXT`, then capped at 2000
  characters instead of storing the full BGG text.

## [0.2.0] - 2026-07-03 to 2026-07-06

### Added

- Game creation endpoint (`CreateGameRequest` DTO).
- BoardGameGeek integration: external search and import of games
  (`BggClient`).

## [0.1.0] - 2026-06-25 to 2026-07-02

### Added

- Initial project scaffold: Spring Boot, PostgreSQL, JWT authentication,
  core entities/controllers/services/repositories.
- Docker Compose local dev environment with Adminer.
- Hot reload for local development.
