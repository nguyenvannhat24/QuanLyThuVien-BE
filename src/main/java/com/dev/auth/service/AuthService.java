package com.dev.auth.service;

import org.springframework.http.ResponseEntity;

import com.dev.auth.dto.AuthResponse;
import com.dev.auth.dto.LoginRequest;
import com.dev.auth.dto.RefreshTokenRequest;
import com.dev.auth.dto.RegisterRequest;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);

    ResponseEntity<?> login(LoginRequest request);

    /**
     * Refresh an access token by providing a valid refresh token.
     */
    ResponseEntity<?> refreshToken(RefreshTokenRequest request);

}
