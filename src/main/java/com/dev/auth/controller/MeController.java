package com.dev.auth.controller;

import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.auth.dto.ChangePasswordRequest;
import com.dev.auth.dto.UpdateProfileRequest;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Hồ sơ người dùng", description = "API hồ sơ người dùng - xem, cập nhật thông tin và đổi mật khẩu")
public class MeController {
      @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
      @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(Authentication authentication) {

        String username = authentication.getName();
        System.out.println("Username from token: " + authentication.getName());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(null); // không trả password

        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, user));
    }

    @PutMapping("/update")
     public ResponseEntity<ApiResponse<User>> updateProfile(
        Authentication authentication,
        @RequestBody UpdateProfileRequest request) {

    String username = authentication.getName();

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (request.getEmail() != null) {
        user.setEmail(request.getEmail());
    }

    if (request.getAge() != null) {
        user.setAge(request.getAge());
    }

    if (request.getGender() != null) {
        user.setGender(request.getGender());
    }

    userRepository.save(user);

    user.setPassword(null);

    return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, user));
}
@PutMapping("/change-password")
public ResponseEntity<ApiResponse<Void>> changePassword(
        Authentication authentication,
        @RequestBody ChangePasswordRequest request) {

    String username = authentication.getName();

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Mật khẩu cũ không đúng"));
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
}
}
