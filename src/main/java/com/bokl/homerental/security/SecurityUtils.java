package com.bokl.homerental.security;

import com.bokl.homerental.entity.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class SecurityUtils {

    public static AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getAuthUser();
        }
        throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
    }
}
