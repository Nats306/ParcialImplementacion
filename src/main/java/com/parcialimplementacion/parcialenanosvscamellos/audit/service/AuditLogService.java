package com.parcialimplementacion.parcialenanosvscamellos.audit.service;

import com.parcialimplementacion.parcialenanosvscamellos.audit.dto.AuditLogResponse;
import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditAction;
import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditLog;
import com.parcialimplementacion.parcialenanosvscamellos.audit.mapper.AuditLogMapper;
import com.parcialimplementacion.parcialenanosvscamellos.audit.repository.IAuditLogRepository;
import com.parcialimplementacion.parcialenanosvscamellos.audit.specification.AuditLogSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final IAuditLogRepository repository;
    private final AuditLogMapper mapper;

    public AuditLogService(IAuditLogRepository repository, AuditLogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Guarda una entrada nueva. Se llama desde AuditLoggingFilter después de
     * que la operación real ya se ejecutó y respondió con éxito; nunca debe
     * lanzar una excepción que rompa la respuesta original (el filtro ya la
     * envuelve en try/catch, pero se mantiene simple igual acá).
     */
    public void record(String username, AuditAction action, String entityType, String entityId,
                        String description, String previousValue, String newValue) {
        AuditLog entry = AuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .description(description)
                .previousValue(previousValue)
                .newValue(newValue)
                .build();
        repository.save(entry);
    }

    public Page<AuditLogResponse> list(int page, int size, String sortBy, String direction,
                                        String username, String entityType, String action) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy == null || sortBy.isBlank()) ? "timestamp" : sortBy;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sortField));

        return repository
                .findAll(AuditLogSpecification.withFilters(username, entityType, action), pageRequest)
                .map(mapper::toResponse);
    }
}
