package com.dev.config.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SystemConfig entity - Quản lý cấu hình động của hệ thống
 * 
 * Các config key quan trọng:
 * - default_borrow_days: Số ngày mượn mặc định (VD: 14)
 * - max_renew_count: Số lần gia hạn tối đa (VD: 2)
 * - fine_per_day: Phạt trễ hạn mỗi ngày (VD: 5000 VND)
 * - reservation_hold_days: Số ngày giữ sách đặt trước (VD: 3)
 * - max_borrow_per_reader: Số sách tối đa Reader có thể mượn cùng lúc (VD: 5)
 */
@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 100)
    @NotBlank(message = "Config key không được để trống")
    @Size(max = 100, message = "Config key không được vượt quá 100 ký tự")
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 500)
    @NotBlank(message = "Config value không được để trống")
    @Size(max = 500, message = "Config value không được vượt quá 500 ký tự")
    private String configValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
