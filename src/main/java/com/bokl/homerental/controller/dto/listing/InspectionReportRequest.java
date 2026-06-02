package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class InspectionReportRequest {

    @NotNull
    private String recommendation;

    @NotNull
    private BigDecimal agreedRent;

    @NotNull
    private Map<String, Object> reportData;

    private List<String> evidencePhotos;

    private String comments;

    @Valid
    @NotNull
    private PropertyDetailRequest propertyDetail;

    public InspectionReportRequest() {
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public BigDecimal getAgreedRent() {
        return agreedRent;
    }

    public void setAgreedRent(BigDecimal agreedRent) {
        this.agreedRent = agreedRent;
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }

    public void setReportData(Map<String, Object> reportData) {
        this.reportData = reportData;
    }

    public List<String> getEvidencePhotos() {
        return evidencePhotos;
    }

    public void setEvidencePhotos(List<String> evidencePhotos) {
        this.evidencePhotos = evidencePhotos;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public PropertyDetailRequest getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetailRequest propertyDetail) {
        this.propertyDetail = propertyDetail;
    }
}
