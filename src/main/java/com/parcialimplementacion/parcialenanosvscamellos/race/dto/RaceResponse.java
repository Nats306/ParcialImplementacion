package com.parcialimplementacion.parcialenanosvscamellos.race.dto;

import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime scheduledDateTime;
    private String startLocation;
    private String finishLocation;
    private double distanceMeters;
    private int maxParticipants;
    private RaceType raceType;
    private RaceStatus raceStatus;
    private String organizer;
    private LocalDateTime registrationDeadline;
    private LocalDateTime creationDate;
    private LocalDateTime lastModificationDate;
}