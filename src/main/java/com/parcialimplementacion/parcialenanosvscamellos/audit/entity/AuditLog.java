package com.parcialimplementacion.parcialenanosvscamellos.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Una entrada del audit log (Módulo 8). Se crea exclusivamente desde
 * {@link com.parcialimplementacion.parcialenanosvscamellos.audit.service.AuditLogService#record},
 * a su vez invocado por {@link com.parcialimplementacion.parcialenanosvscamellos.audit.web.AuditLoggingFilter}
 * después de cada operación exitosa que modifica el sistema.
 *
 * previousValue queda deliberadamente sin usar (el enunciado lo marca como
 * opcional): capturarlo requeriría leer el estado anterior de cada entidad
 * antes de aplicar el cambio, lo cual está fuera del alcance de un filtro
 * HTTP genérico que no conoce el modelo de dominio de cada módulo.
 */
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                // Los cuatro filtros que expone AuditController.list(...):
                // username, entityType, action y el orden por defecto
                // (timestamp desc).
                @Index(name = "idx_audit_username", columnList = "username"),
                @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id", length = 60)
    private String entityId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String description;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
}
