package com.parcialimplementacion.parcialenanosvscamellos.team.dto;

import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTeamStatusRequest {

    @NotNull(message = "Team status is required")
    private TeamStatus status;

    public UpdateTeamStatusRequest() {
    }

    public UpdateTeamStatusRequest(TeamStatus status) {
        this.status = status;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }
}