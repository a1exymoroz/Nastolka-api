package com.nastolka.service.impl;

import com.nastolka.entity.Game;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.User;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.GameRepository;
import com.nastolka.repository.LocationGameExpansionRepository;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationGameServiceImplTest {

    private static final Long LOCATION_ID = 1L;
    private static final Long GAME_ID = 2L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private LocationGameRepository locationGameRepository;
    @Mock
    private LocationGameExpansionRepository locationGameExpansionRepository;
    @Mock
    private GameExpansionRepository gameExpansionRepository;
    @Mock
    private GameService gameService;
    @Mock
    private LocationAccessGuard accessGuard;

    private LocationGameServiceImpl service;
    private Location location;
    private Game game;
    private User user;

    @BeforeEach
    void setUp() {
        service = new LocationGameServiceImpl(
                locationRepository,
                gameRepository,
                locationGameRepository,
                locationGameExpansionRepository,
                gameExpansionRepository,
                gameService,
                accessGuard
        );

        user = User.builder().id(10L).username("alice").build();
        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(user);

        game = new Game();
        game.setId(GAME_ID);
        game.setName("Terraforming Mars");

        lenient().when(accessGuard.requireUser("alice")).thenReturn(user);
        lenient().when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        lenient().when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
    }

    @Test
    void addGame_touchesLocationUpdatedAt() {
        service.addGame(LOCATION_ID, GAME_ID, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void removeGame_touchesLocationUpdatedAt() {
        LocationGame locationGame = new LocationGame();
        locationGame.setLocation(location);
        locationGame.setGame(game);
        when(locationGameRepository.findByLocationIdAndGameId(LOCATION_ID, GAME_ID)).thenReturn(Optional.of(locationGame));

        service.removeGame(LOCATION_ID, GAME_ID, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    }
}
