package com.dev.auth.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.auth.dto.AuthResponse;
import com.dev.auth.dto.LoginRequest;
import com.dev.auth.dto.RegisterRequest;
import com.dev.auth.model.User;
import com.dev.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        // 1.Tìm user theo username
        List<User> users = userRepository.findByUsername(request.getUsername());
        
        // 2.Nếu không tìm thấy
        if (users.isEmpty()) {
            return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("{\"message\": \"Username hoặc password không đúng!\"}");
        }

        User user = users.get(0);

        // 3.Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Username hoặc password không đúng!\"}");
        }

        // 4.Trả về AuthResponse
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());

        return ResponseEntity.ok(response);

    }
    
}
