package com.bokl.homerental.repository.unit;

import com.bokl.homerental.entity.unit.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUnitIdAndReviewTypeOrderByCreatedAtDesc(Long unitId, Review.ReviewType reviewType);

    List<Review> findByRevieweeUserIdAndReviewTypeOrderByCreatedAtDesc(
            Long revieweeUserId, Review.ReviewType reviewType);

    Optional<Review> findByBookingIdAndReviewType(Long bookingId, Review.ReviewType reviewType);

    boolean existsByBookingIdAndReviewType(Long bookingId, Review.ReviewType reviewType);
}
