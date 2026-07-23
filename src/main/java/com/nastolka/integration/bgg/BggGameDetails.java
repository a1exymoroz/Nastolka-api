package com.nastolka.integration.bgg;

public record BggGameDetails(
        Long bggId,
        String name,
        String description,
        String photo,
        Integer minPlayers,
        Integer maxPlayers,
        Integer minPlayTime,
        Integer maxPlayTime
) {
}
