package com.bokl.homerental.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.bokl.homerental.service.MessageService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Catches all unhandled exceptions from controllers and returns a consistent,
 * structured {@link ErrorResponse} so clients always receive the same JSON shape
 * regardless of what went wrong.
 *
 * <p>Security rules enforced here:
 * <ul>
 *   <li>5xx responses always use a generic message — the real exception message is
 *       logged at ERROR level and never returned to the client.
 *   <li>4xx messages are safe to return: they describe the client's mistake, not
 *       server internals.
 *   <li>{@code traceId} is read from the SLF4J MDC and included in every response
 *       so the caller can correlate their error to the server log entry.
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageService      msg;

    public GlobalExceptionHandler(MessageService msg) {
        this.msg = msg;
    }

    /**
     * Validation failure from {@code @Valid} on a request body or request parameters.
     * Returns each failing field and its constraint message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Malformed or unreadable JSON request body (e.g. syntax error, type mismatch).
     * The real parsing error is not forwarded to avoid leaking schema details.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, msg.get("error.body.malformed"), request);
    }

    /**
     * No route matched the incoming request.
     * Requires {@code spring.mvc.throw-exception-if-no-handler-found=true} and
     * {@code spring.web.resources.add-mappings=false} (see application.properties).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        String message = msg.get("error.route.not_found", ex.getHttpMethod(), ex.getRequestURL());
        return build(HttpStatus.NOT_FOUND, message, request);
    }

    /**
     * A valid route exists but does not support the HTTP method used (e.g. POST to a GET endpoint).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String message = msg.get("error.method.not_supported", ex.getMethod());
        return build(HttpStatus.METHOD_NOT_ALLOWED, message, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, msg.get("error.media_type.not_supported"), request);
    }

    /**
     * Explicitly thrown with a specific HTTP status via {@code ResponseStatusException}.
     *
     * <p>4xx: the exception's {@code reason} is forwarded to the client — it describes
     * the client's mistake and is safe to return.
     * <p>5xx: the reason is logged but a generic message is returned — callers must
     * not receive server-internal details even from explicitly thrown exceptions.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        if (status.is5xxServerError()) {
            log.error("ResponseStatusException {}: {}", status.value(), ex.getReason(), ex);
            return build(status, msg.get("error.internal"), request);
        }

        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, message, request);
    }

    /**
     * Catch-all for any exception not handled by the methods above.
     *
     * <p>The full stack trace is logged at ERROR level so it appears in server logs
     * and any connected log aggregator. The response body only ever says
     * "An unexpected error occurred" — the real message is never exposed to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, msg.get("error.internal"), request);
    }

    // ── Private builder ───────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {

        String traceId = MDC.get("traceId");

        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                traceId != null ? traceId : ""
        );

        return ResponseEntity.status(status).body(body);
    }
}
