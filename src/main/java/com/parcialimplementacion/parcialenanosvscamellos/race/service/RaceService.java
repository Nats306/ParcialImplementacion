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

    public RaceService(IRaceRepository raceRepository) {
        this.raceRepository = raceRepository;
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

        Pageable pageable = PageRequest.of(page, size, sort);

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
        return RaceMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public RaceResponse update(Long id, RaceRequest request) {
        Race race = findEntityById(id);

        if (race.getRaceStatus() == RaceStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "A completed race cannot be edited"
            );
        }

        validateDates(request);

        RaceMapper.updateEntity(race, request);
        Race savedRace = raceRepository.save(race);

        return RaceMapper.toResponse(savedRace);
    }

    @Transactional
    public RaceResponse updateStatus(Long id, UpdateRaceStatusRequest request) {
        Race race = findEntityById(id);

        validateStatusTransition(race.getRaceStatus(), request.getStatus());

        race.setRaceStatus(request.getStatus());
        Race savedRace = raceRepository.save(race);

        return RaceMapper.toResponse(savedRace);
    }

    @Transactional
    public void delete(Long id) {
        Race race = findEntityById(id);

        if (race.getRaceStatus() == RaceStatus.IN_PROGRESS
                || race.getRaceStatus() == RaceStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "A race in progress or completed cannot be deleted"
            );
        }

        raceRepository.delete(race);
    }

    private Race findEntityById(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Race with ID " + id + " was not found"
                ));
    }

    private void validateDates(RaceRequest request) {
        if (!request.getScheduledDateTime().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "A race cannot be scheduled in the past"
            );
        }

        if (!request.getRegistrationDeadline().isBefore(request.getScheduledDateTime())) {
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

        if (currentStatus == RaceStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "A completed race cannot change status"
            );
        }

        if (currentStatus == RaceStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "A cancelled race cannot change status"
            );
        }

        Set<RaceStatus> validNextStatuses = switch (currentStatus) {
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
            case COMPLETED, CANCELLED -> Set.of();
        };

        if (!validNextStatuses.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid race status transition from "
                            + currentStatus + " to " + newStatus
            );
        }
    }
}