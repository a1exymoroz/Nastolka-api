package com.nastolka.service;

import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.ActivityStatistics;
import com.nastolka.dto.DailyActivityResponse;
import com.nastolka.dto.ExpansionUsageResponse;
import com.nastolka.dto.GameStatistics;
import com.nastolka.dto.PlayerStatisticsResponse;
import com.nastolka.dto.StatisticsOverview;

import java.util.List;

public interface LocationStatisticsService {

    StatisticsOverview getOverview(Long locationId, String username);

    GameStatistics getGameStatistics(Long locationId, String username);

    PlayerStatisticsResponse getPlayerStatistics(Long locationId, String username);

    ActivityStatistics getActivity(Long locationId, String username, ActivityGranularity granularity);

    List<ExpansionUsageResponse> getMostUsedExpansions(Long locationId, String username);

    List<DailyActivityResponse> getContributionCalendar(Long locationId, String username);
}
