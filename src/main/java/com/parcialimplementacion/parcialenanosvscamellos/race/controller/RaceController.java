package com.parcialimplementacion.parcialenanosvscamellos.race.controller;

import com.parcialimplementacion.parcialenanosvscamellos.race.dto.RaceRequest;
import com.parcialimplementacion.parcialenanosvscamellos.race.dto.RaceResponse;
import com.parcialimplementacion.parcialenanosvscamellos.race.dto.UpdateRaceStatusRequest;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceType;
import com.parcialimplementacion.parcialenanosvscamellos.race.service.RaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @PostMapping
    public ResponseEntity<RaceResponse> create(
            @Valid @RequestBody RaceRequest request
    ) {
        RaceResponse response = raceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<RaceResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RaceType raceType,
            @RequestParam(required = false) RaceStatus raceStatus,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,
            @RequestParam(required = false) String organizer,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(
                raceService.findAll(
                        name,
                        raceType,
                        raceStatus,
                        fromDate,
                        toDate,
                        organizer,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaceResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(raceService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RaceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RaceRequest request
    ) {
        return ResponseEntity.ok(raceService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RaceResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRaceStatusRequest request
    ) {
        return ResponseEntity.ok(raceService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        raceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}