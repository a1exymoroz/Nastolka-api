package com.nastolka.repository;

import com.nastolka.entity.HistoryPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryPlayerRepository extends JpaRepository<HistoryPlayer, Long> {

    List<HistoryPlayer> findByHistoryIdOrderByPlacementAsc(Long historyId);

    void deleteByHistoryId(Long historyId);
}
