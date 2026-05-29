package com.bokl.homerental.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that assigns a trace ID to every incoming HTTP request.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>If the request carries an {@code X-Trace-ID} header, that value is used as-is.
 *       This allows upstream gateways or clients to inject a correlation ID.
 *   <li>Otherwise a random UUID is generated for the request.
 * </ul>
 *
 * <p>The trace ID is:
 * <ol>
 *   <li>Written to the {@code X-Trace-ID} response header so the caller can correlate
 *       their request to log entries.
 *   <li>Written to the SLF4J MDC under the key {@code traceId} so every subsequent
 *       log line for this request carries the trace ID automatically.
 * </ol>
 *
 * <p>Runs at {@code Ordered.HIGHEST_PRECEDENCE + 1} — immediately after
 * {@code ServiceMdcFilter} which initialises the MDC context. The {@code traceId}
 * key set here overwrites the empty-string placeholder set by that filter.
 *
 * <p>MDC is cleaned up in {@code finally} as a defensive measure. The wrapping
 * {@code ServiceMdcFilter} calls {@code MDC.clear()} for the full context, but
 * this {@code remove} is a safety net if this filter ever runs standalone.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestTracingFilter implements Filter {

    static final String TRACE_HEADER = "X-Trace-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq = (HttpServletRequest)  request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String traceId = httpReq.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        // Set response header before chain executes — headers must be written before output
        httpRes.setHeader(TRACE_HEADER, traceId);
        MDC.put("traceId", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }
}
