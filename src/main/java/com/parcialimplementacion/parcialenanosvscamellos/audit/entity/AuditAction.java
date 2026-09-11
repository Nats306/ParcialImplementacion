package com.parcialimplementacion.parcialenanosvscamellos.audit.entity;

/**
 * Tipos de acción que puede registrar el audit log. Se derivan del método
 * HTTP + la ruta de cada request en {@link com.parcialimplementacion.parcialenanosvscamellos.audit.web.AuditLoggingFilter}.
 */
public enum AuditAction {
    LOGIN,
    CREATE,
    UPDATE,
    DELETE,
    STATUS_CHANGE,
    CANCEL,
    APPROVE,
    REJECT,
    REGISTER,
    ADD_MEMBER,
    REMOVE_MEMBER
}
