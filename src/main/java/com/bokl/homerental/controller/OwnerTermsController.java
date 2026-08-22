package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.annotation.RequiresRole;
import com.bokl.homerental.controller.dto.listing.TermsDefinitionResponse;
import com.bokl.homerental.controller.dto.unit.UpsertTermsRequest;
import com.bokl.homerental.service.listing.TermsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Owner terms & conditions for the new unit-upload flow.
 * Readable by anyone (needed before/during owner submit); writable by ADMIN only.
 */
@RestController
@RequestMapping("/api/terms")
public class OwnerTermsController {

    private final TermsService termsService;

    public OwnerTermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    @GetMapping
    public List<TermsDefinitionResponse> getActiveTerms() {
        return termsService.getActiveTerms();
    }

    @GetMapping("/{termsId}")
    public TermsDefinitionResponse getTermsById(@PathVariable Long termsId) {
        return termsService.getTermsById(termsId);
    }

    @RequiresLogin
    @RequiresRole("ADMIN")
    @GetMapping("/all")
    public List<TermsDefinitionResponse> getAllTerms() {
        return termsService.getAllTerms();
    }

    @RequiresLogin
    @RequiresRole("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TermsDefinitionResponse createTerms(@Valid @RequestBody UpsertTermsRequest request) {
        return termsService.createTerms(request);
    }

    @RequiresLogin
    @RequiresRole("ADMIN")
    @PutMapping("/{termsId}")
    public TermsDefinitionResponse updateTerms(@PathVariable Long termsId,
                                               @Valid @RequestBody UpsertTermsRequest request) {
        return termsService.updateTerms(termsId, request);
    }
}
