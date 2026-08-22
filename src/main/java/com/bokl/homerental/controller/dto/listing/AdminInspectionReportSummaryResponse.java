package com.bokl.homerental.controller.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;

public class AdminInspectionReportSummaryResponse {

    private Long id;
    private Long applicationId;
    private Long scheduleId;
    private Long inspectorId;
    private String recommendation;
    private BigDecimal agreedRent;
    private String comments;
    private Instant createdAt;

    public AdminInspectionReportSummaryResponse() {
    }

    public AdminInspectionReportSummaryResponse(Long id, Long applicationId, Long scheduleId, Long inspectorId,
                                                String recommendation, BigDecimal agreedRent, String comments,
                                                Instant createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.scheduleId = scheduleId;
        this.inspectorId = inspectorId;
        this.recommendation = recommendation;
        this.agreedRent = agreedRent;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public BigDecimal getAgreedRent() { return agreedRent; }
    public void setAgreedRent(BigDecimal agreedRent) { this.agreedRent = agreedRent; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
