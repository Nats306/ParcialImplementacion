package com.parcialimplementacion.parcialenanosvscamellos.registration.mapper;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RaceRegistrationRequest;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RaceRegistrationResponse;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RaceRegistration;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RegistrationStatus;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;

public class RaceRegistrationMapper {

    private RaceRegistrationMapper() {
    }

    public static RaceRegistration toEntity(
            RaceRegistrationRequest request,
            Race race,
            Competitor competitor,
            Team team
    ) {
        return RaceRegistration.builder()
                .race(race)
                .competitor(competitor)
                .team(team)
                .status(RegistrationStatus.PENDING)
                .startingPosition(request.getStartingPosition())
                .registeredBy(request.getRegisteredBy().trim())
                .build();
    }

    public static RaceRegistrationResponse toResponse(
            RaceRegistration registration
    ) {
        Competitor competitor = registration.getCompetitor();
        Team team = registration.getTeam();

        return RaceRegistrationResponse.builder()
                .id(registration.getId())
                .raceId(registration.getRace().getId())
                .raceName(registration.getRace().getName())
                .competitorId(
                        competitor == null ? null : competitor.getId()
                )
                .competitorName(
                        competitor == null ? null : competitor.getName()
                )
                .teamId(
                        team == null ? null : team.getId()
                )
                .teamName(
                        team == null ? null : team.getName()
                )
                .registrationDate(registration.getRegistrationDate())
                .status(registration.getStatus())
                .startingPosition(registration.getStartingPosition())
                .validationNotes(registration.getValidationNotes())
                .registeredBy(registration.getRegisteredBy())
                .build();
    }
}