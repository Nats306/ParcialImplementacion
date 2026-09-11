package com.parcialimplementacion.parcialenanosvscamellos.security;

import com.parcialimplementacion.parcialenanosvscamellos.support.AbstractApiTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 9 — Testing Requirements:
 *  - Prevent a viewer from creating a race.
 *  - Allow an administrator to create a race.
 *  - Return 401 without a valid token.
 *  - Return 404 for a missing resource.
 */
class SecurityApiTest extends AbstractApiTest {

    private Map<String, Object> validRaceBody(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", "Carrera de prueba de seguridad");
        body.put("scheduledDateTime", futureDateTime(10));
        body.put("startLocation", "A");
        body.put("finishLocation", "B");
        body.put("distanceMeters", 1000);
        body.put("maxParticipants", 10);
        body.put("raceType", "INDIVIDUAL");
        body.put("organizer", "Test Organizer");
        body.put("registrationDeadline", futureDateTime(9));
        return body;
    }

    @Test
    void preventsAViewerFromCreatingARace() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(asViewer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRaceBody(unique("ViewerRace")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAnAdministratorToCreateARace() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRaceBody(unique("AdminRace")))))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void returns401WithoutAValidToken() throws Exception {
        mockMvc.perform(get("/api/competitors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns404ForAMissingResource() throws Exception {
        mockMvc.perform(get("/api/competitors/00000000-0000-0000-0000-000000000000")
                        .with(asAdministrator()))
                .andExpect(status().isNotFound());
    }
}
