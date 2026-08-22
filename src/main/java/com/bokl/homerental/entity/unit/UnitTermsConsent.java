package com.bokl.homerental.entity.unit;

import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.listing.TermsDefinition;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "unit_terms_consents")
public class UnitTermsConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private RentalUnit unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_definition_id", nullable = false)
    private TermsDefinition termsDefinition;

    @CreationTimestamp
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private Instant acceptedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    public UnitTermsConsent() {
    }

    public Long getId() {
        return id;
    }

    public RentalUnit getUnit() {
        return unit;
    }

    public void setUnit(RentalUnit unit) {
        this.unit = unit;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public TermsDefinition getTermsDefinition() {
        return termsDefinition;
    }

    public void setTermsDefinition(TermsDefinition termsDefinition) {
        this.termsDefinition = termsDefinition;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
