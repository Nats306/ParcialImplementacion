package com.parcialimplementacion.parcialenanosvscamellos.standings.service;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.result.entity.RaceResult;
import com.parcialimplementacion.parcialenanosvscamellos.result.entity.ResultStatus;
import com.parcialimplementacion.parcialenanosvscamellos.result.repository.IRaceResultRepository;
import com.parcialimplementacion.parcialenanosvscamellos.result.util.ResultPoints;
import com.parcialimplementacion.parcialenanosvscamellos.standings.dto.CompetitorStandingResponse;
import com.parcialimplementacion.parcialenanosvscamellos.standings.dto.StandingsResponse;
import com.parcialimplementacion.parcialenanosvscamellos.standings.dto.TeamStandingResponse;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StandingsService {

    private final IRaceResultRepository resultRepository;

    public StandingsService(
            IRaceResultRepository resultRepository
    ) {
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public StandingsResponse getStandings() {

        List<RaceResult> results =
                resultRepository.findAll();

        return new StandingsResponse(
                buildCompetitorStandings(results),
                buildTeamStandings(results)
        );
    }

    @Transactional(readOnly = true)
    public List<CompetitorStandingResponse>
    getCompetitorStandings() {

        return buildCompetitorStandings(
                resultRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public List<TeamStandingResponse>
    getTeamStandings() {

        return buildTeamStandings(
                resultRepository.findAll()
        );
    }

    private List<CompetitorStandingResponse>
    buildCompetitorStandings(
            List<RaceResult> results
    ) {

        Map<UUID, List<RaceResult>> grouped =
                results.stream()
                        .filter(
                                result ->
                                        result.getRegistration()
                                                .getCompetitor()
                                                != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        result ->
                                                result.getRegistration()
                                                        .getCompetitor()
                                                        .getId()
                                )
                        );

        List<CompetitorStandingResponse> unranked =
                grouped.values()
                        .stream()
                        .map(this::buildCompetitorEntry)
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                CompetitorStandingResponse::points
                                        )
                                        .reversed()
                                        .thenComparing(
                                                Comparator
                                                        .comparingInt(
                                                                CompetitorStandingResponse::victories
                                                        )
                                                        .reversed()
                                        )
                                        .thenComparing(
                                                Comparator
                                                        .comparingInt(
                                                                CompetitorStandingResponse::secondPlaces
                                                        )
                                                        .reversed()
                                        )
                                        .thenComparing(
                                                CompetitorStandingResponse::name,
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                        )
                        .toList();

        List<CompetitorStandingResponse> ranked =
                new ArrayList<>();

        for (int i = 0; i < unranked.size(); i++) {

            CompetitorStandingResponse entry =
                    unranked.get(i);

            ranked.add(
                    new CompetitorStandingResponse(
                            i + 1,
                            entry.competitorId(),
                            entry.name(),
                            entry.nickname(),
                            entry.points(),
                            entry.victories(),
                            entry.secondPlaces(),
                            entry.thirdPlaces(),
                            entry.racesWithResults()
                    )
            );
        }

        return ranked;
    }

    private CompetitorStandingResponse
    buildCompetitorEntry(
            List<RaceResult> results
    ) {

        Competitor competitor =
                results.get(0)
                        .getRegistration()
                        .getCompetitor();

        int points =
                results.stream()
                        .mapToInt(
                                result ->
                                        ResultPoints.calculate(
                                                result.getResultStatus(),
                                                result.getFinalPosition()
                                        )
                        )
                        .sum();

        int victories =
                countPosition(results, 1);

        int seconds =
                countPosition(results, 2);

        int thirds =
                countPosition(results, 3);

        return new CompetitorStandingResponse(
                0,
                competitor.getId(),
                competitor.getName(),
                competitor.getNickname(),
                points,
                victories,
                seconds,
                thirds,
                results.size()
        );
    }

    private List<TeamStandingResponse>
    buildTeamStandings(
            List<RaceResult> results
    ) {

        Map<Long, List<RaceResult>> grouped =
                results.stream()
                        .filter(
                                result ->
                                        result.getRegistration()
                                                .getTeam()
                                                != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        result ->
                                                result.getRegistration()
                                                        .getTeam()
                                                        .getId()
                                )
                        );

        List<TeamStandingResponse> unranked =
                grouped.values()
                        .stream()
                        .map(this::buildTeamEntry)
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                TeamStandingResponse::points
                                        )
                                        .reversed()
                                        .thenComparing(
                                                Comparator
                                                        .comparingInt(
                                                                TeamStandingResponse::victories
                                                        )
                                                        .reversed()
                                        )
                                        .thenComparing(
                                                Comparator
                                                        .comparingInt(
                                                                TeamStandingResponse::secondPlaces
                                                        )
                                                        .reversed()
                                        )
                                        .thenComparing(
                                                TeamStandingResponse::name,
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                        )
                        .toList();

        List<TeamStandingResponse> ranked =
                new ArrayList<>();

        for (int i = 0; i < unranked.size(); i++) {

            TeamStandingResponse entry =
                    unranked.get(i);

            ranked.add(
                    new TeamStandingResponse(
                            i + 1,
                            entry.teamId(),
                            entry.name(),
                            entry.points(),
                            entry.victories(),
                            entry.secondPlaces(),
                            entry.thirdPlaces(),
                            entry.racesWithResults()
                    )
            );
        }

        return ranked;
    }

    private TeamStandingResponse buildTeamEntry(
            List<RaceResult> results
    ) {

        Team team =
                results.get(0)
                        .getRegistration()
                        .getTeam();

        int points =
                results.stream()
                        .mapToInt(
                                result ->
                                        ResultPoints.calculate(
                                                result.getResultStatus(),
                                                result.getFinalPosition()
                                        )
                        )
                        .sum();

        return new TeamStandingResponse(
                0,
                team.getId(),
                team.getName(),
                points,
                countPosition(results, 1),
                countPosition(results, 2),
                countPosition(results, 3),
                results.size()
        );
    }

    private int countPosition(
            List<RaceResult> results,
            int position
    ) {

        return Math.toIntExact(
                results.stream()
                        .filter(
                                result ->
                                        result.getResultStatus()
                                                == ResultStatus.FINISHED
                        )
                        .filter(
                                result ->
                                        Integer.valueOf(position)
                                                .equals(
                                                        result.getFinalPosition()
                                                )
                        )
                        .count()
        );
    }
}