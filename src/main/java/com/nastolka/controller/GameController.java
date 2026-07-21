package com.nastolka.controller;

import com.nastolka.dto.BggSearchResult;
import com.nastolka.dto.CreateGameRequest;
import com.nastolka.dto.GameResponse;
import com.nastolka.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getAllGames() {
        List<GameResponse> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGameById(@PathVariable Long id) {
        GameResponse game = gameService.getGameById(id);
        return ResponseEntity.ok(game);
    }

    @GetMapping("/search-external")
    public ResponseEntity<List<BggSearchResult>> searchExternal(@RequestParam String query) {
        List<BggSearchResult> results = gameService.searchExternal(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/import/{bggId}")
    public ResponseEntity<GameResponse> importFromBgg(@PathVariable Long bggId) {
        GameResponse game = gameService.importFromBgg(bggId);
        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        GameResponse game = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
