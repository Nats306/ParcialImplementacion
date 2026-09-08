package com.parcialimplementacion.parcialenanosvscamellos.result.dto;

import com.parcialimplementacion.parcialenanosvscamellos.result.entity.ResultStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaceResultRequest {

    @NotNull(message = "Registration ID is required")
    @Positive(message = "Registration ID must be greater than zero")
    private Long registrationId;

    @NotNull(message = "Result status is required")
    private ResultStatus resultStatus;

    @Positive(message = "Final position must be greater than zero")
    private Integer finalPosition;

    @Positive(message = "Completion time must be greater than zero")
    private Long completionTimeMillis;

    @NotNull(message = "Penalty time is required")
    @PositiveOrZero(message = "Penalty time cannot be negative")
    private Long penaltyTimeMillis;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @NotBlank(message = "Recorded by is required")
    @Size(max = 120, message = "Recorded by must not exceed 120 characters")
    private String recordedBy;
}