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
import com.dev.auth.repository.BlacklistTokenRepository;
import com.dev.auth.security.JwtService;
import com.dev.auth.service.AuthService;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực", description = "API xác thực người dùng - đăng ký, đăng nhập, làm mới token, đăng xuất")
public class AuthController {
    private final JwtService jwtService;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final com.dev.auth.repository.RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;
         @Autowired
    private UserRepository userRepository;
    // Endpoint đăng ký
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.REGISTER_SUCCESS, response));
    }
    
    // Endpoint đăng nhập
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.LOGIN_SUCCESS, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.REFRESH_TOKEN_SUCCESS, response));
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
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request
    ) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(MessageConstants.INVALID_TOKEN));
        }

        String token = header.substring(7);

        Date expiry =  jwtService.extractExpiration(token);

        BlacklistToken blacklist = new BlacklistToken();
        blacklist.setToken(token);
        blacklist.setExpiryDate(expiry);

        blacklistTokenRepository.save(blacklist);

        User user = userRepository.findByUsername(jwtService.extractUsername(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);

        return ResponseEntity.ok(ApiResponse.success(MessageConstants.LOGOUT_SUCCESS, "OK"));
    }
}
