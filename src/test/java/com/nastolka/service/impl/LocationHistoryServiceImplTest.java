package com.nastolka.service.impl;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.HistoryResponse;
import com.nastolka.dto.PlayerPlacementRequest;
import com.nastolka.dto.VoteRequest;
import com.nastolka.entity.Game;
import com.nastolka.entity.HistoryOutcome;
import com.nastolka.entity.HistoryPlayer;
import com.nastolka.entity.HistoryState;
import com.nastolka.entity.HistoryVote;
import com.nastolka.entity.Location;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationHistoryServiceImplTest {

    private static final Long LOCATION_ID = 1L;
    private static final Long GAME_ID = 2L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationGameRepository locationGameRepository;
    @Mock
    private LocationHistoryRepository locationHistoryRepository;
    @Mock
    private HistoryPlayerRepository historyPlayerRepository;
    @Mock
    private GameExpansionRepository expansionRepository;
    @Mock
    private LocationGameExpansionRepository locationGameExpansionRepository;
    @Mock
    private HistoryExpansionRepository historyExpansionRepository;
    @Mock
    private HistoryVoteRepository historyVoteRepository;
    @Mock
    private LocationShareRepository locationShareRepository;
    @Mock
    private LocationAccessGuard accessGuard;
    @Mock
    private TelegramNotifier telegramNotifier;

    private LocationHistoryServiceImpl service;
    private Location location;
    private Game game;
    private User user;

    @BeforeEach
    void setUp() {
        service = new LocationHistoryServiceImpl(
                locationRepository,
                gameRepository,
                userRepository,
                locationGameRepository,
                locationHistoryRepository,
                historyPlayerRepository,
                expansionRepository,
                locationGameExpansionRepository,
                historyExpansionRepository,
                historyVoteRepository,
                locationShareRepository,
                accessGuard,
                telegramNotifier
        );

        user = User.builder().id(10L).username("alice").build();
        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(user);

        game = new Game();
        game.setId(GAME_ID);
        game.setName("Terraforming Mars");

        when(accessGuard.requireUser("alice")).thenReturn(user);
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(locationGameRepository.existsByLocationIdAndGameId(LOCATION_ID, GAME_ID)).thenReturn(true);
        lenient().when(locationHistoryRepository.save(any(LocationHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CreateHistoryRequest requestWithState(HistoryState state) {
        CreateHistoryRequest request = new CreateHistoryRequest();
        request.setGameId(GAME_ID);
        request.setState(state);
        request.setPlayers(List.of());
        return request;
    }

    @Test
    void addHistory_defaultsPlayedAtToNowInstant_whenNotProvided() {
        CreateHistoryRequest request = requestWithState(HistoryState.CREATED);

        HistoryResponse response = service.addHistory(LOCATION_ID, request, "alice");

        assertThat(response.getPlayedAt()).isNotNull();
        assertThat(response.getPlayedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void addHistory_leavesStartedAndFinishedAtNull_whenStateIsCreatedAndNotProvided() {
        CreateHistoryRequest request = requestWithState(HistoryState.CREATED);

        HistoryResponse response = service.addHistory(LOCATION_ID, request, "alice");

        assertThat(response.getStartedAt()).isNull();
        assertThat(response.getFinishedAt()).isNull();
    }

    @Test
    void addHistory_defaultsStartedAndFinishedAtToNow_whenStateIsFinishedAndNotProvided() {
        CreateHistoryRequest request = requestWithState(HistoryState.FINISHED);

        HistoryResponse response = service.addHistory(LOCATION_ID, request, "alice");

        assertThat(response.getStartedAt()).isNotNull();
        assertThat(response.getFinishedAt()).isNotNull();
        assertThat(response.getStartedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(response.getFinishedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void addHistory_rejectsFinishedAtBeforeStartedAt() {
        CreateHistoryRequest request = requestWithState(HistoryState.CREATED);
        Instant now = Instant.now();
        request.setStartedAt(now);
        request.setFinishedAt(now.minusSeconds(60));

        assertThatThrownBy(() -> service.addHistory(LOCATION_ID, request, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Finished time cannot be before started time");
    }

    @Test
    void addHistory_touchesLocationUpdatedAt() {
        CreateHistoryRequest request = requestWithState(HistoryState.CREATED);

        service.addHistory(LOCATION_ID, request, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(location.getUpdatedByUsername()).isEqualTo("alice");
    }

    @Test
    void addHistory_succeedsWithoutPlayerPoints_whenOutcomeIsSet() {
        CreateHistoryRequest request = requestWithState(HistoryState.FINISHED);
        request.setOutcome(HistoryOutcome.WON);
        PlayerPlacementRequest player = new PlayerPlacementRequest();
        player.setUsername("alice");
        request.setPlayers(List.of(player));

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        HistoryResponse response = service.addHistory(LOCATION_ID, request, "alice");

        assertThat(response.getOutcome()).isEqualTo(HistoryOutcome.WON);

        ArgumentCaptor<List<HistoryPlayer>> playersCaptor = ArgumentCaptor.forClass(List.class);
        verify(historyPlayerRepository).saveAll(playersCaptor.capture());
        assertThat(playersCaptor.getValue()).hasSize(1);
        assertThat(playersCaptor.getValue().get(0).getPoints()).isNull();
        assertThat(playersCaptor.getValue().get(0).getPlacement()).isNull();
    }

    @Test
    void getHistoryById_returnsMatchingEntry_andChecksViewAccess() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        history.setGame(game);
        history.setState(HistoryState.FINISHED);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));

        HistoryResponse response = service.getHistoryById(LOCATION_ID, historyId, "alice");

        assertThat(response.getId()).isEqualTo(historyId);
        verify(accessGuard).requireViewAccess(location, user);
    }

    @Test
    void getHistoryById_throwsNotFound_whenEntryBelongsToAnotherLocation() {
        Long historyId = 99L;
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHistoryById(LOCATION_ID, historyId, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("History entry not found");
    }

    @Test
    void deleteHistory_touchesLocationUpdatedAt() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));

        service.deleteHistory(LOCATION_ID, historyId, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(location.getUpdatedByUsername()).isEqualTo("alice");
    }

    @Test
    void addHistory_checksHistoryManageAccess_notBroadManageAccess() {
        CreateHistoryRequest request = requestWithState(HistoryState.CREATED);

        service.addHistory(LOCATION_ID, request, "alice");

        verify(accessGuard).requireHistoryManageAccess(location, user);
    }

    @Test
    void updateHistory_checksHistoryManageAccess_notBroadManageAccess() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        history.setGame(game);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));
        CreateHistoryRequest request = requestWithState(HistoryState.CREATED);

        service.updateHistory(LOCATION_ID, historyId, request, "alice");

        verify(accessGuard).requireHistoryManageAccess(location, user);
    }

    @Test
    void deleteHistory_checksHistoryManageAccess_notBroadManageAccess() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));

        service.deleteHistory(LOCATION_ID, historyId, "alice");

        verify(accessGuard).requireHistoryManageAccess(location, user);
    }

    @Test
    void voteOnHistory_checksViewAccess_notHistoryManageAccess() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        history.setState(HistoryState.FINISHED);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));
        when(historyVoteRepository.findByHistoryIdAndUserId(historyId, user.getId())).thenReturn(Optional.empty());
        VoteRequest request = new VoteRequest();
        request.setScore(8);

        service.voteOnHistory(LOCATION_ID, historyId, request, "alice");

        verify(accessGuard).requireViewAccess(location, user);
    }

    @Test
    void voteOnHistory_rejectsNonFinishedSession() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        history.setState(HistoryState.IN_PROGRESS);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));
        VoteRequest request = new VoteRequest();
        request.setScore(8);

        assertThatThrownBy(() -> service.voteOnHistory(LOCATION_ID, historyId, request, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only finished sessions can be rated");
    }

    @Test
    void voteOnHistory_updatesExistingVote_insteadOfCreatingDuplicate() {
        Long historyId = 99L;
        LocationHistory history = new LocationHistory();
        history.setId(historyId);
        history.setLocation(location);
        history.setState(HistoryState.FINISHED);
        when(locationHistoryRepository.findByIdAndLocationId(historyId, LOCATION_ID)).thenReturn(Optional.of(history));

        HistoryVote existingVote = new HistoryVote();
        existingVote.setId(5L);
        existingVote.setHistory(history);
        existingVote.setUser(user);
        existingVote.setScore(6);
        when(historyVoteRepository.findByHistoryIdAndUserId(historyId, user.getId())).thenReturn(Optional.of(existingVote));

        VoteRequest request = new VoteRequest();
        request.setScore(9);

        service.voteOnHistory(LOCATION_ID, historyId, request, "alice");

        ArgumentCaptor<HistoryVote> voteCaptor = ArgumentCaptor.forClass(HistoryVote.class);
        verify(historyVoteRepository).save(voteCaptor.capture());
        assertThat(voteCaptor.getValue().getId()).isEqualTo(5L);
        assertThat(voteCaptor.getValue().getScore()).isEqualTo(9);
    }
}
