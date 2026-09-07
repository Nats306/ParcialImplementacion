package com.parcialimplementacion.parcialenanosvscamellos.competitor.dto;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorType;

import java.time.LocalDate;
import java.util.UUID;

public record CompetitorResponse (
    UUID id,
    String name,
    String nickname,
    CompetitorType competitorType,
    int age,
    double weight,
    double height,
    String country,
    CompetitorStatus currentStatus,
    LocalDate registrationDate,
    int victories,
    int defeats,
    int completedRaces
){}
