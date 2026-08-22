package com.bokl.homerental.controller.dto.listing;

import com.bokl.homerental.entity.listing.Address;

public class AddressResponse {

    private Long id;
    private String streetAddress;
    private String buildingNumber;
    private String apartmentNumber;
    private String landmark;
    private Double latitude;
    private Double longitude;
    private String googlePlaceId;

    public AddressResponse() {
    }

    public static AddressResponse from(Address a) {
        if (a == null) return null;
        AddressResponse r = new AddressResponse();
        r.id = a.getId();
        r.streetAddress = a.getStreetAddress();
        r.buildingNumber = a.getBuildingNumber();
        r.apartmentNumber = a.getApartmentNumber();
        r.landmark = a.getLandmark();
        r.latitude = a.getLatitude();
        r.longitude = a.getLongitude();
        r.googlePlaceId = a.getGooglePlaceId();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getBuildingNumber() { return buildingNumber; }
    public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getGooglePlaceId() { return googlePlaceId; }
    public void setGooglePlaceId(String googlePlaceId) { this.googlePlaceId = googlePlaceId; }
}
