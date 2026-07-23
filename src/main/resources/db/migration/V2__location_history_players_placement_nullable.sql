-- location_history_players.placement was created NOT NULL on environments whose
-- schema predates this fix, because HistoryPlayer.java used to declare
-- @Column(nullable = false). The application only requires a placement once a
-- history entry's state is FINISHED (see LocationHistoryServiceImpl.savePlayers),
-- so CREATED/IN_PROGRESS entries with unranked players failed to insert.
ALTER TABLE location_history_players ALTER COLUMN placement DROP NOT NULL;
