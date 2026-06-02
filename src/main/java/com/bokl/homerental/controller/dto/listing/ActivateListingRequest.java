package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ActivateListingRequest {

    @NotNull
    private BigDecimal finalRent;

    public ActivateListingRequest() {
    }

    public BigDecimal getFinalRent() {
        return finalRent;
    }

    public void setFinalRent(BigDecimal finalRent) {
        this.finalRent = finalRent;
    }
}
