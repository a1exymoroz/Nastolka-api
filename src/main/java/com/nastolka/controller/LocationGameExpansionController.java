package com.nastolka.controller;

import com.nastolka.dto.ExpansionResponse;
import com.nastolka.service.LocationGameExpansionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations/{locationId}/games/{gameId}/expansions")
public class LocationGameExpansionController {

    private final LocationGameExpansionService expansionService;

    public LocationGameExpansionController(LocationGameExpansionService expansionService) {
        this.expansionService = expansionService;
    }

    @GetMapping
    public ResponseEntity<List<ExpansionResponse>> getExpansions(
            @PathVariable Long locationId,
            @PathVariable Long gameId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(expansionService.getExpansions(locationId, gameId, username));
    }

    @PostMapping("/{expansionId}")
    public ResponseEntity<ExpansionResponse> addExpansion(
            @PathVariable Long locationId,
            @PathVariable Long gameId,
            @PathVariable Long expansionId,
            @AuthenticationPrincipal String username
    ) {
        ExpansionResponse expansion = expansionService.addExpansion(locationId, gameId, expansionId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(expansion);
    }

    @DeleteMapping("/{expansionId}")
    public ResponseEntity<Void> removeExpansion(
            @PathVariable Long locationId,
            @PathVariable Long gameId,
            @PathVariable Long expansionId,
            @AuthenticationPrincipal String username
    ) {
        expansionService.removeExpansion(locationId, gameId, expansionId, username);
        return ResponseEntity.noContent().build();
    }
}
