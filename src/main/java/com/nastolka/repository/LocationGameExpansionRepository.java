package com.nastolka.repository;

import com.nastolka.entity.LocationGameExpansion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationGameExpansionRepository extends JpaRepository<LocationGameExpansion, Long> {

    List<LocationGameExpansion> findByLocationGameId(Long locationGameId);

    List<LocationGameExpansion> findByLocationGameIdIn(List<Long> locationGameIds);

    Optional<LocationGameExpansion> findByLocationGameIdAndExpansionId(Long locationGameId, Long expansionId);

    boolean existsByLocationGameIdAndExpansionId(Long locationGameId, Long expansionId);
}
