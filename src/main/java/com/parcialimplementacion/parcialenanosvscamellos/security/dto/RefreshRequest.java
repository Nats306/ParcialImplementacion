package com.parcialimplementacion.parcialenanosvscamellos.security.dto;

import jakarta.validation.constraints.NotBlank;

/** Refresh token para pedirle un access token nuevo a Keycloak sin volver a pedir contraseña. */
public record RefreshRequest(

        @NotBlank(message = "El refresh token no puede estar vacío")
        String refreshToken
) {
}
