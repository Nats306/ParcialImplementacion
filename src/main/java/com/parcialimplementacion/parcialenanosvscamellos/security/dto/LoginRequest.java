package com.parcialimplementacion.parcialenanosvscamellos.security.dto;

import jakarta.validation.constraints.NotBlank;

/** Credenciales para pedirle un token a Keycloak (grant type "password"). */
public record LoginRequest(

        @NotBlank(message = "El usuario no puede estar vacío")
        String username,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}
