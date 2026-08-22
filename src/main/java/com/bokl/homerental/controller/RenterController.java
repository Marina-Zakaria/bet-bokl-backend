package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.unit.RenterReviewsResponse;
import com.bokl.homerental.service.unit.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/renters")
public class RenterController {
    private final ReviewService reviewService;

    public RenterController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RequiresLogin
    @GetMapping("/{renterId}/reviews")
    public RenterReviewsResponse getRenterReviews(@PathVariable Long renterId) {
        return reviewService.getRenterReviews(renterId);
    }
}
