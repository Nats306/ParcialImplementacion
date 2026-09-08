package com.parcialimplementacion.parcialenanosvscamellos.result.controller;

import com.parcialimplementacion.parcialenanosvscamellos.result.dto.RaceResultRequest;
import com.parcialimplementacion.parcialenanosvscamellos.result.dto.RaceResultResponse;
import com.parcialimplementacion.parcialenanosvscamellos.result.service.RaceResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RaceResultController {

    private final RaceResultService resultService;

    public RaceResultController(
            RaceResultService resultService
    ) {
        this.resultService = resultService;
    }

    @PostMapping("/races/{raceId}/results")
    public ResponseEntity<RaceResultResponse> create(
            @PathVariable Long raceId,
            @Valid @RequestBody RaceResultRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        resultService.create(
                                raceId,
                                request
                        )
                );
    }

    @GetMapping("/races/{raceId}/results")
    public ResponseEntity<List<RaceResultResponse>>
    findAllByRace(
            @PathVariable Long raceId
    ) {
        return ResponseEntity.ok(
                resultService.findAllByRace(
                        raceId
                )
        );
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<RaceResultResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                resultService.findById(id)
        );
    }

    @PutMapping("/results/{id}")
    public ResponseEntity<RaceResultResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RaceResultRequest request
    ) {
        return ResponseEntity.ok(
                resultService.update(
                        id,
                        request
                )
        );
    }
}