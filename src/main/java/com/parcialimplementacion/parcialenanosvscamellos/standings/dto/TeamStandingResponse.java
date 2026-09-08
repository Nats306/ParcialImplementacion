package com.parcialimplementacion.parcialenanosvscamellos.standings.dto;

public record TeamStandingResponse(
        int rank,
        Long teamId,
        String name,
        int points,
        int victories,
        int secondPlaces,
        int thirdPlaces,
        int racesWithResults
) {
}