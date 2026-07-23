package com.nastolka.repository;

import com.nastolka.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByOwnerId(Long ownerId);

    long countByOwnerId(Long ownerId);
}
