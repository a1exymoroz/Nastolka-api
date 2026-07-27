package com.nastolka.controller;

import com.nastolka.dto.GameResponse;
import com.nastolka.service.LocationGameService;
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
@RequestMapping("/api/locations/{locationId}/games")
public class LocationGameController {

    private final LocationGameService locationGameService;

    public LocationGameController(LocationGameService locationGameService) {
        this.locationGameService = locationGameService;
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getGames(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationGameService.getGames(locationId, username));
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<GameResponse> addGame(
            @PathVariable Long locationId,
            @PathVariable Long gameId,
            @AuthenticationPrincipal String username
    ) {
        GameResponse game = locationGameService.addGame(locationId, gameId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }

    @PostMapping("/import/{bggId}")
    public ResponseEntity<GameResponse> importGame(
            @PathVariable Long locationId,
            @PathVariable Long bggId,
            @AuthenticationPrincipal String username
    ) {
        GameResponse game = locationGameService.importGame(locationId, bggId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeGame(
            @PathVariable Long locationId,
            @PathVariable Long gameId,
            @AuthenticationPrincipal String username
    ) {
        locationGameService.removeGame(locationId, gameId, username);
        return ResponseEntity.noContent().build();
    }
}
