package com.nastolka.repository.projection;

public interface GameRatingProjection {

    Long getGameId();

    String getGameName();

    Double getAverageRating();

    Long getRatingCount();
}
