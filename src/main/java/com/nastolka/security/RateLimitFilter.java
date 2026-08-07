package com.nastolka.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private record Rule(String group, RequestMatcher matcher, int capacity, int refillTokens, Duration refillPeriod) {}

    private final List<Rule> rules;
    private final Cache<String, Bucket> buckets;

    public RateLimitFilter(
            @Value("${app.rate-limit.auth.capacity}") int authCapacity,
            @Value("${app.rate-limit.auth.refill-tokens}") int authRefillTokens,
            @Value("${app.rate-limit.auth.refill-period-seconds}") long authRefillSeconds,
            @Value("${app.rate-limit.bgg.capacity}") int bggCapacity,
            @Value("${app.rate-limit.bgg.refill-tokens}") int bggRefillTokens,
            @Value("${app.rate-limit.bgg.refill-period-seconds}") long bggRefillSeconds
    ) {
        RequestMatcher authMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher("/api/auth/login", "POST"),
                new AntPathRequestMatcher("/api/auth/register", "POST"));
        RequestMatcher bggMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher("/api/games/search-external", "GET"),
                new AntPathRequestMatcher("/api/games/import/*", "POST"),
                new AntPathRequestMatcher("/api/games/*/expansions/import/*", "POST"));

        this.rules = List.of(
                new Rule("auth", authMatcher, authCapacity, authRefillTokens, Duration.ofSeconds(authRefillSeconds)),
                new Rule("bgg", bggMatcher, bggCapacity, bggRefillTokens, Duration.ofSeconds(bggRefillSeconds)));
        this.buckets = Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        for (Rule rule : rules) {
            if (rule.matcher().matches(request)) {
                String bucketKey = rule.group() + ":" + clientIp(request);
                Bucket bucket = buckets.get(bucketKey, key -> newBucket(rule));
                if (!bucket.tryConsume(1)) {
                    response.setStatus(429);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                    return;
                }
                break;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Bucket newBucket(Rule rule) {
        Bandwidth limit = Bandwidth.classic(rule.capacity(), Refill.greedy(rule.refillTokens(), rule.refillPeriod()));
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientIp(HttpServletRequest request) {
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
