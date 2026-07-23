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

## The gotcha: `ddl-auto=update`

`application-local.properties` sets `spring.jpa.hibernate.ddl-auto=update`.
Update mode adds missing tables and columns, but it will **not** retroactively
add `ON DELETE CASCADE` to a foreign key that already exists without it. So:

- If the tables don't exist yet, Hibernate creates them fresh with the cascade
  constraint baked in — deleting a game just works.
- If the tables were already created by an earlier run of the app (before the
  `@OnDelete` annotations existed on these entities), the live foreign keys
  may lack `ON DELETE CASCADE`. In that case `DELETE FROM games` fails with a
  foreign-key violation instead of cascading.

`scripts/reset-games-table.sql` exists for exactly this situation — it drops
and recreates `games`, `game_expansions`, `locations`, `location_games`,
`location_game_expansions`, `location_shares`, `location_history`, and
`location_history_players` with the foreign keys explicitly declared
`ON DELETE CASCADE`. Run it if a game delete throws a constraint violation
locally.

## Takeaway

`@OnDelete(action = OnDeleteAction.CASCADE)` is a DDL-time annotation, not a
runtime cascade. It only changes behavior if Hibernate actually gets to
(re)create the constraint — on a fresh schema or after a manual reset. It's
worth checking column/constraint definitions directly (e.g. via
`\d+ games` in `psql`) rather than assuming the annotation is in effect.
