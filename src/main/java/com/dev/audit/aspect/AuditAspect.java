package com.dev.audit.aspect;

import com.dev.audit.annotation.AdminAction;
import com.dev.audit.service.AuditLogService;
import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Around("@annotation(adminAction)")
    public Object auditAdminAction(ProceedingJoinPoint joinPoint, AdminAction adminAction) throws Throwable {
        // Extract authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found for admin action");
            return joinPoint.proceed();
        }
        
        String username = authentication.getName();
        User actor = userRepository.findByUsername(username)
                .orElse(null);
        
        if (actor == null) {
            log.warn("User not found: {}", username);
            return joinPoint.proceed();
        }
        
        // Extract method information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String action = adminAction.value().isEmpty() ? method.getName() : adminAction.value();
        
        // Extract parameters
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        
        // Try to extract entity info from parameters
        String entityType = "Unknown";
        Long entityId = null;
        
        for (int i = 0; i < parameterValues.length; i++) {
            Object param = parameterValues[i];
            
            if (param instanceof Long) {
                entityId = (Long) param;
            } else if (param instanceof Integer) {
                entityId = ((Integer) param).longValue();
            } else if (param != null && parameterNames[i].toLowerCase().contains("id")) {
                try {
                    Method getIdMethod = param.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(param);
                    if (id instanceof Long) {
                        entityId = (Long) id;
                    } else if (id instanceof Integer) {
                        entityId = ((Integer) id).longValue();
                    }
                } catch (Exception e) {
                    log.debug("Could not extract ID from parameter: {}", parameterNames[i]);
                }
            }
        }
        
        // Build details
        String details = buildDetails(parameterNames, parameterValues);
        
        try {
            // Execute the actual method
            Object result = joinPoint.proceed();
            
            // Log the action after successful execution
            auditLogService.logAction(actor, action, entityType, entityId, details);
            
            log.info("Audit log created: user={}, action={}, entityType={}, entityId={}", 
                    username, action, entityType, entityId);
            
            return result;
        } catch (Exception e) {
            // Re-throw exception without logging (method failed)
            throw e;
        }
    }
    
    private String buildDetails(String[] parameterNames, Object[] parameterValues) {
        Map<String, String> detailsMap = new HashMap<>();
        
        for (int i = 0; i < parameterNames.length; i++) {
            Object value = parameterValues[i];
            String valueStr = value != null ? value.toString() : "null";
            
            // Avoid logging sensitive data
            if (parameterNames[i].toLowerCase().contains("password")) {
                valueStr = "***";
            }
            
            detailsMap.put(parameterNames[i], valueStr);
        }
        
        try {
            return objectMapper.writeValueAsString(detailsMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit details", e);
            return detailsMap.toString();
        }
    }
}
