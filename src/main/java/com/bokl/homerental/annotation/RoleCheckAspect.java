package com.bokl.homerental.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import com.bokl.homerental.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces {@link RequiresRole}.
 * <p>
 * Reads the user's {@link GrantedAuthority} values from the SecurityContext
 * (populated by {@code JwtAuthenticationFilter} from the JWT claims) —
 * no database call is needed.
 */
@Aspect
@Component
public class RoleCheckAspect {

    private final MessageService msg;

    public RoleCheckAspect(MessageService msg) {
        this.msg = msg;
    }

    @Around("@annotation(com.bokl.homerental.annotation.RequiresRole) " +
            "|| @within(com.bokl.homerental.annotation.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint pjp) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    msg.get("auth.error.authentication_required"));
        }

        RequiresRole annotation = resolveAnnotation(pjp);
        String[] required = annotation.value();

        Set<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean hasAccess = annotation.requireAll()
                ? Arrays.stream(required).allMatch(userRoles::contains)
                : Arrays.stream(required).anyMatch(userRoles::contains);

        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("auth.error.insufficient_permissions"));
        }

        return pjp.proceed();
    }

    private RequiresRole resolveAnnotation(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RequiresRole methodAnnotation = method.getAnnotation(RequiresRole.class);
        if (methodAnnotation != null) return methodAnnotation;
        return pjp.getTarget().getClass().getAnnotation(RequiresRole.class);
    }
}
