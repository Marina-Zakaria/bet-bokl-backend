package com.bokl.homerental.annotation;

import java.lang.annotation.*;

/**
 * Requires the authenticated user to have at least one of the specified roles.
 * <p>
 * Role names must match values in the {@code user_roles} table (e.g. {@code "ADMIN"}).
 * By default, the user must have ANY of the listed roles. Set {@code requireAll = true}
 * to require ALL listed roles.
 * <p>
 * Returns HTTP 403 if the user is authenticated but does not have the required role(s).
 * Returns HTTP 401 if the user is not authenticated.
 *
 * Usage:
 * <pre>
 *   {@literal @}RequiresRole("ADMIN")
 *   {@literal @}DeleteMapping("/users/{id}")
 *   public void deleteUser(@PathVariable Long id) { ... }
 *
 *   {@literal @}RequiresRole(value = {"ADMIN", "INSPECTOR"}, requireAll = false)
 *   {@literal @}GetMapping("/reports")
 *   public List{@literal <}ReportDto{@literal >} getReports() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /** One or more role names that the user must have. */
    String[] value();

    /**
     * If {@code true}, the user must have ALL listed roles.
     * If {@code false} (default), the user must have ANY of the listed roles.
     */
    boolean requireAll() default false;
}
