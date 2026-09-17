package com.nastolka.service.impl;

import com.nastolka.dto.CreatePickSessionRequest;
import com.nastolka.dto.PickSessionActionRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickSessionServiceImplTest {

    private static final Long LOCATION_ID = 1L;
    private static final Long SESSION_ID = 5L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationGameRepository locationGameRepository;
    @Mock
    private LocationHistoryRepository locationHistoryRepository;
    @Mock
    private PickSessionRepository pickSessionRepository;
    @Mock
    private PickSessionParticipantRepository participantRepository;
    @Mock
    private PickSessionCandidateRepository candidateRepository;
    @Mock
    private LocationAccessGuard accessGuard;

    private PickSessionServiceImpl service;
    private Location location;
    private User creator;
    private User otherUser;

    @BeforeEach
    void setUp() {
        service = new PickSessionServiceImpl(
                locationRepository,
                locationGameRepository,
                locationHistoryRepository,
                pickSessionRepository,
                participantRepository,
                candidateRepository,
                accessGuard
        );

        creator = User.builder().id(10L).username("alice").build();
        otherUser = User.builder().id(11L).username("bob").build();

        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(creator);

        lenient().when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        lenient().when(accessGuard.requireUser("alice")).thenReturn(creator);
        lenient().when(accessGuard.requireUser("bob")).thenReturn(otherUser);
        lenient().when(pickSessionRepository.save(any(PickSession.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(participantRepository.save(any(PickSessionParticipant.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(candidateRepository.save(any(PickSessionCandidate.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(participantRepository.findBySessionId(SESSION_ID)).thenReturn(new ArrayList<>());
        lenient().when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID)).thenReturn(new ArrayList<>());
        lenient().when(candidateRepository.findBySessionId(SESSION_ID)).thenReturn(new ArrayList<>());
    }

    private PickSession newSession(PickSessionStatus status, int targetRemainingCount, boolean excludeAlreadyPlayed) {
        PickSession session = new PickSession();
        session.setId(SESSION_ID);
        session.setLocation(location);
        session.setCreatedBy(creator);
        session.setStatus(status);
        session.setTargetRemainingCount(targetRemainingCount);
        session.setExcludeAlreadyPlayed(excludeAlreadyPlayed);
        return session;
    }

    private Game game(long id) {
        Game game = new Game();
        game.setId(id);
        game.setName("Game " + id);
        return game;
    }

    private LocationGame locationGame(Game game) {
        LocationGame locationGame = new LocationGame();
        locationGame.setLocation(location);
        locationGame.setGame(game);
        return locationGame;
    }

    private PickSessionParticipant participant(PickSession session, User user, Integer turnOrder) {
        PickSessionParticipant participant = new PickSessionParticipant();
        participant.setSession(session);
        participant.setUser(user);
        participant.setTurnOrder(turnOrder);
        return participant;
    }

    private PickSessionCandidate candidate(PickSession session, Game game, PickSessionCandidateAction action) {
        PickSessionCandidate candidate = new PickSessionCandidate();
        candidate.setSession(session);
        candidate.setGame(game);
        candidate.setAction(action);
        return candidate;
    }

    @Test
    void create_rejectsWhenActiveSessionAlreadyExists() {
        when(pickSessionRepository.findByLocationIdAndStatusIn(eq(LOCATION_ID), anyList()))
                .thenReturn(Optional.of(newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 5, false)));

        CreatePickSessionRequest request = new CreatePickSessionRequest();
        request.setTargetRemainingCount(5);

        assertThatThrownBy(() -> service.create(LOCATION_ID, request, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already active");
    }

    @Test
    void create_addsCreatorAsParticipant() {
        when(pickSessionRepository.findByLocationIdAndStatusIn(eq(LOCATION_ID), anyList())).thenReturn(Optional.empty());

        CreatePickSessionRequest request = new CreatePickSessionRequest();
        request.setTargetRemainingCount(5);

        service.create(LOCATION_ID, request, "alice");

        ArgumentCaptor<PickSessionParticipant> captor = ArgumentCaptor.forClass(PickSessionParticipant.class);
        org.mockito.Mockito.verify(participantRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(creator);
    }

    @Test
    void start_onlyCreatorCanStart() {
        PickSession session = newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 1, false);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.start(LOCATION_ID, SESSION_ID, "bob"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the session creator");
    }

    @Test
    void start_rejectsWhenTargetExceedsCatalogSize() {
        PickSession session = newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 5, false);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(locationGameRepository.findByLocationId(LOCATION_ID))
                .thenReturn(List.of(locationGame(game(1L)), locationGame(game(2L))));

        assertThatThrownBy(() -> service.start(LOCATION_ID, SESSION_ID, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exceeds the number of eligible games");
    }

    @Test
    void start_excludesAlreadyPlayedGames_whenFlagSet() {
        PickSession session = newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 1, true);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(locationGameRepository.findByLocationId(LOCATION_ID))
                .thenReturn(List.of(locationGame(game(1L)), locationGame(game(2L))));
        when(locationHistoryRepository.findDistinctGameIdsByLocationIdAndState(LOCATION_ID, HistoryState.FINISHED))
                .thenReturn(List.of(1L));
        when(participantRepository.findBySessionId(SESSION_ID))
                .thenReturn(new ArrayList<>(List.of(participant(session, creator, null))));
        when(candidateRepository.findBySessionId(SESSION_ID))
                .thenReturn(List.of(candidate(session, game(2L), PickSessionCandidateAction.UNDECIDED)));

        PickSessionResponse response = service.start(LOCATION_ID, SESSION_ID, "alice");

        ArgumentCaptor<List<PickSessionCandidate>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(candidateRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getGame().getId()).isEqualTo(2L);
        assertThat(response.getRequiredBanCount()).isZero();
        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.COMPLETED);
    }

    @Test
    void start_autoCompletes_whenRequiredBanCountIsNotPositive() {
        PickSession session = newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 2, false);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        Game g1 = game(1L);
        Game g2 = game(2L);
        when(locationGameRepository.findByLocationId(LOCATION_ID))
                .thenReturn(List.of(locationGame(g1), locationGame(g2)));
        when(participantRepository.findBySessionId(SESSION_ID))
                .thenReturn(new ArrayList<>(List.of(participant(session, creator, null))));
        when(candidateRepository.findBySessionId(SESSION_ID))
                .thenReturn(List.of(candidate(session, g1, PickSessionCandidateAction.UNDECIDED),
                        candidate(session, g2, PickSessionCandidateAction.UNDECIDED)));

        PickSessionResponse response = service.start(LOCATION_ID, SESSION_ID, "alice");

        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.COMPLETED);
        assertThat(response.getSelectedGameId()).isIn(1L, 2L);
    }

    @Test
    void submitAction_rejectsOutOfTurn() {
        PickSession session = newSession(PickSessionStatus.IN_PROGRESS, 1, false);
        session.setRequiredBanCount(1);
        session.setBanCount(0);
        session.setCurrentTurnIndex(0);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID))
                .thenReturn(List.of(participant(session, creator, 0), participant(session, otherUser, 1)));

        PickSessionActionRequest request = new PickSessionActionRequest();
        request.setGameId(1L);
        request.setAction(PickSessionCandidateAction.BANNED);

        assertThatThrownBy(() -> service.submitAction(LOCATION_ID, SESSION_ID, request, "bob"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    void submitAction_rejectsAlreadyDecidedCandidate() {
        PickSession session = newSession(PickSessionStatus.IN_PROGRESS, 1, false);
        session.setRequiredBanCount(1);
        session.setBanCount(0);
        session.setCurrentTurnIndex(0);
        Game g1 = game(1L);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID))
                .thenReturn(List.of(participant(session, creator, 0)));
        when(candidateRepository.findBySessionIdAndGameId(SESSION_ID, 1L))
                .thenReturn(Optional.of(candidate(session, g1, PickSessionCandidateAction.BANNED)));

        PickSessionActionRequest request = new PickSessionActionRequest();
        request.setGameId(1L);
        request.setAction(PickSessionCandidateAction.BANNED);

        assertThatThrownBy(() -> service.submitAction(LOCATION_ID, SESSION_ID, request, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already been decided");
    }

    @Test
    void submitAction_rejectsPickThatWouldStarveRequiredBans() {
        PickSession session = newSession(PickSessionStatus.IN_PROGRESS, 1, false);
        session.setRequiredBanCount(2);
        session.setBanCount(0);
        session.setCurrentTurnIndex(0);
        Game g1 = game(1L);
        Game g2 = game(2L);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID))
                .thenReturn(List.of(participant(session, creator, 0)));
        when(candidateRepository.findBySessionIdAndGameId(SESSION_ID, 1L))
                .thenReturn(Optional.of(candidate(session, g1, PickSessionCandidateAction.UNDECIDED)));
        // Only 2 undecided candidates total but 2 bans are still required: picking one would
        // leave just 1 undecided game, not enough to supply the remaining 2 required bans.
        when(candidateRepository.findBySessionId(SESSION_ID))
                .thenReturn(List.of(candidate(session, g1, PickSessionCandidateAction.UNDECIDED),
                        candidate(session, g2, PickSessionCandidateAction.UNDECIDED)));

        PickSessionActionRequest request = new PickSessionActionRequest();
        request.setGameId(1L);
        request.setAction(PickSessionCandidateAction.PICKED);

        assertThatThrownBy(() -> service.submitAction(LOCATION_ID, SESSION_ID, request, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("too few undecided games");
    }

    @Test
    void submitAction_banHasNoStarveRestriction_andCompletesOnceTargetReached() {
        PickSession session = newSession(PickSessionStatus.IN_PROGRESS, 1, false);
        session.setRequiredBanCount(1);
        session.setBanCount(0);
        session.setCurrentTurnIndex(0);
        Game g1 = game(1L);
        Game g2 = game(2L);
        // Same candidate instances are returned by both lookups, mirroring how a real
        // findBySessionIdAndGameId mutation is visible to a same-transaction findBySessionId
        // read against the database (unlike two independently-stubbed mock returns would be).
        PickSessionCandidate c1 = candidate(session, g1, PickSessionCandidateAction.UNDECIDED);
        PickSessionCandidate c2 = candidate(session, g2, PickSessionCandidateAction.UNDECIDED);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID))
                .thenReturn(List.of(participant(session, creator, 0)));
        when(candidateRepository.findBySessionIdAndGameId(SESSION_ID, 1L)).thenReturn(Optional.of(c1));
        when(candidateRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(c1, c2));

        PickSessionActionRequest request = new PickSessionActionRequest();
        request.setGameId(1L);
        request.setAction(PickSessionCandidateAction.BANNED);

        PickSessionResponse response = service.submitAction(LOCATION_ID, SESSION_ID, request, "alice");

        assertThat(response.getBanCount()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.COMPLETED);
        assertThat(response.getSelectedGameId()).isEqualTo(2L);
    }

    @Test
    void submitAction_autoBansRemainingCandidates_whenPickReachesTarget() {
        PickSession session = newSession(PickSessionStatus.IN_PROGRESS, 1, false);
        session.setRequiredBanCount(2);
        session.setBanCount(1);
        session.setCurrentTurnIndex(0);
        Game g1 = game(1L);
        Game g2 = game(2L);
        Game g3 = game(3L);
        // Same candidate instances are returned by both lookups (see comment on the ban-completion
        // test above) so mutating c2 via the pick is visible to the findBySessionId read that the
        // new auto-ban helper performs afterward.
        PickSessionCandidate c1 = candidate(session, g1, PickSessionCandidateAction.BANNED);
        PickSessionCandidate c2 = candidate(session, g2, PickSessionCandidateAction.UNDECIDED);
        PickSessionCandidate c3 = candidate(session, g3, PickSessionCandidateAction.UNDECIDED);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID))
                .thenReturn(List.of(participant(session, creator, 0)));
        when(candidateRepository.findBySessionIdAndGameId(SESSION_ID, 2L)).thenReturn(Optional.of(c2));
        when(candidateRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(c1, c2, c3));

        PickSessionActionRequest request = new PickSessionActionRequest();
        request.setGameId(2L);
        request.setAction(PickSessionCandidateAction.PICKED);

        PickSessionResponse response = service.submitAction(LOCATION_ID, SESSION_ID, request, "alice");

        assertThat(response.getBanCount()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.COMPLETED);
        assertThat(response.getSelectedGameId()).isEqualTo(2L);
        assertThat(c3.getAction()).isEqualTo(PickSessionCandidateAction.BANNED);
        assertThat(c3.getActedBy()).isNull();

        ArgumentCaptor<List<PickSessionCandidate>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(candidateRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(c3);
    }

    @Test
    void submitAction_doesNotAutoBan_whenMoreUndecidedThanBansNeeded() {
        PickSession session = newSession(PickSessionStatus.IN_PROGRESS, 1, false);
        session.setRequiredBanCount(2);
        session.setBanCount(0);
        session.setCurrentTurnIndex(0);
        Game g1 = game(1L);
        Game g2 = game(2L);
        Game g3 = game(3L);
        Game g4 = game(4L);
        PickSessionCandidate c1 = candidate(session, g1, PickSessionCandidateAction.UNDECIDED);
        PickSessionCandidate c2 = candidate(session, g2, PickSessionCandidateAction.UNDECIDED);
        PickSessionCandidate c3 = candidate(session, g3, PickSessionCandidateAction.UNDECIDED);
        PickSessionCandidate c4 = candidate(session, g4, PickSessionCandidateAction.UNDECIDED);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdOrderByTurnOrderAsc(SESSION_ID))
                .thenReturn(List.of(participant(session, creator, 0)));
        when(candidateRepository.findBySessionIdAndGameId(SESSION_ID, 1L)).thenReturn(Optional.of(c1));
        when(candidateRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(c1, c2, c3, c4));

        PickSessionActionRequest request = new PickSessionActionRequest();
        request.setGameId(1L);
        request.setAction(PickSessionCandidateAction.BANNED);

        PickSessionResponse response = service.submitAction(LOCATION_ID, SESSION_ID, request, "alice");

        assertThat(response.getBanCount()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.IN_PROGRESS);
        org.mockito.Mockito.verify(candidateRepository, org.mockito.Mockito.never()).saveAll(anyList());
    }

    @Test
    void cancel_allowsCreator() {
        PickSession session = newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 1, false);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));

        PickSessionResponse response = service.cancel(LOCATION_ID, SESSION_ID, "alice");

        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.CANCELLED);
    }

    @Test
    void cancel_allowsLocationOwnerEvenIfNotCreator() {
        User locationOwner = User.builder().id(99L).username("owner").build();
        Location ownedLocation = new Location();
        ownedLocation.setId(LOCATION_ID);
        ownedLocation.setOwner(locationOwner);
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(ownedLocation));
        when(accessGuard.requireUser("owner")).thenReturn(locationOwner);
        when(accessGuard.canManage(ownedLocation, locationOwner)).thenReturn(true);

        PickSession session = new PickSession();
        session.setId(SESSION_ID);
        session.setLocation(ownedLocation);
        session.setCreatedBy(creator);
        session.setStatus(PickSessionStatus.IN_PROGRESS);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));

        PickSessionResponse response = service.cancel(LOCATION_ID, SESSION_ID, "owner");

        assertThat(response.getStatus()).isEqualTo(PickSessionStatus.CANCELLED);
    }

    @Test
    void cancel_rejectsUnrelatedMember() {
        PickSession session = newSession(PickSessionStatus.WAITING_FOR_PLAYERS, 1, false);
        when(pickSessionRepository.findByIdAndLocationIdForUpdate(SESSION_ID, LOCATION_ID)).thenReturn(Optional.of(session));
        when(accessGuard.canManage(location, otherUser)).thenReturn(false);

        assertThatThrownBy(() -> service.cancel(LOCATION_ID, SESSION_ID, "bob"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("permission to cancel");
    }
}
