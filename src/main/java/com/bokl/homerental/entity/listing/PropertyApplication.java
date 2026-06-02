package com.bokl.homerental.entity.listing;

import com.bokl.homerental.entity.AuthUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "property_applications")
public class PropertyApplication {

    public enum Status {
        SUBMITTED,
        UNDER_REVIEW,
        DOCUMENTS_REJECTED,
        PENDING_INSPECTOR_SLOTS,
        AWAITING_OWNER_SELECTION,
        INSPECTION_SCHEDULED,
        IN_PROGRESS,
        INSPECTION_COMPLETED,
        REJECTED,
        PENDING_OWNER_CONSENT,
        CONSENT_PROVIDED,
        LISTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_detail_id", nullable = false)
    private PropertyDetail propertyDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private AuthUser inspector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status;

    @Column(name = "admin_comments", columnDefinition = "text")
    private String adminComments;

    @Column(name = "inspector_assigned_at")
    private Instant inspectorAssignedAt;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PropertyApplication() {
    }

    public Long getId() {
        return id;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public PropertyDetail getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetail propertyDetail) {
        this.propertyDetail = propertyDetail;
    }

    public AuthUser getInspector() {
        return inspector;
    }

    public void setInspector(AuthUser inspector) {
        this.inspector = inspector;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }

    public Instant getInspectorAssignedAt() {
        return inspectorAssignedAt;
    }

    public void setInspectorAssignedAt(Instant inspectorAssignedAt) {
        this.inspectorAssignedAt = inspectorAssignedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
