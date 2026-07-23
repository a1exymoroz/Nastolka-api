package com.nastolka.service.impl;

import com.nastolka.dto.ExpansionResponse;
import com.nastolka.dto.GameResponse;
import com.nastolka.entity.Game;
import com.nastolka.entity.GameExpansion;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.LocationGameExpansion;
import com.nastolka.entity.User;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.GameRepository;
import com.nastolka.repository.LocationGameExpansionRepository;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.service.LocationGameService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationGameServiceImpl implements LocationGameService {

    private static final String BGG_GAME_URL_TEMPLATE = "https://boardgamegeek.com/boardgame/%d";

    private final LocationRepository locationRepository;
    private final GameRepository gameRepository;
    private final LocationGameRepository locationGameRepository;
    private final LocationGameExpansionRepository locationGameExpansionRepository;
    private final GameExpansionRepository gameExpansionRepository;
    private final LocationAccessGuard accessGuard;

    public LocationGameServiceImpl(
            LocationRepository locationRepository,
            GameRepository gameRepository,
            LocationGameRepository locationGameRepository,
            LocationGameExpansionRepository locationGameExpansionRepository,
            GameExpansionRepository gameExpansionRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.gameRepository = gameRepository;
        this.locationGameRepository = locationGameRepository;
        this.locationGameExpansionRepository = locationGameExpansionRepository;
        this.gameExpansionRepository = gameExpansionRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public List<GameResponse> getGames(Long locationId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        List<LocationGame> locationGames = locationGameRepository.findByLocationId(locationId);
        if (locationGames.isEmpty()) {
            return List.of();
        }
        List<Long> locationGameIds = locationGames.stream().map(LocationGame::getId).toList();
        List<Long> gameIds = locationGames.stream().map(lg -> lg.getGame().getId()).distinct().toList();

        Map<Long, List<ExpansionResponse>> expansionsByLocationGameId = locationGameExpansionRepository
                .findByLocationGameIdIn(locationGameIds).stream()
                .collect(Collectors.groupingBy(
                        lge -> lge.getLocationGame().getId(),
                        Collectors.mapping(lge -> toExpansionResponse(lge.getExpansion()), Collectors.toList())));

        Map<Long, List<ExpansionResponse>> catalogExpansionsByGameId = gameExpansionRepository
                .findByGameIdIn(gameIds).stream()
                .collect(Collectors.groupingBy(
                        expansion -> expansion.getGame().getId(),
                        Collectors.mapping(this::toExpansionResponse, Collectors.toList())));

        return locationGames.stream()
                .map(locationGame -> toResponse(
                        locationGame.getGame(),
                        expansionsByLocationGameId.getOrDefault(locationGame.getId(), List.of()),
                        catalogExpansionsByGameId.getOrDefault(locationGame.getGame().getId(), List.of())))
                .toList();
    }

    @Override
    public GameResponse addGame(Long locationId, Long gameId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (locationGameRepository.existsByLocationIdAndGameId(locationId, gameId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already added to this location");
        }

        LocationGame locationGame = new LocationGame();
        locationGame.setLocation(location);
        locationGame.setGame(game);
        locationGameRepository.save(locationGame);

        return toResponse(game, List.of(), List.of());
    }

    @Override
    public void removeGame(Long locationId, Long gameId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        LocationGame locationGame = locationGameRepository.findByLocationIdAndGameId(locationId, gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found in this location"));
        locationGameRepository.delete(locationGame);
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private GameResponse toResponse(Game game, List<ExpansionResponse> expansions, List<ExpansionResponse> catalogExpansions) {
        return GameResponse.builder()
                .id(game.getId())
                .bggId(game.getBggId())
                .name(game.getName())
                .description(game.getDescription())
                .photo(game.getPhoto())
                .bggUrl(game.getBggId() != null ? BGG_GAME_URL_TEMPLATE.formatted(game.getBggId()) : null)
                .players(formatRange(game.getMinPlayers(), game.getMaxPlayers(), "player", "players"))
                .duration(formatRange(game.getMinPlayTime(), game.getMaxPlayTime(), "min", "min"))
                .expansions(expansions)
                .catalogExpansions(catalogExpansions)
                .build();
    }

    private ExpansionResponse toExpansionResponse(GameExpansion expansion) {
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
