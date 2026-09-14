package com.nastolka.repository;

import com.nastolka.entity.HistoryState;
import com.nastolka.entity.LocationHistory;
import com.nastolka.repository.projection.GamePlayCountProjection;
import com.nastolka.repository.projection.GameRatingProjection;
import com.nastolka.repository.projection.SessionTimingProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    List<LocationHistory> findByLocationIdOrderByPlayedAtDesc(Long locationId);

    Optional<LocationHistory> findByIdAndLocationId(Long id, Long locationId);

    long countByLocationIdAndState(Long locationId, HistoryState state);

    @Query("select avg(h.rating) from LocationHistory h " +
            "where h.location.id = :locationId and h.state = :state and h.rating is not null")
    Double findAverageRating(@Param("locationId") Long locationId, @Param("state") HistoryState state);

    @Query("select count(distinct h.game.id) from LocationHistory h " +
            "where h.location.id = :locationId and h.state = :state")
    long countDistinctGamesPlayed(@Param("locationId") Long locationId, @Param("state") HistoryState state);

    @Query("select h.startedAt as startedAt, h.finishedAt as finishedAt, h.playedAt as playedAt from LocationHistory h " +
            "where h.location.id = :locationId and h.state = :state")
    List<SessionTimingProjection> findSessionTimings(@Param("locationId") Long locationId, @Param("state") HistoryState state);

    @Query("select h.game.id as gameId, h.game.name as gameName, count(h) as playCount from LocationHistory h " +
            "where h.location.id = :locationId and h.state = :state " +
            "group by h.game.id, h.game.name order by count(h) desc")
    List<GamePlayCountProjection> findMostPlayedGames(@Param("locationId") Long locationId, @Param("state") HistoryState state, Pageable pageable);

    @Query("select h.game.id as gameId, h.game.name as gameName, avg(h.rating) as averageRating, count(h) as ratingCount " +
            "from LocationHistory h where h.location.id = :locationId and h.state = :state and h.rating is not null " +
            "group by h.game.id, h.game.name having count(h) >= :minSamples order by avg(h.rating) desc")
    List<GameRatingProjection> findTopRatedGames(@Param("locationId") Long locationId, @Param("state") HistoryState state,
                                                  @Param("minSamples") long minSamples, Pageable pageable);
}
