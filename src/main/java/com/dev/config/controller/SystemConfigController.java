package com.dev.config.controller;

import com.dev.config.dto.SystemConfigRequest;
import com.dev.config.dto.SystemConfigResponse;
import com.dev.config.model.SystemConfig;
import com.dev.config.service.SystemConfigService;
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
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<List<SystemConfigResponse>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        List<SystemConfigResponse> response = configs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{key}")
    public ResponseEntity<SystemConfigResponse> getConfig(@PathVariable String key) {
        SystemConfig config = systemConfigService.getConfig(key);
        return ResponseEntity.ok(toResponse(config));
    }

    @PutMapping("/{key}")
    public ResponseEntity<SystemConfigResponse> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody SystemConfigRequest request) {
        SystemConfig updated = systemConfigService.updateConfig(key, request.getConfigValue());
        return ResponseEntity.ok(toResponse(updated));
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
