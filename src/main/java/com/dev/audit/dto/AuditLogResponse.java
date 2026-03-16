package com.dev.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long auditId;
    private String actorUsername;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private LocalDateTime timestamp;
}