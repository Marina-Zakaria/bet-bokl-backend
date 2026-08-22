package com.bokl.homerental.security;

import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.repository.AuthUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class SecurityUtils {

    private static AuthUserRepository authUserRepository;

    public SecurityUtils(AuthUserRepository authUserRepository) {
        SecurityUtils.authUserRepository = authUserRepository;
    }

    public static AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getAuthUser();
        }
        if (authentication.getPrincipal() instanceof String username) {
            return authUserRepository.findByUsernameOrPhone(username)
                    .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication required"));
        }
        throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
    }
}
