package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.unit.*;
import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.service.unit.ReviewService;
import com.bokl.homerental.service.unit.UnitCategoryService;
import com.bokl.homerental.service.unit.UnitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitController {

    private final UnitService unitService;
    private final ReviewService reviewService;
    private final UnitCategoryService unitCategoryService;

    public UnitController(UnitService unitService, ReviewService reviewService,
                          UnitCategoryService unitCategoryService) {
        this.unitService = unitService;
        this.reviewService = reviewService;
        this.unitCategoryService = unitCategoryService;
    }

    /** Owner submits a unit; listing becomes ACTIVE immediately after terms acceptance. */
    @RequiresLogin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnitResponse createUnit(@Valid @RequestBody CreateUnitRequest request, HttpServletRequest httpRequest) {
        return unitService.createUnit(request, httpRequest);
    }

    @RequiresLogin
    @GetMapping("/mine")
    public Page<UnitResponse> myUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return unitService.listMyUnits(page, size);
    }

    /** Homepage: most rented active units. */
    @GetMapping("/most-rented")
    public Page<UnitResponse> mostRented(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return unitService.mostRented(page, size);
    }

    /** Localized category names and the current DB-configured price boundaries. */
    @GetMapping("/categories")
    public List<UnitCategoryResponse> categories() {
        return unitCategoryService.listCategories();
    }

    /**
     * Unified search + filter (public).
     * Optional {@code q} matches title / street / governorate / area (ar+en).
     * Optional filters: rent, category, amenities, verified, arrival/leave dates,
     * expected adult/child counts, and sort.
     */
    @GetMapping("/search")
    public Page<UnitResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minRent,
            @RequestParam(required = false) BigDecimal maxRent,
            @RequestParam(required = false) RentalUnit.Category category,
            @RequestParam(required = false) RentalUnit.Furnishing furnishing,
            @RequestParam(required = false) Integer roomsCount,
            @RequestParam(required = false) Boolean hasElevator,
            @RequestParam(required = false) Boolean hasWashingMachine,
            @RequestParam(required = false) Boolean hasWifi,
            @RequestParam(required = false) Boolean hasAirConditioning,
            @RequestParam(required = false) Boolean hasParking,
            @RequestParam(required = false) Boolean hasPool,
            @RequestParam(required = false) Boolean hasTv,
            @RequestParam(required = false) Boolean hasKitchen,
            @RequestParam(required = false) Boolean hasBalcony,
            @RequestParam(required = false) Boolean hasWaterHeater,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Integer governorateId,
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate leaveDate,
            @RequestParam(required = false) Integer adultsCount,
            @RequestParam(required = false) Integer childrenCount,
            // Backward-compatible date aliases; prefer arrivalDate/leaveDate.
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableTo,
            @RequestParam(required = false, defaultValue = "MOST_RENTED") UnitService.SortBy sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return unitService.search(
                q, minRent, maxRent, category, furnishing, roomsCount,
                hasElevator, hasWashingMachine, hasWifi, hasAirConditioning,
                hasParking, hasPool, hasTv, hasKitchen, hasBalcony, hasWaterHeater,
                verified, governorateId, areaId, arrivalDate, leaveDate,
                adultsCount, childrenCount, availableFrom, availableTo,
                sort, page, size);
    }

    @GetMapping("/{unitId}")
    public UnitResponse getUnit(@PathVariable Long unitId) {
        return unitService.getUnit(unitId);
    }

    @GetMapping("/{unitId}/reviews")
    public List<ReviewResponse> unitReviews(@PathVariable Long unitId) {
        return reviewService.listUnitReviews(unitId);
    }

    /** Availability calendar: owner-blocked dates + booked dates. from/to required. */
    @GetMapping("/{unitId}/availability")
    public List<UnavailabilityResponse> availability(
            @PathVariable Long unitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return unitService.getAvailabilityCalendar(unitId, from, to);
    }

    @RequiresLogin
    @PostMapping("/{unitId}/unavailability")
    @ResponseStatus(HttpStatus.CREATED)
    public UnavailabilityResponse addUnavailability(
            @PathVariable Long unitId,
            @Valid @RequestBody UnavailabilityRequest request) {
        return unitService.addUnavailability(unitId, request);
    }

    @RequiresLogin
    @DeleteMapping("/{unitId}/unavailability/{blockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUnavailability(@PathVariable Long unitId, @PathVariable Long blockId) {
        unitService.removeUnavailability(unitId, blockId);
    }
}
