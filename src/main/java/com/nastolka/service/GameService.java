package com.nastolka.service;

import com.nastolka.dto.CreateGameRequest;
import com.nastolka.dto.GameResponse;

import java.util.List;

public interface GameService {

    List<GameResponse> getAllGames();

    GameResponse createGame(CreateGameRequest request);
}
