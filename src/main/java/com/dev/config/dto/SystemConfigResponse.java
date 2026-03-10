package com.dev.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {

    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
