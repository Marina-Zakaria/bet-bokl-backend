package com.bokl.homerental.controller.dto.unit;

/** Nested location object for unit/booking responses (contract shape). */
public class NamedLocationDto {
    private Integer id;
    private String nameAr;
    private String nameEn;

    public NamedLocationDto() {
    }

    public NamedLocationDto(Integer id, String nameAr, String nameEn) {
        this.id = id;
        this.nameAr = nameAr;
        this.nameEn = nameEn;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
}
