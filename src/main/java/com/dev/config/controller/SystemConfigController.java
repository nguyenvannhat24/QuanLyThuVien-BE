package com.dev.config.controller;

import com.dev.audit.annotation.AdminAction;
import com.dev.config.dto.SystemConfigRequest;
import com.dev.config.dto.SystemConfigResponse;
import com.dev.config.model.SystemConfig;
import com.dev.config.service.SystemConfigService;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Cấu hình hệ thống", description = "API quản lý cấu hình hệ thống - xem và cập nhật cấu hình")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        List<SystemConfigResponse> response = configs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getConfig(@PathVariable String key) {
        SystemConfig config = systemConfigService.getConfig(key);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, toResponse(config)));
    }

    @PutMapping("/{key}")
    @AdminAction("UPDATE_SYSTEM_CONFIG")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody SystemConfigRequest request) {
        SystemConfig updated = systemConfigService.updateConfig(key, request.getConfigValue());
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, toResponse(updated)));
    }

    private SystemConfigResponse toResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
