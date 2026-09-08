package com.parcialimplementacion.parcialenanosvscamellos.race.dto;

import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateRaceStatusRequest {

    @NotNull(message = "Race status is required")
    private RaceStatus status;

    public UpdateRaceStatusRequest() {
    }

    public UpdateRaceStatusRequest(RaceStatus status) {
        this.status = status;
    }

    public RaceStatus getStatus() {
        return status;
    }

    public void setStatus(RaceStatus status) {
        this.status = status;
    }
}