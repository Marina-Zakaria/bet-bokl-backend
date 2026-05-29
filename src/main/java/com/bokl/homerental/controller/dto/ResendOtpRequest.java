package com.bokl.homerental.controller.dto;

import jakarta.validation.constraints.NotBlank;

/** @param identifier Phone number (OTP_CHANNEL=phone). */
public record ResendOtpRequest(
        @NotBlank
        String identifier
) {}
