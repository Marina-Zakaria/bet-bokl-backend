package com.bokl.homerental.controller.dto.unit;

import java.math.BigDecimal;
import java.util.List;

public record RenterReviewsResponse(
        Long renterId,
        String renterName,
        BigDecimal averageRating,
        int reviewCount,
        List<ReviewResponse> reviews) {
}
