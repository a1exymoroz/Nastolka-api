package com.nastolka.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String BGG_SEARCH_CACHE = "bggSearch";
    public static final String BGG_GAME_DETAILS_CACHE = "bggGameDetails";
    public static final String GAMES_LIST_CACHE = "gamesList";

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.bgg-search.ttl-minutes}") long bggSearchTtlMinutes,
            @Value("${app.cache.bgg-game-details.ttl-minutes}") long bggGameDetailsTtlMinutes,
            @Value("${app.cache.games-list.ttl-minutes}") long gamesListTtlMinutes
    ) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(BGG_SEARCH_CACHE, bggSearchTtlMinutes, 200),
                buildCache(BGG_GAME_DETAILS_CACHE, bggGameDetailsTtlMinutes, 500),
                buildCache(GAMES_LIST_CACHE, gamesListTtlMinutes, 1)
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, long ttlMinutes, long maximumSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .build());
    }
}
