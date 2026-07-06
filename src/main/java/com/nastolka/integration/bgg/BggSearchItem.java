package com.nastolka.integration.bgg;

public record BggSearchItem(
        Long bggId,
        String name,
        Integer yearPublished
) {
}
