package com.parcialimplementacion.parcialenanosvscamellos.race.service;

import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BusinessRuleException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.ResourceNotFoundException;
import com.parcialimplementacion.parcialenanosvscamellos.race.dto.RaceRequest;
import com.parcialimplementacion.parcialenanosvscamellos.race.dto.RaceResponse;
import com.parcialimplementacion.parcialenanosvscamellos.race.dto.UpdateRaceStatusRequest;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceType;
import com.parcialimplementacion.parcialenanosvscamellos.race.mapper.RaceMapper;
import com.parcialimplementacion.parcialenanosvscamellos.race.repository.IRaceRepository;
import com.parcialimplementacion.parcialenanosvscamellos.race.specification.RaceSpecification;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RegistrationStatus;
import com.parcialimplementacion.parcialenanosvscamellos.registration.repository.IRaceRegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class RaceService {

    private final IRaceRepository raceRepository;
    private final IRaceRegistrationRepository registrationRepository;

    public RaceService(
            IRaceRepository raceRepository,
            IRaceRegistrationRepository registrationRepository
    ) {
        this.raceRepository = raceRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public RaceResponse create(RaceRequest request) {

        validateDates(request);

        Race race = RaceMapper.toEntity(request);
        Race savedRace = raceRepository.save(race);

        return RaceMapper.toResponse(savedRace);
    }

    @Transactional(readOnly = true)
    public Page<RaceResponse> findAll(
            String name,
            RaceType raceType,
            RaceStatus raceStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String organizer,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );

        return raceRepository.findAll(
                RaceSpecification.withFilters(
                        name,
                        raceType,
                        raceStatus,
                        fromDate,
                        toDate,
                        organizer
                ),
                pageable
        ).map(RaceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RaceResponse findById(Long id) {

        return RaceMapper.toResponse(
                findEntityById(id)
        );
    }

    @Transactional
    public RaceResponse update(
            Long id,
            RaceRequest request
    ) {

        Race race = findEntityById(id);

        if (race.getRaceStatus()
                == RaceStatus.COMPLETED) {

            throw new BusinessRuleException(
                    "A completed race cannot be edited"
            );
        }

        validateDates(request);

        RaceMapper.updateEntity(
                race,
                request
        );

        Race savedRace =
                raceRepository.save(race);

        return RaceMapper.toResponse(
                savedRace
        );
    }

    @Transactional
    public RaceResponse updateStatus(
            Long id,
            UpdateRaceStatusRequest request
    ) {

        Race race = findEntityById(id);

        validateStatusTransition(
                race.getRaceStatus(),
                request.getStatus()
        );

        if (request.getStatus()
                == RaceStatus.IN_PROGRESS) {

            validateRaceCanStart(race);
        }

        race.setRaceStatus(
                request.getStatus()
        );

        Race savedRace =
                raceRepository.save(race);

        return RaceMapper.toResponse(
                savedRace
        );
    }

    @Transactional
    public void delete(Long id) {

        Race race = findEntityById(id);

        if (race.getRaceStatus()
                == RaceStatus.IN_PROGRESS
                ||
                race.getRaceStatus()
                        == RaceStatus.COMPLETED) {

            throw new BusinessRuleException(
                    "A race in progress or completed cannot be deleted"
            );
        }

        if (registrationRepository
                .existsByRace_Id(id)) {

            throw new BusinessRuleException(
                    "A race with registration history cannot be deleted"
            );
        }

        raceRepository.delete(race);
    }

    private void validateRaceCanStart(
            Race race
    ) {

        long approvedParticipants =
                registrationRepository
                        .countByRace_IdAndStatus(
                                race.getId(),
                                RegistrationStatus.APPROVED
                        );

        if (approvedParticipants < 2) {

            throw new BusinessRuleException(
                    "At least two approved participants are required to start a race"
            );
        }
    }

    private Race findEntityById(Long id) {

        return raceRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Race with ID "
                                        + id
                                        + " was not found"
                        )
                );
    }

    private void validateDates(
            RaceRequest request
    ) {

        if (!request
                .getScheduledDateTime()
                .isAfter(LocalDateTime.now())) {

            throw new BusinessRuleException(
                    "A race cannot be scheduled in the past"
            );
        }

        if (!request
                .getRegistrationDeadline()
                .isBefore(
                        request.getScheduledDateTime()
                )) {

            throw new BusinessRuleException(
                    "Registration deadline must be earlier than the race start time"
            );
        }
    }

    private void validateStatusTransition(
            RaceStatus currentStatus,
            RaceStatus newStatus
    ) {

        if (currentStatus == newStatus) {
            throw new BusinessRuleException(
                    "Race already has status " + newStatus
            );
        }

        Set<RaceStatus> validNextStatuses =
                switch (currentStatus) {

                    case DRAFT -> Set.of(
                            RaceStatus.OPEN_FOR_REGISTRATION,
                            RaceStatus.CANCELLED
                    );

                    case OPEN_FOR_REGISTRATION -> Set.of(
                            RaceStatus.CLOSED_FOR_REGISTRATION,
                            RaceStatus.CANCELLED
                    );

                    case CLOSED_FOR_REGISTRATION -> Set.of(
                            RaceStatus.IN_PROGRESS,
                            RaceStatus.CANCELLED
                    );

                    case IN_PROGRESS -> Set.of(
                            RaceStatus.COMPLETED,
                            RaceStatus.CANCELLED
                    );

                    case COMPLETED ->
                            throw new BusinessRuleException(
                                    "A completed race cannot change status"
                            );

                    case CANCELLED ->
                            throw new BusinessRuleException(
                                    "A cancelled race cannot change status"
                            );
                };

        if (!validNextStatuses.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid race status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }
}