package com.parcialimplementacion.parcialenanosvscamellos.competitor.controller;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.CompetitorRequest;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.CompetitorResponse;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.UpdateStatusRequest;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorType;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.service.CompetitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/competitors")
@RequiredArgsConstructor
@Slf4j
public class CompetitorController {

    private final CompetitorService competitorService;

    @GetMapping
    public ResponseEntity<Page<CompetitorResponse>> getAllCompetitors(
            @RequestParam(required = false) CompetitorType type,
            @RequestParam(required = false) CompetitorStatus status,
            Pageable pageable
    ) {
        Page<CompetitorResponse> competitors = competitorService.getCompetitors(type, status, pageable);
        return ResponseEntity.ok(competitors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitorResponse> getCompetitorById(@PathVariable UUID id) {
        CompetitorResponse c = competitorService.getCompetitorById(id);
        return ResponseEntity.ok(c);
    }

    @PostMapping
    public ResponseEntity<CompetitorResponse> createCompetitor(
            @Valid @RequestBody CompetitorRequest request
    ) {
        CompetitorResponse c = competitorService.addCompetitor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitorResponse> updateCompetitor(
            @PathVariable UUID id,
            @Valid @RequestBody CompetitorRequest request
    ) {
        CompetitorResponse c = competitorService.updateCompetitor(id, request);
        return ResponseEntity.ok(c);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CompetitorResponse> updateCompetitorStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        CompetitorResponse c = competitorService.updateStatus(id, request.status());
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetitor(@PathVariable UUID id) {
        competitorService.deleteCompetitor(id);
        return ResponseEntity.noContent().build();
    }
}