package com.parcialimplementacion.parcialenanosvscamellos.standings.dto;

import java.util.UUID;

public record CompetitorStandingResponse(
        int rank,
        UUID competitorId,
        String name,
        String nickname,
        int points,
        int victories,
        int secondPlaces,
        int thirdPlaces,
        int racesWithResults
) {
}