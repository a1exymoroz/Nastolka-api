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

    private static final String BGG_GAME_URL_TEMPLATE = "https://boardgamegeek.com/boardgame/%d";
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

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
    public GameResponse getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        return toResponse(game);
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
        game.setDescription(truncateDescription(details.description()));
        game.setPhoto(details.photo());
        game.setMinPlayers(details.minPlayers());
        game.setMaxPlayers(details.maxPlayers());
        game.setMinPlayTime(details.minPlayTime());
        game.setMaxPlayTime(details.maxPlayTime());

        return toResponse(gameRepository.save(game));
    }

    @Override
    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        gameRepository.deleteById(id);
    }

    private BggSearchResult toSearchResult(BggSearchItem item) {
        return new BggSearchResult(item.bggId(), item.name(), item.yearPublished(), item.expansion());
    }

    private String truncateDescription(String description) {
        if (description == null || description.length() <= DESCRIPTION_MAX_LENGTH) {
            return description;
        }
        return description.substring(0, DESCRIPTION_MAX_LENGTH);
    }

    private GameResponse toResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .bggId(game.getBggId())
                .name(game.getName())
                .description(game.getDescription())
                .photo(game.getPhoto())
                .bggUrl(game.getBggId() != null ? BGG_GAME_URL_TEMPLATE.formatted(game.getBggId()) : null)
                .players(formatRange(game.getMinPlayers(), game.getMaxPlayers(), "player", "players"))
                .duration(formatRange(game.getMinPlayTime(), game.getMaxPlayTime(), "min", "min"))
                .build();
    }

    private String formatRange(Integer min, Integer max, String singularSuffix, String pluralSuffix) {
        if (min == null && max == null) {
            return null;
        }
        if (min == null || max == null || min.equals(max)) {
            int value = min != null ? min : max;
            return value + " " + (value == 1 ? singularSuffix : pluralSuffix);
        }
        return min + "-" + max + " " + pluralSuffix;
    }
}
