package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class SubmitPropertyApplicationRequest {

    @Valid
    @NotNull
    private PropertyDetailRequest propertyDetail;

    public SubmitPropertyApplicationRequest() {
    }

    public PropertyDetailRequest getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetailRequest propertyDetail) {
        this.propertyDetail = propertyDetail;
    }
}
