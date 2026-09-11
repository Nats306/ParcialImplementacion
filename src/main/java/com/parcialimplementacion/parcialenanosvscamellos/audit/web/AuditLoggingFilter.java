package com.parcialimplementacion.parcialenanosvscamellos.audit.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcialimplementacion.parcialenanosvscamellos.audit.entity.AuditAction;
import com.parcialimplementacion.parcialenanosvscamellos.audit.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registra en el audit log toda operación de escritura exitosa de la API,
 * sin tocar los controllers/services existentes: se ejecuta como un filtro
 * de servlet normal, después de que Spring Security ya autenticó la
 * petición (los filtros @Component sin @Order explícito se registran al
 * final de la cadena, es decir, más cerca del DispatcherServlet — ver
 * FilterRegistrationBean), así que en el momento en que este filtro
 * procesa la respuesta, SecurityContextHolder todavía tiene la
 * autenticación de la petición.
 *
 * Cómo decide qué registrar: método HTTP + ruta contra una tabla fija de
 * reglas (más abajo). El id de la entidad sale de la URL cuando está ahí
 * (PUT/PATCH/DELETE) o del campo "id" del cuerpo de la respuesta (POST).
 *
 * Limitación conocida y aceptada: "previousValue" (estado ANTES del
 * cambio) no se captura — el enunciado del módulo lo marca como opcional.
 * Capturarlo requeriría que este filtro conociera el modelo de dominio de
 * cada módulo (para leerlo de la base de datos antes de aplicar el
 * cambio), lo cual está fuera del alcance de un filtro HTTP genérico.
 * "newValue" sí se guarda, a partir del cuerpo de la petición o de la
 * respuesta.
 */
