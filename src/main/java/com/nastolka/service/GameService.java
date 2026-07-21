package com.nastolka.service;

import com.nastolka.dto.BggSearchResult;
import com.nastolka.dto.CreateGameRequest;
import com.nastolka.dto.GameResponse;

import java.util.List;

public interface GameService {

    List<GameResponse> getAllGames();

    GameResponse getGameById(Long id);

    GameResponse createGame(CreateGameRequest request);

    List<BggSearchResult> searchExternal(String query);

    GameResponse importFromBgg(Long bggId);

    void deleteGame(Long id);
}
