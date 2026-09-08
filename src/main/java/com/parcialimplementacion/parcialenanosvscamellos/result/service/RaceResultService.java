package com.parcialimplementacion.parcialenanosvscamellos.result.service;

import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BadRequestException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BusinessRuleException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.DuplicateResourceException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.ResourceNotFoundException;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.repository.ICompetitorRepository;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import com.parcialimplementacion.parcialenanosvscamellos.race.repository.IRaceRepository;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RaceRegistration;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RegistrationStatus;
import com.parcialimplementacion.parcialenanosvscamellos.registration.repository.IRaceRegistrationRepository;
import com.parcialimplementacion.parcialenanosvscamellos.result.dto.RaceResultRequest;
import com.parcialimplementacion.parcialenanosvscamellos.result.dto.RaceResultResponse;
import com.parcialimplementacion.parcialenanosvscamellos.result.entity.RaceResult;
import com.parcialimplementacion.parcialenanosvscamellos.result.entity.ResultStatus;
import com.parcialimplementacion.parcialenanosvscamellos.result.mapper.RaceResultMapper;
import com.parcialimplementacion.parcialenanosvscamellos.result.repository.IRaceResultRepository;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import com.parcialimplementacion.parcialenanosvscamellos.team.repository.ITeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RaceResultService {

    private final IRaceResultRepository resultRepository;
    private final IRaceRepository raceRepository;
    private final IRaceRegistrationRepository registrationRepository;
    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;

    public RaceResultService(
            IRaceResultRepository resultRepository,
            IRaceRepository raceRepository,
            IRaceRegistrationRepository registrationRepository,
            ICompetitorRepository competitorRepository,
            ITeamRepository teamRepository
    ) {
        this.resultRepository = resultRepository;
        this.raceRepository = raceRepository;
        this.registrationRepository = registrationRepository;
        this.competitorRepository = competitorRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public RaceResultResponse create(
            Long raceId,
            RaceResultRequest request
    ) {
        Race race = findRaceById(raceId);

        validateRaceInProgress(race);

        RaceRegistration registration =
                findRegistrationById(
                        request.getRegistrationId()
                );

        validateRegistrationBelongsToRace(
                race,
                registration
        );

        validateRegistrationApproved(registration);

        if (resultRepository.existsByRegistration_Id(
                registration.getId()
        )) {
            throw new DuplicateResourceException(
                    "Registration with ID "
                            + registration.getId()
                            + " already has an official result"
            );
        }

        validateResultData(
                race.getId(),
                request,
                null
        );

        RaceResult result =
                RaceResultMapper.toEntity(
                        request,
                        race,
                        registration
                );

        RaceResult savedResult =
                resultRepository.save(result);

        refreshParticipantStatistics(registration);

        return RaceResultMapper.toResponse(
                savedResult
        );
    }

    @Transactional(readOnly = true)
    public List<RaceResultResponse> findAllByRace(
            Long raceId
    ) {
        findRaceById(raceId);

        return resultRepository
                .findAllByRace_IdOrderByFinalPositionAsc(
                        raceId
                )
                .stream()
                .map(RaceResultMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RaceResultResponse findById(Long id) {
        return RaceResultMapper.toResponse(
                findEntityById(id)
        );
    }

    @Transactional
    public RaceResultResponse update(
            Long id,
            RaceResultRequest request
    ) {
        RaceResult result = findEntityById(id);

        Race race = result.getRace();

        validateRaceInProgress(race);

        if (!result.getRegistration()
                .getId()
                .equals(request.getRegistrationId())) {

            throw new BadRequestException(
                    "The registration of an existing result cannot be changed"
            );
        }

        validateRegistrationApproved(
                result.getRegistration()
        );

        validateResultData(
                race.getId(),
                request,
                result.getId()
        );

        RaceResultMapper.updateEntity(
                result,
                request
        );

        RaceResult savedResult =
                resultRepository.save(result);

        refreshParticipantStatistics(
                savedResult.getRegistration()
        );

        return RaceResultMapper.toResponse(
                savedResult
        );
    }

    private void validateResultData(
            Long raceId,
            RaceResultRequest request,
            Long currentResultId
    ) {
        if (request.getResultStatus()
                == ResultStatus.FINISHED) {

            if (request.getFinalPosition() == null) {
                throw new BadRequestException(
                        "A FINISHED result requires a final position"
                );
            }

            if (request.getCompletionTimeMillis() == null) {
                throw new BadRequestException(
                        "A FINISHED result requires a completion time"
                );
            }

            if (request.getFinalPosition() == 1) {
                validateOnlyOneWinner(
                        raceId,
                        currentResultId
                );
            }

            validateFinalPositionAvailable(
                    raceId,
                    request.getFinalPosition(),
                    currentResultId
            );

        } else {

            if (request.getFinalPosition() != null) {
                throw new BusinessRuleException(
                        "Only FINISHED participants may have a final position"
                );
            }

            if (request.getResultStatus()
                    == ResultStatus.DID_NOT_START
                    && request.getCompletionTimeMillis() != null) {

                throw new BusinessRuleException(
                        "A participant that DID_NOT_START cannot have a completion time"
                );
            }
        }
    }

    private void validateOnlyOneWinner(
            Long raceId,
            Long currentResultId
    ) {
        boolean winnerExists;

        if (currentResultId == null) {
            winnerExists =
                    resultRepository
                            .existsByRace_IdAndResultStatusAndFinalPosition(
                                    raceId,
                                    ResultStatus.FINISHED,
                                    1
                            );
        } else {
            winnerExists =
                    resultRepository
                            .existsByRace_IdAndResultStatusAndFinalPositionAndIdNot(
                                    raceId,
                                    ResultStatus.FINISHED,
                                    1,
                                    currentResultId
                            );
        }

        if (winnerExists) {
            throw new BusinessRuleException(
                    "Only one official winner is allowed per race"
            );
        }
    }

    private void validateFinalPositionAvailable(
            Long raceId,
            Integer finalPosition,
            Long currentResultId
    ) {
        boolean positionExists;

        if (currentResultId == null) {
            positionExists =
                    resultRepository
                            .existsByRace_IdAndResultStatusAndFinalPosition(
                                    raceId,
                                    ResultStatus.FINISHED,
                                    finalPosition
                            );
        } else {
            positionExists =
                    resultRepository
                            .existsByRace_IdAndResultStatusAndFinalPositionAndIdNot(
                                    raceId,
                                    ResultStatus.FINISHED,
                                    finalPosition,
                                    currentResultId
                            );
        }

        if (positionExists) {
            throw new DuplicateResourceException(
                    "Final position "
                            + finalPosition
                            + " is already assigned in race with ID "
                            + raceId
            );
        }
    }

    private void validateRaceInProgress(Race race) {
        if (race.getRaceStatus()
                != RaceStatus.IN_PROGRESS) {

            throw new BusinessRuleException(
                    "Results may only be entered or modified while the race is IN_PROGRESS"
            );
        }
    }

    private void validateRegistrationBelongsToRace(
            Race race,
            RaceRegistration registration
    ) {
        if (!registration.getRace()
                .getId()
                .equals(race.getId())) {

            throw new BusinessRuleException(
                    "Registration with ID "
                            + registration.getId()
                            + " does not belong to race with ID "
                            + race.getId()
            );
        }
    }

    private void validateRegistrationApproved(
            RaceRegistration registration
    ) {
        if (registration.getStatus()
                != RegistrationStatus.APPROVED) {

            throw new BusinessRuleException(
                    "Only APPROVED registrations may receive official results"
            );
        }
    }

    private void refreshParticipantStatistics(
            RaceRegistration registration
    ) {
        if (registration.getCompetitor() != null) {
            refreshCompetitorStatistics(
                    registration
                            .getCompetitor()
                            .getId()
            );
        } else {
            refreshTeamStatistics(
                    registration
                            .getTeam()
                            .getId()
            );
        }
    }

    private void refreshCompetitorStatistics(
            UUID competitorId
    ) {
        Competitor competitor =
                competitorRepository.findById(
                                competitorId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Competitor with ID "
                                                        + competitorId
                                                        + " was not found"
                                        )
                        );

        List<RaceResult> results =
                resultRepository
                        .findAllByRegistration_Competitor_Id(
                                competitorId
                        );

        long completedRaces =
                results.stream()
                        .filter(
                                result ->
                                        result.getResultStatus()
                                                != ResultStatus.DID_NOT_START
                        )
                        .count();

        long victories =
                results.stream()
                        .filter(
                                result ->
                                        result.getResultStatus()
                                                == ResultStatus.FINISHED
                                                && Integer.valueOf(1)
                                                .equals(
                                                        result.getFinalPosition()
                                                )
                        )
                        .count();

        competitor.setCompletedRaces(
                Math.toIntExact(completedRaces)
        );

        competitor.setVictories(
                Math.toIntExact(victories)
        );

        competitor.setDefeats(
                Math.toIntExact(
                        completedRaces - victories
                )
        );

        competitorRepository.save(competitor);
    }

    private void refreshTeamStatistics(Long teamId) {

        Team team =
                teamRepository.findById(teamId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Team with ID "
                                                        + teamId
                                                        + " was not found"
                                        )
                        );

        List<RaceResult> results =
                resultRepository
                        .findAllByRegistration_Team_Id(
                                teamId
                        );

        long completedRaces =
                results.stream()
                        .filter(
                                result ->
                                        result.getResultStatus()
                                                != ResultStatus.DID_NOT_START
                        )
                        .count();

        long victories =
                results.stream()
                        .filter(
                                result ->
                                        result.getResultStatus()
                                                == ResultStatus.FINISHED
                                                && Integer.valueOf(1)
                                                .equals(
                                                        result.getFinalPosition()
                                                )
                        )
                        .count();

        team.setVictories(
                Math.toIntExact(victories)
        );

        team.setDefeats(
                Math.toIntExact(
                        completedRaces - victories
                )
        );

        teamRepository.save(team);
    }

    private RaceResult findEntityById(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Result with ID "
                                                + id
                                                + " was not found"
                                )
                );
    }

    private Race findRaceById(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Race with ID "
                                                + id
                                                + " was not found"
                                )
                );
    }

    private RaceRegistration findRegistrationById(
            Long id
    ) {
        return registrationRepository.findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Registration with ID "
                                                + id
                                                + " was not found"
                                )
                );
    }
}