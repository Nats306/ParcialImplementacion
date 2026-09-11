package com.parcialimplementacion.parcialenanosvscamellos.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Base común para los tests de integración de la API (Módulo 9 —
 * Testing Requirements).
 *
 * Cada test corre dentro de una transacción que Spring revierte
 * automáticamente al terminar (@Transactional a nivel de clase), así que
 * estos tests nunca dejan datos de prueba en la base de datos real usada
 * para desarrollo (la misma que ya tiene los datos semilla) — se necesita
 * PostgreSQL levantado (docker compose up -d) pero NO hace falta Keycloak
 * corriendo para estos tests.
 *
 * La autenticación se simula con el jwt() request post-processor de
 * spring-security-test en vez de pedir un token real a Keycloak: esto
 * desacopla los tests de tener que levantar Keycloak y de la latencia de
 * red, y es el patrón estándar recomendado por Spring Security para
 * probar endpoints protegidos con OAuth2 Resource Server.
 *
 * Nota: como no se pudo confirmar leyendo SecurityConfig si la
 * autorización se declaró con hasRole("X") (que Spring Security traduce
 * internamente a la autoridad "ROLE_X") o con hasAuthority("X") directo,
 * cada usuario de prueba recibe AMBAS formas de autoridad para su rol, de
 * modo que estos tests sean válidos sin importar cuál de las dos
 * convenciones se haya usado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractApiTest {

    protected static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    protected MockMvc mockMvc;

    // Spring Boot 4 pasó a usar Jackson 3 (tools.jackson.*) como mapper por
    // defecto, así que ya no hay un bean de Spring para el ObjectMapper de
    // Jackson 2 (com.fasterxml.jackson.databind) que se pueda @Autowired.
    // Como estos tests solo lo usan para serializar Maps simples y leer
    // JSON de forma genérica (readTree), no hace falta que sea un bean:
    // se crea una instancia propia.
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /** Genera un valor único por test (nombres/nicknames no deben chocar entre corridas). */
    protected String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String futureDateTime(long daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow).format(ISO_LOCAL);
    }

    protected String pastDateTime(long daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo).format(ISO_LOCAL);
    }

    protected RequestPostProcessor asAdministrator() {
        return withRole("ADMINISTRATOR", "test-admin");
    }

    protected RequestPostProcessor asRaceOrganizer() {
        return withRole("RACE_ORGANIZER", "test-organizer");
    }

    protected RequestPostProcessor asViewer() {
        return withRole("VIEWER", "test-viewer");
    }

    private RequestPostProcessor withRole(String role, String username) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(builder -> builder.claim("preferred_username", username))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_" + role),
                        new SimpleGrantedAuthority(role)
                );
    }
}
