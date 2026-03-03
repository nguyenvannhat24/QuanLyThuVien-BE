package com.dev.auth.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.auth.dto.AuthResponse;
import com.dev.auth.dto.LoginRequest;
import com.dev.auth.dto.RefreshTokenRequest;
import com.dev.auth.dto.RegisterRequest;
import com.dev.auth.model.BlacklistToken;
import com.dev.auth.model.User;
import com.dev.auth.repository.BlacklistTokenRepository;
import com.dev.auth.repository.UserRepository;
import com.dev.auth.security.JwtService;
import com.dev.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final com.dev.auth.repository.RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;
         @Autowired
    private UserRepository userRepository;
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

    // Endpoint làm mới token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

          @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        String username = authentication.getName();
        System.out.println("Username from token: " + authentication.getName());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(null); // không trả password

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
public ResponseEntity<?> logout(
        HttpServletRequest request
) {

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
        return ResponseEntity.badRequest().body("Invalid token");
    }

    String token = header.substring(7);

    Date expiry =  jwtService.extractExpiration(token);

    BlacklistToken blacklist = new BlacklistToken();
    blacklist.setToken(token);
    blacklist.setExpiryDate(expiry);

    blacklistTokenRepository.save(blacklist);

    refreshTokenRepository.deleteByUsername(
            jwtService.extractUsername(token)
    );

    return ResponseEntity.ok("Logged out successfully");
}
}
