package com.parcialimplementacion.parcialenanosvscamellos.team.controller;

import com.parcialimplementacion.parcialenanosvscamellos.team.dto.TeamRequest;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.TeamResponse;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.UpdateTeamStatusRequest;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;
import com.parcialimplementacion.parcialenanosvscamellos.team.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(
            @Valid @RequestBody TeamRequest request
    ) {
        TeamResponse response = teamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TeamResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) TeamStatus status,
            @RequestParam(required = false) String coach,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(
                teamService.findAll(name, status, coach, page, size, sortBy, direction)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(teamService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request
    ) {
        return ResponseEntity.ok(teamService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TeamResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamStatusRequest request
    ) {
        return ResponseEntity.ok(teamService.updateStatus(id, request));
    }

    @PostMapping("/{teamId}/members/{competitorId}")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable Long teamId,
            @PathVariable UUID competitorId
    ) {
        return ResponseEntity.ok(
                teamService.addMember(teamId, competitorId)
        );
    }

    @DeleteMapping("/{teamId}/members/{competitorId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long teamId,
            @PathVariable UUID competitorId
    ) {
        teamService.removeMember(teamId, competitorId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}