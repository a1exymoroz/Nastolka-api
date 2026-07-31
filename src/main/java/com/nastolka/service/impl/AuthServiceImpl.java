package com.nastolka.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.nastolka.dto.AuthResponse;
import com.nastolka.dto.GoogleLoginRequest;
import com.nastolka.dto.LoginRequest;
import com.nastolka.dto.RegisterRequest;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.security.GoogleTokenVerifier;
import com.nastolka.security.JwtUtil;
import com.nastolka.service.AuthService;
import com.nastolka.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthServiceImpl(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                            GoogleTokenVerifier googleTokenVerifier) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Username already exists");
        }
        if (userService.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        userService.save(user);

        String token = jwtUtil.generateToken(user.getUsername());


        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // TODO: Generate JWT token after successful login
        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token"));

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email not verified");
        }

        String sub = payload.getSubject();
        String email = payload.getEmail();

        User user = userService.findByGoogleSub(sub)
                .orElseGet(() -> resolveByEmailOrCreate(sub, email));

        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    private User resolveByEmailOrCreate(String sub, String email) {
        try {
            Optional<User> byEmail = userService.findByEmail(email);
            if (byEmail.isPresent()) {
                User existing = byEmail.get();
                if (existing.getGoogleSub() != null && !existing.getGoogleSub().equals(sub)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Account already linked to a different Google identity");
                }
                existing.setGoogleSub(sub);
                return userService.save(existing);
            }

            User newUser = User.builder()
                    .username(generateUniqueUsername(email))
                    .email(email)
                    .googleSub(sub)
                    .role(Role.USER)
                    .build();
            return userService.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account conflict, please retry", e);
        }
    }

    private String generateUniqueUsername(String email) {
        String localPart = email.substring(0, email.indexOf('@'));
        String sanitized = localPart.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        if (sanitized.length() < 3) {
            sanitized = "user" + sanitized;
        }

        String candidate = sanitized;
        int suffix = 1;
        while (userService.existsByUsername(candidate)) {
            candidate = sanitized + suffix;
            suffix++;
        }
        return candidate;
    }
}
