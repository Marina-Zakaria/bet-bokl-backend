package com.bokl.homerental.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Development-only OTP sender that prints the OTP to the application log.
 * <p>
 * Search for {@code TODO: IMPLEMENT_OTP_SENDER} to find and replace this class
 * before deploying to production.
 * <p>
 * To wire in a real sender: create a new {@code @Service} implementing {@link OtpSender},
 * remove {@code @Primary} from this class (or delete it entirely).
 */
@Service
@Primary
public class StubOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(StubOtpSender.class);

    @Override
    public void send(String identifier, String otp) {
        // TODO: IMPLEMENT_OTP_SENDER — replace with real SMS or email provider
        log.info("TODO: IMPLEMENT_OTP_SENDER — identifier={}, otp={}", identifier, otp);
    }
}
