package com.nastolka.repository;

import com.nastolka.entity.GameExpansion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameExpansionRepository extends JpaRepository<GameExpansion, Long> {

    List<GameExpansion> findByGameId(Long gameId);

    List<GameExpansion> findByGameIdIn(List<Long> gameIds);

    Optional<GameExpansion> findByIdAndGameId(Long id, Long gameId);

    boolean existsByBggId(Long bggId);
}
