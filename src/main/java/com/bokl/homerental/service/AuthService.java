package com.bokl.homerental.service;

import com.bokl.homerental.controller.dto.*;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.RefreshToken;
import com.bokl.homerental.entity.UserRole;
import com.bokl.homerental.repository.AuthUserRepository;
import com.bokl.homerental.repository.RefreshTokenRepository;
import com.bokl.homerental.repository.UserRoleRepository;
import com.bokl.homerental.security.JwtTokenProvider;
import com.bokl.homerental.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import com.bokl.homerental.service.MessageService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthUserRepository     authUserRepo;
    private final UserRoleRepository     roleRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final AppConfigService       appConfigService;
    private final OtpService             otpService;
    private final OtpSender             otpSender;
    private final JwtTokenProvider       jwtProvider;
    private final PasswordEncoder        passwordEncoder;
    private final FieldDecryptionService fieldDecryptionService;
    private final MessageService         msg;

    public AuthService(AuthUserRepository authUserRepo,
                       UserRoleRepository roleRepo,
                       RefreshTokenRepository refreshTokenRepo,
                       AppConfigService appConfigService,
                       OtpService otpService,
                       OtpSender otpSender,
                       JwtTokenProvider jwtProvider,
                       PasswordEncoder passwordEncoder,
                       FieldDecryptionService fieldDecryptionService,
                       MessageService msg) {
        this.authUserRepo          = authUserRepo;
        this.roleRepo              = roleRepo;
        this.refreshTokenRepo      = refreshTokenRepo;
        this.appConfigService      = appConfigService;
        this.otpService            = otpService;
        this.otpSender             = otpSender;
        this.jwtProvider           = jwtProvider;
        this.passwordEncoder       = passwordEncoder;
        this.fieldDecryptionService = fieldDecryptionService;
        this.msg                   = msg;
    }

    // ── Register ─────────────────────────────────────────────────────────────

    public MessageResponse register(RegisterRequest req) {
        if (authUserRepo.existsByPhone(req.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, msg.get("auth.register.phone_exists"));
        }

        AuthUser user = new AuthUser();
        user.setName(req.name());
        user.setPhone(req.phone());

        OtpService.OtpPair otp = otpService.generate();
        user.setOtpHash(otp.hash());
        user.setOtpExpiryTime(Instant.now().plus(otpService.expiryMinutes(), ChronoUnit.MINUTES));

        authUserRepo.save(user);

        otpSender.send(user.getPhone(), otp.plain());

        return new MessageResponse(msg.get("auth.register.otp_sent"));
    }

    // ── Verify OTP ───────────────────────────────────────────────────────────

    public RegistrationTokenResponse verifyOtp(VerifyOtpRequest req) {
        AuthUser user = authUserRepo.findByPhone(req.identifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        msg.get("auth.user.not_found")));

        validateOtp(user, fieldDecryptionService.decryptIfEncrypted(req.otp()));

        int expiryMins = appConfigService.getInt("registration_token_expiry_minutes", 30);
        String token = generateSecureToken();
        user.setRegistrationToken(sha256(token));
        user.setRegistrationTokenExpiresAt(Instant.now().plus(expiryMins, ChronoUnit.MINUTES));
        user.setOtpHash(null);
        user.setOtpExpiryTime(null);
        user.setOtpResendCount(0);

        return new RegistrationTokenResponse(token, msg.get("auth.otp.verified"));
    }

    // ── Complete Registration ────────────────────────────────────────────────

    public LoginResponse completeRegistration(CompleteRegistrationRequest req) {
        String tokenHash = sha256(req.registrationToken());
        AuthUser user = authUserRepo.findByRegistrationToken(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        msg.get("auth.token.invalid")));

        if (user.getRegistrationTokenExpiresAt() == null
                || Instant.now().isAfter(user.getRegistrationTokenExpiresAt())) {
            user.setRegistrationToken(null);
            user.setRegistrationTokenExpiresAt(null);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("auth.token.invalid"));
        }

        // Derive username from phone digits (strip leading +), guaranteed unique since phone is unique
        String derivedUsername = user.getPhone().replaceFirst("^\\+", "");
        user.setUsername(derivedUsername);
        user.setHashedPassword(passwordEncoder.encode(
                fieldDecryptionService.decryptIfEncrypted(req.password())));

        UserRole userRole = roleRepo.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role USER not found in database"));
        user.setRoles(Set.of(userRole));

        user.setVerified(true);
        user.setRegistrationToken(null);
        user.setRegistrationTokenExpiresAt(null);

        return issueTokens(user);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public LoginResponse login(LoginRequest req) {
        AuthUser user = authUserRepo.findByUsernameOrPhone(req.usernameOrPhone())
                // Use a generic message to prevent username enumeration (OWASP A01)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        msg.get("auth.credentials.invalid")));

        checkLockout(user);

        if (!user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("auth.account.not_verified"));
        }
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg.get("auth.account.disabled"));
        }

        String rawPassword = fieldDecryptionService.decryptIfEncrypted(req.password());
        if (user.getHashedPassword() == null || !passwordEncoder.matches(rawPassword, user.getHashedPassword())) {
            handleFailedLogin(user);
            // Always return the same message on failure — prevents password vs username enumeration
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg.get("auth.credentials.invalid"));
        }

        // Successful login — reset failure counters
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLogin(Instant.now());

        return issueTokens(user);
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    public MessageResponse logout(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        refreshTokenRepo.findByTokenHash(hash).ifPresent(rt -> rt.setRevoked(true));
        return new MessageResponse(msg.get("auth.logout.success"));
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    public LoginResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken rt = refreshTokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        msg.get("auth.refresh.invalid")));

        if (rt.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg.get("auth.refresh.revoked"));
        }
        if (Instant.now().isAfter(rt.getExpiresAt())) {
            rt.setRevoked(true);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg.get("auth.refresh.expired"));
        }

        AuthUser user = rt.getUser();
        if (!user.isActive() || !user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg.get("auth.account.not_accessible"));
        }

        // Issue a new access token; keep the same refresh token (no rotation)
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                .collect(Collectors.toList());

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getUsername(), authorities);

        return new LoginResponse(
                newAccessToken,
                rawRefreshToken,   // return the same refresh token unchanged
                "Bearer",
                jwtProvider.accessTokenExpirySeconds(),
                new LoginResponse.UserInfo(user.getId(), user.getUsername(), user.getName(),
                        user.getRoles().stream().map(UserRole::getRoleName).collect(Collectors.toList()))
        );
    }

    // ── Resend OTP ───────────────────────────────────────────────────────────

    public MessageResponse resendOtp(ResendOtpRequest req) {
        AuthUser user = authUserRepo.findByPhone(req.identifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        msg.get("auth.user.not_found")));

        // Reset resend count if the window has elapsed
        int windowMinutes = appConfigService.getInt("otp_resend_window_minutes", 60);
        if (user.getOtpResendResetAt() == null
                || Instant.now().isAfter(user.getOtpResendResetAt().plus(windowMinutes, ChronoUnit.MINUTES))) {
            user.setOtpResendCount(0);
            user.setOtpResendResetAt(Instant.now());
        }

        int maxResend = appConfigService.getInt("otp_max_resend_count", 3);
        if (user.getOtpResendCount() >= maxResend) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    msg.get("auth.otp.resend_limit"));
        }

        OtpService.OtpPair otp = otpService.generate();
        user.setOtpHash(otp.hash());
        user.setOtpExpiryTime(Instant.now().plus(otpService.expiryMinutes(), ChronoUnit.MINUTES));
        user.setOtpResendCount(user.getOtpResendCount() + 1);

        otpSender.send(user.getPhone(), otp.plain());

        return new MessageResponse(msg.get("auth.otp.resent"));
    }

    // ── Forgot Password ──────────────────────────────────────────────────────

    public MessageResponse forgotPassword(ForgotPasswordRequest req) {
        // Always return success — never reveal whether the identifier exists (OWASP A01)
        authUserRepo.findByPhone(req.identifier()).ifPresentOrElse(
                user -> {
                    OtpService.OtpPair otp = otpService.generate();
                    user.setOtpHash(otp.hash());
                    user.setOtpExpiryTime(Instant.now().plus(otpService.expiryMinutes(), ChronoUnit.MINUTES));
                    user.setOtpResendCount(0);
                    user.setOtpResendResetAt(Instant.now());
                    otpSender.send(user.getPhone(), otp.plain());
                },
                () -> log.warn("Forgot-password requested for unknown identifier: {}", req.identifier())
        );
        return new MessageResponse(msg.get("auth.forgot_password.sent"));
    }

    // ── Reset Password ───────────────────────────────────────────────────────

    public MessageResponse resetPassword(ResetPasswordRequest req) {
        AuthUser user = authUserRepo.findByPhone(req.identifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        msg.get("auth.user.not_found")));

        validateOtp(user, fieldDecryptionService.decryptIfEncrypted(req.otp()));

        user.setHashedPassword(passwordEncoder.encode(
                fieldDecryptionService.decryptIfEncrypted(req.newPassword())));
        user.setPasswordChangedAt(Instant.now());
        user.setOtpHash(null);
        user.setOtpExpiryTime(null);
        user.setOtpResendCount(0);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        // Revoke all existing refresh tokens — forces re-login with the new password
        refreshTokenRepo.revokeAllByUser(user);

        return new MessageResponse(msg.get("auth.password.reset.success"));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private LoginResponse issueTokens(AuthUser user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                .collect(Collectors.toList());

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getUsername(), authorities);

        // Generate a random refresh token, store its SHA-256 hash in DB
        String rawRefreshToken = generateSecureToken();
        int expiryDays = appConfigService.getInt("refresh_token_expiry_days", 30);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(sha256(rawRefreshToken));
        rt.setExpiresAt(Instant.now().plus(expiryDays, ChronoUnit.DAYS));
        refreshTokenRepo.save(rt);

        return new LoginResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                jwtProvider.accessTokenExpirySeconds(),
                new LoginResponse.UserInfo(user.getId(), user.getUsername(), user.getName(),
                        user.getRoles().stream().map(UserRole::getRoleName).collect(Collectors.toList()))
        );
    }

    private void validateOtp(AuthUser user, String rawOtp) {
        if (user.getOtpHash() == null || user.getOtpExpiryTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("auth.otp.not_issued"));
        }
        if (Instant.now().isAfter(user.getOtpExpiryTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg.get("auth.otp.expired"));
        }
        if (!otpService.matches(rawOtp, user.getOtpHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg.get("auth.otp.invalid"));
        }
    }

    private void checkLockout(AuthUser user) {
        if (user.getAccountLockedUntil() != null && Instant.now().isBefore(user.getAccountLockedUntil())) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    msg.get("auth.account.locked"));
        }
    }

    private void handleFailedLogin(AuthUser user) {
        int maxAttempts = appConfigService.getInt("login_max_failed_attempts", 5);
        int lockoutMins = appConfigService.getInt("account_lockout_minutes", 30);
        int attempts    = user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxAttempts) {
            user.setAccountLockedUntil(Instant.now().plus(lockoutMins, ChronoUnit.MINUTES));
            log.warn("Account locked after {} failed attempts: username={}", attempts, user.getUsername());
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
