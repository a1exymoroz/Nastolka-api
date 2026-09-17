package com.nastolka.service.impl;

import com.nastolka.dto.CreatePickSessionRequest;
import com.nastolka.dto.PickSessionActionRequest;
import com.nastolka.dto.PickSessionCandidateResponse;
import com.nastolka.dto.PickSessionParticipantResponse;
import com.nastolka.dto.PickSessionResponse;
import com.nastolka.entity.Game;
import com.nastolka.entity.HistoryState;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.PickSession;
import com.nastolka.entity.PickSessionCandidate;
import com.nastolka.entity.PickSessionCandidateAction;
import com.nastolka.entity.PickSessionParticipant;
import com.nastolka.entity.PickSessionStatus;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationHistoryRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.PickSessionCandidateRepository;
import com.nastolka.repository.PickSessionParticipantRepository;
import com.nastolka.repository.PickSessionRepository;
import com.nastolka.service.PickSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PickSessionServiceImpl implements PickSessionService {

    private static final List<PickSessionStatus> ACTIVE_STATUSES =
            List.of(PickSessionStatus.WAITING_FOR_PLAYERS, PickSessionStatus.IN_PROGRESS);

    private final SecureRandom random = new SecureRandom();

    private final LocationRepository locationRepository;
    private final LocationGameRepository locationGameRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final PickSessionRepository pickSessionRepository;
    private final PickSessionParticipantRepository participantRepository;
    private final PickSessionCandidateRepository candidateRepository;
    private final LocationAccessGuard accessGuard;

    public PickSessionServiceImpl(
            LocationRepository locationRepository,
            LocationGameRepository locationGameRepository,
            LocationHistoryRepository locationHistoryRepository,
            PickSessionRepository pickSessionRepository,
            PickSessionParticipantRepository participantRepository,
            PickSessionCandidateRepository candidateRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.locationGameRepository = locationGameRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.pickSessionRepository = pickSessionRepository;
        this.participantRepository = participantRepository;
        this.candidateRepository = candidateRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    @Transactional
    public PickSessionResponse create(Long locationId, CreatePickSessionRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        if (pickSessionRepository.findByLocationIdAndStatusIn(locationId, ACTIVE_STATUSES).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pick session is already active for this location");
        }

        PickSession session = new PickSession();
        session.setLocation(location);
        session.setCreatedBy(requester);
        session.setExcludeAlreadyPlayed(request.isExcludeAlreadyPlayed());
        session.setTargetRemainingCount(request.getTargetRemainingCount());
        session = pickSessionRepository.save(session);

        PickSessionParticipant creatorParticipant = new PickSessionParticipant();
        creatorParticipant.setSession(session);
        creatorParticipant.setUser(requester);
        participantRepository.save(creatorParticipant);

        return toResponse(session);
    }

    @Override
    @Transactional
    public PickSessionResponse join(Long locationId, Long sessionId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        PickSession session = requireSessionForUpdate(locationId, sessionId);
        if (session.getStatus() != PickSessionStatus.WAITING_FOR_PLAYERS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session has already started");
        }

        if (!participantRepository.existsBySessionIdAndUserId(session.getId(), requester.getId())) {
            PickSessionParticipant participant = new PickSessionParticipant();
            participant.setSession(session);
            participant.setUser(requester);
            participantRepository.save(participant);
        }

        return toResponse(session);
    }

    @Override
    @Transactional
    public PickSessionResponse start(Long locationId, Long sessionId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        PickSession session = requireSessionForUpdate(locationId, sessionId);
        if (!session.getCreatedBy().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the session creator can start it");
        }
        if (session.getStatus() != PickSessionStatus.WAITING_FOR_PLAYERS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session has already started");
        }

        List<Game> candidateGames = resolveCandidateGames(location, session.isExcludeAlreadyPlayed());
        if (candidateGames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No eligible games to pick from");
        }
        if (session.getTargetRemainingCount() > candidateGames.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Target remaining count exceeds the number of eligible games");
        }

        List<PickSessionCandidate> candidates = new ArrayList<>();
        for (Game game : candidateGames) {
            PickSessionCandidate candidate = new PickSessionCandidate();
            candidate.setSession(session);
            candidate.setGame(game);
            candidates.add(candidate);
        }
        candidateRepository.saveAll(candidates);

        List<PickSessionParticipant> participants = new ArrayList<>(participantRepository.findBySessionId(session.getId()));
        Collections.shuffle(participants, random);
        for (int i = 0; i < participants.size(); i++) {
            participants.get(i).setTurnOrder(i);
        }
        participantRepository.saveAll(participants);

        session.setRequiredBanCount(candidateGames.size() - session.getTargetRemainingCount());
        session.setBanCount(0);
        session.setCurrentTurnIndex(0);
        session.setStatus(PickSessionStatus.IN_PROGRESS);
        session.setStartedAt(Instant.now());

        if (session.getRequiredBanCount() <= 0) {
            finalizeSession(session);
        }

        session = pickSessionRepository.save(session);
        return toResponse(session);
    }

    @Override
    @Transactional
    public PickSessionResponse submitAction(Long locationId, Long sessionId, PickSessionActionRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        PickSession session = requireSessionForUpdate(locationId, sessionId);
        if (session.getStatus() != PickSessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not accepting actions");
        }
        if (request.getAction() == PickSessionCandidateAction.UNDECIDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action must be PICKED or BANNED");
        }

        List<PickSessionParticipant> participants = participantRepository.findBySessionIdOrderByTurnOrderAsc(session.getId());
        PickSessionParticipant currentTurnParticipant = participants.get(session.getCurrentTurnIndex());
        if (!currentTurnParticipant.getUser().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not your turn");
        }

        PickSessionCandidate candidate = candidateRepository.findBySessionIdAndGameId(session.getId(), request.getGameId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game is not a candidate in this session"));
        if (candidate.getAction() != PickSessionCandidateAction.UNDECIDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game has already been decided");
        }

        List<PickSessionCandidate> allCandidates = candidateRepository.findBySessionId(session.getId());
        long undecidedCount = allCandidates.stream()
                .filter(c -> c.getAction() == PickSessionCandidateAction.UNDECIDED)
                .count();
        long decidedCount = allCandidates.size() - undecidedCount;

        if (request.getAction() == PickSessionCandidateAction.PICKED) {
            long remainingBansNeeded = session.getRequiredBanCount() - session.getBanCount();
            long undecidedAfter = undecidedCount - 1;
            if (undecidedAfter < remainingBansNeeded) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Picking this game would leave too few undecided games to complete the required bans");
            }
        }

        candidate.setAction(request.getAction());
        candidate.setActedBy(requester);
        candidate.setActedAt(Instant.now());
        candidate.setActionSequence((int) decidedCount + 1);
        candidateRepository.save(candidate);

        if (request.getAction() == PickSessionCandidateAction.BANNED) {
            session.setBanCount(session.getBanCount() + 1);
        }
        session.setCurrentTurnIndex((session.getCurrentTurnIndex() + 1) % participants.size());

        autoBanRemainingIfNoChoiceLeft(session, (int) decidedCount + 1);

        if (session.getBanCount() >= session.getRequiredBanCount()) {
            finalizeSession(session);
        }

        session = pickSessionRepository.save(session);
        return toResponse(session);
    }

    @Override
    @Transactional
    public PickSessionResponse cancel(Long locationId, Long sessionId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        PickSession session = requireSessionForUpdate(locationId, sessionId);
        boolean isCreator = session.getCreatedBy().getId().equals(requester.getId());
        if (!isCreator && !accessGuard.canManage(location, requester)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to cancel this session");
        }
        if (!ACTIVE_STATUSES.contains(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not active");
        }

        session.setStatus(PickSessionStatus.CANCELLED);
        session.setCancelledAt(Instant.now());
        session = pickSessionRepository.save(session);
        return toResponse(session);
    }

    @Override
    public Optional<PickSessionResponse> getActive(Long locationId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        return pickSessionRepository.findByLocationIdAndStatusIn(locationId, ACTIVE_STATUSES)
                .map(this::toResponse);
    }

    @Override
    public PickSessionResponse getById(Long locationId, Long sessionId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        PickSession session = pickSessionRepository.findByIdAndLocationId(sessionId, locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pick session not found"));
        return toResponse(session);
    }

    private void finalizeSession(PickSession session) {
        List<PickSessionCandidate> survivors = candidateRepository.findBySessionId(session.getId()).stream()
                .filter(c -> c.getAction() != PickSessionCandidateAction.BANNED)
                .toList();

        PickSessionCandidate winner = survivors.get(random.nextInt(survivors.size()));
        session.setSelectedGame(winner.getGame());
        session.setStatus(PickSessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
    }

    /**
     * If exactly as many undecided candidates remain as bans are still required, every one of
     * them is mathematically guaranteed to end up banned - there is no real discretion left.
     * Auto-resolve them as BANNED (actedBy left null to mark this as a system-driven resolution
     * rather than a per-candidate decision by any participant) so the session completes
     * immediately instead of forcing the remaining players through a manual ban of each one.
     */
    private void autoBanRemainingIfNoChoiceLeft(PickSession session, int lastActionSequence) {
        long remainingBansNeeded = session.getRequiredBanCount() - session.getBanCount();
        if (remainingBansNeeded <= 0) {
            return;
        }

        List<PickSessionCandidate> undecided = candidateRepository.findBySessionId(session.getId()).stream()
                .filter(c -> c.getAction() == PickSessionCandidateAction.UNDECIDED)
                .toList();

        if (undecided.size() != remainingBansNeeded) {
            return;
        }

        Instant now = Instant.now();
        int sequence = lastActionSequence;
        for (PickSessionCandidate c : undecided) {
            c.setAction(PickSessionCandidateAction.BANNED);
            c.setActedBy(null);
            c.setActedAt(now);
            c.setActionSequence(++sequence);
        }
        candidateRepository.saveAll(undecided);

        session.setBanCount(session.getBanCount() + undecided.size());
    }

    private List<Game> resolveCandidateGames(Location location, boolean excludeAlreadyPlayed) {
        List<Game> games = locationGameRepository.findByLocationId(location.getId()).stream()
                .map(LocationGame::getGame)
                .toList();

        if (!excludeAlreadyPlayed) {
            return games;
        }

        Set<Long> playedGameIds = new HashSet<>(
                locationHistoryRepository.findDistinctGameIdsByLocationIdAndState(location.getId(), HistoryState.FINISHED));
        return games.stream().filter(game -> !playedGameIds.contains(game.getId())).toList();
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private PickSession requireSessionForUpdate(Long locationId, Long sessionId) {
        return pickSessionRepository.findByIdAndLocationIdForUpdate(sessionId, locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pick session not found"));
    }

    private PickSessionResponse toResponse(PickSession session) {
        List<PickSessionParticipant> participants = participantRepository.findBySessionIdOrderByTurnOrderAsc(session.getId());
        List<PickSessionCandidate> candidates = candidateRepository.findBySessionId(session.getId());

        String currentTurnUsername = null;
        if (session.getStatus() == PickSessionStatus.IN_PROGRESS
                && session.getCurrentTurnIndex() != null
                && session.getCurrentTurnIndex() < participants.size()) {
            currentTurnUsername = participants.get(session.getCurrentTurnIndex()).getUser().getUsername();
        }

        return PickSessionResponse.builder()
                .id(session.getId())
                .locationId(session.getLocation().getId())
                .status(session.getStatus())
                .excludeAlreadyPlayed(session.isExcludeAlreadyPlayed())
                .targetRemainingCount(session.getTargetRemainingCount())
                .requiredBanCount(session.getRequiredBanCount())
                .banCount(session.getBanCount())
                .currentTurnUsername(currentTurnUsername)
                .createdByUsername(session.getCreatedBy().getUsername())
                .createdAt(session.getCreatedAt())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .cancelledAt(session.getCancelledAt())
                .selectedGameId(session.getSelectedGame() != null ? session.getSelectedGame().getId() : null)
                .selectedGameName(session.getSelectedGame() != null ? session.getSelectedGame().getName() : null)
                .participants(participants.stream().map(this::toParticipantResponse).toList())
                .candidates(candidates.stream().map(this::toCandidateResponse).toList())
                .build();
    }

    private PickSessionParticipantResponse toParticipantResponse(PickSessionParticipant participant) {
        return PickSessionParticipantResponse.builder()
                .userId(participant.getUser().getId())
                .username(participant.getUser().getUsername())
                .turnOrder(participant.getTurnOrder())
                .joinedAt(participant.getJoinedAt())
                .build();
    }

    private PickSessionCandidateResponse toCandidateResponse(PickSessionCandidate candidate) {
        return PickSessionCandidateResponse.builder()
                .gameId(candidate.getGame().getId())
                .gameName(candidate.getGame().getName())
                .action(candidate.getAction())
                .actedByUsername(candidate.getActedBy() != null ? candidate.getActedBy().getUsername() : null)
                .actedAt(candidate.getActedAt())
                .build();
    }
}
