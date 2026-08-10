package com.nastolka.service.impl;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.HistoryResponse;
import com.nastolka.entity.Game;
import com.nastolka.entity.HistoryState;
import com.nastolka.entity.Location;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
