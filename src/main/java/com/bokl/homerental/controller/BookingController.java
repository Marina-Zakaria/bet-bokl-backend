package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.unit.BookingResponse;
import com.bokl.homerental.controller.dto.unit.CreateBookingRequest;
import com.bokl.homerental.controller.dto.unit.CreateReviewRequest;
import com.bokl.homerental.controller.dto.unit.RejectBookingRequest;
import com.bokl.homerental.service.unit.BookingService;
import com.bokl.homerental.service.unit.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final ReviewService reviewService;

    public BookingController(BookingService bookingService, ReviewService reviewService) {
        this.bookingService = bookingService;
        this.reviewService = reviewService;
    }

    @RequiresLogin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @RequiresLogin
    @GetMapping("/mine")
    public Page<BookingResponse> myGuestBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return bookingService.myGuestBookings(page, size);
    }

    @RequiresLogin
    @GetMapping("/as-owner")
    public Page<BookingResponse> myOwnerBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return bookingService.myOwnerBookings(page, size);
    }

    @RequiresLogin
    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable Long bookingId) {
        return bookingService.getBooking(bookingId);
    }

    @RequiresLogin
    @PostMapping("/{bookingId}/pay")
    public BookingResponse pay(@PathVariable Long bookingId) {
        return bookingService.pay(bookingId);
    }

    @RequiresLogin
    @PostMapping("/{bookingId}/approve")
    public BookingResponse approve(@PathVariable Long bookingId) {
        return bookingService.approve(bookingId);
    }

    @RequiresLogin
    @PostMapping("/{bookingId}/reject")
    public BookingResponse reject(@PathVariable Long bookingId,
                                  @Valid @RequestBody(required = false) RejectBookingRequest request) {
        return bookingService.reject(bookingId, request == null ? null : request.getReason());
    }

    @RequiresLogin
    @PostMapping("/{bookingId}/check-in")
    public BookingResponse checkIn(@PathVariable Long bookingId) {
        return bookingService.checkIn(bookingId);
    }

    @RequiresLogin
    @PostMapping("/{bookingId}/check-out")
    public BookingResponse checkOut(@PathVariable Long bookingId) {
        return bookingService.checkOut(bookingId);
    }

    @RequiresLogin
    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable Long bookingId) {
        return bookingService.cancel(bookingId);
    }

    /** Contract: returns updated Booking (guestReviewed / COMPLETED). */
    @RequiresLogin
    @PostMapping("/{bookingId}/reviews/unit")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse reviewUnit(@PathVariable Long bookingId,
                                      @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.reviewUnit(bookingId, request);
    }

    /** Contract: returns updated Booking (ownerReviewed). */
    @RequiresLogin
    @PostMapping("/{bookingId}/reviews/renter")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse reviewRenter(@PathVariable Long bookingId,
                                        @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.reviewRenter(bookingId, request);
    }
}
