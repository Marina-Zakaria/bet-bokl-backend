package com.bokl.homerental.service.unit;

import com.bokl.homerental.controller.dto.unit.BookingResponse;
import com.bokl.homerental.controller.dto.unit.CreateReviewRequest;
import com.bokl.homerental.controller.dto.unit.ReviewResponse;
import com.bokl.homerental.controller.dto.unit.RenterReviewsResponse;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.unit.Booking;
import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.entity.unit.Review;
import com.bokl.homerental.repository.unit.BookingRepository;
import com.bokl.homerental.repository.unit.RentalUnitRepository;
import com.bokl.homerental.repository.unit.ReviewRepository;
import com.bokl.homerental.repository.AuthUserRepository;
import com.bokl.homerental.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final RentalUnitRepository unitRepository;
    private final BookingService bookingService;
    private final AuthUserRepository authUserRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BookingRepository bookingRepository,
                         RentalUnitRepository unitRepository,
                         BookingService bookingService,
                         AuthUserRepository authUserRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.unitRepository = unitRepository;
        this.bookingService = bookingService;
        this.authUserRepository = authUserRepository;
    }

    @Transactional
    public BookingResponse reviewUnit(Long bookingId, CreateReviewRequest request) {
        AuthUser guest = SecurityUtils.currentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getGuest().getId().equals(guest.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the guest can review the unit");
        }
        if (booking.getStatus() != Booking.Status.CHECKED_OUT && booking.getStatus() != Booking.Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Guest can review only after check-out");
        }
        if (reviewRepository.existsByBookingIdAndReviewType(bookingId, Review.ReviewType.UNIT)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit already reviewed for this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(guest);
        review.setReviewType(Review.ReviewType.UNIT);
        review.setUnit(booking.getUnit());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        booking.setGuestReviewed(true);
        updateUnitRating(booking.getUnit());
        maybeCompleteAndVerify(booking);
        bookingRepository.save(booking);

        return bookingService.toResponse(booking);
    }

    @Transactional
    public BookingResponse reviewRenter(Long bookingId, CreateReviewRequest request) {
        AuthUser owner = SecurityUtils.currentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUnit().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can review the renter");
        }
        if (booking.getStatus() != Booking.Status.CHECKED_OUT && booking.getStatus() != Booking.Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Owner can review only after check-out");
        }
        if (reviewRepository.existsByBookingIdAndReviewType(bookingId, Review.ReviewType.RENTER)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Renter already reviewed for this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(owner);
        review.setReviewType(Review.ReviewType.RENTER);
        review.setRevieweeUser(booking.getGuest());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        booking.setOwnerReviewed(true);
        maybeCompleteAndVerify(booking);
        bookingRepository.save(booking);

        return bookingService.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listUnitReviews(Long unitId) {
        if (!unitRepository.existsById(unitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found");
        }
        return reviewRepository.findByUnitIdAndReviewTypeOrderByCreatedAtDesc(unitId, Review.ReviewType.UNIT)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RenterReviewsResponse getRenterReviews(Long renterId) {
        AuthUser renter = authUserRepository.findById(renterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Renter not found"));
        List<ReviewResponse> reviews = reviewRepository
                .findByRevieweeUserIdAndReviewTypeOrderByCreatedAtDesc(
                        renterId, Review.ReviewType.RENTER)
                .stream()
                .map(this::toResponse)
                .toList();
        BigDecimal averageRating = reviews.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(reviews.stream()
                        .mapToInt(ReviewResponse::getRating)
                        .average()
                        .orElse(0))
                        .setScale(2, RoundingMode.HALF_UP);
        return new RenterReviewsResponse(
                renter.getId(), renter.getName(), averageRating, reviews.size(), reviews);
    }

    private void updateUnitRating(RentalUnit unit) {
        List<Review> reviews = reviewRepository.findByUnitIdAndReviewTypeOrderByCreatedAtDesc(
                unit.getId(), Review.ReviewType.UNIT);
        if (reviews.isEmpty()) {
            unit.setAverageRating(BigDecimal.ZERO);
            unit.setReviewCount(0);
            return;
        }
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        unit.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        unit.setReviewCount(reviews.size());
    }

    private void maybeCompleteAndVerify(Booking booking) {
        if (booking.getStatus() == Booking.Status.CHECKED_OUT && booking.isGuestReviewed()) {
            booking.setStatus(Booking.Status.COMPLETED);
            RentalUnit unit = booking.getUnit();
            if (!unit.isVerified()
                    && booking.getPaidAt() != null
                    && booking.getCheckedInAt() != null
                    && booking.getCheckedOutAt() != null) {
                unit.setVerified(true);
            }
        }
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setReviewer(new ReviewResponse.ReviewerDto(review.getReviewer().getName()));
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
