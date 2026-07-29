package com.nastolka.controller;

import com.nastolka.dto.ChatMessageResponse;
import com.nastolka.dto.SendChatMessageRequest;
import com.nastolka.service.LocationChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class LocationChatWebSocketController {

    private final LocationChatService locationChatService;
    private final SimpMessagingTemplate messagingTemplate;

    public LocationChatWebSocketController(LocationChatService locationChatService, SimpMessagingTemplate messagingTemplate) {
        this.locationChatService = locationChatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/locations/{locationId}/chat.send")
    public void send(
            @DestinationVariable Long locationId,
            @Payload SendChatMessageRequest request,
            Principal principal
    ) {
        ChatMessageResponse response = locationChatService.sendMessage(locationId, request, principal.getName());
        messagingTemplate.convertAndSend("/topic/locations/" + locationId + "/chat", response);
    }
}
