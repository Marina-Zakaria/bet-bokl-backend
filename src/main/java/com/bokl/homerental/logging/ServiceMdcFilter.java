package com.bokl.homerental.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that initialises the SLF4J MDC for every request.
 *
 * <p>The {@code service} field is handled statically by {@code customFields}
 * in {@code logback-spring.xml} — it must not be duplicated here.
 *
 * <p>The {@code traceId} key is intentionally left empty here; it is
 * populated by the request-tracing filter (spring-request-tracing skill).
 * Pre-declaring it here means the JSON field is always present in request
 * logs even before the tracing skill is added, keeping the log schema
 * consistent.
 *
 * <p>MDC is cleared in {@code finally} to prevent leakage across requests
 * on pooled threads.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServiceMdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            MDC.put("traceId", "");   // placeholder — overwritten by request-tracing skill
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
