package com.nastolka.repository.projection;

public interface GamePlayCountProjection {

    Long getGameId();

    String getGameName();

    Long getPlayCount();
}
