package com.nastolka.repository;

import com.nastolka.entity.HistoryExpansion;
import com.nastolka.entity.HistoryState;
import com.nastolka.repository.projection.ExpansionCountProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryExpansionRepository extends JpaRepository<HistoryExpansion, Long> {

    List<HistoryExpansion> findByHistoryId(Long historyId);

    List<HistoryExpansion> findByHistoryIdIn(List<Long> historyIds);

    void deleteByHistoryId(Long historyId);

    @Query("select e.expansion.id as expansionId, e.expansion.name as expansionName, count(e) as useCount " +
            "from HistoryExpansion e where e.history.location.id = :locationId and e.history.state = :state " +
            "group by e.expansion.id, e.expansion.name order by count(e) desc")
    List<ExpansionCountProjection> findMostUsedExpansions(@Param("locationId") Long locationId, @Param("state") HistoryState state, Pageable pageable);
}
