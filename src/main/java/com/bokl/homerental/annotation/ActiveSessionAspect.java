package com.bokl.homerental.annotation;

import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.repository.AuthUserRepository;
import com.bokl.homerental.security.JwtAuthDetails;
import com.bokl.homerental.service.MessageService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Enforces {@link RequiresActiveSession}.
 * <p>
 * Loads the user from the database and compares {@code passwordChangedAt}
 * against the JWT's {@code issuedAt} timestamp (stored in {@link JwtAuthDetails}).
 * If the token predates the password change, it is considered stale and the request is rejected.
 */
@Aspect
@Component
public class ActiveSessionAspect {

    private final AuthUserRepository authUserRepository;
    private final MessageService    msg;

    public ActiveSessionAspect(AuthUserRepository authUserRepository, MessageService msg) {
        this.authUserRepository = authUserRepository;
        this.msg               = msg;
    }

    @Around("@annotation(com.bokl.homerental.annotation.RequiresActiveSession) " +
            "|| @within(com.bokl.homerental.annotation.RequiresActiveSession)")
    public Object checkActiveSession(ProceedingJoinPoint pjp) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || !(auth instanceof UsernamePasswordAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    msg.get("auth.error.authentication_required"));
        }

        if (!(auth.getDetails() instanceof JwtAuthDetails details)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    msg.get("auth.error.authentication_context_malformed"));
        }

        AuthUser user = authUserRepository.findByUsername(details.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        msg.get("auth.error.user_not_found")));

        Instant passwordChangedAt = user.getPasswordChangedAt();
        if (passwordChangedAt != null && details.issuedAt().isBefore(passwordChangedAt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    msg.get("auth.error.session_invalid"));
        }

        return pjp.proceed();
    }
}
