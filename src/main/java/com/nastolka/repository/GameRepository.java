package com.nastolka.repository;

import com.nastolka.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByBggId(Long bggId);

    boolean existsByBggId(Long bggId);
}
