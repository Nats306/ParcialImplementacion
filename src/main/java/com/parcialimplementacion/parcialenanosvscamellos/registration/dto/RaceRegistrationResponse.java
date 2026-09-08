package com.parcialimplementacion.parcialenanosvscamellos.registration.dto;

import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceRegistrationResponse {

    private Long id;

    private Long raceId;
    private String raceName;

    private UUID competitorId;
    private String competitorName;

    private Long teamId;
    private String teamName;

    private LocalDateTime registrationDate;
    private RegistrationStatus status;
    private Integer startingPosition;
    private String validationNotes;
    private String registeredBy;
}