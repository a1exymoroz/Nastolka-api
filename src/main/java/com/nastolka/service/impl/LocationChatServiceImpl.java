package com.nastolka.service.impl;

import com.nastolka.dto.ChatMessageResponse;
import com.nastolka.dto.SendChatMessageRequest;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationChatMessage;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationChatMessageRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.service.LocationChatService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationChatServiceImpl implements LocationChatService {

    private final LocationRepository locationRepository;
    private final LocationChatMessageRepository locationChatMessageRepository;
    private final LocationAccessGuard accessGuard;

    public LocationChatServiceImpl(
            LocationRepository locationRepository,
            LocationChatMessageRepository locationChatMessageRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.locationChatMessageRepository = locationChatMessageRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public List<ChatMessageResponse> getMessages(Long locationId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        return locationChatMessageRepository.findByLocationIdOrderByCreatedAtAsc(locationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ChatMessageResponse sendMessage(Long locationId, SendChatMessageRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        LocationChatMessage message = new LocationChatMessage();
        message.setLocation(location);
        message.setSender(requester);
        message.setContent(request.getContent());
        message = locationChatMessageRepository.save(message);

        return toResponse(message);
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private ChatMessageResponse toResponse(LocationChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .locationId(message.getLocation().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAdmin(message.getSender().getRole() == Role.ADMIN)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
