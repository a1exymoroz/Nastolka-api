package com.nastolka.service.impl;

import com.nastolka.dto.SendChatMessageRequest;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationChatMessage;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationChatMessageRepository;
import com.nastolka.repository.LocationRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationChatServiceImplTest {

    private static final Long LOCATION_ID = 1L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationChatMessageRepository locationChatMessageRepository;
    @Mock
    private LocationAccessGuard accessGuard;

    private LocationChatServiceImpl service;
    private Location location;
    private User user;

    @BeforeEach
    void setUp() {
        service = new LocationChatServiceImpl(locationRepository, locationChatMessageRepository, accessGuard);

        user = User.builder().id(10L).username("alice").build();
        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(user);

        when(accessGuard.requireUser("alice")).thenReturn(user);
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        when(locationChatMessageRepository.save(any(LocationChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sendMessage_touchesLocationUpdatedAt() {
        SendChatMessageRequest request = new SendChatMessageRequest();
        request.setContent("Anyone free tonight?");

        service.sendMessage(LOCATION_ID, request, "alice");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        assertThat(location.getUpdatedByUsername()).isEqualTo("alice");
    }
}
