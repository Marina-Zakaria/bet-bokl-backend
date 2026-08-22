package com.bokl.homerental.controller.dto.unit;

import com.bokl.homerental.entity.unit.RentalUnit;

import java.math.BigDecimal;

public record UnitCategoryResponse(
        RentalUnit.Category code,
        String nameEn,
        String nameAr,
        BigDecimal minimumNightlyPrice,
        boolean minimumInclusive,
        BigDecimal maximumNightlyPrice,
        boolean maximumInclusive) {
}
