package com.dev.auth.service;

import java.util.Date;
import java.util.List;

import java.util.Optional;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    @Override
    public ResponseEntity register(RegisterRequest request) {
        // 1.Check username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
            .status(HttpStatus.CONFLICT)
                    .body("{\"message\": \"Username đã tồn tại!\"}");
        }

        // 2.Check email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
            .status(HttpStatus.CONFLICT)
                    .body("{\"message\": \"Email đã tồn tại!\"}");
        }

        // 3.Tạo User mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setAge(request.getAge());

        // 4.Lưu vào database
        User savedUser = userRepository.save(user);

        // 5.Trả về AuthResponse
        AuthResponse response = new AuthResponse();
        response.setId(String.valueOf(savedUser.getId()));
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    }
@Override
public ResponseEntity<?> login(LoginRequest request) {

    if(request.getUsername() == null || request.getPassword() == null) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("{\"message\": \"Username và password không được để trống!\"}");
    }
    // 1. Xác thực bằng AuthenticationManager (chuẩn Spring Security)
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            )
    );

    // 2. Lấy user từ database
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));

    // 3. Tạo JWT token và refresh token
    String token = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    // 3a. Lưu refresh token vào database (xóa token cũ nếu có)
    refreshTokenRepository.deleteByUser(user);
    RefreshToken model = new RefreshToken();
    model.setUser(user);
    model.setToken(refreshToken);
    model.setExpiryDate(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
    refreshTokenRepository.save(model);

    // 4. Trả về response
    AuthResponse response = new AuthResponse();
    response.setId(String.valueOf(user.getId()));
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setToken(token);
    response.setRefreshToken(refreshToken);

    return ResponseEntity.ok(response);
}
    
    @Override
    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"Refresh token không được để trống\"}");
        }

        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(request.getRefreshToken());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Refresh token không hợp lệ\"}");
        }

        RefreshToken stored = optional.get();
        if (stored.getExpiryDate().before(new Date())) {
            // token expired, xoá khỏi database
            refreshTokenRepository.delete(stored);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Refresh token đã hết hạn\"}");
        }

        // tìm user tương ứng
        User user = userRepository.findById(stored.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // tạo token mới
        String newToken = jwtService.generateToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);

        // cập nhật refresh token trong db
        stored.setToken(newRefresh);
        stored.setExpiryDate(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
        refreshTokenRepository.save(stored);

        AuthResponse resp = new AuthResponse();
        resp.setId(String.valueOf(user.getId()));
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setToken(newToken);
        resp.setRefreshToken(newRefresh);

        return ResponseEntity.ok(resp);
    }

}
