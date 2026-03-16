package com.dev.audit.controller;

import com.dev.audit.dto.AuditLogResponse;
import com.dev.audit.entity.AuditLog;
import com.dev.audit.service.AuditLogService;
import com.dev.constant.MessageConstants;
import com.dev.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "API xem lịch sử hoạt động của admin")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get audit logs", description = "Lấy danh sách audit logs với phân trang")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogs(startDate, endDate, pageRequest);
        
        Page<AuditLogResponse> response = auditLogs.map(log -> AuditLogResponse.builder()
                .auditId(log.getAuditId())
                .actorUsername(log.getActor() != null ? log.getActor().getUsername() : "System")
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build());
        
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID", description = "Lấy chi tiết một audit log")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.OPERATION_SUCCESS, null));
    }
}