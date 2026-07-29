package com.nastolka.controller;

import com.nastolka.dto.ChatMessageResponse;
import com.nastolka.service.LocationChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations/{locationId}/chat/messages")
public class LocationChatController {

    private final LocationChatService locationChatService;

    public LocationChatController(LocationChatService locationChatService) {
        this.locationChatService = locationChatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationChatService.getMessages(locationId, username));
    }
}
