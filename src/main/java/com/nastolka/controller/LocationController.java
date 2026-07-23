package com.nastolka.controller;

import com.nastolka.dto.CreateLocationRequest;
import com.nastolka.dto.LocationResponse;
import com.nastolka.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(locationService.getAllLocations(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(
            @PathVariable Long id,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationService.getLocationById(id, username));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal String username
    ) {
        LocationResponse location = locationService.createLocation(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationService.updateLocation(id, request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long id,
            @AuthenticationPrincipal String username
    ) {
        locationService.deleteLocation(id, username);
        return ResponseEntity.noContent().build();
    }
}
