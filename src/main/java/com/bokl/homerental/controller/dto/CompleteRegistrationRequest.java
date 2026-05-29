package com.bokl.homerental.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationRequest(
        @NotBlank
        String registrationToken,

        @NotBlank @Size(min = 8, message = "must be at least 8 characters")
        String password
) {}
