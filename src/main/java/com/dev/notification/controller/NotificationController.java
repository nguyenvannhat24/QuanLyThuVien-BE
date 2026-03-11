package com.dev.notification.controller;

import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import com.dev.notification.dto.NotificationResponse;
import com.dev.notification.service.NotificationService;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Thông báo", description = "API quản lý thông báo - xem và đánh dấu đã đọc")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<NotificationResponse> notifications = notificationService.getMyNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, notifications));
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, null));
    }
}
