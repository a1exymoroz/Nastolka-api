package com.nastolka.repository;

import com.nastolka.entity.HistoryState;
import com.nastolka.entity.HistoryVote;
import com.nastolka.repository.projection.GameRatingProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryVoteRepository extends JpaRepository<HistoryVote, Long> {

    Optional<HistoryVote> findByHistoryIdAndUserId(Long historyId, Long userId);

    List<HistoryVote> findByHistoryIdOrderByCreatedAtAsc(Long historyId);

    List<HistoryVote> findByHistoryIdInOrderByCreatedAtAsc(List<Long> historyIds);

    @Query("select avg(v.score) from HistoryVote v " +
            "where v.history.location.id = :locationId and v.history.state = :state")
    Double findAverageRating(@Param("locationId") Long locationId, @Param("state") HistoryState state);

    @Query("select v.history.game.id as gameId, v.history.game.name as gameName, " +
            "avg(v.score) as averageRating, count(v) as ratingCount " +
            "from HistoryVote v where v.history.location.id = :locationId and v.history.state = :state " +
            "group by v.history.game.id, v.history.game.name having count(v) >= :minSamples order by avg(v.score) desc")
    List<GameRatingProjection> findTopRatedGames(@Param("locationId") Long locationId, @Param("state") HistoryState state,
                                                  @Param("minSamples") long minSamples, Pageable pageable);
}
