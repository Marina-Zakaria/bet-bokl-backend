package com.bokl.homerental.controller.dto.unit;

import java.time.Instant;

public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String comment;
    private ReviewerDto reviewer;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public ReviewerDto getReviewer() { return reviewer; }
    public void setReviewer(ReviewerDto reviewer) { this.reviewer = reviewer; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class ReviewerDto {
        private String name;

        public ReviewerDto() {
        }

        public ReviewerDto(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
