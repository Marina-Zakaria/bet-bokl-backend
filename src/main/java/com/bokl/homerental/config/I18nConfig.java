package com.bokl.homerental.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class I18nConfig {

    /**
     * Loads translated messages from {@code src/main/resources/messages*.properties}.
     *
     * <ul>
     *   <li>UTF-8 encoding — Arabic and other non-Latin scripts work without escaping.
     *   <li>No system-locale fallback — if a key is missing from the requested language
     *       file, Spring falls back to {@code messages.properties} (English), not to the
     *       OS locale of the server.
     *   <li>Missing keys throw {@code NoSuchMessageException} — caught as a 500 by
     *       {@code GlobalExceptionHandler}. This forces every key to be declared so
     *       missing translations surface during development, not in production.
     * </ul>
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    /**
     * Resolves the active locale from the {@code Accept-Language} HTTP header.
     *
     * <ul>
     *   <li>Defaults to English when the header is absent or blank.
     *   <li>Ignores any locale not listed in {@code supportedLocales} and falls back
     *       to English — prevents unexpected language files from being loaded.
     * </ul>
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(
                Locale.ENGLISH,
                Locale.forLanguageTag("ar")
        ));
        return resolver;
    }
}
