package com.nastolka.service.impl;

import com.nastolka.entity.Game;
import com.nastolka.entity.GameExpansion;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.LocationGameExpansion;
import com.nastolka.entity.User;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.LocationGameExpansionRepository;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.service.GameExpansionService;
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
class LocationGameExpansionServiceImplTest {

    private static final Long LOCATION_ID = 1L;
    private static final Long GAME_ID = 2L;
    private static final Long EXPANSION_ID = 3L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationGameRepository locationGameRepository;
    @Mock
    private GameExpansionRepository expansionRepository;
    @Mock
    private LocationGameExpansionRepository locationGameExpansionRepository;
    @Mock
    private GameExpansionService gameExpansionService;
    @Mock
    private LocationAccessGuard accessGuard;

    private LocationGameExpansionServiceImpl service;
    private Location location;
    private LocationGame locationGame;
    private GameExpansion expansion;
    private User user;

    @BeforeEach
    void setUp() {
        service = new LocationGameExpansionServiceImpl(
                locationRepository,
                locationGameRepository,
                expansionRepository,
                locationGameExpansionRepository,
                gameExpansionService,
                accessGuard
        );

        user = User.builder().id(10L).username("alice").build();
        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(user);

        Game game = new Game();
        game.setId(GAME_ID);

        locationGame = new LocationGame();
        locationGame.setLocation(location);
        locationGame.setGame(game);

        expansion = new GameExpansion();
        expansion.setId(EXPANSION_ID);
        expansion.setGame(game);

        lenient().when(accessGuard.requireUser("alice")).thenReturn(user);
        lenient().when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        lenient().when(locationGameRepository.findByLocationIdAndGameId(LOCATION_ID, GAME_ID)).thenReturn(Optional.of(locationGame));
        lenient().when(expansionRepository.findById(EXPANSION_ID)).thenReturn(Optional.of(expansion));
    }

    @Test
    void addExpansion_touchesLocationUpdatedAt() {
        service.addExpansion(LOCATION_ID, GAME_ID, EXPANSION_ID, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(location.getUpdatedByUsername()).isEqualTo("alice");
    }

    @Test
    void removeExpansion_touchesLocationUpdatedAt() {
        LocationGameExpansion locationGameExpansion = new LocationGameExpansion();
        locationGameExpansion.setLocationGame(locationGame);
        locationGameExpansion.setExpansion(expansion);
        when(locationGameExpansionRepository.findByLocationGameIdAndExpansionId(locationGame.getId(), EXPANSION_ID))
                .thenReturn(Optional.of(locationGameExpansion));

        service.removeExpansion(LOCATION_ID, GAME_ID, EXPANSION_ID, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(location.getUpdatedByUsername()).isEqualTo("alice");
    }
}
