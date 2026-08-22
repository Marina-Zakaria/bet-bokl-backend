package com.bokl.homerental.controller.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;

public class AdminDecisionResponse {

    private Long id;
    private Long applicationId;
    private Long adminId;
    private Long reportId;
    private String decision;
    private BigDecimal finalRent;
    private String comments;
    private Instant createdAt;

    public AdminDecisionResponse() {
    }

    public AdminDecisionResponse(Long id, Long applicationId, Long adminId, Long reportId,
                                  String decision, BigDecimal finalRent, String comments, Instant createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.adminId = adminId;
        this.reportId = reportId;
        this.decision = decision;
        this.finalRent = finalRent;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public BigDecimal getFinalRent() { return finalRent; }
    public void setFinalRent(BigDecimal finalRent) { this.finalRent = finalRent; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
