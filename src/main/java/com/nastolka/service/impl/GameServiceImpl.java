package com.nastolka.service.impl;

import com.nastolka.dto.CreateGameRequest;
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
    public List<GameResponse> getAllGames() {
        return gameRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GameResponse createGame(CreateGameRequest request) {
        Game game = new Game();
        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setPhoto(request.getPhoto());

        Game saved = gameRepository.save(game);
        return toResponse(saved);
    }

    private GameResponse toResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescription())
                .photo(game.getPhoto())
                .build();
    }
}
