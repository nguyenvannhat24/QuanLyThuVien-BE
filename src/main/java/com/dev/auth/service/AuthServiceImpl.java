package com.dev.auth.service;

import java.util.Date;
import java.util.List;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.auth.dto.AuthResponse;
import com.dev.auth.dto.LoginRequest;
import com.dev.auth.dto.RefreshTokenRequest;
import com.dev.auth.dto.RegisterRequest;
import com.dev.auth.model.RefreshToken;
import com.dev.auth.repository.RefreshTokenRepository;
import com.dev.auth.security.JwtService;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username already exists - {}", request.getUsername());
            throw new RuntimeException("Username đã tồn tại!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists - {}", request.getEmail());
            throw new RuntimeException("Email đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setGender(request.getGender());
        user.setAge(request.getAge());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        AuthResponse response = new AuthResponse();
        response.setId(String.valueOf(savedUser.getId()));
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());

        return response;
        
    }
@Override
public AuthResponse login(LoginRequest request) {
    log.info("Login attempt for username: {}", request.getUsername());

    if(request.getUsername() == null || request.getPassword() == null) {
        log.warn("Login failed: missing username or password");
        throw new RuntimeException("Username và password không được để trống!");
    }
    
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
    } catch (Exception e) {
        log.error("Authentication failed for username: {}", request.getUsername(), e);
        throw e;
    }

    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));

    String token = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    refreshTokenRepository.deleteByUser(user);
    RefreshToken model = new RefreshToken();
    model.setUser(user);
    model.setToken(refreshToken);
    model.setExpiryDate(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
    refreshTokenRepository.save(model);

    log.info("User logged in successfully: {}", user.getUsername());

    AuthResponse response = new AuthResponse();
    response.setId(String.valueOf(user.getId()));
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setToken(token);
    response.setRefreshToken(refreshToken);

    return response;
}
    
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null) {
            log.warn("Refresh token request missing token");
            throw new RuntimeException("Refresh token không được để trống");
        }

        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(request.getRefreshToken());
        if (optional.isEmpty()) {
            log.warn("Invalid refresh token provided");
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        RefreshToken stored = optional.get();
        if (stored.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(stored);
            log.warn("Expired refresh token attempted");
            throw new RuntimeException("Refresh token đã hết hạn");
        }

        User user = userRepository.findById(stored.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        String newToken = jwtService.generateToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);

        stored.setToken(newRefresh);
        stored.setExpiryDate(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
        refreshTokenRepository.save(stored);

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        AuthResponse resp = new AuthResponse();
        resp.setId(String.valueOf(user.getId()));
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setToken(newToken);
        resp.setRefreshToken(newRefresh);

        return resp;
    }

}
