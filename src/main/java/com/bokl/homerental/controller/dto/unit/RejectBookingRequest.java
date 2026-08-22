package com.bokl.homerental.controller.dto.unit;

import jakarta.validation.constraints.Size;

public class RejectBookingRequest {
    @Size(max = 2000)
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
