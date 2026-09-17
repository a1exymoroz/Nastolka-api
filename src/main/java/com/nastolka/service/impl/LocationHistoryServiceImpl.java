package com.nastolka.service.impl;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.ExpansionResponse;
import com.nastolka.dto.HistoryResponse;
import com.nastolka.dto.HistoryVoteResponse;
import com.nastolka.dto.PlayerPlacementRequest;
import com.nastolka.dto.PlayerResultResponse;
import com.nastolka.dto.VoteRequest;
import com.nastolka.entity.Game;
import com.nastolka.entity.GameExpansion;
import com.nastolka.entity.HistoryExpansion;
import com.nastolka.entity.HistoryPlayer;
import com.nastolka.entity.HistoryState;
import com.nastolka.entity.HistoryVote;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.LocationHistory;
import com.nastolka.entity.User;
import com.nastolka.integration.telegram.TelegramNotifier;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.GameRepository;
import com.nastolka.repository.HistoryExpansionRepository;
import com.nastolka.repository.HistoryPlayerRepository;
import com.nastolka.repository.HistoryVoteRepository;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final HistoryVoteRepository historyVoteRepository;
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
            HistoryVoteRepository historyVoteRepository,
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
        this.historyVoteRepository = historyVoteRepository;
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

        Map<Long, List<HistoryVoteResponse>> votesByHistoryId = historyVoteRepository
                .findByHistoryIdInOrderByCreatedAtAsc(historyIds).stream()
                .collect(Collectors.groupingBy(
                        historyVote -> historyVote.getHistory().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::toVoteResponse, Collectors.toList())));

        return historyEntries.stream()
                .map(history -> toResponse(
                        history,
                        playersByHistoryId.getOrDefault(history.getId(), List.of()),
                        expansionsByHistoryId.getOrDefault(history.getId(), List.of()),
                        votesByHistoryId.getOrDefault(history.getId(), List.of())))
                .toList();
    }

    @Override
    public List<HistoryResponse> getRecentHistoryByChatId(String telegramChatId, int limit) {
        Location location = locationRepository.findByTelegramChatId(telegramChatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No location linked to this chat"));

        return locationHistoryRepository.findByLocationIdOrderByPlayedAtDesc(location.getId()).stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public HistoryResponse addHistory(Long locationId, CreateHistoryRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireHistoryManageAccess(location, requester);

        Game game = resolveLocationGame(locationId, request.getGameId());

        LocationHistory history = new LocationHistory();
        history.setLocation(location);
        history.setGame(game);
        history.setPlayedAt(request.getPlayedAt() != null ? request.getPlayedAt() : Instant.now());
        history.setState(request.getState());
        history.setStartedAt(resolveStartedAt(null, request));
        history.setFinishedAt(resolveFinishedAt(null, request));
        history.setRating(request.getRating());
        history.setOutcome(request.getOutcome());
        validateTimes(history);
        history = locationHistoryRepository.save(history);

        savePlayers(history, request.getPlayers(), request.getState() == HistoryState.FINISHED && request.getOutcome() == null);
        saveExpansions(history, request.getExpansionIds());
        location.touch(requester);
        locationRepository.save(location);

        HistoryResponse response = toResponse(history);
        if (response.getState() == HistoryState.FINISHED) {
            telegramNotifier.notifyHistoryFinished(location, response);
        }
        return response;
    }

    @Override
    @Transactional
    public HistoryResponse updateHistory(Long locationId, Long historyId, CreateHistoryRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireHistoryManageAccess(location, requester);

        LocationHistory history = requireHistory(locationId, historyId);
        HistoryState previousState = history.getState();

        Game game = resolveLocationGame(locationId, request.getGameId());
        history.setGame(game);
        history.setPlayedAt(request.getPlayedAt() != null ? request.getPlayedAt() : Instant.now());
        history.setStartedAt(resolveStartedAt(history.getStartedAt(), request));
        history.setFinishedAt(resolveFinishedAt(history.getFinishedAt(), request));
        history.setState(request.getState());
        history.setRating(request.getRating());
        history.setOutcome(request.getOutcome());
        validateTimes(history);
        history = locationHistoryRepository.save(history);

        historyPlayerRepository.deleteByHistoryId(history.getId());
        historyPlayerRepository.flush();
        savePlayers(history, request.getPlayers(), request.getState() == HistoryState.FINISHED && request.getOutcome() == null);

        historyExpansionRepository.deleteByHistoryId(history.getId());
        historyExpansionRepository.flush();
        saveExpansions(history, request.getExpansionIds());
        location.touch(requester);
        locationRepository.save(location);

        HistoryResponse response = toResponse(history);
        if (previousState != HistoryState.FINISHED && response.getState() == HistoryState.FINISHED) {
            telegramNotifier.notifyHistoryFinished(location, response);
        }
        return response;
    }

    @Override
    @Transactional
    public void deleteHistory(Long locationId, Long historyId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireHistoryManageAccess(location, requester);

        LocationHistory history = requireHistory(locationId, historyId);
        locationHistoryRepository.delete(history);
        location.touch(requester);
        locationRepository.save(location);
    }

    @Override
    @Transactional
    public HistoryResponse voteOnHistory(Long locationId, Long historyId, VoteRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        LocationHistory history = requireHistory(locationId, historyId);
        if (history.getState() != HistoryState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only finished sessions can be rated");
        }

        HistoryVote vote = historyVoteRepository.findByHistoryIdAndUserId(historyId, requester.getId())
                .orElseGet(HistoryVote::new);
        vote.setHistory(history);
        vote.setUser(requester);
        vote.setScore(request.getScore());
        historyVoteRepository.save(vote);

        return toResponse(history);
    }

    private void savePlayers(LocationHistory history, List<PlayerPlacementRequest> playerRequests, boolean requireRanking) {
        Location location = history.getLocation();
        Set<String> seenUsernames = new HashSet<>();
        List<HistoryPlayer> players = new ArrayList<>();

        for (PlayerPlacementRequest playerRequest : playerRequests) {
            if (!seenUsernames.add(playerRequest.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Player '" + playerRequest.getUsername() + "' is listed more than once");
            }

            if (requireRanking && playerRequest.getPoints() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Points are required for every player once the session is finished");
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
            historyPlayer.setPlacement(requireRanking ? null : playerRequest.getPlacement());
            historyPlayer.setPoints(playerRequest.getPoints());
            historyPlayer.setMeeples(playerRequest.getMeeples());
            players.add(historyPlayer);
        }

        players.sort(Comparator
                .comparing(HistoryPlayer::getPoints, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(historyPlayer -> historyPlayer.getUser().getUsername()));

        if (requireRanking) {
            // Placement is always derived from points, not the client-supplied value, so it never
            // drifts out of sync with the score (ties are broken by username for a stable ranking).
            for (int i = 0; i < players.size(); i++) {
                players.get(i).setPlacement(i + 1);
            }
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

    private Instant resolveStartedAt(Instant existing, CreateHistoryRequest request) {
        if (request.getStartedAt() != null) {
            return request.getStartedAt();
        }
        if (existing != null) {
            return existing;
        }
        if (request.getState() == HistoryState.IN_PROGRESS || request.getState() == HistoryState.FINISHED) {
            return Instant.now();
        }
        return null;
    }

    private Instant resolveFinishedAt(Instant existing, CreateHistoryRequest request) {
        if (request.getFinishedAt() != null) {
            return request.getFinishedAt();
        }
        if (existing != null) {
            return existing;
        }
        if (request.getState() == HistoryState.FINISHED) {
            return Instant.now();
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

        List<HistoryVoteResponse> votes = historyVoteRepository.findByHistoryIdOrderByCreatedAtAsc(history.getId()).stream()
                .map(this::toVoteResponse)
                .toList();

        return toResponse(history, players, expansions, votes);
    }

    private HistoryResponse toResponse(LocationHistory history, List<PlayerResultResponse> players,
            List<ExpansionResponse> expansions, List<HistoryVoteResponse> votes) {
        Long durationMinutes = (history.getStartedAt() != null && history.getFinishedAt() != null)
                ? Duration.between(history.getStartedAt(), history.getFinishedAt()).toMinutes()
                : null;

        Double averageRating = votes.isEmpty() ? null
                : votes.stream().mapToInt(HistoryVoteResponse::getScore).average().orElseThrow();

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
                .outcome(history.getOutcome())
                .votes(votes)
                .averageRating(averageRating)
                .voteCount((long) votes.size())
                .build();
    }

    private PlayerResultResponse toPlayerResponse(HistoryPlayer historyPlayer) {
        return PlayerResultResponse.builder()
                .username(historyPlayer.getUser().getUsername())
                .placement(historyPlayer.getPlacement())
                .points(historyPlayer.getPoints())
                .meeples(historyPlayer.getMeeples())
                .build();
    }

    private HistoryVoteResponse toVoteResponse(HistoryVote vote) {
        return HistoryVoteResponse.builder()
                .username(vote.getUser().getUsername())
                .score(vote.getScore())
                .votedAt(vote.getUpdatedAt())
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
