package com.nastolka.controller;

import com.nastolka.dto.LocationShareResponse;
import com.nastolka.dto.ShareLocationRequest;
import com.nastolka.service.LocationShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations/{locationId}/shares")
public class LocationShareController {

    private final LocationShareService locationShareService;

    public LocationShareController(LocationShareService locationShareService) {
        this.locationShareService = locationShareService;
    }

    @GetMapping
    public ResponseEntity<List<LocationShareResponse>> getShares(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationShareService.getShares(locationId, username));
    }

    @PostMapping
    public ResponseEntity<LocationShareResponse> shareLocation(
            @PathVariable Long locationId,
            @Valid @RequestBody ShareLocationRequest request,
            @AuthenticationPrincipal String username
    ) {
        LocationShareResponse response = locationShareService.shareLocation(locationId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{targetUsername}")
    public ResponseEntity<Void> removeShare(
            @PathVariable Long locationId,
            @PathVariable String targetUsername,
            @AuthenticationPrincipal String username
    ) {
        locationShareService.removeShare(locationId, targetUsername, username);
        return ResponseEntity.noContent().build();
    }
}
