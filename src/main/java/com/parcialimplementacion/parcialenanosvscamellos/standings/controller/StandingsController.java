package com.parcialimplementacion.parcialenanosvscamellos.standings.controller;

import com.parcialimplementacion.parcialenanosvscamellos.standings.dto.CompetitorStandingResponse;
import com.parcialimplementacion.parcialenanosvscamellos.standings.dto.StandingsResponse;
import com.parcialimplementacion.parcialenanosvscamellos.standings.dto.TeamStandingResponse;
import com.parcialimplementacion.parcialenanosvscamellos.standings.service.StandingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(
            StandingsService standingsService
    ) {
        this.standingsService = standingsService;
    }

    @GetMapping
    public ResponseEntity<StandingsResponse>
    getStandings() {

        return ResponseEntity.ok(
                standingsService.getStandings()
        );
    }

    @GetMapping("/competitors")
    public ResponseEntity<List<CompetitorStandingResponse>>
    getCompetitorStandings() {

        return ResponseEntity.ok(
                standingsService
                        .getCompetitorStandings()
        );
    }

    @GetMapping("/teams")
    public ResponseEntity<List<TeamStandingResponse>>
    getTeamStandings() {

        return ResponseEntity.ok(
                standingsService
                        .getTeamStandings()
        );
    }
}