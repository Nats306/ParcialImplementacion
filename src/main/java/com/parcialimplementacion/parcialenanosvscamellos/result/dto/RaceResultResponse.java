package com.parcialimplementacion.parcialenanosvscamellos.result.dto;

import com.parcialimplementacion.parcialenanosvscamellos.result.entity.ResultStatus;
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
public class RaceResultResponse {

    private Long id;

    private Long raceId;
    private String raceName;

    private Long registrationId;

    private String participantType;

    private UUID competitorId;
    private String competitorName;

    private Long teamId;
    private String teamName;

    private Integer startingPosition;
    private Integer finalPosition;

    private Long completionTimeMillis;
    private Long penaltyTimeMillis;
    private Long totalTimeMillis;

    private ResultStatus resultStatus;

    private Integer points;

    private String notes;

    private String recordedBy;
    private LocalDateTime recordedAt;
}