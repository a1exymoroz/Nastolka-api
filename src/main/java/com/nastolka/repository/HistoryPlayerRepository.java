package com.nastolka.repository;

import com.nastolka.entity.HistoryPlayer;
import com.nastolka.entity.HistoryState;
import com.nastolka.repository.projection.PlayerStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryPlayerRepository extends JpaRepository<HistoryPlayer, Long> {

    List<HistoryPlayer> findByHistoryIdOrderByPlacementAsc(Long historyId);

    List<HistoryPlayer> findByHistoryIdInOrderByPlacementAsc(List<Long> historyIds);

    void deleteByHistoryId(Long historyId);

    @Query("select p.user.username as username, count(p) as gamesPlayed, " +
            "sum(case when p.placement = 1 then 1L else 0L end) as wins, " +
            "coalesce(sum(p.points), 0) as totalPoints, avg(p.points) as averagePoints " +
            "from HistoryPlayer p where p.history.location.id = :locationId and p.history.state = :state " +
            "group by p.user.id, p.user.username")
    List<PlayerStatsProjection> findPlayerStats(@Param("locationId") Long locationId, @Param("state") HistoryState state);
}
