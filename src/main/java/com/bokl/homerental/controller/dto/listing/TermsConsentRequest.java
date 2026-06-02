package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TermsConsentRequest {

    @NotNull
    private Long termsDefinitionId;

    @NotNull
    private BigDecimal agreedRent;

    @NotNull
    private BigDecimal commissionPercentage;

    private String ipAddress;
    private String userAgent;

    public TermsConsentRequest() {
    }

    public Long getTermsDefinitionId() {
        return termsDefinitionId;
    }

    public void setTermsDefinitionId(Long termsDefinitionId) {
        this.termsDefinitionId = termsDefinitionId;
    }

    public BigDecimal getAgreedRent() {
        return agreedRent;
    }

    public void setAgreedRent(BigDecimal agreedRent) {
        this.agreedRent = agreedRent;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
