package com.nastolka.service.impl;

import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.ActivityStatistics;
import com.nastolka.dto.DailyActivityResponse;
import com.nastolka.dto.GameStatistics;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationStatisticsServiceImplTest {

    private static final Long LOCATION_ID = 1L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationHistoryRepository locationHistoryRepository;
    @Mock
    private HistoryPlayerRepository historyPlayerRepository;
    @Mock
    private HistoryExpansionRepository historyExpansionRepository;
    @Mock
    private LocationGameRepository locationGameRepository;
    @Mock
    private LocationAccessGuard accessGuard;

    private LocationStatisticsServiceImpl service;
    private Location location;
    private User user;

    @BeforeEach
    void setUp() {
        service = new LocationStatisticsServiceImpl(
                locationRepository,
                locationHistoryRepository,
                historyPlayerRepository,
                historyExpansionRepository,
                locationGameRepository,
                accessGuard
        );

        user = User.builder().id(10L).username("alice").build();
        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(user);

        lenient().when(accessGuard.requireUser("alice")).thenReturn(user);
        lenient().when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        lenient().when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of());
        lenient().when(locationHistoryRepository.countByLocationIdAndState(LOCATION_ID, HistoryState.FINISHED)).thenReturn(0L);
        lenient().when(locationHistoryRepository.findAverageRating(LOCATION_ID, HistoryState.FINISHED)).thenReturn(null);
        lenient().when(locationHistoryRepository.countDistinctGamesPlayed(LOCATION_ID, HistoryState.FINISHED)).thenReturn(0L);
        lenient().when(locationHistoryRepository.findMostPlayedGames(eq(LOCATION_ID), eq(HistoryState.FINISHED), any(Pageable.class))).thenReturn(List.of());
        lenient().when(locationHistoryRepository.findTopRatedGames(eq(LOCATION_ID), eq(HistoryState.FINISHED), any(Long.class), any(Pageable.class))).thenReturn(List.of());
        lenient().when(locationGameRepository.countByLocationId(LOCATION_ID)).thenReturn(0L);
        lenient().when(historyPlayerRepository.findPlayerStats(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of());
        lenient().when(historyExpansionRepository.findMostUsedExpansions(eq(LOCATION_ID), eq(HistoryState.FINISHED), any(Pageable.class))).thenReturn(List.of());
    }

    private SessionTimingProjection sessionTiming(Instant playedAt, Instant startedAt, Instant finishedAt) {
        SessionTimingProjection timing = mock(SessionTimingProjection.class);
        lenient().when(timing.getPlayedAt()).thenReturn(playedAt);
        lenient().when(timing.getStartedAt()).thenReturn(startedAt);
        lenient().when(timing.getFinishedAt()).thenReturn(finishedAt);
        return timing;
    }

    private PlayerStatsProjection playerStats(String username, long gamesPlayed, long wins, long totalPoints, double averagePoints) {
        PlayerStatsProjection projection = mock(PlayerStatsProjection.class);
        lenient().when(projection.getUsername()).thenReturn(username);
        lenient().when(projection.getGamesPlayed()).thenReturn(gamesPlayed);
        lenient().when(projection.getWins()).thenReturn(wins);
        lenient().when(projection.getTotalPoints()).thenReturn(totalPoints);
        lenient().when(projection.getAveragePoints()).thenReturn(averagePoints);
        return projection;
    }

    // --- overview ---

    @Test
    void getOverview_returnsZerosAndNullAverages_whenNoFinishedSessions() {
        StatisticsOverview overview = service.getOverview(LOCATION_ID, "alice");

        assertThat(overview.getTotalFinishedSessions()).isZero();
        assertThat(overview.getTotalPlayTimeMinutes()).isZero();
        assertThat(overview.getAverageSessionLengthMinutes()).isNull();
        assertThat(overview.getAverageRating()).isNull();
    }

    @Test
    void getOverview_computesTotalAndAveragePlayTime_fromFinishedSessionDurations() {
        Instant now = Instant.now();
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(now, now.minus(60, ChronoUnit.MINUTES), now),
                sessionTiming(now, now.minus(30, ChronoUnit.MINUTES), now)
        ));

        StatisticsOverview overview = service.getOverview(LOCATION_ID, "alice");

        assertThat(overview.getTotalPlayTimeMinutes()).isEqualTo(90L);
        assertThat(overview.getAverageSessionLengthMinutes()).isEqualTo(45.0);
    }

    @Test
    void getOverview_throwsForbidden_whenAccessDenied() {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You do not have access to this location"))
                .when(accessGuard).requireViewAccess(location, user);

        assertThatThrownBy(() -> service.getOverview(LOCATION_ID, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You do not have access to this location");
    }

    @Test
    void getOverview_throwsNotFound_whenLocationMissing() {
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOverview(LOCATION_ID, "alice"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Location not found");
    }

    // --- game stats ---

    @Test
    void getGameStatistics_computesLibraryCoveragePercentage() {
        when(locationHistoryRepository.countDistinctGamesPlayed(LOCATION_ID, HistoryState.FINISHED)).thenReturn(2L);
        when(locationGameRepository.countByLocationId(LOCATION_ID)).thenReturn(4L);

        GameStatistics gameStatistics = service.getGameStatistics(LOCATION_ID, "alice");

        assertThat(gameStatistics.getLibraryCoverage().getCoveragePercentage()).isEqualTo(50.0);
    }

    @Test
    void getGameStatistics_returnsNullCoverage_whenLibraryIsEmpty() {
        GameStatistics gameStatistics = service.getGameStatistics(LOCATION_ID, "alice");

        assertThat(gameStatistics.getLibraryCoverage().getCoveragePercentage()).isNull();
    }

    // --- player statistics ---

    @Test
    void getPlayerStatistics_computesWinRateAndSortsLeaderboardByWinsDescending() {
        PlayerStatsProjection alice = playerStats("alice", 4L, 3L, 30L, 7.5);
        PlayerStatsProjection bob = playerStats("bob", 4L, 1L, 20L, 5.0);
        when(historyPlayerRepository.findPlayerStats(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(bob, alice));

        PlayerStatisticsResponse response = service.getPlayerStatistics(LOCATION_ID, "alice");

        assertThat(response.getLeaderboard()).extracting("username").containsExactly("alice", "bob");
        assertThat(response.getLeaderboard().get(0).getWinRatePercentage()).isEqualTo(75.0);
        assertThat(response.getLeaderboard().get(1).getWinRatePercentage()).isEqualTo(25.0);
    }

    @Test
    void getPlayerStatistics_sortsMostActiveByGamesPlayedDescending() {
        PlayerStatsProjection alice = playerStats("alice", 2L, 1L, 10L, 5.0);
        PlayerStatsProjection bob = playerStats("bob", 6L, 1L, 10L, 5.0);
        when(historyPlayerRepository.findPlayerStats(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(alice, bob));

        PlayerStatisticsResponse response = service.getPlayerStatistics(LOCATION_ID, "alice");

        assertThat(response.getMostActive()).extracting("username").containsExactly("bob", "alice");
    }

    // --- activity trend ---

    @Test
    void getActivity_bucketsByCalendarMonth() {
        Instant sessionOne = Instant.parse("2026-03-05T10:00:00Z");
        Instant sessionTwo = Instant.parse("2026-03-20T10:00:00Z");
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(sessionOne, null, null),
                sessionTiming(sessionTwo, null, null)
        ));

        ActivityStatistics activity = service.getActivity(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(activity.getBuckets()).hasSize(1);
        assertThat(activity.getBuckets().get(0).getBucketStart().toString()).isEqualTo("2026-03-01");
        assertThat(activity.getBuckets().get(0).getSessionCount()).isEqualTo(2L);
    }

    @Test
    void getActivity_bucketsByCalendarWeek() {
        Instant monday = Instant.parse("2026-03-02T10:00:00Z");
        Instant wednesdaySameWeek = Instant.parse("2026-03-04T10:00:00Z");
        Instant nextMonday = Instant.parse("2026-03-09T10:00:00Z");
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(monday, null, null),
                sessionTiming(wednesdaySameWeek, null, null),
                sessionTiming(nextMonday, null, null)
        ));

        ActivityStatistics activity = service.getActivity(LOCATION_ID, "alice", ActivityGranularity.WEEK);

        assertThat(activity.getBuckets()).hasSize(2);
        assertThat(activity.getBuckets().get(0).getBucketStart().toString()).isEqualTo("2026-03-02");
        assertThat(activity.getBuckets().get(0).getSessionCount()).isEqualTo(2L);
        assertThat(activity.getBuckets().get(1).getBucketStart().toString()).isEqualTo("2026-03-09");
        assertThat(activity.getBuckets().get(1).getSessionCount()).isEqualTo(1L);
    }

    // --- expansions ---

    @Test
    void getMostUsedExpansions_returnsEmptyList_whenNoneRecorded() {
        assertThat(service.getMostUsedExpansions(LOCATION_ID, "alice")).isEmpty();
    }

    // --- contribution calendar ---

    @Test
    void getContributionCalendar_excludesSessionsOlderThanTrailingYear() {
        Instant recent = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant tooOld = Instant.now().minus(400, ChronoUnit.DAYS);
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(recent, null, null),
                sessionTiming(tooOld, null, null)
        ));

        List<DailyActivityResponse> calendar = service.getContributionCalendar(LOCATION_ID, "alice");

        assertThat(calendar).hasSize(1);
        assertThat(calendar.get(0).getSessionCount()).isEqualTo(1L);
    }

    @Test
    void getContributionCalendar_bucketsByLocalCalendarDay_notUtcCalendarDay() {
        // The client sends playedAt as local midnight of the chosen date. During CEST
        // (UTC+2) that instant is still "yesterday" in UTC, so bucketing by UTC would
        // misfile a session logged "today" under yesterday's date (the actual bug).
        ZoneId warsaw = ZoneId.of("Europe/Warsaw");
        LocalDate todayInWarsaw = LocalDate.now(warsaw);
        Instant localMidnight = todayInWarsaw.atStartOfDay(warsaw).toInstant();

        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(localMidnight, null, null)
        ));

        List<DailyActivityResponse> calendar = service.getContributionCalendar(LOCATION_ID, "alice");

        assertThat(calendar).hasSize(1);
        assertThat(calendar.get(0).getDate()).isEqualTo(todayInWarsaw);
    }

    @Test
    void getActivity_bucketsByLocalCalendarDay_notUtcCalendarDay() {
        ZoneId warsaw = ZoneId.of("Europe/Warsaw");
        LocalDate todayInWarsaw = LocalDate.now(warsaw);
        Instant localMidnight = todayInWarsaw.atStartOfDay(warsaw).toInstant();

        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(localMidnight, null, null)
        ));

        ActivityStatistics activity = service.getActivity(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(activity.getBuckets()).hasSize(1);
        assertThat(activity.getBuckets().get(0).getBucketStart()).isEqualTo(todayInWarsaw.withDayOfMonth(1));
    }
}
