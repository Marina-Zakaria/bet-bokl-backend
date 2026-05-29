package com.bokl.homerental.exception;

/**
 * Standard error response body returned by {@link GlobalExceptionHandler}
 * for all unhandled exceptions.
 *
 * <p>Using a Java record ensures the shape is immutable and serialises cleanly
 * to JSON with Jackson — no extra annotations required.
 *
 * @param timestamp ISO-8601 instant when the error occurred
 * @param status    HTTP status code
 * @param error     HTTP status reason phrase (e.g. "Bad Request")
 * @param message   Human-readable description safe to return to the client
 * @param path      Request URI that produced the error
 * @param traceId   Trace ID from the SLF4J MDC — links this response to the server log entry
 */
public record ErrorResponse(
        String timestamp,
        int    status,
        String error,
        String message,
        String path,
        String traceId
) {}
