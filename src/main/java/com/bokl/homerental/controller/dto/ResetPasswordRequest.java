package com.bokl.homerental.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        String identifier,

        @NotBlank @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
        String otp,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {}
