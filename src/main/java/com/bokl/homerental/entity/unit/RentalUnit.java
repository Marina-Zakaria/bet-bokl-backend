package com.bokl.homerental.entity.unit;

import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.Governorate;
import com.bokl.homerental.entity.listing.TermsDefinition;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rental_units")
public class RentalUnit {

    public enum Status {
        ACTIVE,
        PAUSED,
        ARCHIVED
    }

    public enum Category {
        ECONOMY,
        PREMIUM,
        HOTEL
    }

    public enum Furnishing {
        FURNISHED,
        SEMI_FURNISHED,
        UNFURNISHED
    }

    public enum IdDocumentType {
        NATIONAL_ID,
        PASSPORT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AuthUser owner;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "governorate_id", nullable = false)
    private Governorate governorate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "street_name", nullable = false, length = 255)
    private String streetName;

    @Column(name = "building_number", length = 50)
    private String buildingNumber;

    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;

    @Column(length = 255)
    private String landmark;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "rooms_count", nullable = false)
    private Integer roomsCount;

    @Column(name = "bathrooms_count")
    private Integer bathroomsCount = 1;

    @Column(name = "area_sqm", nullable = false)
    private Integer areaSqm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Furnishing furnishing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(name = "rent_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentPerDay;

    @Column(name = "max_adults", nullable = false)
    private Integer maxAdults;

    @Column(name = "max_children", nullable = false)
    private Integer maxChildren;

    @Column(name = "requires_owner_approval", nullable = false)
    private boolean requiresOwnerApproval = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instant_booking_terms_id")
    private TermsDefinition instantBookingTerms;

    @Column(name = "instant_booking_terms_accepted_at")
    private Instant instantBookingTermsAcceptedAt;

    @Column(name = "has_elevator")
    private boolean hasElevator;

    @Column(name = "has_washing_machine")
    private boolean hasWashingMachine;

    @Column(name = "has_wifi")
    private boolean hasWifi;

    @Column(name = "has_air_conditioning")
    private boolean hasAirConditioning;

    @Column(name = "has_parking")
    private boolean hasParking;

    @Column(name = "has_pool")
    private boolean hasPool;

    @Column(name = "has_tv")
    private boolean hasTv;

    @Column(name = "has_kitchen")
    private boolean hasKitchen;

    @Column(name = "has_balcony")
    private boolean hasBalcony;

    @Column(name = "has_water_heater")
    private boolean hasWaterHeater;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", nullable = false)
    private String photos;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_document_type", nullable = false, length = 50)
    private IdDocumentType idDocumentType;

    @Column(name = "id_front_url", nullable = false, length = 1024)
    private String idFrontUrl;

    @Column(name = "id_back_url", nullable = false, length = 1024)
    private String idBackUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status = Status.ACTIVE;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "booking_count")
    private Integer bookingCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_definition_id", nullable = false)
    private TermsDefinition termsDefinition;

    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RentalUnit() {
    }

    public Long getId() {
        return id;
    }

    public AuthUser getOwner() {
        return owner;
    }

    public void setOwner(AuthUser owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getRoomsCount() {
        return roomsCount;
    }

    public void setRoomsCount(Integer roomsCount) {
        this.roomsCount = roomsCount;
    }

    public Integer getBathroomsCount() {
        return bathroomsCount;
    }

    public void setBathroomsCount(Integer bathroomsCount) {
        this.bathroomsCount = bathroomsCount;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getRentPerDay() {
        return rentPerDay;
    }

    public void setRentPerDay(BigDecimal rentPerDay) {
        this.rentPerDay = rentPerDay;
    }

    public Integer getMaxAdults() {
        return maxAdults;
    }

    public void setMaxAdults(Integer maxAdults) {
        this.maxAdults = maxAdults;
    }

    public Integer getMaxChildren() {
        return maxChildren;
    }

    public void setMaxChildren(Integer maxChildren) {
        this.maxChildren = maxChildren;
    }

    public boolean isRequiresOwnerApproval() { return requiresOwnerApproval; }
    public void setRequiresOwnerApproval(boolean requiresOwnerApproval) { this.requiresOwnerApproval = requiresOwnerApproval; }
    public TermsDefinition getInstantBookingTerms() { return instantBookingTerms; }
    public void setInstantBookingTerms(TermsDefinition instantBookingTerms) { this.instantBookingTerms = instantBookingTerms; }
    public Instant getInstantBookingTermsAcceptedAt() { return instantBookingTermsAcceptedAt; }
    public void setInstantBookingTermsAcceptedAt(Instant instantBookingTermsAcceptedAt) {
        this.instantBookingTermsAcceptedAt = instantBookingTermsAcceptedAt;
    }

    public boolean isHasElevator() {
        return hasElevator;
    }

    public void setHasElevator(boolean hasElevator) {
        this.hasElevator = hasElevator;
    }

    public boolean isHasWashingMachine() {
        return hasWashingMachine;
    }

    public void setHasWashingMachine(boolean hasWashingMachine) {
        this.hasWashingMachine = hasWashingMachine;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isHasAirConditioning() {
        return hasAirConditioning;
    }

    public void setHasAirConditioning(boolean hasAirConditioning) {
        this.hasAirConditioning = hasAirConditioning;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isHasPool() {
        return hasPool;
    }

    public void setHasPool(boolean hasPool) {
        this.hasPool = hasPool;
    }

    public boolean isHasTv() {
        return hasTv;
    }

    public void setHasTv(boolean hasTv) {
        this.hasTv = hasTv;
    }

    public boolean isHasKitchen() {
        return hasKitchen;
    }

    public void setHasKitchen(boolean hasKitchen) {
        this.hasKitchen = hasKitchen;
    }

    public boolean isHasBalcony() {
        return hasBalcony;
    }

    public void setHasBalcony(boolean hasBalcony) {
        this.hasBalcony = hasBalcony;
    }

    public boolean isHasWaterHeater() {
        return hasWaterHeater;
    }

    public void setHasWaterHeater(boolean hasWaterHeater) {
        this.hasWaterHeater = hasWaterHeater;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public IdDocumentType getIdDocumentType() {
        return idDocumentType;
    }

    public void setIdDocumentType(IdDocumentType idDocumentType) {
        this.idDocumentType = idDocumentType;
    }

    public String getIdFrontUrl() {
        return idFrontUrl;
    }

    public void setIdFrontUrl(String idFrontUrl) {
        this.idFrontUrl = idFrontUrl;
    }

    public String getIdBackUrl() {
        return idBackUrl;
    }

    public void setIdBackUrl(String idBackUrl) {
        this.idBackUrl = idBackUrl;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Integer bookingCount) {
        this.bookingCount = bookingCount;
    }

    public TermsDefinition getTermsDefinition() {
        return termsDefinition;
    }

    public void setTermsDefinition(TermsDefinition termsDefinition) {
        this.termsDefinition = termsDefinition;
    }

    public Instant getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public void setTermsAcceptedAt(Instant termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
