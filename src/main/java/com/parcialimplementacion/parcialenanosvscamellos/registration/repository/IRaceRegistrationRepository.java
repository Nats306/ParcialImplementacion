package com.parcialimplementacion.parcialenanosvscamellos.registration.repository;

import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RaceRegistration;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IRaceRegistrationRepository
        extends JpaRepository<RaceRegistration, Long> {

    List<RaceRegistration> findAllByRace_IdOrderByStartingPositionAsc(
            Long raceId
    );

    boolean existsByRace_Id(Long raceId);

    boolean existsByRace_IdAndCompetitor_Id(
            Long raceId,
            UUID competitorId
    );

    boolean existsByRace_IdAndTeam_Id(
            Long raceId,
            Long teamId
    );

    boolean existsByRace_IdAndCompetitor_IdAndStatusIn(
            Long raceId,
            UUID competitorId,
            Collection<RegistrationStatus> statuses
    );

    boolean existsByRace_IdAndTeam_IdAndStatusIn(
            Long raceId,
            Long teamId,
            Collection<RegistrationStatus> statuses
    );

    boolean existsByRace_IdAndStartingPositionAndStatusIn(
            Long raceId,
            Integer startingPosition,
            Collection<RegistrationStatus> statuses
    );

    boolean existsByRace_IdAndStartingPositionAndStatusInAndIdNot(
            Long raceId,
            Integer startingPosition,
            Collection<RegistrationStatus> statuses,
            Long registrationId
    );

    long countByRace_IdAndStatusIn(
            Long raceId,
            Collection<RegistrationStatus> statuses
    );

    long countByRace_IdAndStatus(
            Long raceId,
            RegistrationStatus status
    );
}