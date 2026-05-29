package com.bokl.homerental.controller.dto;

public record RegistrationTokenResponse(
        String registrationToken,
        String message
) {}
