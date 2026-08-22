package com.bokl.homerental.repository.unit;

import com.bokl.homerental.entity.unit.RentalUnit;
import com.bokl.homerental.entity.unit.RentalUnitCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalUnitCategoryRepository
        extends JpaRepository<RentalUnitCategory, RentalUnit.Category> {
    List<RentalUnitCategory> findAllByOrderBySortOrderAsc();
}
