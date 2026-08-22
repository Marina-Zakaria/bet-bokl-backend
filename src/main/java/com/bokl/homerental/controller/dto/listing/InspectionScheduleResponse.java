package com.bokl.homerental.controller.dto.listing;

import java.time.Instant;

public class InspectionScheduleResponse {
    private Long id;
    private Long applicationId;
    private Instant proposedStart;
    private Instant proposedEnd;
    private Instant exactTime;
    private String status;
    private Integer selectionOrder;
    private Instant confirmedAt;
    private Instant createdAt;

    public InspectionScheduleResponse(Long id, Long applicationId, Instant proposedStart, Instant proposedEnd,
                                      Instant exactTime, String status, Integer selectionOrder,
                                      Instant confirmedAt, Instant createdAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.proposedStart = proposedStart;
        this.proposedEnd = proposedEnd;
        this.exactTime = exactTime;
        this.status = status;
        this.selectionOrder = selectionOrder;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Instant getProposedStart() { return proposedStart; }
    public void setProposedStart(Instant proposedStart) { this.proposedStart = proposedStart; }

    public Instant getProposedEnd() { return proposedEnd; }
    public void setProposedEnd(Instant proposedEnd) { this.proposedEnd = proposedEnd; }

    public Instant getExactTime() { return exactTime; }
    public void setExactTime(Instant exactTime) { this.exactTime = exactTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getSelectionOrder() { return selectionOrder; }
    public void setSelectionOrder(Integer selectionOrder) { this.selectionOrder = selectionOrder; }

    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
