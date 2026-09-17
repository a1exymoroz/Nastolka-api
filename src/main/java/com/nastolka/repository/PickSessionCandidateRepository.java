package com.nastolka.repository;

import com.nastolka.entity.PickSessionCandidate;
import com.nastolka.entity.PickSessionCandidateAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickSessionCandidateRepository extends JpaRepository<PickSessionCandidate, Long> {

    List<PickSessionCandidate> findBySessionId(Long sessionId);

    Optional<PickSessionCandidate> findBySessionIdAndGameId(Long sessionId, Long gameId);

    long countBySessionIdAndAction(Long sessionId, PickSessionCandidateAction action);
}
