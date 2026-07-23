package com.nastolka.repository;

import com.nastolka.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    List<LocationHistory> findByLocationIdOrderByPlayedAtDesc(Long locationId);

    Optional<LocationHistory> findByIdAndLocationId(Long id, Long locationId);
}
