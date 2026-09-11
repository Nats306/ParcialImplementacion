package com.parcialimplementacion.parcialenanosvscamellos.registration;

import com.parcialimplementacion.parcialenanosvscamellos.race.repository.IRaceRepository;
import com.parcialimplementacion.parcialenanosvscamellos.support.AbstractApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 9 — Testing Requirements:
 *  - Register an active competitor successfully.
 *  - Reject a suspended competitor.
 *  - Reject a duplicated registration.
 *  - Reject registration after the deadline.
 */
class RegistrationApiTest extends AbstractApiTest {

    @Autowired
    private IRaceRepository raceRepository;

    private String createCompetitor(String nickname) throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Reg Competitor",
                "nickname", nickname,
                "competitorType", "DWARF",
                "age", 25,
                "weight", 65.0,
                "height", 1.5,
                "country", "Colombia"
        );
        String response = mockMvc.perform(post("/api/competitors")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createOpenRace(String name) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", "Carrera de prueba");
        body.put("scheduledDateTime", futureDateTime(10));
        body.put("startLocation", "A");
        body.put("finishLocation", "B");
        body.put("distanceMeters", 1000);
        body.put("maxParticipants", 10);
        body.put("raceType", "INDIVIDUAL");
        body.put("organizer", "Test Organizer");
        body.put("registrationDeadline", futureDateTime(9));

        String response = mockMvc.perform(post("/api/races")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        String raceId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/api/races/" + raceId + "/status")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "OPEN_FOR_REGISTRATION"))))
                .andExpect(status().is2xxSuccessful());

        return raceId;
    }

    private Map<String, Object> registrationBody(String competitorId, int position) {
        return Map.of(
                "competitorId", competitorId,
                "startingPosition", position,
                "registeredBy", "test-organizer"
        );
    }

    @Test
    void registersAnActiveCompetitorSuccessfully() throws Exception {
        String competitorId = createCompetitor(unique("active"));
        String raceId = createOpenRace(unique("OpenRace"));

        mockMvc.perform(post("/api/races/" + raceId + "/registrations")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody(competitorId, 1))))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void rejectsASuspendedCompetitor() throws Exception {
        String competitorId = createCompetitor(unique("suspended"));
        String raceId = createOpenRace(unique("OpenRace2"));

        mockMvc.perform(patch("/api/competitors/" + competitorId + "/status")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "SUSPENDED"))))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post("/api/races/" + raceId + "/registrations")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody(competitorId, 1))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void rejectsADuplicatedRegistration() throws Exception {
        String competitorId = createCompetitor(unique("dup"));
        String raceId = createOpenRace(unique("OpenRace3"));

        mockMvc.perform(post("/api/races/" + raceId + "/registrations")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody(competitorId, 1))))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post("/api/races/" + raceId + "/registrations")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody(competitorId, 2))))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsRegistrationAfterTheDeadline() throws Exception {
        String competitorId = createCompetitor(unique("late"));
        String raceId = createOpenRace(unique("OpenRace4"));

        // La API no permite crear una carrera con deadline en el pasado (se
        // valida como fecha futura en la creación), así que para probar esta
        // regla se adelanta el reloj moviendo el deadline al pasado
        // directamente en la base de datos, simulando que ya venció.
        var race = raceRepository.findById(Long.valueOf(raceId)).orElseThrow();
        race.setRegistrationDeadline(LocalDateTime.now().minusDays(1));
        raceRepository.save(race);

        mockMvc.perform(post("/api/races/" + raceId + "/registrations")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody(competitorId, 1))))
                .andExpect(status().is4xxClientError());
    }
}
