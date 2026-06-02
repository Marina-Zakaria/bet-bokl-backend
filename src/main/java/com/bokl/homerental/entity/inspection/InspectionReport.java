package com.bokl.homerental.entity.inspection;

import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.listing.PropertyDetail;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inspection_reports")
public class InspectionReport {

    public enum Recommendation {
        APPROVE,
        REJECT,
        RETRY_WITH_FIXES
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private InspectionSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id", nullable = false)
    private AuthUser inspector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_detail_id", nullable = false)
    private PropertyDetail propertyDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Recommendation recommendation;

    @Column(name = "agreed_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedRent;

    @Column(name = "report_data", columnDefinition = "json", nullable = false)
    private String reportData;

    @Column(name = "evidence_photos", columnDefinition = "json")
    private String evidencePhotos;

    @Column(columnDefinition = "text")
    private String comments;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InspectionReport() {
    }

    public Long getId() {
        return id;
    }

    public InspectionSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(InspectionSchedule schedule) {
        this.schedule = schedule;
    }

    public AuthUser getInspector() {
        return inspector;
    }

    public void setInspector(AuthUser inspector) {
        this.inspector = inspector;
    }

    public PropertyDetail getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetail propertyDetail) {
        this.propertyDetail = propertyDetail;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public BigDecimal getAgreedRent() {
        return agreedRent;
    }

    public void setAgreedRent(BigDecimal agreedRent) {
        this.agreedRent = agreedRent;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }

    public String getEvidencePhotos() {
        return evidencePhotos;
    }

    public void setEvidencePhotos(String evidencePhotos) {
        this.evidencePhotos = evidencePhotos;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
