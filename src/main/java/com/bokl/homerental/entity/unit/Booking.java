package com.bokl.homerental.entity.unit;

import com.bokl.homerental.entity.AuthUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    public enum Status {
        PENDING_OWNER_APPROVAL,
        PENDING_PAYMENT,
        PAID,
        CHECKED_IN,
        CHECKED_OUT,
        COMPLETED,
        CANCELLED,
        OWNER_REJECTED,
        APPROVAL_EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private RentalUnit unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private AuthUser guest;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "adults_count", nullable = false)
    private Integer adultsCount;

    @Column(name = "children_count", nullable = false)
    private Integer childrenCount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status = Status.PENDING_PAYMENT;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "checked_out_at")
    private Instant checkedOutAt;

    @Column(name = "approval_expires_at")
    private Instant approvalExpiresAt;

    @Column(name = "owner_decided_at")
    private Instant ownerDecidedAt;

    @Column(name = "owner_rejection_reason", columnDefinition = "text")
    private String ownerRejectionReason;

    @Column(name = "guest_reviewed")
    private boolean guestReviewed;

    @Column(name = "owner_reviewed")
    private boolean ownerReviewed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Booking() {
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

    public AuthUser getGuest() {
        return guest;
    }

    public void setGuest(AuthUser guest) {
        this.guest = guest;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getAdultsCount() {
        return adultsCount;
    }

    public void setAdultsCount(Integer adultsCount) {
        this.adultsCount = adultsCount;
    }

    public Integer getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Instant checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Instant getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(Instant checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public Instant getApprovalExpiresAt() { return approvalExpiresAt; }
    public void setApprovalExpiresAt(Instant approvalExpiresAt) { this.approvalExpiresAt = approvalExpiresAt; }
    public Instant getOwnerDecidedAt() { return ownerDecidedAt; }
    public void setOwnerDecidedAt(Instant ownerDecidedAt) { this.ownerDecidedAt = ownerDecidedAt; }
    public String getOwnerRejectionReason() { return ownerRejectionReason; }
    public void setOwnerRejectionReason(String ownerRejectionReason) { this.ownerRejectionReason = ownerRejectionReason; }

    public boolean isGuestReviewed() {
        return guestReviewed;
    }

    public void setGuestReviewed(boolean guestReviewed) {
        this.guestReviewed = guestReviewed;
    }

    public boolean isOwnerReviewed() {
        return ownerReviewed;
    }

    public void setOwnerReviewed(boolean ownerReviewed) {
        this.ownerReviewed = ownerReviewed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
