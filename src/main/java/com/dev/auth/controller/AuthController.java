package com.dev.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.auth.dto.AuthResponse;
import com.dev.auth.dto.LoginRequest;
import com.dev.auth.dto.RegisterRequest;
import com.dev.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    // Endpoint đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }
    
    // Endpoint đăng nhập
    @PostMapping("/login")

    public ResponseEntity<?> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}
