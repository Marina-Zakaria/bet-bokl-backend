package com.bokl.homerental.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "must be a valid phone number")
        String phone
) {}
