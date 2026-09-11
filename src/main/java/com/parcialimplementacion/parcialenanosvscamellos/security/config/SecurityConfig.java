package com.parcialimplementacion.parcialenanosvscamellos.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Seguridad de la API, basada en Keycloak como proveedor de identidad.
 *
 * <p>Esta app no emite tokens ni guarda contraseñas: eso es trabajo de
 * Keycloak. Acá solo se valida el token que llega en la cabecera
 * {@code Authorization: Bearer ...} (firma, emisor y vencimiento) y se
 * decide qué puede hacer quien lo trae.</p>
 *
 * <p>Los tres roles del realm y sus permisos (ver también
 * {@code keycloak/realm-export.json}):</p>
 * <ul>
 *   <li>{@code ADMINISTRATOR} — gestiona usuarios, competidores, equipos,
 *       carreras, inscripciones, resultados y el log de auditoría.</li>
 *   <li>{@code RACE_ORGANIZER} — gestiona carreras, inscripciones y
 *       resultados; solo puede leer competidores y equipos.</li>
 *   <li>{@code VIEWER} — solo lectura de información pública (competidores,
 *       equipos, carreras, resultados, standings).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Lo único que se puede ver sin token: la documentación, el healthcheck
     * que miran Docker/Compose, y /error (si no está, un 401 se convierte en
     * un 500 al intentar renderizar el error). El login y el refresh también
     * quedan públicos porque son ellos los que consiguen el token.
     */
    private static final String[] RUTAS_PUBLICAS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/error",
            "/api/auth/login",
            "/api/auth/refresh"
    };

    /**
     * Competidores y equipos: cualquier rol autenticado puede leer, pero
     * solo un administrador puede crear, editar, cambiar de estado o borrar.
     */
    private static final String[] RUTAS_COMPETIDORES_EQUIPOS = {
            "/api/competitors/**",
            "/api/teams/**"
    };

    /**
     * Carreras, inscripciones y resultados: cualquier rol autenticado puede
     * leer; administradores y organizadores pueden escribir. Standings es
     * de solo lectura para todos los roles autenticados.
     */
    private static final String[] RUTAS_OPERACION_CARRERAS = {
            "/api/races/**",
            "/api/registrations/**",
            "/api/results/**"
    };

    private static final String[] RUTAS_STANDINGS = {
            "/api/standings/**"
    };

    /** Módulo 8 (log de auditoría): reservado, solo administradores. */
    private static final String[] RUTAS_AUDITORIA = {
            "/api/audit/**"
    };

    /** Dirección de Keycloak alcanzable desde dentro de la red de Docker. */
    @Value("${keycloak.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Emisor tal como queda escrito en el token ({@code iss}): es la URL
     * pública de Keycloak (la que ve el navegador/el cliente), no la interna
     * de Docker. Por eso NO se usa {@code spring.security.oauth2.
     * resourceserver.jwt.issuer-uri} (que exige poder resolver esa misma URL
     * desde dentro del contenedor para el "well-known"): se arma el
     * {@link JwtDecoder} a mano, bajando las llaves por la URL interna y
     * validando el emisor contra la URL pública.
     */
    @Value("${keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        return http
                // Sin sesión no hay cookie de sesión, y sin cookie no hay
                // ataque CSRF posible: cada petición se autentica sola con
                // su token.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.GET, RUTAS_STANDINGS).hasAnyRole("ADMINISTRATOR", "RACE_ORGANIZER", "VIEWER")
                        .requestMatchers(HttpMethod.GET, RUTAS_COMPETIDORES_EQUIPOS).hasAnyRole("ADMINISTRATOR", "RACE_ORGANIZER", "VIEWER")
                        // Todo lo que no sea GET bajo competidores/equipos
                        // (POST, PUT, PATCH, DELETE) cae acá: escribir es
                        // cosa exclusiva de administradores.
                        .requestMatchers(RUTAS_COMPETIDORES_EQUIPOS).hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, RUTAS_OPERACION_CARRERAS).hasAnyRole("ADMINISTRATOR", "RACE_ORGANIZER", "VIEWER")
                        // Carreras, inscripciones y resultados: escribir es
                        // cosa de administradores y organizadores.
                        .requestMatchers(RUTAS_OPERACION_CARRERAS).hasAnyRole("ADMINISTRATOR", "RACE_ORGANIZER")
                        .requestMatchers(RUTAS_AUDITORIA).hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/auth/profile").authenticated()
                        // Regla de cierre. Sin ella, cualquier ruta que no
                        // esté en la lista de arriba (una nueva, /actuator/...,
                        // lo que sea) queda abierta sin que nadie se entere.
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(this::handleUnauthorized)
                        .accessDeniedHandler(this::handleForbidden))
                .exceptionHandling(handling -> handling.accessDeniedHandler(this::handleForbidden))
                .build();
    }

    /**
     * Baja las llaves públicas por la URL interna de Docker (alcanzable
     * desde este contenedor) pero valida el claim {@code iss} contra la URL
     * pública que Keycloak realmente escribe en el token. Sin este truco,
     * {@code issuer-uri} automático falla apenas Keycloak vive en su propio
     * contenedor y el backend en otro.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    /**
     * Traductor de roles. Keycloak los manda dentro del claim
     * {@code realm_access.roles} tal como se llaman en el realm
     * ("ADMINISTRATOR", "RACE_ORGANIZER", "VIEWER"); Spring los busca como
     * authorities con prefijo ("ROLE_ADMINISTRATOR"...). Sin esta
     * traducción {@code hasRole(...)} nunca da verdadero y todo responde 403.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::rolesDelRealm);
        return converter;
    }

    private static Collection<GrantedAuthority> rolesDelRealm(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        // Un token sin realm_access es válido: es un usuario sin ningún rol
        // asignado. Se devuelve lista vacía y las reglas de acceso lo
        // rechazan solas con un 403, en vez de reventar con
        // NullPointerException.
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority(
                        "ROLE_" + rol.toString().toUpperCase(Locale.ROOT)))
                .toList();
    }

    /**
     * Orígenes desde donde el frontend puede llamar la API (el GUI web
     * corre en otro puerto). Configurable por variable de entorno para no
     * tener que tocar código cuando cambie el puerto del frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /**
     * 401: falta el token o no es válido (vencido, mal firmado, etc). Se
     * arma a mano en el mismo formato que {@code GlobalExceptionHandler}
     * (timestamp, status, error, message, path) porque este punto se
     * ejecuta ANTES de que la petición llegue a un controller, así que el
     * manejador de excepciones normal nunca la ve.
     */
    private void handleUnauthorized(HttpServletRequest request,
                                     jakarta.servlet.http.HttpServletResponse response,
                                     org.springframework.security.core.AuthenticationException ex) throws IOException {
        writeError(response, request, HttpStatus.UNAUTHORIZED,
                "Se requiere un token de autenticación válido para acceder a este recurso.");
    }

    /** 403: el token es válido pero el rol no tiene permiso para esta acción. */
    private void handleForbidden(HttpServletRequest request,
                                  jakarta.servlet.http.HttpServletResponse response,
                                  org.springframework.security.access.AccessDeniedException ex) throws IOException {
        writeError(response, request, HttpStatus.FORBIDDEN,
                "No tienes permisos suficientes para realizar esta acción.");
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse response,
                            HttpServletRequest request,
                            HttpStatus status,
                            String message) throws IOException {
        response.setStatus(status.value());
        // Content-Type solo (sin setCharacterEncoding) deja que el servlet
        // container use ISO-8859-1 por defecto, y ahí se rompen las tildes.
        // Con MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8" queda
        // explícito.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
