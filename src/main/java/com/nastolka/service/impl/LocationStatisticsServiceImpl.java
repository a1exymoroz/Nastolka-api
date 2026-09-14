package com.nastolka.service.impl;

import com.nastolka.dto.ActivityBucketResponse;
import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.ActivityStatistics;
import com.nastolka.dto.DailyActivityResponse;
import com.nastolka.dto.ExpansionUsageResponse;
import com.nastolka.dto.GamePlayCountResponse;
import com.nastolka.dto.GameRatingResponse;
import com.nastolka.dto.GameStatistics;
import com.nastolka.dto.LibraryCoverageResponse;
import com.nastolka.dto.PlayerStatisticResponse;
import com.nastolka.dto.PlayerStatisticsResponse;
import com.nastolka.dto.StatisticsOverview;
import com.nastolka.entity.HistoryState;
import com.nastolka.entity.Location;
import com.nastolka.entity.User;
import com.nastolka.repository.HistoryExpansionRepository;
import com.nastolka.repository.HistoryPlayerRepository;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationHistoryRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.projection.PlayerStatsProjection;
import com.nastolka.repository.projection.SessionTimingProjection;
import com.nastolka.service.LocationStatisticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class LocationStatisticsServiceImpl implements LocationStatisticsService {

    private static final int TOP_N_DEFAULT = 5;
    private static final long MIN_RATING_SAMPLE_SIZE = 2;
    private static final int CONTRIBUTION_CALENDAR_DAYS = 365;
    private static final HistoryState STATS_STATE = HistoryState.FINISHED;
    // playedAt is stored as an instant, but the client sends it as local midnight of the
    // chosen date (see V8__convert_location_history_timestamps_to_timestamptz.sql, which
    // treats Europe/Warsaw as this app's reference zone) — bucketing by UTC would attribute
    // a session played e.g. "2026-09-14" local to 2026-09-13 during CEST.
    private static final ZoneId LOCATION_ZONE = ZoneId.of("Europe/Warsaw");

    private final LocationRepository locationRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final HistoryPlayerRepository historyPlayerRepository;
    private final HistoryExpansionRepository historyExpansionRepository;
    private final LocationGameRepository locationGameRepository;
    private final LocationAccessGuard accessGuard;

    public LocationStatisticsServiceImpl(
            LocationRepository locationRepository,
            LocationHistoryRepository locationHistoryRepository,
            HistoryPlayerRepository historyPlayerRepository,
            HistoryExpansionRepository historyExpansionRepository,
            LocationGameRepository locationGameRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.historyPlayerRepository = historyPlayerRepository;
        this.historyExpansionRepository = historyExpansionRepository;
        this.locationGameRepository = locationGameRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public StatisticsOverview getOverview(Long locationId, String username) {
        requireAccess(locationId, username);

        long totalFinishedSessions = locationHistoryRepository.countByLocationIdAndState(locationId, STATS_STATE);
        Double averageRating = locationHistoryRepository.findAverageRating(locationId, STATS_STATE);

        List<Long> sessionDurationMinutes = locationHistoryRepository.findSessionTimings(locationId, STATS_STATE).stream()
                .filter(timing -> timing.getStartedAt() != null && timing.getFinishedAt() != null)
                .map(timing -> Duration.between(timing.getStartedAt(), timing.getFinishedAt()).toMinutes())
                .toList();
        long totalPlayTimeMinutes = sessionDurationMinutes.stream().mapToLong(Long::longValue).sum();
        Double averageSessionLengthMinutes = sessionDurationMinutes.isEmpty()
                ? null
                : totalPlayTimeMinutes / (double) sessionDurationMinutes.size();

        return StatisticsOverview.builder()
                .totalFinishedSessions(totalFinishedSessions)
                .totalPlayTimeMinutes(totalPlayTimeMinutes)
                .averageSessionLengthMinutes(round2(averageSessionLengthMinutes))
                .averageRating(round2(averageRating))
                .build();
    }

    @Override
    public GameStatistics getGameStatistics(Long locationId, String username) {
        requireAccess(locationId, username);

        List<GamePlayCountResponse> mostPlayedGames = locationHistoryRepository
                .findMostPlayedGames(locationId, STATS_STATE, PageRequest.of(0, TOP_N_DEFAULT)).stream()
                .map(projection -> GamePlayCountResponse.builder()
                        .gameId(projection.getGameId())
                        .gameName(projection.getGameName())
                        .playCount(projection.getPlayCount())
                        .build())
                .toList();

        List<GameRatingResponse> topRatedGames = locationHistoryRepository
                .findTopRatedGames(locationId, STATS_STATE, MIN_RATING_SAMPLE_SIZE, PageRequest.of(0, TOP_N_DEFAULT)).stream()
                .map(projection -> GameRatingResponse.builder()
                        .gameId(projection.getGameId())
                        .gameName(projection.getGameName())
                        .averageRating(round2(projection.getAverageRating()))
                        .ratingCount(projection.getRatingCount())
                        .build())
                .toList();

        long gamesPlayed = locationHistoryRepository.countDistinctGamesPlayed(locationId, STATS_STATE);
        long totalGamesInLibrary = locationGameRepository.countByLocationId(locationId);
        Double coveragePercentage = totalGamesInLibrary == 0 ? null : gamesPlayed * 100.0 / totalGamesInLibrary;

        LibraryCoverageResponse libraryCoverage = LibraryCoverageResponse.builder()
                .gamesPlayed(gamesPlayed)
                .totalGamesInLibrary(totalGamesInLibrary)
                .coveragePercentage(round2(coveragePercentage))
                .build();

        return GameStatistics.builder()
                .mostPlayedGames(mostPlayedGames)
                .topRatedGames(topRatedGames)
                .libraryCoverage(libraryCoverage)
                .build();
    }

    @Override
    public PlayerStatisticsResponse getPlayerStatistics(Long locationId, String username) {
        requireAccess(locationId, username);

        List<PlayerStatisticResponse> playerStats = historyPlayerRepository.findPlayerStats(locationId, STATS_STATE).stream()
                .map(this::toPlayerStatisticResponse)
                .toList();

        List<PlayerStatisticResponse> leaderboard = playerStats.stream()
                .sorted(Comparator.comparingLong(PlayerStatisticResponse::getWins).reversed()
                        .thenComparing(Comparator.comparingLong(PlayerStatisticResponse::getTotalPoints).reversed())
                        .thenComparing(PlayerStatisticResponse::getUsername))
                .toList();

        List<PlayerStatisticResponse> mostActive = playerStats.stream()
                .sorted(Comparator.comparingLong(PlayerStatisticResponse::getGamesPlayed).reversed()
                        .thenComparing(PlayerStatisticResponse::getUsername))
                .limit(TOP_N_DEFAULT)
                .toList();

        return PlayerStatisticsResponse.builder()
                .leaderboard(leaderboard)
                .mostActive(mostActive)
                .build();
    }

    @Override
    public ActivityStatistics getActivity(Long locationId, String username, ActivityGranularity granularity) {
        requireAccess(locationId, username);

        List<SessionTimingProjection> timings = locationHistoryRepository.findSessionTimings(locationId, STATS_STATE);

        Map<LocalDate, Long> buckets = timings.stream()
                .map(timing -> timing.getPlayedAt().atZone(LOCATION_ZONE).toLocalDate())
                .map(date -> granularity == ActivityGranularity.WEEK
                        ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        : date.withDayOfMonth(1))
                .collect(Collectors.groupingBy(date -> date, TreeMap::new, Collectors.counting()));

        List<ActivityBucketResponse> bucketResponses = buckets.entrySet().stream()
                .map(entry -> ActivityBucketResponse.builder()
                        .bucketStart(entry.getKey())
                        .sessionCount(entry.getValue())
                        .build())
                .toList();

        return ActivityStatistics.builder()
                .granularity(granularity)
                .buckets(bucketResponses)
                .build();
    }

    @Override
    public List<ExpansionUsageResponse> getMostUsedExpansions(Long locationId, String username) {
        requireAccess(locationId, username);

        return historyExpansionRepository.findMostUsedExpansions(locationId, STATS_STATE, PageRequest.of(0, TOP_N_DEFAULT)).stream()
                .map(projection -> ExpansionUsageResponse.builder()
                        .expansionId(projection.getExpansionId())
                        .expansionName(projection.getExpansionName())
                        .useCount(projection.getUseCount())
                        .build())
                .toList();
    }

    @Override
    public List<DailyActivityResponse> getContributionCalendar(Long locationId, String username) {
        requireAccess(locationId, username);

        LocalDate cutoff = LocalDate.now(LOCATION_ZONE).minusDays(CONTRIBUTION_CALENDAR_DAYS - 1L);

        Map<LocalDate, Long> dailyCounts = locationHistoryRepository.findSessionTimings(locationId, STATS_STATE).stream()
                .map(timing -> timing.getPlayedAt().atZone(LOCATION_ZONE).toLocalDate())
                .filter(date -> !date.isBefore(cutoff))
                .collect(Collectors.groupingBy(date -> date, TreeMap::new, Collectors.counting()));

        return dailyCounts.entrySet().stream()
                .map(entry -> DailyActivityResponse.builder()
                        .date(entry.getKey())
                        .sessionCount(entry.getValue())
                        .build())
                .toList();
    }

    private PlayerStatisticResponse toPlayerStatisticResponse(PlayerStatsProjection projection) {
        long gamesPlayed = projection.getGamesPlayed();
        long wins = projection.getWins();
        double winRatePercentage = gamesPlayed == 0 ? 0.0 : wins * 100.0 / gamesPlayed;
        double averagePoints = projection.getAveragePoints() != null ? projection.getAveragePoints() : 0.0;
        return PlayerStatisticResponse.builder()
                .username(projection.getUsername())
                .gamesPlayed(gamesPlayed)
                .wins(wins)
                .winRatePercentage(round2(winRatePercentage))
                .totalPoints(projection.getTotalPoints())
                .averagePoints(round2(averagePoints))
                .build();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static Double round2(Double value) {
        return value == null ? null : round2(value.doubleValue());
    }

    private void requireAccess(Long locationId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }
}
