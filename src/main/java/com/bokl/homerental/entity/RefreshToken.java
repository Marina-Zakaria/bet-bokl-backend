package com.bokl.homerental.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    /**
     * SHA-256 hex digest of the raw token value sent to the client.
     * Never store the raw token — store only the hash.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshToken() {}

    public Long getId()                       { return id; }
    public AuthUser getUser()                 { return user; }
    public void setUser(AuthUser user)        { this.user = user; }
    public String getTokenHash()              { return tokenHash; }
    public void setTokenHash(String hash)     { this.tokenHash = hash; }
    public Instant getExpiresAt()             { return expiresAt; }
    public void setExpiresAt(Instant t)       { this.expiresAt = t; }
    public boolean isRevoked()                { return revoked; }
    public void setRevoked(boolean revoked)   { this.revoked = revoked; }
    public Instant getCreatedAt()             { return createdAt; }
}
