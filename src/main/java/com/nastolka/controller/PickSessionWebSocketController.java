package com.nastolka.controller;

import com.nastolka.dto.PickSessionActionRequest;
import com.nastolka.dto.PickSessionErrorResponse;
import com.nastolka.dto.PickSessionResponse;
import com.nastolka.service.PickSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class PickSessionWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(PickSessionWebSocketController.class);

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

    // Without this, an exception thrown from a @MessageMapping method (e.g. "not your turn")
    // is only logged server-side and never reaches the client: Spring's default handling for
    // unhandled STOMP message exceptions is to log and drop them, unlike REST where
    // GlobalExceptionHandler turns them into a response. Delivered via the user-specific queue
    // (STOMP ERROR frames close the connection, which is too heavy-handed for routine validation
    // failures like "not your turn" or "game already decided").
    @MessageExceptionHandler(ResponseStatusException.class)
    @SendToUser("/queue/pick-session-errors")
    public PickSessionErrorResponse handleResponseStatusException(ResponseStatusException ex) {
        return new PickSessionErrorResponse(ex.getReason());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/pick-session-errors")
    public PickSessionErrorResponse handleUnexpectedException(Exception ex) {
        log.error("Unexpected error handling pick session message", ex);
        return new PickSessionErrorResponse("An unexpected error occurred");
    }
}
