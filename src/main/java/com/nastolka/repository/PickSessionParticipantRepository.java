package com.nastolka.repository;

import com.nastolka.entity.PickSessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickSessionParticipantRepository extends JpaRepository<PickSessionParticipant, Long> {

    List<PickSessionParticipant> findBySessionId(Long sessionId);

    List<PickSessionParticipant> findBySessionIdOrderByTurnOrderAsc(Long sessionId);

    Optional<PickSessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
}
