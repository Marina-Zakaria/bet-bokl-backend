package com.bokl.homerental.controller.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long   expiresInSeconds,
        UserInfo user
) {
    public record UserInfo(
            Long         id,
            String       username,
            String       name,
            List<String> roles
    ) {}
}
