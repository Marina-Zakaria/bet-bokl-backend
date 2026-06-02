package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdminDecisionRequest {

    @NotBlank
    private String decision;

    private BigDecimal finalRent;

    private String comments;

    public AdminDecisionRequest() {
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public BigDecimal getFinalRent() {
        return finalRent;
    }

    public void setFinalRent(BigDecimal finalRent) {
        this.finalRent = finalRent;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
