package com.nastolka.service.impl;

import com.nastolka.dto.CreateExpansionRequest;
import com.nastolka.dto.ExpansionResponse;
import com.nastolka.entity.Game;
import com.nastolka.entity.GameExpansion;
import com.nastolka.integration.bgg.BggClient;
import com.nastolka.integration.bgg.BggGameDetails;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.GameRepository;
import com.nastolka.service.GameExpansionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GameExpansionServiceImpl implements GameExpansionService {

    private static final String BGG_GAME_URL_TEMPLATE = "https://boardgamegeek.com/boardgame/%d";
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    private final GameRepository gameRepository;
    private final GameExpansionRepository expansionRepository;
    private final BggClient bggClient;

    public GameExpansionServiceImpl(
            GameRepository gameRepository,
            GameExpansionRepository expansionRepository,
            BggClient bggClient
    ) {
        this.gameRepository = gameRepository;
        this.expansionRepository = expansionRepository;
        this.bggClient = bggClient;
    }

    @Override
    public List<ExpansionResponse> getExpansions(Long gameId) {
        requireGame(gameId);
        return expansionRepository.findByGameId(gameId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ExpansionResponse createExpansion(Long gameId, CreateExpansionRequest request) {
        Game game = requireGame(gameId);

        GameExpansion expansion = new GameExpansion();
        expansion.setGame(game);
        expansion.setName(request.getName());
        expansion.setDescription(request.getDescription());
        expansion.setPhoto(request.getPhoto());

        return toResponse(expansionRepository.save(expansion));
    }

    @Override
    public ExpansionResponse importFromBgg(Long gameId, Long bggId) {
        Game game = requireGame(gameId);

        if (expansionRepository.existsByBggId(bggId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Expansion already imported from BoardGameGeek");
        }

        BggGameDetails details = bggClient.getGameDetails(bggId);

        GameExpansion expansion = new GameExpansion();
        expansion.setGame(game);
        expansion.setBggId(details.bggId());
        expansion.setName(details.name());
        expansion.setDescription(truncateDescription(details.description()));
        expansion.setPhoto(details.photo());

        return toResponse(expansionRepository.save(expansion));
    }

    @Override
    public void deleteExpansion(Long gameId, Long expansionId) {
        requireGame(gameId);
        GameExpansion expansion = expansionRepository.findByIdAndGameId(expansionId, gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expansion not found"));
        expansionRepository.delete(expansion);
    }

    private Game requireGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private String truncateDescription(String description) {
        if (description == null || description.length() <= DESCRIPTION_MAX_LENGTH) {
            return description;
        }
        return description.substring(0, DESCRIPTION_MAX_LENGTH);
    }

    private ExpansionResponse toResponse(GameExpansion expansion) {
        return ExpansionResponse.builder()
                .id(expansion.getId())
                .gameId(expansion.getGame().getId())
                .bggId(expansion.getBggId())
                .name(expansion.getName())
                .description(expansion.getDescription())
                .photo(expansion.getPhoto())
                .bggUrl(expansion.getBggId() != null ? BGG_GAME_URL_TEMPLATE.formatted(expansion.getBggId()) : null)
                .build();
    }
}
