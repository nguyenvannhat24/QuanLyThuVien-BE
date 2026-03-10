package com.dev.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigRequest {

    @NotBlank(message = "Config value không được để trống")
    @Size(max = 500, message = "Config value không được vượt quá 500 ký tự")
    private String configValue;
}
