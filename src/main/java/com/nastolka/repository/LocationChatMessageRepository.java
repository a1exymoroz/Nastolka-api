package com.nastolka.repository;

import com.nastolka.entity.LocationChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationChatMessageRepository extends JpaRepository<LocationChatMessage, Long> {

    List<LocationChatMessage> findByLocationIdOrderByCreatedAtAsc(Long locationId);
}
