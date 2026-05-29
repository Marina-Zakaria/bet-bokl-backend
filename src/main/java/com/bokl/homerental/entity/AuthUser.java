package com.bokl.homerental.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "auth_users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    @Column(unique = true, length = 100)
    private String username;

    @Column(length = 255)
    private String hashedPassword;

    /** SHA-256 hash of the short-lived token returned by /auth/verify-otp. Cleared on complete-registration. */
    @Column(length = 64)
    private String registrationToken;

    @Column
    private Instant registrationTokenExpiresAt;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @Column
    private Instant accountLockedUntil;

    @Column
    private Instant lastLogin;

    /** BCrypt hash of the most recently sent OTP. */
    @Column(length = 255)
    private String otpHash;

    @Column
    private Instant otpExpiryTime;

    @Column(nullable = false)
    private int otpResendCount = 0;

    /** When otpResendCount was last reset — used to enforce the sliding resend window. */
    @Column
    private Instant otpResendResetAt;

    /** Set on every successful password change; used by @RequiresActiveSession to invalidate stale JWTs. */
    @Column
    private Instant passwordChangedAt;

    /**
     * Roles are fetched eagerly because they are always required to build JWT claims
     * and populate Spring Security authorities. The roles set is small (typically 1-2 per user)
     * so eager loading is appropriate here.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "auth_user_roles",
        joinColumns        = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRole> roles = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public AuthUser() {}

    // ── Getters and setters ───────────────────────────────────────────────────

    public Long getId()                                        { return id; }
    public String getName()                                    { return name; }
    public void setName(String name)                           { this.name = name; }
    public String getPhone()                                   { return phone; }
    public void setPhone(String phone)                         { this.phone = phone; }
    public String getUsername()                                { return username; }
    public void setUsername(String username)                   { this.username = username; }
    public String getHashedPassword()                          { return hashedPassword; }
    public void setHashedPassword(String hashedPassword)       { this.hashedPassword = hashedPassword; }
    public boolean isVerified()                                { return verified; }
    public void setVerified(boolean verified)                  { this.verified = verified; }
    public boolean isActive()                                  { return active; }
    public void setActive(boolean active)                      { this.active = active; }
    public int getFailedLoginAttempts()                        { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int attempts)           { this.failedLoginAttempts = attempts; }
    public Instant getAccountLockedUntil()                     { return accountLockedUntil; }
    public void setAccountLockedUntil(Instant t)               { this.accountLockedUntil = t; }
    public Instant getLastLogin()                              { return lastLogin; }
    public void setLastLogin(Instant t)                        { this.lastLogin = t; }
    public String getOtpHash()                                 { return otpHash; }
    public void setOtpHash(String otpHash)                     { this.otpHash = otpHash; }
    public Instant getOtpExpiryTime()                          { return otpExpiryTime; }
    public void setOtpExpiryTime(Instant t)                    { this.otpExpiryTime = t; }
    public int getOtpResendCount()                             { return otpResendCount; }
    public void setOtpResendCount(int count)                   { this.otpResendCount = count; }
    public Instant getOtpResendResetAt()                       { return otpResendResetAt; }
    public void setOtpResendResetAt(Instant t)                 { this.otpResendResetAt = t; }
    public Instant getPasswordChangedAt()                      { return passwordChangedAt; }
    public void setPasswordChangedAt(Instant t)                { this.passwordChangedAt = t; }
    public String getRegistrationToken()                        { return registrationToken; }
    public void setRegistrationToken(String t)                  { this.registrationToken = t; }
    public Instant getRegistrationTokenExpiresAt()              { return registrationTokenExpiresAt; }
    public void setRegistrationTokenExpiresAt(Instant t)        { this.registrationTokenExpiresAt = t; }
    public Set<UserRole> getRoles()                            { return roles; }
    public void setRoles(Set<UserRole> roles)                  { this.roles = roles; }
    public Instant getCreatedAt()                              { return createdAt; }
    public Instant getUpdatedAt()                              { return updatedAt; }
}
