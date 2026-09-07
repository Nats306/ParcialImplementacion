package com.parcialimplementacion.parcialenanosvscamellos.competitor.dto;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Status is mandatory")
        CompetitorStatus status
) {}
