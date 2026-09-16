package com.nastolka.service.impl;

import com.nastolka.dto.LocationResponse;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationShare;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.LocationShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationShareRepository locationShareRepository;
    @Mock
    private LocationAccessGuard accessGuard;

    private LocationServiceImpl service;
    private User requester;

    @BeforeEach
    void setUp() {
        service = new LocationServiceImpl(locationRepository, locationShareRepository, accessGuard);

        requester = User.builder().id(1L).username("alice").role(Role.USER).build();
        lenient().when(accessGuard.requireUser("alice")).thenReturn(requester);
        lenient().when(locationShareRepository.findByUserId(requester.getId())).thenReturn(List.of());
    }

    private Location locationUpdatedAt(long id, Instant updatedAt) {
        Location location = new Location();
        location.setId(id);
        location.setOwner(requester);
        location.setName("Location " + id);
        ReflectionTestUtils.setField(location, "updatedAt", updatedAt);
        return location;
    }

    @Test
    void getAllLocations_ordersOwnedAndSharedLocationsByUpdatedAtDescending() {
        Instant now = Instant.now();
        Location oldest = locationUpdatedAt(1L, now.minus(2, ChronoUnit.DAYS));
        Location newest = locationUpdatedAt(2L, now);
        Location middle = locationUpdatedAt(3L, now.minus(1, ChronoUnit.DAYS));

        when(locationRepository.findByOwnerId(requester.getId())).thenReturn(List.of(oldest, newest, middle));

        List<LocationResponse> responses = service.getAllLocations("alice");

        assertThat(responses).extracting(LocationResponse::getId).containsExactly(2L, 3L, 1L);
    }

    @Test
    void getAllLocations_ordersAllLocationsByUpdatedAtDescending_forAdmin() {
        User admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();
        when(accessGuard.requireUser("admin")).thenReturn(admin);

        Instant now = Instant.now();
        Location oldest = locationUpdatedAt(1L, now.minus(1, ChronoUnit.DAYS));
        Location newest = locationUpdatedAt(2L, now);
        when(locationRepository.findAll()).thenReturn(List.of(oldest, newest));

        List<LocationResponse> responses = service.getAllLocations("admin");

        assertThat(responses).extracting(LocationResponse::getId).containsExactly(2L, 1L);
    }

    @Test
    void getAllLocations_includesUpdatedAtInResponse() {
        Instant updatedAt = Instant.now();
        Location location = locationUpdatedAt(1L, updatedAt);
        when(locationRepository.findByOwnerId(requester.getId())).thenReturn(List.of(location));

        List<LocationResponse> responses = service.getAllLocations("alice");

        assertThat(responses.get(0).getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getAllLocations_placesSharedLocationsAmongOwnedByUpdatedAt() {
        Instant now = Instant.now();
        Location owned = locationUpdatedAt(1L, now.minus(2, ChronoUnit.DAYS));
        Location shared = locationUpdatedAt(2L, now);

        when(locationRepository.findByOwnerId(requester.getId())).thenReturn(List.of(owned));
        LocationShare share = new LocationShare();
        share.setLocation(shared);
        when(locationShareRepository.findByUserId(requester.getId())).thenReturn(List.of(share));

        List<LocationResponse> responses = service.getAllLocations("alice");

        assertThat(responses).extracting(LocationResponse::getId).containsExactly(2L, 1L);
    }
}
