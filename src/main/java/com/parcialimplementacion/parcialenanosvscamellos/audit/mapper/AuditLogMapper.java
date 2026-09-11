package com.parcialimplementacion.parcialenanosvscamellos.audit.mapper;

import com.parcialimplementacion.parcialenanosvscamellos.audit.dto.AuditLogResponse;
import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getTimestamp(),
                entity.getDescription(),
                entity.getPreviousValue(),
                entity.getNewValue()
        );
    }
}
