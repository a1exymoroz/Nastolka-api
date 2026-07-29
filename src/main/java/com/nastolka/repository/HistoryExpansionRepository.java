package com.nastolka.repository;

import com.nastolka.entity.HistoryExpansion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryExpansionRepository extends JpaRepository<HistoryExpansion, Long> {

    List<HistoryExpansion> findByHistoryId(Long historyId);

    List<HistoryExpansion> findByHistoryIdIn(List<Long> historyIds);

    void deleteByHistoryId(Long historyId);
}
