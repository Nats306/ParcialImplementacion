package com.parcialimplementacion.parcialenanosvscamellos.team.dto;

import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate creationDate;
    private String coach;
    private TeamStatus status;
    private Integer victories;
    private Integer defeats;
    private Integer memberCount;
    private List<MemberResponse> members;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberResponse {

        private UUID id;
        private String name;
        private String nickname;
    }
}