@Component
public class AuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);

    private record Rule(String method, Pattern uri, AuditAction action, String entityType) {
    }

    private final List<Rule> rules = List.of(
            new Rule("POST", Pattern.compile("^/api/auth/login$"), AuditAction.LOGIN, "Auth"),

            new Rule("POST", Pattern.compile("^/api/competitors$"), AuditAction.CREATE, "Competitor"),
            new Rule("PUT", Pattern.compile("^/api/competitors/([^/]+)$"), AuditAction.UPDATE, "Competitor"),
            new Rule("PATCH", Pattern.compile("^/api/competitors/([^/]+)/status$"), AuditAction.STATUS_CHANGE, "Competitor"),
            new Rule("DELETE", Pattern.compile("^/api/competitors/([^/]+)$"), AuditAction.DELETE, "Competitor"),

            new Rule("POST", Pattern.compile("^/api/teams$"), AuditAction.CREATE, "Team"),
            new Rule("PUT", Pattern.compile("^/api/teams/([^/]+)$"), AuditAction.UPDATE, "Team"),
            new Rule("PATCH", Pattern.compile("^/api/teams/([^/]+)/status$"), AuditAction.STATUS_CHANGE, "Team"),
            new Rule("DELETE", Pattern.compile("^/api/teams/([^/]+)$"), AuditAction.DELETE, "Team"),
            new Rule("POST", Pattern.compile("^/api/teams/([^/]+)/members/([^/]+)$"), AuditAction.ADD_MEMBER, "Team"),
            new Rule("DELETE", Pattern.compile("^/api/teams/([^/]+)/members/([^/]+)$"), AuditAction.REMOVE_MEMBER, "Team"),

            new Rule("POST", Pattern.compile("^/api/races$"), AuditAction.CREATE, "Race"),
            new Rule("PUT", Pattern.compile("^/api/races/([^/]+)$"), AuditAction.UPDATE, "Race"),
            new Rule("PATCH", Pattern.compile("^/api/races/([^/]+)/status$"), AuditAction.STATUS_CHANGE, "Race"),
            new Rule("DELETE", Pattern.compile("^/api/races/([^/]+)$"), AuditAction.DELETE, "Race"),

            new Rule("POST", Pattern.compile("^/api/races/([^/]+)/registrations$"), AuditAction.REGISTER, "Registration"),
            new Rule("PATCH", Pattern.compile("^/api/registrations/([^/]+)/approve$"), AuditAction.APPROVE, "Registration"),
            new Rule("PATCH", Pattern.compile("^/api/registrations/([^/]+)/reject$"), AuditAction.REJECT, "Registration"),
            new Rule("DELETE", Pattern.compile("^/api/registrations/([^/]+)$"), AuditAction.CANCEL, "Registration"),

            new Rule("POST", Pattern.compile("^/api/races/([^/]+)/results$"), AuditAction.CREATE, "Result"),
            new Rule("PUT", Pattern.compile("^/api/results/([^/]+)$"), AuditAction.UPDATE, "Result")
    );

    private final AuditLogService auditLogService;
    // Spring Boot 4 pasó a usar Jackson 3 (tools.jackson.*) como mapper por
    // defecto, así que ya no hay un bean de Spring para el ObjectMapper de
    // Jackson 2 (com.fasterxml.jackson.databind). Como este filtro solo
    // necesita leer JSON de forma genérica (JsonNode), no hace falta pedirlo
    // inyectado: se crea una instancia propia.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLoggingFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo interesan las escrituras; las lecturas no se auditan.
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Spring Framework 7 quitó el constructor de un solo argumento: ahora
        // hay que declarar explícitamente cuántos bytes del body cachear.
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 65536);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            if (isSuccessful(responseWrapper.getStatus())) {
                tryRecord(requestWrapper, responseWrapper);
            }
        } finally {
            // Imprescindible: sin esto el cliente recibe el body vacío.
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean isSuccessful(int status) {
        return status >= 200 && status < 300;
    }

    private void tryRecord(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();

            for (Rule rule : rules) {
                if (!rule.method().equalsIgnoreCase(method)) continue;
                Matcher matcher = rule.uri().matcher(uri);
                if (!matcher.matches()) continue;

                String pathId = matcher.groupCount() >= 1 ? matcher.group(1) : null;
                JsonNode requestBody = readJson(request.getContentAsByteArray());
                JsonNode responseBody = readJson(response.getContentAsByteArray());

                AuditAction action = rule.action();
                String entityId = pathId;
                String username = (action == AuditAction.LOGIN)
                        ? textOrDefault(requestBody, "username", "desconocido")
                        : currentUsername();

                if (entityId == null && responseBody != null && responseBody.hasNonNull("id")) {
                    entityId = responseBody.get("id").asText();
                }

                // "race cancellation" es un caso especial de cambio de estado.
                if ("Race".equals(rule.entityType()) && action == AuditAction.STATUS_CHANGE
                        && "CANCELLED".equalsIgnoreCase(textOrDefault(requestBody, "status", ""))) {
                    action = AuditAction.CANCEL;
                }

                String description = describe(action, rule.entityType(), entityId, username);
                String newValue = firstNonBlank(compact(responseBody), compact(requestBody));

                auditLogService.record(username, action, rule.entityType(), entityId, description, null, newValue);
                return; // una sola regla aplica por request
            }
        } catch (Exception ex) {
            // El audit log nunca debe romper la respuesta real al cliente.
            log.warn("No se pudo registrar audit log para {} {}: {}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "desconocido";
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
        }
        return authentication.getName();
    }

    private JsonNode readJson(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return objectMapper.readTree(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            return null;
        }
    }

    private String textOrDefault(JsonNode node, String field, String fallback) {
        if (node == null || !node.hasNonNull(field)) return fallback;
        return node.get(field).asText(fallback);
    }

    private String compact(JsonNode node) {
        if (node == null) return null;
        String json = node.toString();
        return json.length() > 2000 ? json.substring(0, 2000) : json;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private String describe(AuditAction action, String entityType, String entityId, String username) {
        String subject = entityType + (entityId != null ? " #" + entityId : "");
        return switch (action) {
            case LOGIN -> username + " inició sesión.";
            case CREATE -> username + " creó " + subject + ".";
            case UPDATE -> username + " actualizó " + subject + ".";
            case DELETE -> username + " eliminó " + subject + ".";
            case STATUS_CHANGE -> username + " cambió el estado de " + subject + ".";
            case CANCEL -> username + " canceló " + subject + ".";
            case APPROVE -> username + " aprobó " + subject + ".";
            case REJECT -> username + " rechazó " + subject + ".";
            case REGISTER -> username + " creó una inscripción en " + subject + ".";
            case ADD_MEMBER -> username + " agregó un miembro a " + subject + ".";
            case REMOVE_MEMBER -> username + " quitó un miembro de " + subject + ".";
        };
    }
}
