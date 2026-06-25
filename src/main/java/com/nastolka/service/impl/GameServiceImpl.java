package com.nastolka.service.impl;

import com.nastolka.dto.GameResponse;
import com.nastolka.entity.Game;
import com.nastolka.repository.GameRepository;
import com.nastolka.service.GameService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public List<GameResponse> getAllAvailableGames() {
        // TODO: Implement query logic (filtering, pagination, sorting, etc.)
        List<Game> games = gameRepository.findByAvailableTrue();

        return games.stream()
                .map(this::toResponse)
                .toList();
    }

    private GameResponse toResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .title(game.getTitle())
                .description(game.getDescription())
                .minPlayers(game.getMinPlayers())
                .maxPlayers(game.getMaxPlayers())
                .playTimeMinutes(game.getPlayTimeMinutes())
                .available(game.getAvailable())
                .build();
    }
}
