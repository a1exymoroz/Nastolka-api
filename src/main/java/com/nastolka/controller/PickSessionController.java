package com.nastolka.controller;

import com.nastolka.dto.CreatePickSessionRequest;
import com.nastolka.dto.PickSessionResponse;
import com.nastolka.service.PickSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations/{locationId}/pick-sessions")
public class PickSessionController {

    private final PickSessionService pickSessionService;

    public PickSessionController(PickSessionService pickSessionService) {
        this.pickSessionService = pickSessionService;
    }

    @PostMapping
    public ResponseEntity<PickSessionResponse> create(
            @PathVariable Long locationId,
            @Valid @RequestBody CreatePickSessionRequest request,
            @AuthenticationPrincipal String username
    ) {
        PickSessionResponse response = pickSessionService.create(locationId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/active")
    public ResponseEntity<PickSessionResponse> getActive(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return pickSessionService.getActive(locationId, username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<PickSessionResponse> getById(
            @PathVariable Long locationId,
            @PathVariable Long sessionId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(pickSessionService.getById(locationId, sessionId, username));
    }
}
