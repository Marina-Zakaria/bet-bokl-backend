package com.bokl.homerental.controller.dto.listing;

import com.bokl.homerental.entity.listing.PropertyDetail;

import java.math.BigDecimal;
import java.time.Instant;

public class PropertyDetailResponse {

    private Long id;
    private AddressResponse address;
    private Integer governorateId;
    private Integer areaId;
    private Integer roomsCount;
    private Integer areaSqm;
    private String furnishing;
    private BigDecimal expectedRent;
    private String amenities;
    private String photos;
    private Instant createdAt;

    public PropertyDetailResponse() {
    }

    public static PropertyDetailResponse from(PropertyDetail d) {
        if (d == null) return null;
        PropertyDetailResponse r = new PropertyDetailResponse();
        r.id = d.getId();
        r.address = AddressResponse.from(d.getAddress());
        r.governorateId = d.getGovernorate() != null ? d.getGovernorate().getId() : null;
        r.areaId = d.getArea() != null ? d.getArea().getId() : null;
        r.roomsCount = d.getRoomsCount();
        r.areaSqm = d.getAreaSqm();
        r.furnishing = d.getFurnishing() != null ? d.getFurnishing().name() : null;
        r.expectedRent = d.getExpectedRent();
        r.amenities = d.getAmenities();
        r.photos = d.getPhotos();
        r.createdAt = d.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AddressResponse getAddress() { return address; }
    public void setAddress(AddressResponse address) { this.address = address; }

    public Integer getGovernorateId() { return governorateId; }
    public void setGovernorateId(Integer governorateId) { this.governorateId = governorateId; }

    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }

    public Integer getRoomsCount() { return roomsCount; }
    public void setRoomsCount(Integer roomsCount) { this.roomsCount = roomsCount; }

    public Integer getAreaSqm() { return areaSqm; }
    public void setAreaSqm(Integer areaSqm) { this.areaSqm = areaSqm; }

    public String getFurnishing() { return furnishing; }
    public void setFurnishing(String furnishing) { this.furnishing = furnishing; }

    public BigDecimal getExpectedRent() { return expectedRent; }
    public void setExpectedRent(BigDecimal expectedRent) { this.expectedRent = expectedRent; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
