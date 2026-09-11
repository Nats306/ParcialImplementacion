package com.parcialimplementacion.parcialenanosvscamellos.race;

import com.parcialimplementacion.parcialenanosvscamellos.support.AbstractApiTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 9 — Testing Requirements:
 *  - Create a valid race.
 *  - Reject a race scheduled in the past.
 */
class RaceApiTest extends AbstractApiTest {

    private Map<String, Object> validRaceBody(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", "Carrera de prueba automatizada");
        body.put("scheduledDateTime", futureDateTime(10));
        body.put("startLocation", "Universidad EIA");
        body.put("finishLocation", "Alto de Las Palmas");
        body.put("distanceMeters", 1000);
        body.put("maxParticipants", 10);
        body.put("raceType", "INDIVIDUAL");
        body.put("organizer", "Test Organizer");
        body.put("registrationDeadline", futureDateTime(9));
        return body;
    }

    @Test
    void createsAValidRace() throws Exception {
        mockMvc.perform(post("/api/races")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRaceBody(unique("Race")))))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.raceStatus").value("DRAFT"));
    }

    @Test
    void rejectsARaceScheduledInThePast() throws Exception {
        Map<String, Object> body = validRaceBody(unique("PastRace"));
        body.put("scheduledDateTime", pastDateTime(1));
        body.put("registrationDeadline", pastDateTime(2));

        mockMvc.perform(post("/api/races")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
