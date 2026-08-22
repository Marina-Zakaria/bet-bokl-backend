package com.bokl.homerental.service.unit;

import com.bokl.homerental.controller.dto.unit.BookingResponse;
import com.bokl.homerental.controller.dto.unit.CreateBookingRequest;
import com.bokl.homerental.controller.dto.unit.NamedLocationDto;
import com.bokl.homerental.controller.dto.unit.OwnerSummaryDto;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.unit.Booking;
import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.repository.unit.BookingRepository;
import com.bokl.homerental.repository.unit.RentalUnitRepository;
import com.bokl.homerental.security.SecurityUtils;
import com.bokl.homerental.service.AppConfigService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RentalUnitRepository unitRepository;
    private final AvailabilityService availabilityService;
    private final AppConfigService appConfigService;

    public BookingService(BookingRepository bookingRepository,
                          RentalUnitRepository unitRepository,
                          AvailabilityService availabilityService,
                          AppConfigService appConfigService) {
        this.bookingRepository = bookingRepository;
        this.unitRepository = unitRepository;
        this.availabilityService = availabilityService;
        this.appConfigService = appConfigService;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        AuthUser guest = SecurityUtils.currentUser();
        RentalUnit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));

        if (unit.getStatus() != RentalUnit.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit is not available for booking");
        }
        if (unit.getOwner().getId().equals(guest.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot book their own unit");
        }

        int childrenCount = request.getChildrenCount() != null ? request.getChildrenCount() : 0;
        if (request.getAdultsCount() > unit.getMaxAdults()
                || childrenCount > unit.getMaxChildren()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Guest count exceeds unit capacity (max adults: " + unit.getMaxAdults()
                            + ", max children: " + unit.getMaxChildren() + ")");
        }

        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate cannot be in the past");
        }
        if (!availabilityService.isAvailable(unit.getId(), checkIn, checkOut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit is not available for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking must be at least 1 night");
        }

        Booking booking = new Booking();
        booking.setUnit(unit);
        booking.setGuest(guest);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setAdultsCount(request.getAdultsCount());
        booking.setChildrenCount(childrenCount);
        booking.setTotalAmount(unit.getRentPerDay().multiply(BigDecimal.valueOf(nights)));
        if (unit.isRequiresOwnerApproval()) {
            int expirationHours = Math.max(1,
                    appConfigService.getInt("booking_approval_expiration_hours", 24));
            booking.setStatus(Booking.Status.PENDING_OWNER_APPROVAL);
            booking.setApprovalExpiresAt(Instant.now().plus(expirationHours, ChronoUnit.HOURS));
        } else {
            booking.setStatus(Booking.Status.PENDING_PAYMENT);
        }
        booking = bookingRepository.save(booking);

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse pay(Long bookingId) {
        Booking booking = requireGuestBooking(bookingId);
        if (booking.getStatus() != Booking.Status.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not awaiting payment");
        }
        booking.setStatus(Booking.Status.PAID);
        booking.setPaidAt(Instant.now());

        RentalUnit unit = booking.getUnit();
        unit.setBookingCount(unit.getBookingCount() + 1);

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse approve(Long bookingId) {
        Booking booking = requireOwnerBooking(bookingId);
        requirePendingApproval(booking);
        booking.setStatus(Booking.Status.PENDING_PAYMENT);
        booking.setOwnerDecidedAt(Instant.now());
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse reject(Long bookingId, String reason) {
        Booking booking = requireOwnerBooking(bookingId);
        requirePendingApproval(booking);
        booking.setStatus(Booking.Status.OWNER_REJECTED);
        booking.setOwnerDecidedAt(Instant.now());
        booking.setOwnerRejectionReason(reason);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public int expirePendingApprovals() {
        Instant now = Instant.now();
        List<Booking> expired = bookingRepository.findByStatusAndApprovalExpiresAtLessThanEqual(
                Booking.Status.PENDING_OWNER_APPROVAL, now);
        expired.forEach(booking -> booking.setStatus(Booking.Status.APPROVAL_EXPIRED));
        bookingRepository.saveAll(expired);
        return expired.size();
    }

    @Transactional
    public BookingResponse checkIn(Long bookingId) {
        Booking booking = requireGuestOrOwnerBooking(bookingId);
        if (booking.getStatus() != Booking.Status.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking must be paid before check-in");
        }
        booking.setStatus(Booking.Status.CHECKED_IN);
        booking.setCheckedInAt(Instant.now());
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse checkOut(Long bookingId) {
        Booking booking = requireGuestOrOwnerBooking(bookingId);
        if (booking.getStatus() != Booking.Status.CHECKED_IN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking must be checked in before check-out");
        }
        booking.setStatus(Booking.Status.CHECKED_OUT);
        booking.setCheckedOutAt(Instant.now());
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancel(Long bookingId) {
        Booking booking = requireGuestBooking(bookingId);
        if (booking.getStatus() != Booking.Status.PENDING_PAYMENT
                && booking.getStatus() != Booking.Status.PAID
                && booking.getStatus() != Booking.Status.PENDING_OWNER_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel booking in current status");
        }
        booking.setStatus(Booking.Status.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> myGuestBookings(int page, int size) {
        AuthUser guest = SecurityUtils.currentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findByGuestId(guest.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> myOwnerBookings(int page, int size) {
        AuthUser owner = SecurityUtils.currentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findByUnitOwnerId(owner.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        return toResponse(requireGuestOrOwnerBooking(bookingId));
    }

    Booking requireGuestBooking(Long bookingId) {
        AuthUser guest = SecurityUtils.currentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getGuest().getId().equals(guest.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the booking guest");
        }
        return booking;
    }

    Booking requireGuestOrOwnerBooking(Long bookingId) {
        AuthUser user = SecurityUtils.currentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        boolean isGuest = booking.getGuest().getId().equals(user.getId());
        boolean isOwner = booking.getUnit().getOwner().getId().equals(user.getId());
        if (!isGuest && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this booking");
        }
        return booking;
    }

    private Booking requireOwnerBooking(Long bookingId) {
        AuthUser owner = SecurityUtils.currentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUnit().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the unit owner");
        }
        return booking;
    }

    private void requirePendingApproval(Booking booking) {
        if (booking.getStatus() != Booking.Status.PENDING_OWNER_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking is not awaiting owner approval");
        }
        if (booking.getApprovalExpiresAt() != null
                && !booking.getApprovalExpiresAt().isAfter(Instant.now())) {
            booking.setStatus(Booking.Status.APPROVAL_EXPIRED);
            bookingRepository.save(booking);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Owner approval request has expired");
        }
    }

    public BookingResponse toResponse(Booking booking) {
        RentalUnit unit = booking.getUnit();
        List<String> photos = UnitMapper.parsePhotos(unit.getPhotos());

        BookingResponse.BookingUnitSummaryDto unitDto = new BookingResponse.BookingUnitSummaryDto();
        unitDto.setId(unit.getId());
        unitDto.setTitle(unit.getTitle());
        unitDto.setRentPerDay(unit.getRentPerDay());
        unitDto.setMaxAdults(unit.getMaxAdults());
        unitDto.setMaxChildren(unit.getMaxChildren());
        unitDto.setPhotos(photos);
        unitDto.setGovernorate(new NamedLocationDto(
                unit.getGovernorate().getId(),
                unit.getGovernorate().getNameAr(),
                unit.getGovernorate().getNameEn()));
        unitDto.setArea(new NamedLocationDto(
                unit.getArea().getId(),
                unit.getArea().getNameAr(),
                unit.getArea().getNameEn()));

        OwnerSummaryDto guest = new OwnerSummaryDto(
                booking.getGuest().getId(),
                booking.getGuest().getName(),
                booking.getGuest().getUsername());

        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setUnitId(unit.getId());
        response.setUnit(unitDto);
        response.setGuest(guest);
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setAdultsCount(booking.getAdultsCount());
        response.setChildrenCount(booking.getChildrenCount());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus());
        response.setGuestReviewed(booking.isGuestReviewed());
        response.setOwnerReviewed(booking.isOwnerReviewed());
        response.setPaidAt(booking.getPaidAt());
        response.setCheckedInAt(booking.getCheckedInAt());
        response.setCheckedOutAt(booking.getCheckedOutAt());
        response.setApprovalExpiresAt(booking.getApprovalExpiresAt());
        response.setOwnerDecidedAt(booking.getOwnerDecidedAt());
        response.setOwnerRejectionReason(booking.getOwnerRejectionReason());
        response.setCreatedAt(booking.getCreatedAt());

        // Flat aliases
        response.setUnitTitle(unit.getTitle());
        response.setUnitPhoto(photos.isEmpty() ? null : photos.get(0));
        response.setUnitLocation(unit.getArea().getNameEn() + ", " + unit.getGovernorate().getNameEn());
        response.setRentPerDay(unit.getRentPerDay());
        response.setGuestId(guest.getId());
        response.setGuestName(guest.getName());
        response.setOwnerId(unit.getOwner().getId());
        return response;
    }
}
