package com.dev.admin.controller;

import com.dev.admin.service.AdminUserService;
import com.dev.audit.annotation.AdminAction;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Quản lý người dùng (Admin)", description = "API quản lý người dùng cho admin - khóa, mở khóa, thay đổi vai trò")
public class AdminUserController {
    
    private final AdminUserService adminUserService;

    @PutMapping("/{id}/lock")
    @AdminAction("LOCK_USER")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id) {
        adminUserService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Đã khóa tài khoản người dùng thành công", null));
    }

    @PutMapping("/{id}/unlock")
    @AdminAction("UNLOCK_USER")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        adminUserService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Đã mở khóa tài khoản người dùng thành công", null));
    }

    @PutMapping("/{id}/role")
    @AdminAction("CHANGE_USER_ROLE")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        adminUserService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Đã thay đổi vai trò người dùng thành công", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
}
