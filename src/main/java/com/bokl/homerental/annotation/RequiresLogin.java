package com.bokl.homerental.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Requires the request to carry a valid JWT access token.
 * Returns HTTP 401 if the token is missing or invalid.
 *
 * Usage:
 * <pre>
 *   {@literal @}RequiresLogin
 *   {@literal @}GetMapping("/profile")
 *   public ProfileResponse getProfile() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("isAuthenticated()")
public @interface RequiresLogin {}
