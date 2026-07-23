package com.nastolka.controller;

import com.nastolka.dto.CreateExpansionRequest;
import com.nastolka.dto.ExpansionResponse;
import com.nastolka.service.GameExpansionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/expansions")
public class GameExpansionController {

    private final GameExpansionService expansionService;

    public GameExpansionController(GameExpansionService expansionService) {
        this.expansionService = expansionService;
    }

    @GetMapping
    public ResponseEntity<List<ExpansionResponse>> getExpansions(@PathVariable Long gameId) {
        List<ExpansionResponse> expansions = expansionService.getExpansions(gameId);
        return ResponseEntity.ok(expansions);
    }

    @PostMapping
    public ResponseEntity<ExpansionResponse> createExpansion(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateExpansionRequest request
    ) {
        ExpansionResponse expansion = expansionService.createExpansion(gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expansion);
    }

    @PostMapping("/import/{bggId}")
    public ResponseEntity<ExpansionResponse> importFromBgg(
            @PathVariable Long gameId,
            @PathVariable Long bggId
    ) {
        ExpansionResponse expansion = expansionService.importFromBgg(gameId, bggId);
        return ResponseEntity.status(HttpStatus.CREATED).body(expansion);
    }

    @DeleteMapping("/{expansionId}")
    public ResponseEntity<Void> deleteExpansion(
            @PathVariable Long gameId,
            @PathVariable Long expansionId
    ) {
        expansionService.deleteExpansion(gameId, expansionId);
        return ResponseEntity.noContent().build();
    }
}
