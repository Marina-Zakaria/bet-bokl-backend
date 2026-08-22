package com.bokl.homerental.controller.dto.unit;

import java.time.LocalDate;

public class UnavailabilityResponse {

    private Long id;
    private Long unitId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String source;

    public UnavailabilityResponse() {
    }

    public UnavailabilityResponse(Long id, Long unitId, LocalDate startDate, LocalDate endDate,
                                  String reason, String source) {
        this.id = id;
        this.unitId = unitId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.source = source;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
