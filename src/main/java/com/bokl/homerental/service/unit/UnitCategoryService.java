package com.bokl.homerental.service.unit;

import com.bokl.homerental.controller.dto.unit.UnitCategoryResponse;
import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.entity.unit.RentalUnitCategory;
import com.bokl.homerental.repository.unit.RentalUnitCategoryRepository;
import com.bokl.homerental.service.AppConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UnitCategoryService {
    private static final String ECONOMY_MAX_KEY =
            "unit_category_economy_max_nightly_price";
    private static final String PREMIUM_MAX_KEY =
            "unit_category_premium_max_nightly_price";
    private static final String HOTEL_MIN_KEY =
            "unit_category_hotel_min_nightly_price";

    private final AppConfigService appConfigService;
    private final RentalUnitCategoryRepository categoryRepository;

    public UnitCategoryService(AppConfigService appConfigService,
                               RentalUnitCategoryRepository categoryRepository) {
        this.appConfigService = appConfigService;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public RentalUnit.Category categoryFor(BigDecimal nightlyPrice) {
        Limits limits = limits();
        if (nightlyPrice.compareTo(limits.economyMax()) <= 0) {
            return RentalUnit.Category.ECONOMY;
        }
        if (nightlyPrice.compareTo(limits.premiumMax()) <= 0) {
            return RentalUnit.Category.PREMIUM;
        }
        return RentalUnit.Category.HOTEL;
    }

    @Transactional(readOnly = true)
    public List<UnitCategoryResponse> listCategories() {
        Limits limits = limits();
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(category -> toResponse(category, limits))
                .toList();
    }

    private UnitCategoryResponse toResponse(RentalUnitCategory category, Limits limits) {
        return switch (category.getCode()) {
            case ECONOMY -> new UnitCategoryResponse(
                    category.getCode(), category.getNameEn(), category.getNameAr(),
                    BigDecimal.ZERO, true, limits.economyMax(), true);
            case PREMIUM -> new UnitCategoryResponse(
                    category.getCode(), category.getNameEn(), category.getNameAr(),
                    limits.economyMax(), false, limits.premiumMax(), true);
            case HOTEL -> new UnitCategoryResponse(
                    category.getCode(), category.getNameEn(), category.getNameAr(),
                    limits.hotelMin(), false, null, false);
        };
    }

    private Limits limits() {
        BigDecimal economyMax = appConfigService.getDecimal(
                ECONOMY_MAX_KEY, new BigDecimal("1000.00"));
        BigDecimal premiumMax = appConfigService.getDecimal(
                PREMIUM_MAX_KEY, new BigDecimal("2000.00"));
        BigDecimal hotelMin = appConfigService.getDecimal(
                HOTEL_MIN_KEY, new BigDecimal("2000.00"));

        if (economyMax.signum() <= 0
                || premiumMax.compareTo(economyMax) <= 0
                || hotelMin.compareTo(premiumMax) != 0) {
            throw new IllegalStateException("Invalid unit category price limits in app_config");
        }
        return new Limits(economyMax, premiumMax, hotelMin);
    }

    private record Limits(BigDecimal economyMax, BigDecimal premiumMax, BigDecimal hotelMin) {
    }
}
