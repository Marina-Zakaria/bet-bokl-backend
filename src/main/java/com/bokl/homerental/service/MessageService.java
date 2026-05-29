package com.bokl.homerental.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around Spring's {@link MessageSource} that resolves the caller's
 * locale automatically from {@link LocaleContextHolder} — populated per-request by
 * the {@code AcceptHeaderLocaleResolver} declared in {@code I18nConfig}.
 *
 * <p>Inject this instead of {@code MessageSource} directly so services never need to
 * pass a {@code Locale} argument manually.
 *
 * <p>Usage:
 * <pre>
 * // Simple key lookup
 * msg.get("auth.otp.sent")
 *
 * // Key with positional arguments (MessageFormat syntax)
 * msg.get("error.route.not_found", "GET", "/unknown")
 * </pre>
 *
 * <p>If a key is missing from both the requested language file and the default
 * {@code messages.properties}, Spring throws {@code NoSuchMessageException}, which
 * the global exception handler converts to a 500. This is intentional — missing keys
 * should surface during development, not silently return the key name.
 */
@Component
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolve {@code key} using the locale set by the current HTTP request.
     *
     * @param key  the message key (e.g. {@code "auth.otp.sent"})
     * @param args optional positional arguments for {@code MessageFormat} placeholders
     * @return the translated message string
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
