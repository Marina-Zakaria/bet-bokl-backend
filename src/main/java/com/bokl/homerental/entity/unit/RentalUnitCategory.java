package com.bokl.homerental.entity.unit;

import jakarta.persistence.*;

@Entity
@Table(name = "rental_unit_categories")
public class RentalUnitCategory {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RentalUnit.Category code;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ar", nullable = false, length = 100)
    private String nameAr;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public RentalUnit.Category getCode() { return code; }
    public String getNameEn() { return nameEn; }
    public String getNameAr() { return nameAr; }
    public Integer getSortOrder() { return sortOrder; }
}
