package com.parcialimplementacion.parcialenanosvscamellos.audit.specification;

import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditAction;
import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> withFilters(String username, String entityType, String action) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (username != null && !username.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("username")), username.toLowerCase()));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("entityType")), entityType.toLowerCase()));
            }
            if (action != null && !action.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("action"), AuditAction.valueOf(action.toUpperCase())));
                } catch (IllegalArgumentException ignored) {
                    // acción desconocida: no se agrega el filtro, se listan todas
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
