package com.parcialimplementacion.parcialenanosvscamellos.security.dto;

import java.util.List;

/**
 * Lo que el frontend necesita para saber quién inició sesión y qué botones
 * mostrar u ocultar según el rol. Se arma leyendo el JWT ya validado; no se
 * consulta ninguna tabla local (Keycloak es la única fuente de verdad para
 * usuarios y roles).
 */
public record UserProfileResponse(
        String subject,
        String username,
        String email,
        String fullName,
        List<String> roles
) {
}
