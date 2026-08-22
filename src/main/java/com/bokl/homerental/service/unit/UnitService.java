package com.bokl.homerental.service.unit;

import com.bokl.homerental.controller.dto.unit.*;
import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.Governorate;
import com.bokl.homerental.entity.listing.TermsDefinition;
import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.entity.unit.UnitTermsConsent;
import com.bokl.homerental.entity.unit.UnitUnavailability;
import com.bokl.homerental.repository.AreaRepository;
import com.bokl.homerental.repository.GovernorateRepository;
import com.bokl.homerental.repository.listing.TermsDefinitionRepository;
import com.bokl.homerental.repository.unit.RentalUnitRepository;
import com.bokl.homerental.repository.unit.UnitTermsConsentRepository;
import com.bokl.homerental.repository.unit.UnitUnavailabilityRepository;
import com.bokl.homerental.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;

@Service
public class UnitService {

    public enum SortBy {
        PRICE_ASC,
        PRICE_DESC,
        RATING,
        MOST_RENTED
    }

    private final RentalUnitRepository unitRepository;
    private final GovernorateRepository governorateRepository;
    private final AreaRepository areaRepository;
    private final TermsDefinitionRepository termsDefinitionRepository;
    private final UnitTermsConsentRepository unitTermsConsentRepository;
    private final UnitUnavailabilityRepository unavailabilityRepository;
    private final AvailabilityService availabilityService;
    private final UnitCategoryService unitCategoryService;

    public UnitService(
            RentalUnitRepository unitRepository,
            GovernorateRepository governorateRepository,
            AreaRepository areaRepository,
            TermsDefinitionRepository termsDefinitionRepository,
            UnitTermsConsentRepository unitTermsConsentRepository,
            UnitUnavailabilityRepository unavailabilityRepository,
            AvailabilityService availabilityService,
            UnitCategoryService unitCategoryService) {
        this.unitRepository = unitRepository;
        this.governorateRepository = governorateRepository;
        this.areaRepository = areaRepository;
        this.termsDefinitionRepository = termsDefinitionRepository;
        this.unitTermsConsentRepository = unitTermsConsentRepository;
        this.unavailabilityRepository = unavailabilityRepository;
        this.availabilityService = availabilityService;
        this.unitCategoryService = unitCategoryService;
    }

