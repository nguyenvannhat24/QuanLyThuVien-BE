package com.dev.audit.service;

import com.dev.audit.entity.AuditLog;
import com.dev.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {
    
    void logAction(User actor, String action, String entityType, Long entityId, String details);
    
    Page<AuditLog> getAuditLogs(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
