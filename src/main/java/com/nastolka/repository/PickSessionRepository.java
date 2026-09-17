package com.nastolka.repository;

import com.nastolka.entity.PickSession;
import com.nastolka.entity.PickSessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickSessionRepository extends JpaRepository<PickSession, Long> {

    Optional<PickSession> findByLocationIdAndStatusIn(Long locationId, List<PickSessionStatus> statuses);

    Optional<PickSession> findByIdAndLocationId(Long id, Long locationId);

    // Serializes concurrent join/start/action/cancel calls on the same session, since unlike chat
    // (append-only) this feature has genuinely contended shared mutable state (turn order, ban count).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PickSession s where s.id = :id and s.location.id = :locationId")
    Optional<PickSession> findByIdAndLocationIdForUpdate(@Param("id") Long id, @Param("locationId") Long locationId);
}
