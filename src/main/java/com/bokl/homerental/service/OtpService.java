package com.bokl.homerental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordEncoder passwordEncoder;
    private final AppConfigService appConfigService;
    private final String fixedOtpValue;

    public OtpService(PasswordEncoder passwordEncoder, AppConfigService appConfigService,
                      @Value("${otp.fixed-value:}") String fixedOtpValue) {
        this.passwordEncoder  = passwordEncoder;
        this.appConfigService = appConfigService;
        this.fixedOtpValue    = fixedOtpValue;
    }

    /**
     * Generates an OTP and its BCrypt hash.
     * When {@code otp.fixed-value} is set (non-empty), returns that fixed value
     * instead of a random one — useful for local/test environments.
     */
    public OtpPair generate() {
        String plain = fixedOtpValue.isBlank()
                ? String.format("%0" + OTP_LENGTH + "d", RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH)))
                : fixedOtpValue;
        String hash = passwordEncoder.encode(plain);
        return new OtpPair(plain, hash);
    }

    /**
     * OTP expiry in minutes, sourced from {@code app_config}.
     * Read at call time so a DB update takes effect without restart.
     */
    public int expiryMinutes() {
        return appConfigService.getInt("otp_expiry_minutes", 10);
    }

    /**
     * @param rawOtp      The plain OTP entered by the user.
     * @param storedHash  The BCrypt hash stored in {@code auth_users.otp_hash}.
     * @return {@code true} if the OTP matches the hash; {@code false} otherwise.
     */
    public boolean matches(String rawOtp, String storedHash) {
        return passwordEncoder.matches(rawOtp, storedHash);
    }

    /** Pair of (plain-text OTP to send, BCrypt hash to store). */
    public record OtpPair(String plain, String hash) {}
}
