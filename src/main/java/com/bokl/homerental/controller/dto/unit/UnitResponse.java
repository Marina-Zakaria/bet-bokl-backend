package com.bokl.homerental.controller.dto.unit;

import com.bokl.homerental.entity.unit.RentalUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Contract-aligned unit payload with nested governorate/area/owner
 * plus flat aliases for older clients.
 */
public class UnitResponse {

    private Long id;
    private String title;
    private String description;
    private NamedLocationDto governorate;
    private NamedLocationDto area;
    private String streetName;
    private String buildingNumber;
    private String apartmentNumber;
    private String landmark;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer roomsCount;
    private Integer bathroomsCount;
    private Integer areaSqm;
    private RentalUnit.Furnishing furnishing;
    private RentalUnit.Category category;
    private BigDecimal rentPerDay;
    private Integer maxAdults;
    private Integer maxChildren;
    private boolean requiresOwnerApproval;
    private Long instantBookingTermsId;
    private boolean hasElevator;
    private boolean hasWashingMachine;
    private boolean hasWifi;
    private boolean hasAirConditioning;
    private boolean hasParking;
    private boolean hasPool;
    private boolean hasTv;
    private boolean hasKitchen;
    private boolean hasBalcony;
    private boolean hasWaterHeater;
    private List<String> photos;
    private RentalUnit.Status status;
    private boolean verified;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer bookingCount;
    private OwnerSummaryDto owner;
    private Instant publishedAt;
    private Instant createdAt;

    // Flat aliases
    private Long ownerId;
    private String ownerName;
    private Integer governorateId;
    private String governorateName;
    private String governorateNameAr;
    private String governorateNameEn;
    private Integer areaId;
    private String areaName;
    private String areaNameAr;
    private String areaNameEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public NamedLocationDto getGovernorate() { return governorate; }
    public void setGovernorate(NamedLocationDto governorate) { this.governorate = governorate; }

    public NamedLocationDto getArea() { return area; }
    public void setArea(NamedLocationDto area) { this.area = area; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getBuildingNumber() { return buildingNumber; }
    public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Integer getRoomsCount() { return roomsCount; }
    public void setRoomsCount(Integer roomsCount) { this.roomsCount = roomsCount; }

    public Integer getBathroomsCount() { return bathroomsCount; }
    public void setBathroomsCount(Integer bathroomsCount) { this.bathroomsCount = bathroomsCount; }

    public Integer getAreaSqm() { return areaSqm; }
    public void setAreaSqm(Integer areaSqm) { this.areaSqm = areaSqm; }

    public RentalUnit.Furnishing getFurnishing() { return furnishing; }
    public void setFurnishing(RentalUnit.Furnishing furnishing) { this.furnishing = furnishing; }

    public RentalUnit.Category getCategory() { return category; }
    public void setCategory(RentalUnit.Category category) { this.category = category; }

    public BigDecimal getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(BigDecimal rentPerDay) { this.rentPerDay = rentPerDay; }

    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }
    public boolean isRequiresOwnerApproval() { return requiresOwnerApproval; }
    public void setRequiresOwnerApproval(boolean requiresOwnerApproval) {
        this.requiresOwnerApproval = requiresOwnerApproval;
    }
    public Long getInstantBookingTermsId() { return instantBookingTermsId; }
    public void setInstantBookingTermsId(Long instantBookingTermsId) {
        this.instantBookingTermsId = instantBookingTermsId;
    }

    public boolean isHasElevator() { return hasElevator; }
    public void setHasElevator(boolean hasElevator) { this.hasElevator = hasElevator; }

    public boolean isHasWashingMachine() { return hasWashingMachine; }
    public void setHasWashingMachine(boolean hasWashingMachine) { this.hasWashingMachine = hasWashingMachine; }

    public boolean isHasWifi() { return hasWifi; }
    public void setHasWifi(boolean hasWifi) { this.hasWifi = hasWifi; }

    public boolean isHasAirConditioning() { return hasAirConditioning; }
    public void setHasAirConditioning(boolean hasAirConditioning) { this.hasAirConditioning = hasAirConditioning; }

    public boolean isHasParking() { return hasParking; }
    public void setHasParking(boolean hasParking) { this.hasParking = hasParking; }

    public boolean isHasPool() { return hasPool; }
    public void setHasPool(boolean hasPool) { this.hasPool = hasPool; }

    public boolean isHasTv() { return hasTv; }
    public void setHasTv(boolean hasTv) { this.hasTv = hasTv; }

    public boolean isHasKitchen() { return hasKitchen; }
    public void setHasKitchen(boolean hasKitchen) { this.hasKitchen = hasKitchen; }

    public boolean isHasBalcony() { return hasBalcony; }
    public void setHasBalcony(boolean hasBalcony) { this.hasBalcony = hasBalcony; }

    public boolean isHasWaterHeater() { return hasWaterHeater; }
    public void setHasWaterHeater(boolean hasWaterHeater) { this.hasWaterHeater = hasWaterHeater; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public RentalUnit.Status getStatus() { return status; }
    public void setStatus(RentalUnit.Status status) { this.status = status; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Integer getBookingCount() { return bookingCount; }
    public void setBookingCount(Integer bookingCount) { this.bookingCount = bookingCount; }

    public OwnerSummaryDto getOwner() { return owner; }
    public void setOwner(OwnerSummaryDto owner) { this.owner = owner; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Integer getGovernorateId() { return governorateId; }
    public void setGovernorateId(Integer governorateId) { this.governorateId = governorateId; }

    public String getGovernorateName() { return governorateName; }
    public void setGovernorateName(String governorateName) { this.governorateName = governorateName; }

    public String getGovernorateNameAr() { return governorateNameAr; }
    public void setGovernorateNameAr(String governorateNameAr) { this.governorateNameAr = governorateNameAr; }

    public String getGovernorateNameEn() { return governorateNameEn; }
    public void setGovernorateNameEn(String governorateNameEn) { this.governorateNameEn = governorateNameEn; }

    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getAreaNameAr() { return areaNameAr; }
    public void setAreaNameAr(String areaNameAr) { this.areaNameAr = areaNameAr; }

    public String getAreaNameEn() { return areaNameEn; }
    public void setAreaNameEn(String areaNameEn) { this.areaNameEn = areaNameEn; }
}
