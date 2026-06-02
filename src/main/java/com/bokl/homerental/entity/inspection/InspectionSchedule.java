package com.bokl.homerental.entity.inspection;

import com.bokl.homerental.entity.listing.PropertyApplication;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "inspection_schedules")
public class InspectionSchedule {

    public enum Status {
        PROPOSED,
        CONFIRMED,
        CANCELLED,
        IN_PROGRESS,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PropertyApplication application;

    @Column(name = "proposed_start", nullable = false)
    private Instant proposedStart;

    @Column(name = "proposed_end", nullable = false)
    private Instant proposedEnd;

    @Column(name = "exact_time")
    private Instant exactTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status = Status.PROPOSED;

    @Column(name = "selection_order")
    private Integer selectionOrder;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InspectionSchedule() {
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

    public Instant getProposedStart() {
        return proposedStart;
    }

    public void setProposedStart(Instant proposedStart) {
        this.proposedStart = proposedStart;
    }

    public Instant getProposedEnd() {
        return proposedEnd;
    }

    public void setProposedEnd(Instant proposedEnd) {
        this.proposedEnd = proposedEnd;
    }

    public Instant getExactTime() {
        return exactTime;
    }

    public void setExactTime(Instant exactTime) {
        this.exactTime = exactTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getSelectionOrder() {
        return selectionOrder;
    }

    public void setSelectionOrder(Integer selectionOrder) {
        this.selectionOrder = selectionOrder;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
