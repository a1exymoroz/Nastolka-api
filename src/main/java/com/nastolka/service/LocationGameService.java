package com.nastolka.service;

import com.nastolka.dto.GameResponse;

import java.util.List;

public interface LocationGameService {

    List<GameResponse> getGames(Long locationId, String username);

    GameResponse addGame(Long locationId, Long gameId, String username);

    void removeGame(Long locationId, Long gameId, String username);
}
