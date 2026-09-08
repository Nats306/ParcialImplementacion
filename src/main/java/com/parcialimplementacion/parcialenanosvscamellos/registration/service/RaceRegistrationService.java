package com.parcialimplementacion.parcialenanosvscamellos.registration.service;

import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BadRequestException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BusinessRuleException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.DuplicateResourceException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.ResourceNotFoundException;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.repository.ICompetitorRepository;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceType;
import com.parcialimplementacion.parcialenanosvscamellos.race.repository.IRaceRepository;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RaceRegistrationRequest;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RaceRegistrationResponse;
import com.parcialimplementacion.parcialenanosvscamellos.registration.dto.RejectRegistrationRequest;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RaceRegistration;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RegistrationStatus;
import com.parcialimplementacion.parcialenanosvscamellos.registration.mapper.RaceRegistrationMapper;
import com.parcialimplementacion.parcialenanosvscamellos.registration.repository.IRaceRegistrationRepository;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;
import com.parcialimplementacion.parcialenanosvscamellos.team.repository.ITeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RaceRegistrationService {

    private static final Set<RegistrationStatus>
            ACTIVE_REGISTRATION_STATUSES = Set.of(
            RegistrationStatus.PENDING,
            RegistrationStatus.APPROVED
    );

    private final IRaceRegistrationRepository registrationRepository;
    private final IRaceRepository raceRepository;
    private final ICompetitorRepository competitorRepository;
    private final ITeamRepository teamRepository;

    public RaceRegistrationService(
            IRaceRegistrationRepository registrationRepository,
            IRaceRepository raceRepository,
            ICompetitorRepository competitorRepository,
            ITeamRepository teamRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.raceRepository = raceRepository;
        this.competitorRepository = competitorRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public RaceRegistrationResponse create(
            Long raceId,
            RaceRegistrationRequest request
    ) {
        validateParticipantSelection(request);

        Race race = findRaceById(raceId);

        validateRaceOpenForRegistration(race);
        validateCapacity(race);
        validateStartingPositionAvailable(
                race.getId(),
                request.getStartingPosition()
        );

        Competitor competitor = null;
        Team team = null;

        if (request.getCompetitorId() != null) {

            competitor = findCompetitorById(
                    request.getCompetitorId()
            );

            validateRaceTypeForCompetitor(race);
            validateCompetitorEligibility(competitor);
            validateCompetitorNotDuplicated(race, competitor);
            validateCompetitorNotRegisteredThroughTeam(
                    race,
                    competitor
            );

        } else {

            team = findTeamById(request.getTeamId());

            validateRaceTypeForTeam(race);
            validateTeamEligibility(team);
            validateTeamNotDuplicated(race, team);
            validateTeamMembersNotRegisteredIndividually(
                    race,
                    team
            );
        }

        RaceRegistration registration =
                RaceRegistrationMapper.toEntity(
                        request,
                        race,
                        competitor,
                        team
                );

        RaceRegistration savedRegistration =
                registrationRepository.save(registration);

        return RaceRegistrationMapper.toResponse(
                savedRegistration
        );
    }

    @Transactional(readOnly = true)
    public List<RaceRegistrationResponse> findAllByRace(
            Long raceId
    ) {
        findRaceById(raceId);

        return registrationRepository
                .findAllByRace_IdOrderByStartingPositionAsc(
                        raceId
                )
                .stream()
                .map(RaceRegistrationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RaceRegistrationResponse findById(Long id) {

        return RaceRegistrationMapper.toResponse(
                findEntityById(id)
        );
    }

    @Transactional
    public RaceRegistrationResponse approve(Long id) {

        RaceRegistration registration =
                findEntityById(id);

        if (registration.getStatus()
                != RegistrationStatus.PENDING) {

            throw new BusinessRuleException(
                    "Only PENDING registrations can be approved"
            );
        }

        Race race = registration.getRace();

        validateRaceAllowsRegistrationDecision(race);

        if (registration.getStartingPosition() == null) {

            throw new BusinessRuleException(
                    "A registration cannot be approved without a starting position"
            );
        }

        validateStartingPositionAvailableExcludingCurrent(
                race.getId(),
                registration.getStartingPosition(),
                registration.getId()
        );

        if (registration.getCompetitor() != null) {

            validateRaceTypeForCompetitor(race);

            validateCompetitorEligibility(
                    registration.getCompetitor()
            );

            validateCompetitorNotRegisteredThroughTeam(
                    race,
                    registration.getCompetitor()
            );

        } else {

            validateRaceTypeForTeam(race);

            validateTeamEligibility(
                    registration.getTeam()
            );

            validateTeamMembersNotRegisteredIndividually(
                    race,
                    registration.getTeam()
            );
        }

        registration.setStatus(
                RegistrationStatus.APPROVED
        );

        registration.setValidationNotes(null);

        RaceRegistration savedRegistration =
                registrationRepository.save(registration);

        return RaceRegistrationMapper.toResponse(
                savedRegistration
        );
    }

    @Transactional
    public RaceRegistrationResponse reject(
            Long id,
            RejectRegistrationRequest request
    ) {

        RaceRegistration registration =
                findEntityById(id);

        if (registration.getStatus()
                != RegistrationStatus.PENDING) {

            throw new BusinessRuleException(
                    "Only PENDING registrations can be rejected"
            );
        }

        validateRaceAllowsRegistrationDecision(
                registration.getRace()
        );

        registration.setStatus(
                RegistrationStatus.REJECTED
        );

        registration.setValidationNotes(
                request.getReason().trim()
        );

        registration.setStartingPosition(null);

        RaceRegistration savedRegistration =
                registrationRepository.save(registration);

        return RaceRegistrationMapper.toResponse(
                savedRegistration
        );
    }

    @Transactional
    public void cancel(Long id) {

        RaceRegistration registration =
                findEntityById(id);

        if (registration.getStatus()
                == RegistrationStatus.CANCELLED) {

            throw new BusinessRuleException(
                    "Registration is already cancelled"
            );
        }

        if (registration.getStatus()
                == RegistrationStatus.REJECTED) {

            throw new BusinessRuleException(
                    "A rejected registration cannot be cancelled"
            );
        }

        RaceStatus raceStatus =
                registration.getRace().getRaceStatus();

        if (raceStatus == RaceStatus.IN_PROGRESS
                || raceStatus == RaceStatus.COMPLETED) {

            throw new BusinessRuleException(
                    "A registration cannot be cancelled after the race has started"
            );
        }

        registration.setStatus(
                RegistrationStatus.CANCELLED
        );

        registration.setValidationNotes(
                "Registration cancelled"
        );

        registration.setStartingPosition(null);

        registrationRepository.save(registration);
    }

    private RaceRegistration findEntityById(Long id) {

        return registrationRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Registration with ID "
                                        + id
                                        + " was not found"
                        )
                );
    }

    private Race findRaceById(Long id) {

        return raceRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Race with ID "
                                        + id
                                        + " was not found"
                        )
                );
    }

    private Competitor findCompetitorById(UUID id) {

        return competitorRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Competitor with ID "
                                        + id
                                        + " was not found"
                        )
                );
    }

    private Team findTeamById(Long id) {

        return teamRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Team with ID "
                                        + id
                                        + " was not found"
                        )
                );
    }

    private void validateParticipantSelection(
            RaceRegistrationRequest request
    ) {

        boolean hasCompetitor =
                request.getCompetitorId() != null;

        boolean hasTeam =
                request.getTeamId() != null;

        if (hasCompetitor == hasTeam) {

            throw new BadRequestException(
                    "Exactly one participant must be provided: competitorId or teamId"
            );
        }
    }

    private void validateRaceOpenForRegistration(
            Race race
    ) {

        if (race.getRaceStatus()
                != RaceStatus.OPEN_FOR_REGISTRATION) {

            throw new BusinessRuleException(
                    "Registrations are allowed only while the race is OPEN_FOR_REGISTRATION"
            );
        }

        if (!LocalDateTime.now().isBefore(
                race.getRegistrationDeadline()
        )) {

            throw new BusinessRuleException(
                    "The registration deadline has already passed"
            );
        }
    }

    private void validateRaceAllowsRegistrationDecision(
            Race race
    ) {

        if (race.getRaceStatus()
                != RaceStatus.OPEN_FOR_REGISTRATION
                &&
                race.getRaceStatus()
                        != RaceStatus.CLOSED_FOR_REGISTRATION) {

            throw new BusinessRuleException(
                    "Registrations can only be approved or rejected before the race starts"
            );
        }
    }

    private void validateCapacity(Race race) {

        long occupiedPlaces =
                registrationRepository
                        .countByRace_IdAndStatusIn(
                                race.getId(),
                                ACTIVE_REGISTRATION_STATUSES
                        );

        if (occupiedPlaces
                >= race.getMaxParticipants()) {

            throw new BusinessRuleException(
                    "Race with ID "
                            + race.getId()
                            + " has reached its maximum number of participants: "
                            + race.getMaxParticipants()
            );
        }
    }

    private void validateStartingPositionAvailable(
            Long raceId,
            Integer startingPosition
    ) {

        if (registrationRepository
                .existsByRace_IdAndStartingPositionAndStatusIn(
                        raceId,
                        startingPosition,
                        ACTIVE_REGISTRATION_STATUSES
                )) {

            throw new DuplicateResourceException(
                    "Starting position "
                            + startingPosition
                            + " is already assigned in race with ID "
                            + raceId
            );
        }
    }

    private void validateStartingPositionAvailableExcludingCurrent(
            Long raceId,
            Integer startingPosition,
            Long registrationId
    ) {

        if (registrationRepository
                .existsByRace_IdAndStartingPositionAndStatusInAndIdNot(
                        raceId,
                        startingPosition,
                        ACTIVE_REGISTRATION_STATUSES,
                        registrationId
                )) {

            throw new DuplicateResourceException(
                    "Starting position "
                            + startingPosition
                            + " is already assigned in race with ID "
                            + raceId
            );
        }
    }

    private void validateRaceTypeForCompetitor(
            Race race
    ) {

        if (race.getRaceType() == RaceType.TEAM) {

            throw new BusinessRuleException(
                    "An individual competitor cannot register in a TEAM race"
            );
        }
    }

    private void validateRaceTypeForTeam(
            Race race
    ) {

        if (race.getRaceType()
                == RaceType.INDIVIDUAL) {

            throw new BusinessRuleException(
                    "A team cannot register in an INDIVIDUAL race"
            );
        }
    }

    private void validateCompetitorEligibility(
            Competitor competitor
    ) {

        if (competitor.getCurrentStatus()
                != CompetitorStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Only ACTIVE competitors can be registered in a race"
            );
        }
    }

    private void validateTeamEligibility(
            Team team
    ) {

        if (team.getStatus() != TeamStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Only ACTIVE teams can be registered in a race"
            );
        }

        if (team.getMembers() == null
                || team.getMembers().isEmpty()) {

            throw new BusinessRuleException(
                    "A team must contain at least one competitor before entering a race"
            );
        }

        boolean hasIneligibleMember =
                team.getMembers()
                        .stream()
                        .anyMatch(
                                member ->
                                        member.getCurrentStatus()
                                                != CompetitorStatus.ACTIVE
                        );

        if (hasIneligibleMember) {

            throw new BusinessRuleException(
                    "All team members must be ACTIVE to register the team in a race"
            );
        }
    }

    private void validateCompetitorNotDuplicated(
            Race race,
            Competitor competitor
    ) {

        if (registrationRepository
                .existsByRace_IdAndCompetitor_Id(
                        race.getId(),
                        competitor.getId()
                )) {

            throw new DuplicateResourceException(
                    "Competitor with ID "
                            + competitor.getId()
                            + " is already registered in race with ID "
                            + race.getId()
            );
        }
    }

    private void validateTeamNotDuplicated(
            Race race,
            Team team
    ) {

        if (registrationRepository
                .existsByRace_IdAndTeam_Id(
                        race.getId(),
                        team.getId()
                )) {

            throw new DuplicateResourceException(
                    "Team with ID "
                            + team.getId()
                            + " is already registered in race with ID "
                            + race.getId()
            );
        }
    }

    private void validateCompetitorNotRegisteredThroughTeam(
            Race race,
            Competitor competitor
    ) {

        Team team = competitor.getTeam();

        if (team == null) {
            return;
        }

        if (registrationRepository
                .existsByRace_IdAndTeam_IdAndStatusIn(
                        race.getId(),
                        team.getId(),
                        ACTIVE_REGISTRATION_STATUSES
                )) {

            throw new BusinessRuleException(
                    "Competitor with ID "
                            + competitor.getId()
                            + " cannot compete individually because its team is already registered in this race"
            );
        }
    }

    private void validateTeamMembersNotRegisteredIndividually(
            Race race,
            Team team
    ) {

        for (Competitor member : team.getMembers()) {

            if (registrationRepository
                    .existsByRace_IdAndCompetitor_IdAndStatusIn(
                            race.getId(),
                            member.getId(),
                            ACTIVE_REGISTRATION_STATUSES
                    )) {

                throw new BusinessRuleException(
                        "Team cannot be registered because competitor with ID "
                                + member.getId()
                                + " is already registered individually in this race"
                );
            }
        }
    }
}