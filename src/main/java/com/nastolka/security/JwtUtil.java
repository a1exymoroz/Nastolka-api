package com.nastolka.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(String username) {
        // TODO: Implement JWT token creation
        // Hint: use io.jsonwebtoken (Jwts.builder()) with secretKey and expirationMs
        // Include username as a claim (e.g. "sub" or custom claim)
        return "[...]";
    }

    public String extractUsername(String token) {
        // TODO: Parse the JWT and extract the username claim
        return "[...]";
    }

    public boolean validateToken(String token, String username) {
        // TODO: Verify token signature, expiration, and that username matches
        return false;
    }
}
