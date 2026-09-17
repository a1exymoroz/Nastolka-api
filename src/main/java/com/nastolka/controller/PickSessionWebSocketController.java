package com.nastolka.controller;

import com.nastolka.dto.PickSessionActionRequest;
import com.nastolka.dto.PickSessionResponse;
import com.nastolka.service.PickSessionService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class PickSessionWebSocketController {

    private final PickSessionService pickSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public PickSessionWebSocketController(PickSessionService pickSessionService, SimpMessagingTemplate messagingTemplate) {
        this.pickSessionService = pickSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/locations/{locationId}/pick-sessions/{sessionId}/join")
    public void join(
            @DestinationVariable Long locationId,
            @DestinationVariable Long sessionId,
            Principal principal
    ) {
        broadcast(locationId, sessionId, pickSessionService.join(locationId, sessionId, principal.getName()));
    }

    @MessageMapping("/locations/{locationId}/pick-sessions/{sessionId}/start")
    public void start(
            @DestinationVariable Long locationId,
            @DestinationVariable Long sessionId,
            Principal principal
    ) {
        broadcast(locationId, sessionId, pickSessionService.start(locationId, sessionId, principal.getName()));
    }

    @MessageMapping("/locations/{locationId}/pick-sessions/{sessionId}/action")
    public void submitAction(
            @DestinationVariable Long locationId,
            @DestinationVariable Long sessionId,
            @Payload PickSessionActionRequest request,
            Principal principal
    ) {
        broadcast(locationId, sessionId, pickSessionService.submitAction(locationId, sessionId, request, principal.getName()));
    }

    @MessageMapping("/locations/{locationId}/pick-sessions/{sessionId}/cancel")
    public void cancel(
            @DestinationVariable Long locationId,
            @DestinationVariable Long sessionId,
            Principal principal
    ) {
        broadcast(locationId, sessionId, pickSessionService.cancel(locationId, sessionId, principal.getName()));
    }

    private void broadcast(Long locationId, Long sessionId, PickSessionResponse response) {
        messagingTemplate.convertAndSend("/topic/locations/" + locationId + "/pick-sessions/" + sessionId, response);
    }
}
