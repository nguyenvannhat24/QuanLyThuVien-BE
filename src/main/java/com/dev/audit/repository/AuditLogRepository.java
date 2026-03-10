package com.dev.audit.repository;

import com.dev.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.actor WHERE a.timestamp BETWEEN :start AND :end")
    List<AuditLog> findByTimestampBetweenWithActor(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
