package com.bokl.homerental.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param identifier Phone number (OTP_CHANNEL=phone).
 * @param otp        The plain 6-digit OTP entered by the user.
 */
public record VerifyOtpRequest(
        @NotBlank
        String identifier,

        @NotBlank @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
        String otp
) {}
