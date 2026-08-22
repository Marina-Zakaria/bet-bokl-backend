package com.bokl.homerental.controller.dto.unit;

import com.bokl.homerental.entity.listing.TermsDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpsertTermsRequest {
    @NotNull
    private TermsDefinition.Type termsType = TermsDefinition.Type.OWNER_LISTING;

    @NotBlank
    @Size(max = 50)
    private String version;

    @Size(max = 255)
    private String titleEn;

    @Size(max = 255)
    private String titleAr;

    @NotBlank
    private String contentEn;

    @NotBlank
    private String contentAr;

    @NotNull
    private Boolean active;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getTitleAr() { return titleAr; }
    public void setTitleAr(String titleAr) { this.titleAr = titleAr; }

    public String getContentEn() { return contentEn; }
    public void setContentEn(String contentEn) { this.contentEn = contentEn; }

    public String getContentAr() { return contentAr; }
    public void setContentAr(String contentAr) { this.contentAr = contentAr; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public TermsDefinition.Type getTermsType() { return termsType; }
    public void setTermsType(TermsDefinition.Type termsType) { this.termsType = termsType; }
}
