package com.bokl.homerental.controller.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;

public class ListingResponse {

    private Long id;
    private Long applicationId;
    private Long ownerId;
    private Long propertyDetailId;
    private Long decisionId;
    private BigDecimal finalRent;
    private String status;
    private Instant activatedAt;

    public ListingResponse() {
    }

    public ListingResponse(Long id, Long applicationId, Long ownerId, Long propertyDetailId,
                            Long decisionId, BigDecimal finalRent, String status, Instant activatedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.ownerId = ownerId;
        this.propertyDetailId = propertyDetailId;
        this.decisionId = decisionId;
        this.finalRent = finalRent;
        this.status = status;
        this.activatedAt = activatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getPropertyDetailId() { return propertyDetailId; }
    public void setPropertyDetailId(Long propertyDetailId) { this.propertyDetailId = propertyDetailId; }

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }

    public BigDecimal getFinalRent() { return finalRent; }
    public void setFinalRent(BigDecimal finalRent) { this.finalRent = finalRent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getActivatedAt() { return activatedAt; }
    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }
}
