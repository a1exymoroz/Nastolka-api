package com.nastolka.repository.projection;

public interface PlayerStatsProjection {

    String getUsername();

    Long getGamesPlayed();

    Long getWins();

    Long getTotalPoints();

    Double getAveragePoints();
}
