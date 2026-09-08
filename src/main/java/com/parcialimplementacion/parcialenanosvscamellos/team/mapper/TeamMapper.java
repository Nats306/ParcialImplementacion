package com.parcialimplementacion.parcialenanosvscamellos.team.mapper;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.TeamRequest;
import com.parcialimplementacion.parcialenanosvscamellos.team.dto.TeamResponse;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TeamMapper {

    private TeamMapper() {
    }

    public static Team toEntity(TeamRequest request) {
        Team team = new Team();
        team.setName(request.getName().trim());
        team.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );
        team.setCoach(request.getCoach().trim());
        team.setCreationDate(LocalDate.now());
        team.setStatus(TeamStatus.ACTIVE);
        team.setVictories(0);
        team.setDefeats(0);

        return team;
    }

    public static void updateEntity(Team team, TeamRequest request) {
        team.setName(request.getName().trim());
        team.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );
        team.setCoach(request.getCoach().trim());
    }

    public static TeamResponse toResponse(Team team) {
        List<Competitor> members = team.getMembers() == null
                ? Collections.emptyList()
                : team.getMembers();

        List<TeamResponse.MemberResponse> memberResponses = members.stream()
                .map(TeamMapper::toMemberResponse)
                .collect(Collectors.toList());

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreationDate(),
                team.getCoach(),
                team.getStatus(),
                team.getVictories(),
                team.getDefeats(),
                memberResponses.size(),
                memberResponses
        );
    }

    private static TeamResponse.MemberResponse toMemberResponse(Competitor competitor) {
        return new TeamResponse.MemberResponse(
                competitor.getId(),
                competitor.getName(),
                competitor.getNickname()
        );
    }
}