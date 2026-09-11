package com.parcialimplementacion.parcialenanosvscamellos.audit.dto;

import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String username,
        AuditAction action,
        String entityType,
        String entityId,
        LocalDateTime timestamp,
        String description,
        String previousValue,
        String newValue
) {
}
