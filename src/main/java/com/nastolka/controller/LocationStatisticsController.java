package com.nastolka.controller;

import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.LocationStatisticsResponse;
import com.nastolka.service.LocationStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations/{locationId}/statistics")
public class LocationStatisticsController {

    private final LocationStatisticsService locationStatisticsService;

    public LocationStatisticsController(LocationStatisticsService locationStatisticsService) {
        this.locationStatisticsService = locationStatisticsService;
    }

    @GetMapping
    public ResponseEntity<LocationStatisticsResponse> getStatistics(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "MONTH") String granularity,
            @AuthenticationPrincipal String username
    ) {
        ActivityGranularity parsedGranularity = ActivityGranularity.valueOf(granularity.toUpperCase());
        return ResponseEntity.ok(locationStatisticsService.getStatistics(locationId, username, parsedGranularity));
    }
}
