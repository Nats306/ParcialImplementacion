package com.parcialimplementacion.parcialenanosvscamellos.standings.dto;

import java.util.List;

public record StandingsResponse(
        List<CompetitorStandingResponse> competitors,
        List<TeamStandingResponse> teams
) {
}