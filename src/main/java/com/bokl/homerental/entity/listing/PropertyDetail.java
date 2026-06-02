package com.bokl.homerental.entity.listing;

import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.Governorate;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "property_details")
public class PropertyDetail {

    public enum SourceType {
        APPLICATION,
        REPORT
    }

    public enum Furnishing {
        FURNISHED,
        SEMI_FURNISHED,
        UNFURNISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "rooms_count", nullable = false)
    private Integer roomsCount;

    @Column(name = "area_sqm", nullable = false)
    private Integer areaSqm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Furnishing furnishing;

    @Column(name = "expected_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal expectedRent;

    @Column(columnDefinition = "json")
    private String amenities;

    @Column(columnDefinition = "json", nullable = false)
    private String photos;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private SourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PropertyDetail() {
    }

    public Long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Governorate getGovernorate() {
        return governorate;
    }

    public void setGovernorate(Governorate governorate) {
        this.governorate = governorate;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Integer getRoomsCount() {
        return roomsCount;
    }

    public void setRoomsCount(Integer roomsCount) {
        this.roomsCount = roomsCount;
    }

    public Integer getAreaSqm() {
        return areaSqm;
    }

    public void setAreaSqm(Integer areaSqm) {
        this.areaSqm = areaSqm;
    }

    public Furnishing getFurnishing() {
        return furnishing;
    }

    public void setFurnishing(Furnishing furnishing) {
        this.furnishing = furnishing;
    }

    public BigDecimal getExpectedRent() {
        return expectedRent;
    }

    public void setExpectedRent(BigDecimal expectedRent) {
        this.expectedRent = expectedRent;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
