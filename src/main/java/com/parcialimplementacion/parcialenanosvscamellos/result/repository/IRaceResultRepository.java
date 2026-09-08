package com.parcialimplementacion.parcialenanosvscamellos.result.repository;

import com.parcialimplementacion.parcialenanosvscamellos.result.entity.RaceResult;
import com.parcialimplementacion.parcialenanosvscamellos.result.entity.ResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IRaceResultRepository
        extends JpaRepository<RaceResult, Long> {

    List<RaceResult> findAllByRace_IdOrderByFinalPositionAsc(
            Long raceId
    );

    boolean existsByRace_Id(Long raceId);

    boolean existsByRegistration_Id(
            Long registrationId
    );

    boolean existsByRace_IdAndResultStatusAndFinalPosition(
            Long raceId,
            ResultStatus resultStatus,
            Integer finalPosition
    );

    boolean existsByRace_IdAndResultStatusAndFinalPositionAndIdNot(
            Long raceId,
            ResultStatus resultStatus,
            Integer finalPosition,
            Long resultId
    );

    boolean existsByRegistration_Competitor_Id(
            UUID competitorId
    );

    boolean existsByRegistration_Team_Id(
            Long teamId
    );

    List<RaceResult> findAllByRegistration_Competitor_Id(
            UUID competitorId
    );

    List<RaceResult> findAllByRegistration_Team_Id(
            Long teamId
    );
}