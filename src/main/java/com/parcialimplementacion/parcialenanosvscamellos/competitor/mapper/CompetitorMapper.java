package com.parcialimplementacion.parcialenanosvscamellos.competitor.mapper;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.CompetitorRequest;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.CompetitorResponse;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;

public class CompetitorMapper {
    private CompetitorMapper() {} //constructor

    public static Competitor toEntity(CompetitorRequest request) {
        if (request == null) return null;
        return Competitor.builder().name(request.name())
                .nickname(request.nickname())
                .competitorType(request.competitorType())
                .age(request.age())
                .weight(request.weight())
                .height(request.height())
                .country(request.country())
                .currentStatus(CompetitorStatus.ACTIVE)
                .build();
    }

    public static CompetitorResponse toResponse(Competitor competitor) {
        if (competitor == null) return null;
        return new CompetitorResponse(
          competitor.getId(),
                competitor.getName(),
                competitor.getNickname(),
                competitor.getCompetitorType(),
                competitor.getAge(),
                competitor.getWeight(),
                competitor.getHeight(),
                competitor.getCountry(),
                competitor.getCurrentStatus(),
                competitor.getRegistrationDate(),
                competitor.getVictories(),
                competitor.getDefeats(),
                competitor.getCompletedRaces()

        );
    }

    public static void updateEntity(Competitor competitor, CompetitorRequest request) {
        competitor.setName(request.name());
        competitor.setNickname(request.nickname());
        competitor.setAge(request.age());
        competitor.setWeight(request.weight());
        competitor.setHeight(request.height());
        competitor.setCountry(request.country());
    }
}
