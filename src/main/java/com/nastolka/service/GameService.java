package com.nastolka.service;

import com.nastolka.dto.GameResponse;

import java.util.List;

public interface GameService {

    List<GameResponse> getAllAvailableGames();
}
