# Game deletion cascade

What happens, end to end, when `DELETE /api/games/{id}` is called.

## Entry point

`GameServiceImpl.deleteGame()` does nothing clever — it checks the game exists,
then calls `gameRepository.deleteById(id)`, which issues a single
`DELETE FROM games WHERE id = ?`. There is no application-level code that
touches expansions, locations, or history.

## Why that's enough

Every entity that references `Game` (directly or transitively) is annotated
`@OnDelete(action = OnDeleteAction.CASCADE)`. That annotation doesn't make
Hibernate cascade the delete in Java — it tells Hibernate to generate the
foreign key constraint itself with `ON DELETE CASCADE`. The cascade is
enforced by the database, not the application, so one `DELETE` on `games`
ripples outward automatically:

```
games
 ├─ game_expansions            (GameExpansion.game)
 │   └─ location_game_expansions  (LocationGameExpansion.expansion)
 ├─ location_games             (LocationGame.game)
 │   └─ location_game_expansions  (LocationGameExpansion.locationGame)
 └─ location_history           (LocationHistory.game)
     └─ location_history_players  (HistoryPlayer.history)
```

Deleting a game therefore also removes: its expansions, every location's copy
of the game, every location-specific expansion pairing, all play history for
that game, and all player placements/points tied to that history.

## The gotcha: schema changes need a migration now

Schema is managed by Flyway (`src/main/resources/db/migration`), not
`spring.jpa.hibernate.ddl-auto` (now `validate`, which only checks entities
against the live schema and never alters it). So:

- A fresh database gets the full schema, cascades included, from
  `V1__baseline.sql` onward.
- An existing database only picks up a constraint change (like adding
  `ON DELETE CASCADE` to a foreign key that was created without it) if there's
  a migration script for it. Nothing happens automatically anymore.

`scripts/reset-games-table.sql` predates Flyway and drops/recreates the game
and location tables from scratch with cascades baked in — it's a
sledgehammer for local dev only (it deletes all data in those tables). Prefer
adding a proper `V{n}__*.sql` migration under `db/migration` instead, even
locally, so the fix is captured and replays on every environment.

## Takeaway

`@OnDelete(action = OnDeleteAction.CASCADE)` is a DDL-time annotation, not a
runtime cascade. It only takes effect once the corresponding constraint is
actually created in the database — which now means a Flyway migration, not a
Hibernate auto-update. It's worth checking column/constraint definitions
directly (e.g. via `\d+ games` in `psql`) rather than assuming the annotation
is in effect.
