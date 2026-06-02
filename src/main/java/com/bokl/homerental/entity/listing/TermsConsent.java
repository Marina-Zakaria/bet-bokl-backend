package com.bokl.homerental.entity.listing;

import com.bokl.homerental.entity.AuthUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "terms_consents")
public class TermsConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyApplication application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_definition_id", nullable = false)
    private TermsDefinition termsDefinition;

    @Column(name = "agreed_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedRent;

    @Column(name = "commission_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @CreationTimestamp
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private Instant acceptedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    public TermsConsent() {
    }

    public Long getId() {
        return id;
    }

    public PropertyApplication getApplication() {
        return application;
    }

    public void setApplication(PropertyApplication application) {
        this.application = application;
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

    public BigDecimal getAgreedRent() {
        return agreedRent;
    }

    public void setAgreedRent(BigDecimal agreedRent) {
        this.agreedRent = agreedRent;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
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
