package com.bokl.homerental.annotation;

import java.lang.annotation.*;

/**
 * Requires the request's JWT to have been issued <em>after</em> the user's last
 * password change ({@code auth_users.password_changed_at}).
 * <p>
 * Use on endpoints where stale sessions must be invalidated after a password reset —
 * for example: change-password, delete-account, high-value profile updates.
 * <p>
 * Triggers one database lookup per request (loads the user to read {@code passwordChangedAt}).
 * Only annotate endpoints where this extra security check is warranted.
 * <p>
 * Returns HTTP 401 if the JWT pre-dates the last password change, or if the user
 * is not authenticated at all.
 *
 * Usage:
 * <pre>
 *   {@literal @}RequiresActiveSession
 *   {@literal @}PutMapping("/account/password")
 *   public void changePassword() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresActiveSession {}
