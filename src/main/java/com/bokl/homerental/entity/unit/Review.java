package com.bokl.homerental.entity.unit;

import com.bokl.homerental.entity.AuthUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {

    public enum ReviewType {
        UNIT,
        RENTER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private AuthUser reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 50)
    private ReviewType reviewType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private RentalUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_user_id")
    private AuthUser revieweeUser;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "text")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Review() {
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public AuthUser getReviewer() {
        return reviewer;
    }

    public void setReviewer(AuthUser reviewer) {
        this.reviewer = reviewer;
    }

    public ReviewType getReviewType() {
        return reviewType;
    }

    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }

    public RentalUnit getUnit() {
        return unit;
    }

    public void setUnit(RentalUnit unit) {
        this.unit = unit;
    }

    public AuthUser getRevieweeUser() {
        return revieweeUser;
    }

    public void setRevieweeUser(AuthUser revieweeUser) {
        this.revieweeUser = revieweeUser;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
