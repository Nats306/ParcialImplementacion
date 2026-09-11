package com.parcialimplementacion.parcialenanosvscamellos.security;

import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BadRequestException;
import com.parcialimplementacion.parcialenanosvscamellos.security.dto.LoginRequest;
import com.parcialimplementacion.parcialenanosvscamellos.security.dto.RefreshRequest;
import com.parcialimplementacion.parcialenanosvscamellos.security.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Puerta de entrada de autenticación pensada para el frontend: en vez de
 * hacerlo hablar directamente con Keycloak (que le obligaría a conocer su
 * URL, el nombre del realm y el clientId), el frontend solo le manda
 * usuario/contraseña a esta API y esta hace de proxy hacia el endpoint de
 * token de Keycloak.
 *
 * <p>Esta app NO crea usuarios ni guarda contraseñas: los tres usuarios de
 * prueba (admin, organizer, viewer) ya vienen sembrados por
 * {@code keycloak/realm-export.json}. Crear usuarios nuevos es una tarea de
 * administración de Keycloak (consola de administración o su API), no de
 * este backend.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RestClient restClient = RestClient.create();
    private final String tokenUrl;
    private final String clientId;

    public AuthController(@Value("${keycloak.internal-url}") String keycloakInternalUrl,
                           @Value("${keycloak.realm}") String realm,
                           @Value("${keycloak.client-id}") String clientId) {
        this.tokenUrl = keycloakInternalUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        this.clientId = clientId;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión contra Keycloak y devuelve el access/refresh token")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", request.username());
        form.add("password", request.password());
        return ResponseEntity.ok(requestToken(form));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Cambia un refresh token vigente por un access token nuevo")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("refresh_token", request.refreshToken());
        return ResponseEntity.ok(requestToken(form));
    }

    @GetMapping("/profile")
    @Operation(summary = "Devuelve la identidad y los roles del usuario autenticado")
    public ResponseEntity<UserProfileResponse> profile(@AuthenticationPrincipal Jwt jwt) {
        String fullName = jwt.getClaimAsString("name");
        if (fullName == null) {
            String given = jwt.getClaimAsString("given_name");
            String family = jwt.getClaimAsString("family_name");
            fullName = (given != null ? given : "") + (family != null ? " " + family : "");
            fullName = fullName.isBlank() ? jwt.getClaimAsString("preferred_username") : fullName.trim();
        }

        UserProfileResponse response = new UserProfileResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                fullName,
                realmRoles(jwt)
        );
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private static List<String> realmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream().map(Object::toString).toList();
    }

    private Map<String, Object> requestToken(MultiValueMap<String, String> form) {
        try {
            return restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            // Keycloak responde 400/401 con un JSON propio ("invalid_grant",
            // etc.) cuando el usuario, la contraseña o el refresh token no
            // son válidos. Se traduce a la excepción de negocio del proyecto
            // para que salga con el mismo formato de error que el resto de
            // la API en vez del cuerpo crudo de Keycloak.
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST || ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BadRequestException("Usuario, contraseña o refresh token inválidos");
            }
            throw ex;
        }
    }
}
