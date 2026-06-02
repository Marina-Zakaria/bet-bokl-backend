package com.bokl.homerental.entity.listing;

import com.bokl.homerental.entity.AuthUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "listings")
public class Listing {

    public enum Status {
        ACTIVE,
        PAUSED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_detail_id", nullable = false)
    private PropertyDetail propertyDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AuthUser owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyApplication application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private AdminDecision decision;

    @Column(name = "final_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalRent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    @Column(name = "activated_at", nullable = false, updatable = false)
    private Instant activatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Listing() {
    }

    public Long getId() {
        return id;
    }

    public PropertyDetail getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetail propertyDetail) {
        this.propertyDetail = propertyDetail;
    }

    public AuthUser getOwner() {
        return owner;
    }

    public void setOwner(AuthUser owner) {
        this.owner = owner;
    }

    public PropertyApplication getApplication() {
        return application;
    }

    public void setApplication(PropertyApplication application) {
        this.application = application;
    }

    public AdminDecision getDecision() {
        return decision;
    }

    public void setDecision(AdminDecision decision) {
        this.decision = decision;
    }

    public BigDecimal getFinalRent() {
        return finalRent;
    }

    public void setFinalRent(BigDecimal finalRent) {
        this.finalRent = finalRent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
