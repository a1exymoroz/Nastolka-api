package com.nastolka.integration.bgg;

import java.util.List;

public record BggGameDetails(
        Long bggId,
        String name,
        String description,
        String photo,
        Integer minPlayers,
        Integer maxPlayers,
        Integer minPlayTime,
        Integer maxPlayTime,
        boolean expansion,
        List<BggExpansionLink> expansions
) {
}
