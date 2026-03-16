# Phase 8: Quản Trị Viên

## Tổng Quan

**Ngày bắt đầu:** 16/03/2026  
**Dự kiến hoàn thành:** 16/03/2026  
**Mục tiêu:** Triển khai các tính năng quản trị nâng cao  
**Branch:** `phase-8`

---

## Mục Tiêu Chi Tiết

1. Quản lý người dùng nâng cao (block/unblock, reset password)
2. Cấu hình hệ thống qua API
3. Xem và tìm kiếm audit logs

---

## Hiện Trạng

### Module Admin đã có:
- `AdminUserController.java` - Quản lý người dùng cơ bản
- `AdminUserService.java` và `AdminUserServiceImpl.java`
- `AuditLog`, `AuditLogRepository`, `AuditLogService` (backend đã có)

### Tính năng cần thêm:
- Block/unblock users
- Reset password
- Xem audit logs qua API

---

## Tính Năng Cần Triển Khai

### 1. Quản Lý Người Dùng Nâng Cao

Cập nhật `AdminUserService` và `AdminUserController`:

```java
// Methods cần thêm:
void blockUser(Long userId);
void unblockUser(Long userId);
void resetPassword(Long userId);
void changeUserRole(Long userId, Role newRole);
```

**Endpoints mới:**
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/admin/users/{id}/block` | Khóa tài khoản user |
| POST | `/api/admin/users/{id}/unblock` | Mở khóa tài khoản user |
| POST | `/api/admin/users/{id}/reset-password` | Đặt lại mật khẩu |
| PUT | `/api/admin/users/{id}/role` | Thay đổi role user |

---

### 2. Cấu Hình Hệ Thống

Cải thiện `SystemConfigController`:
- Thêm create/update config
- Thêm delete config
- Validate config key/value

---

### 3. Audit Logs Viewer

Tạo `AuditLogController`:

```java
@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Return paginated audit logs
    }
}
```

**Endpoints:**
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/admin/audit-logs` | Xem danh sách audit logs |
| GET | `/api/admin/audit-logs/{id}` | Xem chi tiết audit log |

---

## Files Cần Tạo Mới

### Controller:
1. `src/main/java/com/dev/audit/controller/AuditLogController.java`

### DTO:
2. `src/main/java/com/dev/audit/dto/AuditLogResponse.java`

---

## Files Cần Sửa Đổi

1. **AdminUserService.java** - Thêm methods block/unblock/reset password
2. **AdminUserServiceImpl.java** - Implement các methods mới
3. **AdminUserController.java** - Thêm endpoints mới
4. **SystemConfigController.java** - Cải thiện CRUD

---

## Error Handling

1. **User not found**: Trả về 404
2. **Cannot block admin**: Trả về 400 Bad Request
3. **Cannot change own role**: Trả về 400
4. **Invalid role**: Validate role trước khi thay đổi

---

## Security Considerations

1. Chỉ ADMIN mới có thể truy cập các endpoints
2. Không cho phép block chính mình
3. Không cho phép thay đổi role của chính mình
4. Reset password nên gửi email thông báo (sử dụng EmailService đã có)

---

## Estimated Time

- User Management nâng cao: 20 phút
- System Config cải thiện: 10 phút
- Audit Log Controller: 15 phút
- Testing & Fixes: 10 phút

**Tổng:** ~55 phút

---

## Next Steps

Hoàn thành Phase 8, các tính năng tiềm năng cho tương lai:

1. **Web Dashboard**: Frontend cho admin
2. **Real-time Notifications**: WebSocket
3. **API Rate Limiting**: Bảo mật API
4. **Multi-language**: Hỗ trợ nhiều ngôn ngữ

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-16  
**Author:** Development Team