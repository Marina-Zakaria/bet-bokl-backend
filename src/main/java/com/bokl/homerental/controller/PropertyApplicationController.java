package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.listing.*;
import com.bokl.homerental.service.listing.PropertyApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LEGACY — Owner/User API for property applications and inspection workflow.
 * Isolated from the new unit-upload flow. Base path: /api/legacy/listings
 */
@RestController
@RequestMapping("/api/legacy/listings")
public class PropertyApplicationController {

    private final PropertyApplicationService applicationService;

    public PropertyApplicationController(PropertyApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Submit a new property application.
     * The owner specifies property details, amenities, and photos.
     * The application begins in SUBMITTED status.
     */
    @RequiresLogin
    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyApplicationResponse createApplication(@Valid @RequestBody SubmitPropertyApplicationRequest request) {
        return applicationService.createApplication(request);
    }

    /**
     * Retrieve all applications for the current owner.
     * Pageable request: page, size, sort
     * Filterable by: status, createdAfter, createdBefore, inspectorId
     */
    @RequiresLogin
    @GetMapping("/applications")
    public Page<PropertyApplicationResponse> listApplications(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return applicationService.listApplicationsByOwner(status, pageable);
    }

    /**
     * Get details of a specific application.
     * Returns full details: property, location, status, assigned inspector.
     */
    @RequiresLogin
    @GetMapping("/applications/{applicationId}")
    public PropertyApplicationResponse getApplication(@PathVariable Long applicationId) {
        return applicationService.getApplicationByIdForOwner(applicationId);
    }

    /**
     * Submit 4-5 owner availability windows for inspection.
     * Called after admin assigns an inspector to the application.
     * Each window specifies a proposed time range when owner can meet inspector.
     */
    @RequiresLogin
    @PostMapping("/applications/{applicationId}/inspection-windows")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InspectionWindowRequest> submitInspectionWindows(
            @PathVariable Long applicationId,
            @Valid @RequestBody List<InspectionWindowRequest> windows) {
        return applicationService.submitInspectionWindows(applicationId, windows);
    }

    /**
     * Retrieve the inspection schedule for an application.
     * Shows proposed windows and any confirmed inspection time.
     */
    @RequiresLogin
    @GetMapping("/applications/{applicationId}/inspection-schedules")
    public List<InspectionScheduleResponse> getInspectionSchedules(@PathVariable Long applicationId) {
        return applicationService.getInspectionSchedulesByApplication(applicationId);
    }

    /**
     * Provide owner consent for the agreed rent and terms.
     * Called after admin reviews and approves the inspection report.
     * Requires the owner to agree to commission percentage and final rent.
     */
    @RequiresLogin
    @PostMapping("/applications/{applicationId}/terms-consent")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyApplicationResponse submitTermsConsent(
            @PathVariable Long applicationId,
            @Valid @RequestBody TermsConsentRequest request) {
        return applicationService.submitTermsConsent(applicationId, request);
    }
}
