package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class ConfirmInspectionRequest {

    @NotNull
    private Instant exactTime;

    public ConfirmInspectionRequest() {
    }

    public Instant getExactTime() {
        return exactTime;
    }

    public void setExactTime(Instant exactTime) {
        this.exactTime = exactTime;
    }
}
