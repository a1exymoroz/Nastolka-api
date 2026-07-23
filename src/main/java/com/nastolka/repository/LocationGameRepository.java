package com.nastolka.repository;

import com.nastolka.entity.LocationGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationGameRepository extends JpaRepository<LocationGame, Long> {

    List<LocationGame> findByLocationId(Long locationId);

    Optional<LocationGame> findByLocationIdAndGameId(Long locationId, Long gameId);

    boolean existsByLocationIdAndGameId(Long locationId, Long gameId);
}
