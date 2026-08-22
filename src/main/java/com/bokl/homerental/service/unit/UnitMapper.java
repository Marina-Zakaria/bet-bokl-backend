package com.bokl.homerental.service.unit;

import com.bokl.homerental.controller.dto.unit.NamedLocationDto;
import com.bokl.homerental.controller.dto.unit.OwnerSummaryDto;
import com.bokl.homerental.controller.dto.unit.UnitResponse;
import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class UnitMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UnitMapper() {
    }

    public static UnitResponse toResponse(RentalUnit unit) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean useArabic = "ar".equalsIgnoreCase(locale.getLanguage());

        NamedLocationDto gov = new NamedLocationDto(
                unit.getGovernorate().getId(),
                unit.getGovernorate().getNameAr(),
                unit.getGovernorate().getNameEn());
        NamedLocationDto area = new NamedLocationDto(
                unit.getArea().getId(),
                unit.getArea().getNameAr(),
                unit.getArea().getNameEn());
        OwnerSummaryDto owner = new OwnerSummaryDto(
                unit.getOwner().getId(),
                unit.getOwner().getName(),
                unit.getOwner().getUsername());

        UnitResponse response = new UnitResponse();
        response.setId(unit.getId());
        response.setTitle(unit.getTitle());
        response.setDescription(unit.getDescription());
        response.setGovernorate(gov);
        response.setArea(area);
        response.setStreetName(unit.getStreetName());
        response.setBuildingNumber(unit.getBuildingNumber());
        response.setApartmentNumber(unit.getApartmentNumber());
        response.setLandmark(unit.getLandmark());
        response.setLatitude(unit.getLatitude());
        response.setLongitude(unit.getLongitude());
        response.setRoomsCount(unit.getRoomsCount());
        response.setBathroomsCount(unit.getBathroomsCount());
        response.setAreaSqm(unit.getAreaSqm());
        response.setFurnishing(unit.getFurnishing());
        response.setCategory(unit.getCategory());
        response.setRentPerDay(unit.getRentPerDay());
        response.setMaxAdults(unit.getMaxAdults());
        response.setMaxChildren(unit.getMaxChildren());
        response.setRequiresOwnerApproval(unit.isRequiresOwnerApproval());
        response.setInstantBookingTermsId(unit.getInstantBookingTerms() == null
                ? null : unit.getInstantBookingTerms().getId());
        response.setHasElevator(unit.isHasElevator());
        response.setHasWashingMachine(unit.isHasWashingMachine());
        response.setHasWifi(unit.isHasWifi());
        response.setHasAirConditioning(unit.isHasAirConditioning());
        response.setHasParking(unit.isHasParking());
        response.setHasPool(unit.isHasPool());
        response.setHasTv(unit.isHasTv());
        response.setHasKitchen(unit.isHasKitchen());
        response.setHasBalcony(unit.isHasBalcony());
        response.setHasWaterHeater(unit.isHasWaterHeater());
        response.setPhotos(parsePhotos(unit.getPhotos()));
        response.setStatus(unit.getStatus());
        response.setVerified(unit.isVerified());
        response.setAverageRating(unit.getAverageRating());
        response.setReviewCount(unit.getReviewCount());
        response.setBookingCount(unit.getBookingCount());
        response.setOwner(owner);
        response.setPublishedAt(unit.getPublishedAt());
        response.setCreatedAt(unit.getCreatedAt());

        // Flat aliases
        response.setOwnerId(owner.getId());
        response.setOwnerName(owner.getName());
        response.setGovernorateId(gov.getId());
        response.setGovernorateNameAr(gov.getNameAr());
        response.setGovernorateNameEn(gov.getNameEn());
        response.setGovernorateName(useArabic ? gov.getNameAr() : gov.getNameEn());
        response.setAreaId(area.getId());
        response.setAreaNameAr(area.getNameAr());
        response.setAreaNameEn(area.getNameEn());
        response.setAreaName(useArabic ? area.getNameAr() : area.getNameEn());
        return response;
    }

    public static String photosToJson(List<String> photos) {
        return JsonUtils.toJson(photos);
    }

    public static List<String> parsePhotos(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
