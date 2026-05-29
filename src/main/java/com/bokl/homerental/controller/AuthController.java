package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.*;
import com.bokl.homerental.security.RsaKeyProvider;
import com.bokl.homerental.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService    authService;
    private final RsaKeyProvider rsaKeyProvider;

    public AuthController(AuthService authService, RsaKeyProvider rsaKeyProvider) {
        this.authService    = authService;
        this.rsaKeyProvider = rsaKeyProvider;
    }

    /**
     * Returns the server's RSA public key (DER, Base64-encoded).
     * Clients use this key to encrypt sensitive fields (password, otp) before sending.
     */
    @GetMapping("/public-key")
    public Map<String, String> publicKey() {
        return Map.of("publicKey", rsaKeyProvider.getPublicKeyBase64());
    }

    /**
     * Creates a new account and sends an OTP for verification.
     * The account cannot be used until {@code /auth/verify-otp} is called.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    /**
     * Validates the OTP sent during registration.
     * On success, returns a short-lived registration token to be used in /auth/complete-registration.
     */
    @PostMapping("/verify-otp")
    public RegistrationTokenResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        return authService.verifyOtp(req);
    }

    /**
     * Sets the account password using the registration token returned by /auth/verify-otp.
     * Auto-generates a username from the phone number, marks the account verified,
     * and returns JWT + refresh token so the user is immediately logged in.
     */
    @PostMapping("/complete-registration")
    public LoginResponse completeRegistration(@Valid @RequestBody CompleteRegistrationRequest req) {
        return authService.completeRegistration(req);
    }

    /**
     * Authenticates with username/phone and password.
     * Returns JWT access token + refresh token on success.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /**
     * Revokes the given refresh token.
     * The access token continues to be valid until its expiry — clients must discard it.
     */
    @PostMapping("/logout")
    @RequiresLogin
    public MessageResponse logout(@Valid @RequestBody RefreshTokenRequest req) {
        return authService.logout(req.refreshToken());
    }

    /**
     * Exchanges a valid, non-revoked refresh token for a new JWT access token.
     */
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return authService.refresh(req.refreshToken());
    }

    /**
     * Resends an OTP to the user's phone.
     * Respects {@code otp_max_resend_count} and {@code otp_resend_window_minutes} from app_config.
     */
    @PostMapping("/resend-otp")
    public MessageResponse resendOtp(@Valid @RequestBody ResendOtpRequest req) {
        return authService.resendOtp(req);
    }

    /**
     * Sends a password-reset OTP.
     * Always returns 200 regardless of whether the identifier exists — prevents enumeration.
     */
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return authService.forgotPassword(req);
    }

    /**
     * Validates the OTP and sets a new password.
     * Revokes all refresh tokens for the user on success.
     */
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return authService.resetPassword(req);
    }
}
