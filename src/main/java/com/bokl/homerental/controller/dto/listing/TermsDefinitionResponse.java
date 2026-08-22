package com.bokl.homerental.controller.dto.listing;

import com.bokl.homerental.entity.listing.TermsDefinition;
import java.time.Instant;

/** Contract-aligned terms payload with both AR/EN fields. */
public class TermsDefinitionResponse {
    private Long id;
    private String version;
    private TermsDefinition.Type termsType;
    private String titleAr;
    private String titleEn;
    private String contentAr;
    private String contentEn;
    private boolean active;
    private Instant effectiveAt;

    // Localized aliases for older clients
    private String title;
    private String content;

    public TermsDefinitionResponse() {
    }

    public TermsDefinitionResponse(Long id, String version, TermsDefinition.Type termsType,
                                   String titleAr, String titleEn,
                                   String contentAr, String contentEn,
                                   Instant effectiveAt, boolean active) {
        this.id = id;
        this.version = version;
        this.termsType = termsType;
        this.titleAr = titleAr;
        this.titleEn = titleEn;
        this.contentAr = contentAr;
        this.contentEn = contentEn;
        this.effectiveAt = effectiveAt;
        this.active = active;
        this.title = titleEn != null ? titleEn : titleAr;
        this.content = contentEn != null ? contentEn : contentAr;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public TermsDefinition.Type getTermsType() { return termsType; }
    public void setTermsType(TermsDefinition.Type termsType) { this.termsType = termsType; }

    public String getTitleAr() { return titleAr; }
    public void setTitleAr(String titleAr) { this.titleAr = titleAr; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getContentAr() { return contentAr; }
    public void setContentAr(String contentAr) { this.contentAr = contentAr; }

    public String getContentEn() { return contentEn; }
    public void setContentEn(String contentEn) { this.contentEn = contentEn; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(Instant effectiveAt) { this.effectiveAt = effectiveAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
