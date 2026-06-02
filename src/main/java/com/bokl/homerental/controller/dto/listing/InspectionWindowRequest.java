package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class InspectionWindowRequest {

    @NotNull
    private Instant proposedStart;

    @NotNull
    private Instant proposedEnd;

    public InspectionWindowRequest() {
    }

    public Instant getProposedStart() {
        return proposedStart;
    }

    public void setProposedStart(Instant proposedStart) {
        this.proposedStart = proposedStart;
    }

    public Instant getProposedEnd() {
        return proposedEnd;
    }

    public void setProposedEnd(Instant proposedEnd) {
        this.proposedEnd = proposedEnd;
    }
}
