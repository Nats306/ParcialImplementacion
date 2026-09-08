package com.parcialimplementacion.parcialenanosvscamellos.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaceRegistrationRequest {

    private UUID competitorId;

    @Positive(message = "Team ID must be greater than zero")
    private Long teamId;

    @NotNull(message = "Starting position is required")
    @Positive(message = "Starting position must be greater than zero")
    private Integer startingPosition;

    @NotBlank(message = "Registered by is required")
    @Size(max = 120, message = "Registered by must not exceed 120 characters")
    private String registeredBy;
}