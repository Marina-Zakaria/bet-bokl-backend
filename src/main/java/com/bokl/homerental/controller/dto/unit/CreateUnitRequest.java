package com.bokl.homerental.controller.dto.unit;

import com.bokl.homerental.entity.unit.RentalUnit;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class CreateUnitRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    private Integer governorateId;

    @NotNull
    private Integer areaId;

    @NotBlank
    @Size(max = 255)
    private String streetName;

    @Size(max = 50)
    private String buildingNumber;

    @Size(max = 50)
    private String apartmentNumber;

    @Size(max = 255)
    private String landmark;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull
    @Min(1)
    private Integer roomsCount;

    @Min(1)
    private Integer bathroomsCount = 1;

    @NotNull
    @Min(1)
    private Integer areaSqm;

    @NotNull
    private RentalUnit.Furnishing furnishing;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal rentPerDay;

    @NotNull
    @Min(1)
    private Integer maxAdults;

    @NotNull
    @Min(0)
    private Integer maxChildren;

    @NotNull
    private Boolean requiresOwnerApproval;

    private Long instantBookingTermsId;
    private Boolean acceptInstantBookingTerms = false;

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

    @NotEmpty
    private List<@NotBlank String> photos;

    @NotNull
    private RentalUnit.IdDocumentType idDocumentType;

    @NotBlank
    @Size(max = 1024)
    private String idFrontUrl;

    @NotBlank
    @Size(max = 1024)
    private String idBackUrl;

    @NotNull
    private Long termsDefinitionId;

    @AssertTrue(message = "Owner must accept terms and conditions")
    private boolean acceptTerms;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getGovernorateId() { return governorateId; }
    public void setGovernorateId(Integer governorateId) { this.governorateId = governorateId; }

    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }

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

    public BigDecimal getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(BigDecimal rentPerDay) { this.rentPerDay = rentPerDay; }

    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }
    public Boolean getRequiresOwnerApproval() { return requiresOwnerApproval; }
    public void setRequiresOwnerApproval(Boolean requiresOwnerApproval) {
        this.requiresOwnerApproval = requiresOwnerApproval;
    }
    public Long getInstantBookingTermsId() { return instantBookingTermsId; }
    public void setInstantBookingTermsId(Long instantBookingTermsId) {
        this.instantBookingTermsId = instantBookingTermsId;
    }
    public Boolean getAcceptInstantBookingTerms() { return acceptInstantBookingTerms; }
    public void setAcceptInstantBookingTerms(Boolean acceptInstantBookingTerms) {
        this.acceptInstantBookingTerms = acceptInstantBookingTerms;
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

    public RentalUnit.IdDocumentType getIdDocumentType() { return idDocumentType; }
    public void setIdDocumentType(RentalUnit.IdDocumentType idDocumentType) { this.idDocumentType = idDocumentType; }

    public String getIdFrontUrl() { return idFrontUrl; }
    public void setIdFrontUrl(String idFrontUrl) { this.idFrontUrl = idFrontUrl; }

    public String getIdBackUrl() { return idBackUrl; }
    public void setIdBackUrl(String idBackUrl) { this.idBackUrl = idBackUrl; }

    public Long getTermsDefinitionId() { return termsDefinitionId; }
    public void setTermsDefinitionId(Long termsDefinitionId) { this.termsDefinitionId = termsDefinitionId; }

    public boolean isAcceptTerms() { return acceptTerms; }
    public void setAcceptTerms(boolean acceptTerms) { this.acceptTerms = acceptTerms; }
}
