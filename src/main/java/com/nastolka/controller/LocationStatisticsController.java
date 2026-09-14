package com.nastolka.controller;

import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.ActivityStatistics;
import com.nastolka.dto.DailyActivityResponse;
import com.nastolka.dto.ExpansionUsageResponse;
import com.nastolka.dto.GameStatistics;
import com.nastolka.dto.PlayerStatisticsResponse;
import com.nastolka.dto.StatisticsOverview;
import com.nastolka.service.LocationStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations/{locationId}/statistics")
public class LocationStatisticsController {

    private final LocationStatisticsService locationStatisticsService;

    public LocationStatisticsController(LocationStatisticsService locationStatisticsService) {
        this.locationStatisticsService = locationStatisticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<StatisticsOverview> getOverview(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationStatisticsService.getOverview(locationId, username));
    }

    @GetMapping("/games")
    public ResponseEntity<GameStatistics> getGameStatistics(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationStatisticsService.getGameStatistics(locationId, username));
    }

    @GetMapping("/players")
    public ResponseEntity<PlayerStatisticsResponse> getPlayerStatistics(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationStatisticsService.getPlayerStatistics(locationId, username));
    }

    @GetMapping("/activity")
    public ResponseEntity<ActivityStatistics> getActivity(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "MONTH") String granularity,
            @AuthenticationPrincipal String username
    ) {
        ActivityGranularity parsedGranularity = ActivityGranularity.valueOf(granularity.toUpperCase());
        return ResponseEntity.ok(locationStatisticsService.getActivity(locationId, username, parsedGranularity));
    }

    @GetMapping("/expansions")
    public ResponseEntity<List<ExpansionUsageResponse>> getMostUsedExpansions(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationStatisticsService.getMostUsedExpansions(locationId, username));
    }

    @GetMapping("/contribution-calendar")
    public ResponseEntity<List<DailyActivityResponse>> getContributionCalendar(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationStatisticsService.getContributionCalendar(locationId, username));
    }
}
