package com.nastolka.service.impl;

import com.nastolka.dto.AuthResponse;
import com.nastolka.dto.LoginRequest;
import com.nastolka.dto.RegisterRequest;
import com.nastolka.entity.User;
import com.nastolka.security.JwtUtil;
import com.nastolka.service.AuthService;
import com.nastolka.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // TODO: Check if username/email already exists and throw appropriate exception
        // TODO: Hash the password before saving (e.g. BCryptPasswordEncoder)
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .build();

        userService.save(user);

        // TODO: Generate JWT token after successful registration
        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // TODO: Look up user by username
        // TODO: Verify password matches (compare hashed password)
        // TODO: Throw exception if credentials are invalid

        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // TODO: Generate JWT token after successful login
        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }
}