    @Transactional
    public UnitResponse createUnit(CreateUnitRequest request, HttpServletRequest httpRequest) {
        AuthUser owner = SecurityUtils.currentUser();

        Governorate governorate = governorateRepository.findById(request.getGovernorateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Governorate not found"));
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Area not found"));
        if (!area.getGovernorate().getId().equals(governorate.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Area does not belong to governorate");
        }

        TermsDefinition terms = termsDefinitionRepository.findById(request.getTermsDefinitionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terms definition not found"));
        if (!Boolean.TRUE.equals(terms.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terms definition is not active");
        }
        if (terms.getType() != TermsDefinition.Type.OWNER_LISTING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "termsDefinitionId must reference owner listing terms");
        }

        TermsDefinition instantTerms = null;
        if (!Boolean.TRUE.equals(request.getRequiresOwnerApproval())) {
            if (!Boolean.TRUE.equals(request.getAcceptInstantBookingTerms())
                    || request.getInstantBookingTermsId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Instant booking terms must be selected and accepted");
            }
            instantTerms = termsDefinitionRepository.findById(request.getInstantBookingTermsId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Instant booking terms not found"));
            if (!Boolean.TRUE.equals(instantTerms.getActive())
                    || instantTerms.getType() != TermsDefinition.Type.INSTANT_BOOKING_COMMITMENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Instant booking terms are not active or have the wrong type");
            }
        }

        Instant now = Instant.now();
        RentalUnit unit = new RentalUnit();
        unit.setOwner(owner);
        unit.setTitle(request.getTitle());
        unit.setDescription(request.getDescription());
        unit.setGovernorate(governorate);
        unit.setArea(area);
        unit.setStreetName(request.getStreetName());
        unit.setBuildingNumber(request.getBuildingNumber());
        unit.setApartmentNumber(request.getApartmentNumber());
        unit.setLandmark(request.getLandmark());
        unit.setLatitude(request.getLatitude());
        unit.setLongitude(request.getLongitude());
        unit.setRoomsCount(request.getRoomsCount());
        unit.setBathroomsCount(request.getBathroomsCount() != null ? request.getBathroomsCount() : 1);
        unit.setAreaSqm(request.getAreaSqm());
        unit.setFurnishing(request.getFurnishing());
        unit.setCategory(unitCategoryService.categoryFor(request.getRentPerDay()));
        unit.setRentPerDay(request.getRentPerDay());
        unit.setMaxAdults(request.getMaxAdults());
        unit.setMaxChildren(request.getMaxChildren());
        unit.setRequiresOwnerApproval(Boolean.TRUE.equals(request.getRequiresOwnerApproval()));
        unit.setInstantBookingTerms(instantTerms);
        unit.setInstantBookingTermsAcceptedAt(instantTerms == null ? null : now);
        unit.setHasElevator(request.isHasElevator());
        unit.setHasWashingMachine(request.isHasWashingMachine());
        unit.setHasWifi(request.isHasWifi());
        unit.setHasAirConditioning(request.isHasAirConditioning());
        unit.setHasParking(request.isHasParking());
        unit.setHasPool(request.isHasPool());
        unit.setHasTv(request.isHasTv());
        unit.setHasKitchen(request.isHasKitchen());
        unit.setHasBalcony(request.isHasBalcony());
        unit.setHasWaterHeater(request.isHasWaterHeater());
        unit.setPhotos(UnitMapper.photosToJson(request.getPhotos()));
        unit.setIdDocumentType(request.getIdDocumentType());
        unit.setIdFrontUrl(request.getIdFrontUrl());
        unit.setIdBackUrl(request.getIdBackUrl());
        unit.setStatus(RentalUnit.Status.ACTIVE);
        unit.setVerified(false);
        unit.setAverageRating(BigDecimal.ZERO);
        unit.setReviewCount(0);
        unit.setBookingCount(0);
        unit.setTermsDefinition(terms);
        unit.setTermsAcceptedAt(now);
        unit.setPublishedAt(now);

        unit = unitRepository.save(unit);

        saveConsent(unit, owner, terms, httpRequest);
        if (instantTerms != null) {
            saveConsent(unit, owner, instantTerms, httpRequest);
        }

        return UnitMapper.toResponse(unit);
    }

    private void saveConsent(RentalUnit unit, AuthUser owner, TermsDefinition terms,
                             HttpServletRequest httpRequest) {
        UnitTermsConsent consent = new UnitTermsConsent();
        consent.setUnit(unit);
        consent.setUser(owner);
        consent.setTermsDefinition(terms);
        if (httpRequest != null) {
            consent.setIpAddress(httpRequest.getRemoteAddr());
            consent.setUserAgent(httpRequest.getHeader("User-Agent"));
        }
        unitTermsConsentRepository.save(consent);
    }

    @Transactional(readOnly = true)
    public UnitResponse getUnit(Long unitId) {
        RentalUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));
        if (unit.getStatus() != RentalUnit.Status.ACTIVE) {
            // Owners can still see their own non-active units via /mine; public get only ACTIVE
            AuthUser current = tryCurrentUser();
            if (current == null || !current.getId().equals(unit.getOwner().getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found");
            }
        }
        return UnitMapper.toResponse(unit);
    }

    @Transactional(readOnly = true)
    public Page<UnitResponse> listMyUnits(int page, int size) {
        AuthUser owner = SecurityUtils.currentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return unitRepository.findByOwnerId(owner.getId(), pageable).map(UnitMapper::toResponse);
    }

    /**
     * Unified public browse: optional free-text {@code q} plus filters (rent, amenities,
     * category, availability dates, etc.) and sort. All params optional.
     */
    @Transactional(readOnly = true)
    public Page<UnitResponse> search(
            String q,
            BigDecimal minRent,
            BigDecimal maxRent,
            RentalUnit.Category category,
            RentalUnit.Furnishing furnishing,
            Integer roomsCount,
            Boolean hasElevator,
            Boolean hasWashingMachine,
            Boolean hasWifi,
            Boolean hasAirConditioning,
            Boolean hasParking,
            Boolean hasPool,
            Boolean hasTv,
            Boolean hasKitchen,
            Boolean hasBalcony,
            Boolean hasWaterHeater,
            Boolean verified,
            Integer governorateId,
            Integer areaId,
            LocalDate arrivalDate,
            LocalDate leaveDate,
            Integer adultsCount,
            Integer childrenCount,
            LocalDate availableFrom,
            LocalDate availableTo,
            SortBy sortBy,
            int page,
            int size) {

        if (adultsCount != null && adultsCount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "adultsCount must be at least 1");
        }
        if (childrenCount != null && childrenCount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "childrenCount cannot be negative");
        }

        LocalDate effectiveArrival = resolveDateAlias(
                arrivalDate, availableFrom, "arrivalDate", "availableFrom");
        LocalDate effectiveLeave = resolveDateAlias(
                leaveDate, availableTo, "leaveDate", "availableTo");

        if ((effectiveArrival == null) != (effectiveLeave == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Both arrivalDate and leaveDate must be provided together");
        }
        if (effectiveArrival != null && !effectiveLeave.isAfter(effectiveArrival)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "leaveDate must be after arrivalDate");
        }

        Pageable pageable = PageRequest.of(page, size, toSort(sortBy));
        return unitRepository.findAll(
                UnitSpecifications.withFilters(
                        q, minRent, maxRent, category, furnishing, roomsCount,
                        hasElevator, hasWashingMachine, hasWifi, hasAirConditioning,
                        hasParking, hasPool, hasTv, hasKitchen, hasBalcony, hasWaterHeater,
                        verified, governorateId, areaId, adultsCount, childrenCount,
                        effectiveArrival, effectiveLeave),
                pageable
        ).map(UnitMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UnitResponse> mostRented(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingCount")
                .and(Sort.by(Sort.Direction.DESC, "averageRating")));
        return unitRepository.findByStatus(RentalUnit.Status.ACTIVE, pageable).map(UnitMapper::toResponse);
    }

    @Transactional
    public UnavailabilityResponse addUnavailability(Long unitId, UnavailabilityRequest request) {
        RentalUnit unit = requireOwnedUnit(unitId);
        UnitUnavailability block = new UnitUnavailability();
        block.setUnit(unit);
        block.setStartDate(request.getStartDate());
        block.setEndDate(request.getEndDate());
        block.setReason(request.getReason());
        block = unavailabilityRepository.save(block);
        return new UnavailabilityResponse(block.getId(), unitId, block.getStartDate(), block.getEndDate(),
                block.getReason(), "OWNER");
    }

    @Transactional
    public void removeUnavailability(Long unitId, Long blockId) {
        requireOwnedUnit(unitId);
        UnitUnavailability block = unavailabilityRepository.findById(blockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unavailability not found"));
        if (!block.getUnit().getId().equals(unitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unavailability not found");
        }
        unavailabilityRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public List<UnavailabilityResponse> getAvailabilityCalendar(Long unitId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }
        if (!unitRepository.existsById(unitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found");
        }
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to must be on or after from");
        }
        return availabilityService.getUnavailableRanges(unitId, from, to);
    }

    private RentalUnit requireOwnedUnit(Long unitId) {
        AuthUser owner = SecurityUtils.currentUser();
        RentalUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));
        if (!unit.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the unit owner");
        }
        return unit;
    }

    private AuthUser tryCurrentUser() {
        try {
            return SecurityUtils.currentUser();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate resolveDateAlias(
            LocalDate preferred,
            LocalDate legacy,
            String preferredName,
            String legacyName) {
        if (preferred != null && legacy != null && !preferred.equals(legacy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    preferredName + " and " + legacyName + " must match when both are provided");
        }
        return preferred != null ? preferred : legacy;
    }

    private Sort toSort(SortBy sortBy) {
        if (sortBy == null) {
            sortBy = SortBy.MOST_RENTED;
        }
        return switch (sortBy) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "rentPerDay");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "rentPerDay");
            case RATING -> Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "reviewCount"));
            case MOST_RENTED -> Sort.by(Sort.Direction.DESC, "bookingCount")
                    .and(Sort.by(Sort.Direction.DESC, "averageRating"));
        };
    }
}
