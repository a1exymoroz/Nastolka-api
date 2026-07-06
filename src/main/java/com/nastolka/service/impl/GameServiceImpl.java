package com.nastolka.service.impl;

import com.nastolka.dto.BggSearchResult;
import com.nastolka.dto.CreateGameRequest;
import com.nastolka.dto.GameResponse;
import com.nastolka.entity.Game;
import com.nastolka.integration.bgg.BggClient;
import com.nastolka.integration.bgg.BggGameDetails;
import com.nastolka.integration.bgg.BggSearchItem;
import com.nastolka.repository.GameRepository;
import com.nastolka.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final BggClient bggClient;

    public GameServiceImpl(GameRepository gameRepository, BggClient bggClient) {
        this.gameRepository = gameRepository;
        this.bggClient = bggClient;
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

    @Override
    public List<BggSearchResult> searchExternal(String query) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }

        return bggClient.search(query.trim()).stream()
                .map(this::toSearchResult)
                .toList();
    }

    @Override
    public GameResponse importFromBgg(Long bggId) {
        if (gameRepository.existsByBggId(bggId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already imported from BoardGameGeek");
        }

        BggGameDetails details = bggClient.getGameDetails(bggId);

        Game game = new Game();
        game.setBggId(details.bggId());
        game.setName(details.name());
        game.setDescription(details.description());
        game.setPhoto(details.photo());

        return toResponse(gameRepository.save(game));
    }

    private BggSearchResult toSearchResult(BggSearchItem item) {
        return new BggSearchResult(item.bggId(), item.name(), item.yearPublished());
    }

    private GameResponse toResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .bggId(game.getBggId())
                .name(game.getName())
                .description(game.getDescription())
                .photo(game.getPhoto())
                .build();
    }
}
