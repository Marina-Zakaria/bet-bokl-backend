package com.bokl.homerental.security;

import java.time.Instant;

/**
 * Attached as the {@code details} object on every authenticated
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}.
 * <p>
 * Carries just enough information for {@code @RequiresActiveSession} to check
 * whether the JWT was issued before or after the user's last password change —
 * without an additional database call in the normal authentication path.
 */
public record JwtAuthDetails(String username, Instant issuedAt) {}
