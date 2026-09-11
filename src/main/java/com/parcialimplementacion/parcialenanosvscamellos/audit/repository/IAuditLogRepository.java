package com.parcialimplementacion.parcialenanosvscamellos.audit.repository;

import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IAuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
}
