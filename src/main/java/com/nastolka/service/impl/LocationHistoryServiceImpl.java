package com.nastolka.service.impl;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.ExpansionResponse;
import com.nastolka.dto.HistoryResponse;
import com.nastolka.dto.PlayerPlacementRequest;
import com.nastolka.dto.PlayerResultResponse;
import com.nastolka.entity.Game;
import com.nastolka.entity.GameExpansion;
import com.nastolka.entity.HistoryExpansion;
import com.nastolka.entity.HistoryPlayer;
import com.nastolka.entity.HistoryState;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.LocationHistory;
import com.nastolka.entity.User;
import com.nastolka.integration.telegram.TelegramNotifier;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.GameRepository;
import com.nastolka.repository.HistoryExpansionRepository;
import com.nastolka.repository.HistoryPlayerRepository;
import com.nastolka.repository.LocationGameExpansionRepository;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationHistoryRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.LocationShareRepository;
import com.nastolka.repository.UserRepository;
import com.nastolka.service.LocationHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocationHistoryServiceImpl implements LocationHistoryService {

    private static final String BGG_GAME_URL_TEMPLATE = "https://boardgamegeek.com/boardgame/%d";

    private final LocationRepository locationRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final LocationGameRepository locationGameRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final HistoryPlayerRepository historyPlayerRepository;
    private final GameExpansionRepository expansionRepository;
    private final LocationGameExpansionRepository locationGameExpansionRepository;
    private final HistoryExpansionRepository historyExpansionRepository;
    private final LocationShareRepository locationShareRepository;
    private final LocationAccessGuard accessGuard;
    private final TelegramNotifier telegramNotifier;

    public LocationHistoryServiceImpl(
            LocationRepository locationRepository,
            GameRepository gameRepository,
            UserRepository userRepository,
            LocationGameRepository locationGameRepository,
            LocationHistoryRepository locationHistoryRepository,
            HistoryPlayerRepository historyPlayerRepository,
            GameExpansionRepository expansionRepository,
            LocationGameExpansionRepository locationGameExpansionRepository,
            HistoryExpansionRepository historyExpansionRepository,
            LocationShareRepository locationShareRepository,
            LocationAccessGuard accessGuard,
            TelegramNotifier telegramNotifier
    ) {
        this.locationRepository = locationRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.locationGameRepository = locationGameRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.historyPlayerRepository = historyPlayerRepository;
        this.expansionRepository = expansionRepository;
        this.locationGameExpansionRepository = locationGameExpansionRepository;
        this.historyExpansionRepository = historyExpansionRepository;
        this.locationShareRepository = locationShareRepository;
        this.accessGuard = accessGuard;
        this.telegramNotifier = telegramNotifier;
    }

    @Override
    public List<HistoryResponse> getHistory(Long locationId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        List<LocationHistory> historyEntries = locationHistoryRepository.findByLocationIdOrderByPlayedAtDesc(locationId);
        if (historyEntries.isEmpty()) {
            return List.of();
        }
        List<Long> historyIds = historyEntries.stream().map(LocationHistory::getId).toList();

        Map<Long, List<PlayerResultResponse>> playersByHistoryId = historyPlayerRepository
                .findByHistoryIdInOrderByPlacementAsc(historyIds).stream()
                .collect(Collectors.groupingBy(
                        historyPlayer -> historyPlayer.getHistory().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::toPlayerResponse, Collectors.toList())));

        Map<Long, List<ExpansionResponse>> expansionsByHistoryId = historyExpansionRepository
                .findByHistoryIdIn(historyIds).stream()
                .collect(Collectors.groupingBy(
                        historyExpansion -> historyExpansion.getHistory().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(he -> toExpansionResponse(he.getExpansion()), Collectors.toList())));

        return historyEntries.stream()
                .map(history -> toResponse(
                        history,
                        playersByHistoryId.getOrDefault(history.getId(), List.of()),
                        expansionsByHistoryId.getOrDefault(history.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public HistoryResponse addHistory(Long locationId, CreateHistoryRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        Game game = resolveLocationGame(locationId, request.getGameId());

        LocationHistory history = new LocationHistory();
        history.setLocation(location);
        history.setGame(game);
        history.setPlayedAt(request.getPlayedAt() != null ? request.getPlayedAt() : LocalDateTime.now());
        history.setState(request.getState());
        history.setStartedAt(resolveStartedAt(null, request));
        history.setFinishedAt(resolveFinishedAt(null, request));
        history.setRating(request.getRating());
        validateTimes(history);
        history = locationHistoryRepository.save(history);

        savePlayers(history, request.getPlayers(), request.getState() == HistoryState.FINISHED);
        saveExpansions(history, request.getExpansionIds());

        HistoryResponse response = toResponse(history);
        telegramNotifier.notifyHistoryAdded(location, response);
        return response;
    }

    @Override
    @Transactional
    public HistoryResponse updateHistory(Long locationId, Long historyId, CreateHistoryRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        LocationHistory history = requireHistory(locationId, historyId);

        Game game = resolveLocationGame(locationId, request.getGameId());
        history.setGame(game);
        history.setPlayedAt(request.getPlayedAt() != null ? request.getPlayedAt() : LocalDateTime.now());
        history.setStartedAt(resolveStartedAt(history.getStartedAt(), request));
        history.setFinishedAt(resolveFinishedAt(history.getFinishedAt(), request));
        history.setState(request.getState());
        history.setRating(request.getRating());
        validateTimes(history);
        history = locationHistoryRepository.save(history);

        historyPlayerRepository.deleteByHistoryId(history.getId());
        historyPlayerRepository.flush();
        savePlayers(history, request.getPlayers(), request.getState() == HistoryState.FINISHED);

        historyExpansionRepository.deleteByHistoryId(history.getId());
        historyExpansionRepository.flush();
        saveExpansions(history, request.getExpansionIds());

        return toResponse(history);
    }

    @Override
    @Transactional
    public void deleteHistory(Long locationId, Long historyId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        LocationHistory history = requireHistory(locationId, historyId);
        locationHistoryRepository.delete(history);
    }

    private void savePlayers(LocationHistory history, List<PlayerPlacementRequest> playerRequests, boolean requireRanking) {
        Location location = history.getLocation();
        int playerCount = playerRequests.size();
        Set<Integer> seenPlacements = new HashSet<>();
        Set<String> seenUsernames = new HashSet<>();
        List<HistoryPlayer> players = new ArrayList<>();

        for (PlayerPlacementRequest playerRequest : playerRequests) {
            if (!seenUsernames.add(playerRequest.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Player '" + playerRequest.getUsername() + "' is listed more than once");
            }

            Integer placement = playerRequest.getPlacement();
            if (requireRanking) {
                if (placement == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Placement is required for every player once the session is finished");
                }
                if (!seenPlacements.add(placement)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Placement " + placement + " is assigned to more than one player");
                }
                if (placement < 1 || placement > playerCount) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Placements must be a ranking from 1 to " + playerCount + " with no gaps or ties");
                }
            }

            User player = userRepository.findByUsername(playerRequest.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User not found: " + playerRequest.getUsername()));

            boolean isOwner = location.getOwner().getId().equals(player.getId());
            boolean isShared = locationShareRepository.existsByLocationIdAndUserId(location.getId(), player.getId());
            if (!isOwner && !isShared) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Player '" + playerRequest.getUsername() + "' does not have access to this location");
            }

            HistoryPlayer historyPlayer = new HistoryPlayer();
            historyPlayer.setHistory(history);
            historyPlayer.setUser(player);
            historyPlayer.setPlacement(placement);
            historyPlayer.setPoints(playerRequest.getPoints());
            players.add(historyPlayer);
        }

        historyPlayerRepository.saveAll(players);
    }

    private void saveExpansions(LocationHistory history, List<Long> expansionIds) {
        if (expansionIds == null || expansionIds.isEmpty()) {
            return;
        }

        LocationGame locationGame = locationGameRepository
                .findByLocationIdAndGameId(history.getLocation().getId(), history.getGame().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not part of this location"));

        Set<Long> seenExpansionIds = new HashSet<>();
        List<HistoryExpansion> historyExpansions = new ArrayList<>();

        for (Long expansionId : expansionIds) {
            if (!seenExpansionIds.add(expansionId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Expansion " + expansionId + " is listed more than once");
            }

            GameExpansion expansion = expansionRepository.findById(expansionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Expansion not found: " + expansionId));

            if (!locationGameExpansionRepository.existsByLocationGameIdAndExpansionId(locationGame.getId(), expansionId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Expansion '" + expansion.getName() + "' is not part of this location's game library");
            }

            HistoryExpansion historyExpansion = new HistoryExpansion();
            historyExpansion.setHistory(history);
            historyExpansion.setExpansion(expansion);
            historyExpansions.add(historyExpansion);
        }

        historyExpansionRepository.saveAll(historyExpansions);
    }

    private LocalDateTime resolveStartedAt(LocalDateTime existing, CreateHistoryRequest request) {
        if (request.getStartedAt() != null) {
            return request.getStartedAt();
        }
        if (existing != null) {
            return existing;
        }
        if (request.getState() == HistoryState.IN_PROGRESS || request.getState() == HistoryState.FINISHED) {
            return LocalDateTime.now();
        }
        return null;
    }

    private LocalDateTime resolveFinishedAt(LocalDateTime existing, CreateHistoryRequest request) {
        if (request.getFinishedAt() != null) {
            return request.getFinishedAt();
        }
        if (existing != null) {
            return existing;
        }
        if (request.getState() == HistoryState.FINISHED) {
            return LocalDateTime.now();
        }
        return null;
    }

    private void validateTimes(LocationHistory history) {
        if (history.getStartedAt() != null && history.getFinishedAt() != null
                && history.getFinishedAt().isBefore(history.getStartedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Finished time cannot be before started time");
        }
    }

    private Game resolveLocationGame(Long locationId, Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (!locationGameRepository.existsByLocationIdAndGameId(locationId, gameId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not part of this location");
        }

        return game;
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private LocationHistory requireHistory(Long locationId, Long historyId) {
        return locationHistoryRepository.findByIdAndLocationId(historyId, locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "History entry not found"));
    }

    private HistoryResponse toResponse(LocationHistory history) {
        List<PlayerResultResponse> players = historyPlayerRepository.findByHistoryIdOrderByPlacementAsc(history.getId()).stream()
                .map(this::toPlayerResponse)
                .toList();

        List<ExpansionResponse> expansions = historyExpansionRepository.findByHistoryId(history.getId()).stream()
                .map(HistoryExpansion::getExpansion)
                .map(this::toExpansionResponse)
                .toList();

        return toResponse(history, players, expansions);
    }

    private HistoryResponse toResponse(LocationHistory history, List<PlayerResultResponse> players, List<ExpansionResponse> expansions) {
        Long durationMinutes = (history.getStartedAt() != null && history.getFinishedAt() != null)
                ? Duration.between(history.getStartedAt(), history.getFinishedAt()).toMinutes()
                : null;

        return HistoryResponse.builder()
                .id(history.getId())
                .locationId(history.getLocation().getId())
                .gameId(history.getGame().getId())
                .gameName(history.getGame().getName())
                .playedAt(history.getPlayedAt())
                .state(history.getState())
                .startedAt(history.getStartedAt())
                .finishedAt(history.getFinishedAt())
                .durationMinutes(durationMinutes)
                .rating(history.getRating())
                .players(players)
                .expansions(expansions)
                .build();
    }

    private PlayerResultResponse toPlayerResponse(HistoryPlayer historyPlayer) {
        return PlayerResultResponse.builder()
                .username(historyPlayer.getUser().getUsername())
                .placement(historyPlayer.getPlacement())
                .points(historyPlayer.getPoints())
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
}
