package com.parcialimplementacion.parcialenanosvscamellos.race.mapper;

import com.parcialimplementacion.parcialenanosvscamellos.race.dto.RaceRequest;
import com.parcialimplementacion.parcialenanosvscamellos.race.dto.RaceResponse;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;

public class RaceMapper {

    private RaceMapper() {
    }

    public static Race toEntity(RaceRequest request) {
        return Race.builder()
                .name(request.getName().trim())
                .description(
                        request.getDescription() == null
                                ? null
                                : request.getDescription().trim()
                )
                .scheduledDateTime(request.getScheduledDateTime())
                .startLocation(request.getStartLocation().trim())
                .finishLocation(request.getFinishLocation().trim())
                .distanceMeters(request.getDistanceMeters())
                .maxParticipants(request.getMaxParticipants())
                .raceType(request.getRaceType())
                .raceStatus(RaceStatus.DRAFT)
                .organizer(request.getOrganizer().trim())
                .registrationDeadline(request.getRegistrationDeadline())
                .build();
    }

    public static void updateEntity(Race race, RaceRequest request) {
        race.setName(request.getName().trim());
        race.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );
        race.setScheduledDateTime(request.getScheduledDateTime());
        race.setStartLocation(request.getStartLocation().trim());
        race.setFinishLocation(request.getFinishLocation().trim());
        race.setDistanceMeters(request.getDistanceMeters());
        race.setMaxParticipants(request.getMaxParticipants());
        race.setRaceType(request.getRaceType());
        race.setOrganizer(request.getOrganizer().trim());
        race.setRegistrationDeadline(request.getRegistrationDeadline());
    }

    public static RaceResponse toResponse(Race race) {
        return RaceResponse.builder()
                .id(race.getId())
                .name(race.getName())
                .description(race.getDescription())
                .scheduledDateTime(race.getScheduledDateTime())
                .startLocation(race.getStartLocation())
                .finishLocation(race.getFinishLocation())
                .distanceMeters(race.getDistanceMeters())
                .maxParticipants(race.getMaxParticipants())
                .raceType(race.getRaceType())
                .raceStatus(race.getRaceStatus())
                .organizer(race.getOrganizer())
                .registrationDeadline(race.getRegistrationDeadline())
                .creationDate(race.getCreationDate())
                .lastModificationDate(race.getLastModificationDate())
                .build();
    }
}