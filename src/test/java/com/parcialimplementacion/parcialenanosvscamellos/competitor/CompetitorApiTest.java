package com.parcialimplementacion.parcialenanosvscamellos.competitor;

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
 *  - Create a valid competitor.
 *  - Reject a competitor with invalid weight.
 *  - Reject a duplicated nickname.
 */
class CompetitorApiTest extends AbstractApiTest {

    private Map<String, Object> validCompetitorBody(String nickname) {
        return Map.of(
                "name", "Test Competitor",
                "nickname", nickname,
                "competitorType", "DWARF",
                "age", 30,
                "weight", 70.0,
                "height", 1.6,
                "country", "Colombia"
        );
    }

    @Test
    void createsAValidCompetitor() throws Exception {
        String nickname = unique("nick");

        mockMvc.perform(post("/api/competitors")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCompetitorBody(nickname))))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.competitorType").value("DWARF"));
    }

    @Test
    void rejectsACompetitorWithInvalidWeight() throws Exception {
        Map<String, Object> body = new HashMap<>(validCompetitorBody(unique("nick")));
        body.put("weight", -5.0);

        mockMvc.perform(post("/api/competitors")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsADuplicatedNickname() throws Exception {
        String nickname = unique("dup-nick");

        mockMvc.perform(post("/api/competitors")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCompetitorBody(nickname))))
                .andExpect(status().is2xxSuccessful());

        // Mismo nickname, otro nombre: debe rechazarse por duplicado.
        Map<String, Object> secondBody = new HashMap<>(validCompetitorBody(nickname));
        secondBody.put("name", "Another Competitor");

        mockMvc.perform(post("/api/competitors")
                        .with(asAdministrator())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondBody)))
                .andExpect(status().isConflict());
    }
}
