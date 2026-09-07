package com.parcialimplementacion.parcialenanosvscamellos.competitor.dto;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompetitorRequest (
        @NotBlank(message = "Name is mandatory")
        String name,
        @NotBlank(message = "Nickname is mandatory")
        String nickname,
        @NotNull(message = "Competitor type is mandatory")
        CompetitorType competitorType,
        @Positive
        int age,
        @Positive
        double weight,
        @Positive
        double height,
        @NotBlank(message = "Country of origin is mandatory")
        String country
){}
