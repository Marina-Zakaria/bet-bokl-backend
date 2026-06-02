package com.bokl.homerental.entity.listing;

import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.inspection.InspectionReport;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "admin_decisions")
public class AdminDecision {

    public enum Decision {
        APPROVE,
        REJECT,
        REQUEST_REINSPECTION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyApplication application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AuthUser admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private InspectionReport report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Decision decision;

    @Column(name = "final_rent", precision = 10, scale = 2)
    private BigDecimal finalRent;

    @Column(columnDefinition = "text")
    private String comments;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AdminDecision() {
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

    public AuthUser getAdmin() {
        return admin;
    }

    public void setAdmin(AuthUser admin) {
        this.admin = admin;
    }

    public InspectionReport getReport() {
        return report;
    }

    public void setReport(InspectionReport report) {
        this.report = report;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public BigDecimal getFinalRent() {
        return finalRent;
    }

    public void setFinalRent(BigDecimal finalRent) {
        this.finalRent = finalRent;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
