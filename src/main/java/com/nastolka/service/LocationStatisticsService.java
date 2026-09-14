package com.nastolka.service;

import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.LocationStatisticsResponse;

public interface LocationStatisticsService {

    LocationStatisticsResponse getStatistics(Long locationId, String username, ActivityGranularity granularity);
}
