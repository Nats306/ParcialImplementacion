package com.parcialimplementacion.parcialenanosvscamellos.team.service;

import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.BusinessRuleException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.DuplicateResourceException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.ResourceNotFoundException;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.repository.ICompetitorRepository;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.TeamRequest;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.TeamResponse;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.UpdateTeamStatusRequest;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;
import com.parcialimplementacion.parcialenanosvscamellos.team.mapper.TeamMapper;
import com.parcialimplementacion.parcialenanosvscamellos.team.repository.ITeamRepository;
import com.parcialimplementacion.parcialenanosvscamellos.team.specification.TeamSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TeamService {

    private final ITeamRepository teamRepository;
    private final ICompetitorRepository competitorRepository;
    private final Integer maxMembers;

    public TeamService(
            ITeamRepository teamRepository,
            ICompetitorRepository competitorRepository,
            @Value("${app.team.max-members:5}") Integer maxMembers
    ) {
        this.teamRepository = teamRepository;
        this.competitorRepository = competitorRepository;
        this.maxMembers = maxMembers;
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        validateUniqueNameForCreate(request.getName());

        Team team = TeamMapper.toEntity(request);
        Team savedTeam = teamRepository.save(team);

        return TeamMapper.toResponse(savedTeam);
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> findAll(
            String name,
            TeamStatus status,
            String coach,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return teamRepository.findAll(
                TeamSpecification.withFilters(name, status, coach),
                pageable
        ).map(TeamMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        Team team = findEntityById(id);
        return TeamMapper.toResponse(team);
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = findEntityById(id);

        validateUniqueNameForUpdate(request.getName(), id);

        TeamMapper.updateEntity(team, request);
        Team savedTeam = teamRepository.save(team);

        return TeamMapper.toResponse(savedTeam);
    }

    @Transactional
    public TeamResponse updateStatus(Long id, UpdateTeamStatusRequest request) {
        Team team = findEntityById(id);

        team.setStatus(request.getStatus());
        Team savedTeam = teamRepository.save(team);

        return TeamMapper.toResponse(savedTeam);
    }

    @Transactional
    public TeamResponse addMember(Long teamId, UUID competitorId) {
        Team team = findEntityById(teamId);
        Competitor competitor = findCompetitorById(competitorId);

        validateTeamCanReceiveMembers(team);
        validateCompetitorCanJoinTeam(team, competitor);
        validateTeamCapacity(team);

        team.addMember(competitor);
        competitorRepository.save(competitor);

        return TeamMapper.toResponse(team);
    }

    @Transactional
    public void removeMember(Long teamId, UUID competitorId) {
        Team team = findEntityById(teamId);
        Competitor competitor = findCompetitorById(competitorId);

        if (competitor.getTeam() == null
                || !competitor.getTeam().getId().equals(team.getId())) {
            throw new BusinessRuleException(
                    "Competitor with ID " + competitorId
                            + " does not belong to team with ID " + teamId
            );
        }

        team.removeMember(competitor);
        competitorRepository.save(competitor);
    }

    @Transactional
    public void delete(Long id) {
        Team team = findEntityById(id);

        if (team.getMembers() != null && !team.getMembers().isEmpty()) {
            throw new BusinessRuleException(
                    "Team with ID " + id + " cannot be deleted while it has members. "
                            + "Remove the members or change the team status to INACTIVE."
            );
        }

        teamRepository.delete(team);
    }

    private Team findEntityById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team with ID " + id + " was not found"
                ));
    }

    private Competitor findCompetitorById(UUID id) {
        return competitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Competitor with ID " + id + " was not found"
                ));
    }

    private void validateUniqueNameForCreate(String name) {
        if (teamRepository.existsByNameIgnoreCase(name.trim())) {
            throw new DuplicateResourceException(
                    "A team with name '" + name.trim() + "' already exists"
            );
        }
    }

    private void validateUniqueNameForUpdate(String name, Long teamId) {
        if (teamRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), teamId)) {
            throw new DuplicateResourceException(
                    "A team with name '" + name.trim() + "' already exists"
            );
        }
    }

    private void validateTeamCanReceiveMembers(Team team) {
        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Only ACTIVE teams can receive new members"
            );
        }
    }

    private void validateCompetitorCanJoinTeam(Team team, Competitor competitor) {
        if (competitor.getTeam() != null) {
            if (competitor.getTeam().getId().equals(team.getId())) {
                throw new BusinessRuleException(
                        "Competitor with ID " + competitor.getId()
                                + " already belongs to team with ID " + team.getId()
                );
            }

            throw new BusinessRuleException(
                    "Competitor with ID " + competitor.getId()
                            + " already belongs to another active team"
            );
        }
    }

    private void validateTeamCapacity(Team team) {
        int currentMembers = team.getMembers() == null
                ? 0
                : team.getMembers().size();

        if (currentMembers >= maxMembers) {
            throw new BusinessRuleException(
                    "Team with ID " + team.getId()
                            + " reached the maximum allowed number of members: " + maxMembers
            );
        }
    }
}