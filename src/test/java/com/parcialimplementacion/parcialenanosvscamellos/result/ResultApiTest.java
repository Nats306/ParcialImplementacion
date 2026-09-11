package com.parcialimplementacion.parcialenanosvscamellos.result;

import com.parcialimplementacion.parcialenanosvscamellos.support.AbstractApiTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 9 — Testing Requirements:
 *  - Record a valid result.
 *  - Reject two winners in one race.
 */
class ResultApiTest extends AbstractApiTest {

    private record RaceSetup(String raceId, String registrationId1, String registrationId2) {
    }

    private String createCompetitor(String nickname) throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Result Competitor",
                "nickname", nickname,
                "competitorType", "CAMEL",
                "age", 8,
                "weight", 450.0,
                "height", 2.1,
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

    private String createRace(String name) throws Exception {
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
        return objectMapper.readTree(response).get("id").asText();
    }

    private void changeRaceStatus(String raceId, String newStatus) throws Exception {
        mockMvc.perform(patch("/api/races/" + raceId + "/status")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", newStatus))))
                .andExpect(status().is2xxSuccessful());
    }

    private String registerAndApprove(String raceId, String competitorId, int position) throws Exception {
        Map<String, Object> registration = Map.of(
                "competitorId", competitorId,
                "startingPosition", position,
                "registeredBy", "test-organizer"
        );
        String response = mockMvc.perform(post("/api/races/" + raceId + "/registrations")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        String registrationId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/api/registrations/" + registrationId + "/approve")
                        .with(asAdministrator()))
                .andExpect(status().is2xxSuccessful());

        return registrationId;
    }

    /** Deja una carrera IN_PROGRESS con dos inscripciones aprobadas (mínimo exigido para poder iniciarla). */
    private RaceSetup setUpInProgressRaceWithTwoApprovedRegistrations(String raceName) throws Exception {
        String raceId = createRace(raceName);
        changeRaceStatus(raceId, "OPEN_FOR_REGISTRATION");

        String competitor1 = createCompetitor(unique("res1"));
        String competitor2 = createCompetitor(unique("res2"));
        String registrationId1 = registerAndApprove(raceId, competitor1, 1);
        String registrationId2 = registerAndApprove(raceId, competitor2, 2);

        changeRaceStatus(raceId, "CLOSED_FOR_REGISTRATION");
        changeRaceStatus(raceId, "IN_PROGRESS");

        return new RaceSetup(raceId, registrationId1, registrationId2);
    }

    @Test
    void recordsAValidResult() throws Exception {
        RaceSetup setup = setUpInProgressRaceWithTwoApprovedRegistrations(unique("ResultRace"));

        Map<String, Object> resultBody = Map.of(
                "registrationId", Long.valueOf(setup.registrationId1()),
                "resultStatus", "FINISHED",
                "finalPosition", 1,
                "completionTimeMillis", 60000,
                "penaltyTimeMillis", 0,
                "notes", "Resultado de prueba",
                "recordedBy", "test-organizer"
        );

        mockMvc.perform(post("/api/races/" + setup.raceId() + "/results")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultBody)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.points").value(10));
    }

    @Test
    void rejectsTwoWinnersInOneRace() throws Exception {
        RaceSetup setup = setUpInProgressRaceWithTwoApprovedRegistrations(unique("TwoWinnersRace"));

        Map<String, Object> firstResult = Map.of(
                "registrationId", Long.valueOf(setup.registrationId1()),
                "resultStatus", "FINISHED",
                "finalPosition", 1,
                "completionTimeMillis", 60000,
                "penaltyTimeMillis", 0,
                "notes", "Ganador 1",
                "recordedBy", "test-organizer"
        );
        mockMvc.perform(post("/api/races/" + setup.raceId() + "/results")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstResult)))
                .andExpect(status().is2xxSuccessful());

        Map<String, Object> secondResult = Map.of(
                "registrationId", Long.valueOf(setup.registrationId2()),
                "resultStatus", "FINISHED",
                "finalPosition", 1,
                "completionTimeMillis", 61000,
                "penaltyTimeMillis", 0,
                "notes", "Ganador 2 (no debería permitirse)",
                "recordedBy", "test-organizer"
        );
        mockMvc.perform(post("/api/races/" + setup.raceId() + "/results")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondResult)))
                .andExpect(status().isConflict());
    }
}
