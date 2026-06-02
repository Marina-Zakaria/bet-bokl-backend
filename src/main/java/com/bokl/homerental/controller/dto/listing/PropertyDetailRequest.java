package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class PropertyDetailRequest {

    @NotNull
    private AddressRequest address;

    @NotNull
    private Integer governorateId;

    @NotNull
    private Integer areaId;

    @NotNull
    @Positive
    private Integer roomsCount;

    @NotNull
    @Positive
    private Integer areaSqm;

    @NotNull
    private String furnishing;

    @NotNull
    private BigDecimal expectedRent;

    private List<String> amenities;

    @NotNull
    private List<String> photos;

    public PropertyDetailRequest() {
    }

    public AddressRequest getAddress() {
        return address;
    }

    public void setAddress(AddressRequest address) {
        this.address = address;
    }

    public Integer getGovernorateId() {
        return governorateId;
    }

    public void setGovernorateId(Integer governorateId) {
        this.governorateId = governorateId;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
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

    public String getFurnishing() {
        return furnishing;
    }

    public void setFurnishing(String furnishing) {
        this.furnishing = furnishing;
    }

    public BigDecimal getExpectedRent() {
        return expectedRent;
    }

    public void setExpectedRent(BigDecimal expectedRent) {
        this.expectedRent = expectedRent;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}
