package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.listing.TermsDefinitionResponse;
import com.bokl.homerental.service.listing.TermsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LEGACY — Terms API for the old inspection consent flow.
 * New flow uses /api/terms. Base path: /api/legacy/listings/terms
 */
@RestController
@RequestMapping("/api/legacy/listings/terms")
public class TermsController {

    private final TermsService termsService;

    public TermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    /**
     * Fetch all active terms definitions.
     * Terms specify commission percentage and other rental policies.
     * Returns current active terms in the user's preferred language (Accept-Language header).
     */
    @GetMapping
    public List<TermsDefinitionResponse> getActiveTerms() {
        return termsService.getActiveTerms();
    }

    /**
     * Get a specific terms definition by ID.
     * Used for reference before providing consent.
     */
    @GetMapping("/{termsId}")
    public TermsDefinitionResponse getTermsById(@PathVariable Long termsId) {
        return termsService.getTermsById(termsId);
    }
}
