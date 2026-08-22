package com.bokl.homerental.controller.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;

public class AdminInspectionReportDetailsResponse {

    private Long id;
    private Long applicationId;
    private Long scheduleId;
    private Long inspectorId;
    private PropertyDetailResponse propertyDetail;
    private String recommendation;
    private BigDecimal agreedRent;
    private Object reportData;
    private Object evidencePhotos;
    private String comments;
    private Instant createdAt;
    private Instant updatedAt;

    public AdminInspectionReportDetailsResponse() {
    }

    public AdminInspectionReportDetailsResponse(Long id, Long applicationId, Long scheduleId, Long inspectorId,
                                                PropertyDetailResponse propertyDetail, String recommendation,
                                                BigDecimal agreedRent, Object reportData, Object evidencePhotos,
                                                String comments, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.scheduleId = scheduleId;
        this.inspectorId = inspectorId;
        this.propertyDetail = propertyDetail;
        this.recommendation = recommendation;
        this.agreedRent = agreedRent;
        this.reportData = reportData;
        this.evidencePhotos = evidencePhotos;
        this.comments = comments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

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

    public Object getReportData() { return reportData; }
    public void setReportData(Object reportData) { this.reportData = reportData; }

    public Object getEvidencePhotos() { return evidencePhotos; }
    public void setEvidencePhotos(Object evidencePhotos) { this.evidencePhotos = evidencePhotos; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
