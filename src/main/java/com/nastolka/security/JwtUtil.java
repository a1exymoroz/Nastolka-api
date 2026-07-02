package com.nastolka.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secretKey, @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Optional<String> validateAndExtractUsername(String token) {
        try {
            Claims claims = parseClaimsJws(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public String extractUsername(String token) {
        return parseClaimsJws(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        return validateAndExtractUsername(token)
                .filter(u -> username.equals(u))
                .isPresent();
    }

    public boolean isTokenValid(String token) {
        return validateAndExtractUsername(token).isPresent();
    }

    private Claims parseClaimsJws(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
