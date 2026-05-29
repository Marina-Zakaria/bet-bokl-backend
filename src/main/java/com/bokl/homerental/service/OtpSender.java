package com.bokl.homerental.service;

/**
 * Sends a one-time password to the user via the configured channel (SMS or email).
 * <p>
 * This is a port — implement it for your real SMS or email provider.
 * The default implementation ({@link StubOtpSender}) logs the OTP to the console
 * and must be replaced before going to production.
 */
public interface OtpSender {

    /**
     * @param identifier The phone number (E.164 format) or email address of the recipient.
     * @param otp        The plain-text 6-digit OTP to send. Do NOT log this in production.
     */
    void send(String identifier, String otp);
}
