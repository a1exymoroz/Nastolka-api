package com.nastolka.repository;

import com.nastolka.entity.LocationShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationShareRepository extends JpaRepository<LocationShare, Long> {

    List<LocationShare> findByLocationId(Long locationId);

    List<LocationShare> findByUserId(Long userId);

    Optional<LocationShare> findByLocationIdAndUserId(Long locationId, Long userId);

    boolean existsByLocationIdAndUserId(Long locationId, Long userId);
}
