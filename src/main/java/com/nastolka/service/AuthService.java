package com.nastolka.service;

import com.nastolka.dto.AuthResponse;
import com.nastolka.dto.LoginRequest;
import com.nastolka.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
