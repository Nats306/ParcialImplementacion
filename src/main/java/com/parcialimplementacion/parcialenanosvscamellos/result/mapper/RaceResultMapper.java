package com.parcialimplementacion.parcialenanosvscamellos.result.mapper;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RaceRegistration;
import com.parcialimplementacion.parcialenanosvscamellos.result.dto.RaceResultRequest;
import com.parcialimplementacion.parcialenanosvscamellos.result.dto.RaceResultResponse;
import com.parcialimplementacion.parcialenanosvscamellos.result.entity.RaceResult;
import com.parcialimplementacion.parcialenanosvscamellos.result.util.ResultPoints;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;

import java.time.LocalDateTime;

public class RaceResultMapper {

    private RaceResultMapper() {
    }

    public static RaceResult toEntity(
            RaceResultRequest request,
            Race race,
            RaceRegistration registration
    ) {
        return RaceResult.builder()
                .race(race)
                .registration(registration)
                .startingPosition(registration.getStartingPosition())
                .finalPosition(request.getFinalPosition())
                .completionTimeMillis(request.getCompletionTimeMillis())
                .penaltyTimeMillis(request.getPenaltyTimeMillis())
                .resultStatus(request.getResultStatus())
                .notes(request.getNotes())
                .recordedBy(request.getRecordedBy().trim())
                .build();
    }

    public static void updateEntity(
            RaceResult result,
            RaceResultRequest request
    ) {
        result.setFinalPosition(request.getFinalPosition());
        result.setCompletionTimeMillis(
                request.getCompletionTimeMillis()
        );
        result.setPenaltyTimeMillis(
                request.getPenaltyTimeMillis()
        );
        result.setResultStatus(request.getResultStatus());
        result.setNotes(request.getNotes());
        result.setRecordedBy(
                request.getRecordedBy().trim()
        );
        result.setRecordedAt(LocalDateTime.now());
    }

    public static RaceResultResponse toResponse(
            RaceResult result
    ) {
        RaceRegistration registration =
                result.getRegistration();

        Competitor competitor =
                registration.getCompetitor();

        Team team =
                registration.getTeam();

        Long totalTime =
                result.getCompletionTimeMillis() == null
                        ? null
                        : result.getCompletionTimeMillis()
                        + result.getPenaltyTimeMillis();

        return RaceResultResponse.builder()
                .id(result.getId())
                .raceId(result.getRace().getId())
                .raceName(result.getRace().getName())
                .registrationId(registration.getId())
                .participantType(
                        competitor != null
                                ? "COMPETITOR"
                                : "TEAM"
                )
                .competitorId(
                        competitor == null
                                ? null
                                : competitor.getId()
                )
                .competitorName(
                        competitor == null
                                ? null
                                : competitor.getName()
                )
                .teamId(
                        team == null
                                ? null
                                : team.getId()
                )
                .teamName(
                        team == null
                                ? null
                                : team.getName()
                )
                .startingPosition(
                        result.getStartingPosition()
                )
                .finalPosition(
                        result.getFinalPosition()
                )
                .completionTimeMillis(
                        result.getCompletionTimeMillis()
                )
                .penaltyTimeMillis(
                        result.getPenaltyTimeMillis()
                )
                .totalTimeMillis(totalTime)
                .resultStatus(
                        result.getResultStatus()
                )
                .points(
                        ResultPoints.calculate(
                                result.getResultStatus(),
                                result.getFinalPosition()
                        )
                )
                .notes(result.getNotes())
                .recordedBy(result.getRecordedBy())
                .recordedAt(result.getRecordedAt())
                .build();
    }
}