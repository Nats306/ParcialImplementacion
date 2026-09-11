package com.parcialimplementacion.parcialenanosvscamellos.audit.controller;

import com.parcialimplementacion.parcialenanosvscamellos.audit.dto.AuditLogResponse;
import com.parcialimplementacion.parcialenanosvscamellos.audit.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo lectura: las entradas se crean únicamente desde AuditLoggingFilter.
 *
 * La restricción a ADMINISTRATOR para todo /api/audit/** ya está declarada
 * en SecurityConfig (ver README, sección Security → Endpoint authorization),
 * así que este controller no repite esa autorización.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Page<AuditLogResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action
    ) {
        return auditLogService.list(page, size, sortBy, direction, username, entityType, action);
    }
}
