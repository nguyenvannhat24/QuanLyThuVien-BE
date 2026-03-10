package com.dev.admin.controller;

import com.dev.admin.service.AdminUserService;
import com.dev.audit.annotation.AdminAction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    private final AdminUserService adminUserService;

    @PutMapping("/{id}/lock")
    @AdminAction("LOCK_USER")
    public ResponseEntity<Map<String, String>> lockUser(@PathVariable Long id) {
        adminUserService.lockUser(id);
        return ResponseEntity.ok(Map.of("message", "Đã khóa tài khoản người dùng thành công"));
    }

    @PutMapping("/{id}/unlock")
    @AdminAction("UNLOCK_USER")
    public ResponseEntity<Map<String, String>> unlockUser(@PathVariable Long id) {
        adminUserService.unlockUser(id);
        return ResponseEntity.ok(Map.of("message", "Đã mở khóa tài khoản người dùng thành công"));
    }

    @PutMapping("/{id}/role")
    @AdminAction("CHANGE_USER_ROLE")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        adminUserService.changeUserRole(id, role);
        return ResponseEntity.ok(Map.of("message", "Đã thay đổi vai trò người dùng thành công"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
