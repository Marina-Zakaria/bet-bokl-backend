package com.bokl.homerental.controller.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;

public class InspectionReportResponse {

    private Long id;
    private Long scheduleId;
    private Long inspectorId;
    private PropertyDetailResponse propertyDetail;
    private String recommendation;
    private BigDecimal agreedRent;
    private String comments;
    private Instant createdAt;

    public InspectionReportResponse() {
    }

    public InspectionReportResponse(Long id, Long scheduleId, Long inspectorId, PropertyDetailResponse propertyDetail,
                                     String recommendation, BigDecimal agreedRent, String comments, Instant createdAt) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.inspectorId = inspectorId;
        this.propertyDetail = propertyDetail;
        this.recommendation = recommendation;
        this.agreedRent = agreedRent;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }

    public PropertyDetailResponse getPropertyDetail() { return propertyDetail; }
    public void setPropertyDetail(PropertyDetailResponse propertyDetail) { this.propertyDetail = propertyDetail; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public BigDecimal getAgreedRent() { return agreedRent; }
    public void setAgreedRent(BigDecimal agreedRent) { this.agreedRent = agreedRent; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
