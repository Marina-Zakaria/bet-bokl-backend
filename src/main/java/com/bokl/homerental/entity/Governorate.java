package com.bokl.homerental.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "governorates")
public class Governorate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name_ar", nullable = false, length = 255)
    private String nameAr;

    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    public Governorate() {}

    public Integer getId() {
        return id;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
}
