package com.bokl.homerental.service.unit;

import com.bokl.homerental.controller.dto.unit.UnavailabilityResponse;
import com.bokl.homerental.entity.unit.Booking;
import com.bokl.homerental.entity.unit.UnitUnavailability;
import com.bokl.homerental.repository.unit.BookingRepository;
import com.bokl.homerental.repository.unit.UnitUnavailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AvailabilityService {

    private static final Set<Booking.Status> BLOCKING_STATUSES = EnumSet.of(
            Booking.Status.PENDING_OWNER_APPROVAL,
            Booking.Status.PENDING_PAYMENT,
            Booking.Status.PAID,
            Booking.Status.CHECKED_IN,
            Booking.Status.CHECKED_OUT,
            Booking.Status.COMPLETED
    );

    private final UnitUnavailabilityRepository unavailabilityRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(UnitUnavailabilityRepository unavailabilityRepository,
                               BookingRepository bookingRepository) {
        this.unavailabilityRepository = unavailabilityRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(Long unitId, LocalDate checkIn, LocalDate checkOut) {
        if (unavailabilityRepository.existsOverlapping(unitId, checkIn, checkOut.minusDays(1))) {
            return false;
        }
        // Booking uses exclusive check-out day (guest leaves that morning)
        return !bookingRepository.existsOverlappingActive(unitId, checkIn, checkOut, BLOCKING_STATUSES);
    }

    @Transactional(readOnly = true)
    public List<UnavailabilityResponse> getUnavailableRanges(Long unitId, LocalDate from, LocalDate to) {
        List<UnavailabilityResponse> result = new ArrayList<>();

        for (UnitUnavailability block : unavailabilityRepository.findOverlapping(unitId, from, to)) {
            result.add(new UnavailabilityResponse(
                    block.getId(), unitId, block.getStartDate(), block.getEndDate(),
                    block.getReason(), "OWNER"));
        }

        for (Booking booking : bookingRepository.findOverlapping(unitId, from, to, BLOCKING_STATUSES)) {
            // Present booked nights as inclusive dates [checkIn, checkOut-1]
            LocalDate endInclusive = booking.getCheckOutDate().minusDays(1);
            if (!endInclusive.isBefore(booking.getCheckInDate())) {
                result.add(new UnavailabilityResponse(
                        booking.getId(), unitId, booking.getCheckInDate(), endInclusive,
                        "Booked", "BOOKING"));
            }
        }

        return result;
    }
}
