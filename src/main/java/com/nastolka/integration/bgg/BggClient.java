package com.nastolka.integration.bgg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
public class BggClient {

    private static final long MIN_REQUEST_INTERVAL_MS = 5000;

    private final RestClient restClient;
    private final BggXmlParser xmlParser;
    private final String token;

    private long lastRequestAtMs;

    public BggClient(
            RestClient bggRestClient,
            BggXmlParser xmlParser,
            @Value("${app.bgg.token:}") String token
    ) {
        this.restClient = bggRestClient;
        this.xmlParser = xmlParser;
        this.token = token;
    }

    public List<BggSearchItem> search(String query) {
        String xml = get(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("query", query)
                .queryParam("type", "boardgame")
                .build());
        return xmlParser.parseSearchResults(xml);
    }

    public BggGameDetails getGameDetails(Long bggId) {
        String xml = get(uriBuilder -> uriBuilder
                .path("/thing")
                .queryParam("id", bggId)
                .build());
        return xmlParser.parseGameDetails(xml, bggId);
    }

    private String get(java.util.function.Function<
            org.springframework.web.util.UriBuilder, java.net.URI> uriFunction) {
        ensureTokenConfigured();
        respectRateLimit();

        try {
            return restClient.get()
                    .uri(uriFunction)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(BAD_GATEWAY, "BoardGameGeek API request failed", e);
        }
    }

    private void ensureTokenConfigured() {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "BGG_TOKEN is not configured. Add it to .env.local"
            );
        }
    }

    private synchronized void respectRateLimit() {
        long now = System.currentTimeMillis();
        long waitMs = lastRequestAtMs + MIN_REQUEST_INTERVAL_MS - now;
        if (waitMs > 0) {
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(BAD_GATEWAY, "Interrupted while waiting for BGG rate limit", e);
            }
        }
        lastRequestAtMs = System.currentTimeMillis();
    }
}
