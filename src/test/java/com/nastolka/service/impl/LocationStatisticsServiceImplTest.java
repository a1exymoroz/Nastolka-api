package com.nastolka.service.impl;

import com.nastolka.dto.ActivityGranularity;
import com.nastolka.dto.LocationStatisticsResponse;
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

    @Test
    void getStatistics_returnsZerosAndEmptyLists_whenNoFinishedSessions() {
        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getOverview().getTotalFinishedSessions()).isZero();
        assertThat(response.getOverview().getTotalPlayTimeMinutes()).isZero();
        assertThat(response.getOverview().getAverageSessionLengthMinutes()).isNull();
        assertThat(response.getOverview().getAverageRating()).isNull();
        assertThat(response.getGameStats().getMostPlayedGames()).isEmpty();
        assertThat(response.getGameStats().getTopRatedGames()).isEmpty();
        assertThat(response.getGameStats().getLibraryCoverage().getCoveragePercentage()).isNull();
        assertThat(response.getPlayerLeaderboard()).isEmpty();
        assertThat(response.getMostActivePlayers()).isEmpty();
        assertThat(response.getActivity().getBuckets()).isEmpty();
        assertThat(response.getMostUsedExpansions()).isEmpty();
        assertThat(response.getContributionCalendar()).isEmpty();
    }

    @Test
    void getStatistics_computesTotalAndAveragePlayTime_fromFinishedSessionDurations() {
        Instant now = Instant.now();
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(now, now.minus(60, ChronoUnit.MINUTES), now),
                sessionTiming(now, now.minus(30, ChronoUnit.MINUTES), now)
        ));

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getOverview().getTotalPlayTimeMinutes()).isEqualTo(90L);
        assertThat(response.getOverview().getAverageSessionLengthMinutes()).isEqualTo(45.0);
    }

    @Test
    void getStatistics_computesLibraryCoveragePercentage() {
        when(locationHistoryRepository.countDistinctGamesPlayed(LOCATION_ID, HistoryState.FINISHED)).thenReturn(2L);
        when(locationGameRepository.countByLocationId(LOCATION_ID)).thenReturn(4L);

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getGameStats().getLibraryCoverage().getCoveragePercentage()).isEqualTo(50.0);
    }

    @Test
    void getStatistics_computesWinRateAndSortsLeaderboardByWinsDescending() {
        PlayerStatsProjection alice = playerStats("alice", 4L, 3L, 30L, 7.5);
        PlayerStatsProjection bob = playerStats("bob", 4L, 1L, 20L, 5.0);
        when(historyPlayerRepository.findPlayerStats(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(bob, alice));

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getPlayerLeaderboard()).extracting("username").containsExactly("alice", "bob");
        assertThat(response.getPlayerLeaderboard().get(0).getWinRatePercentage()).isEqualTo(75.0);
        assertThat(response.getPlayerLeaderboard().get(1).getWinRatePercentage()).isEqualTo(25.0);
    }

    @Test
    void getStatistics_sortsMostActivePlayersByGamesPlayedDescending() {
        PlayerStatsProjection alice = playerStats("alice", 2L, 1L, 10L, 5.0);
        PlayerStatsProjection bob = playerStats("bob", 6L, 1L, 10L, 5.0);
        when(historyPlayerRepository.findPlayerStats(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(alice, bob));

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getMostActivePlayers()).extracting("username").containsExactly("bob", "alice");
    }

    @Test
    void getStatistics_bucketsActivityByCalendarMonth() {
        Instant sessionOne = Instant.parse("2026-03-05T10:00:00Z");
        Instant sessionTwo = Instant.parse("2026-03-20T10:00:00Z");
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(sessionOne, null, null),
                sessionTiming(sessionTwo, null, null)
        ));

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getActivity().getBuckets()).hasSize(1);
        assertThat(response.getActivity().getBuckets().get(0).getBucketStart().toString()).isEqualTo("2026-03-01");
        assertThat(response.getActivity().getBuckets().get(0).getSessionCount()).isEqualTo(2L);
    }

    @Test
    void getStatistics_bucketsActivityByCalendarWeek() {
        Instant monday = Instant.parse("2026-03-02T10:00:00Z");
        Instant wednesdaySameWeek = Instant.parse("2026-03-04T10:00:00Z");
        Instant nextMonday = Instant.parse("2026-03-09T10:00:00Z");
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(monday, null, null),
                sessionTiming(wednesdaySameWeek, null, null),
                sessionTiming(nextMonday, null, null)
        ));

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.WEEK);

        assertThat(response.getActivity().getBuckets()).hasSize(2);
        assertThat(response.getActivity().getBuckets().get(0).getBucketStart().toString()).isEqualTo("2026-03-02");
        assertThat(response.getActivity().getBuckets().get(0).getSessionCount()).isEqualTo(2L);
        assertThat(response.getActivity().getBuckets().get(1).getBucketStart().toString()).isEqualTo("2026-03-09");
        assertThat(response.getActivity().getBuckets().get(1).getSessionCount()).isEqualTo(1L);
    }

    @Test
    void getStatistics_contributionCalendarExcludesSessionsOlderThanTrailingYear() {
        Instant recent = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant tooOld = Instant.now().minus(400, ChronoUnit.DAYS);
        when(locationHistoryRepository.findSessionTimings(LOCATION_ID, HistoryState.FINISHED)).thenReturn(List.of(
                sessionTiming(recent, null, null),
                sessionTiming(tooOld, null, null)
        ));

        LocationStatisticsResponse response = service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH);

        assertThat(response.getContributionCalendar()).hasSize(1);
        assertThat(response.getContributionCalendar().get(0).getSessionCount()).isEqualTo(1L);
    }

    @Test
    void getStatistics_throwsForbidden_whenAccessDenied() {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You do not have access to this location"))
                .when(accessGuard).requireViewAccess(location, user);

        assertThatThrownBy(() -> service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You do not have access to this location");
    }

    @Test
    void getStatistics_throwsNotFound_whenLocationMissing() {
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatistics(LOCATION_ID, "alice", ActivityGranularity.MONTH))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Location not found");
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
}
