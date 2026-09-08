package com.parcialimplementacion.parcialenanosvscamellos.registration.controller;

import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RaceRegistrationRequest;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RaceRegistrationResponse;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RejectRegistrationRequest;
import com.parcialimplementacion.parcialenanosvscamellos.registration.service.RaceRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RaceRegistrationController {

    private final RaceRegistrationService registrationService;

    public RaceRegistrationController(
            RaceRegistrationService registrationService
    ) {
        this.registrationService = registrationService;
    }

    @PostMapping("/races/{raceId}/registrations")
    public ResponseEntity<RaceRegistrationResponse> create(
            @PathVariable Long raceId,
            @Valid @RequestBody RaceRegistrationRequest request
    ) {

        RaceRegistrationResponse response =
                registrationService.create(
                        raceId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/races/{raceId}/registrations")
    public ResponseEntity<List<RaceRegistrationResponse>>
    findAllByRace(
            @PathVariable Long raceId
    ) {

        return ResponseEntity.ok(
                registrationService.findAllByRace(
                        raceId
                )
        );
    }

    @GetMapping("/registrations/{id}")
    public ResponseEntity<RaceRegistrationResponse> findById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                registrationService.findById(id)
        );
    }

    @PatchMapping("/registrations/{id}/approve")
    public ResponseEntity<RaceRegistrationResponse> approve(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                registrationService.approve(id)
        );
    }

    @PatchMapping("/registrations/{id}/reject")
    public ResponseEntity<RaceRegistrationResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRegistrationRequest request
    ) {

        return ResponseEntity.ok(
                registrationService.reject(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id
    ) {

        registrationService.cancel(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}