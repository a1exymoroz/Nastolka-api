package com.nastolka.repository.projection;

public interface PlayerStatsProjection {

    String getUsername();

    String getDisplayName();

    Long getGamesPlayed();

    Long getWins();

    Long getTotalPoints();

    Double getAveragePoints();
}
