package com.bokl.homerental.controller.dto.listing;

import java.time.Instant;

public class PropertyApplicationResponse {

    private Long id;
    private String status;
    private Instant submittedAt;
    private Instant updatedAt;
    private PropertyDetailResponse propertyDetail;

    public PropertyApplicationResponse() {
    }

    public PropertyApplicationResponse(Long id, String status, Instant submittedAt, Instant updatedAt, PropertyDetailResponse propertyDetail) {
        this.id = id;
        this.status = status;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
        this.propertyDetail = propertyDetail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public PropertyDetailResponse getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetailResponse propertyDetail) {
        this.propertyDetail = propertyDetail;
    }
}
