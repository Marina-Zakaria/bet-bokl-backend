package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.annotation.RequiresRole;
import com.bokl.homerental.controller.dto.listing.*;
import com.bokl.homerental.service.listing.ListingAdministrationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * LEGACY — Admin API for the inspection/approval listing workflow.
 * Isolated from the new unit-upload flow. Base path: /api/legacy/admin/listings
 */
@RestController
@RequestMapping("/api/legacy/admin/listings")
public class AdminListingController {

    private final ListingAdministrationService adminService;

    public AdminListingController(ListingAdministrationService adminService) {
        this.adminService = adminService;
    }

    /**
     * List all property applications.
     * Optional filter: status (e.g. SUBMITTED, UNDER_REVIEW, REJECTED, LISTED...)
     * Supports pagination and sorting.
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @GetMapping("/applications")
    public Page<PropertyApplicationResponse> listAllApplications(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return adminService.listAllApplications(status, pageable);
    }

    /**
     * Get a single application by ID.
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @GetMapping("/applications/{applicationId}")
    public PropertyApplicationResponse getApplication(@PathVariable Long applicationId) {
        return adminService.getApplicationById(applicationId);
    }

    /**
     * List inspection reports with optional filters.
     * Supports pagination and sorting.
     * Optional filters:
     * - inspectorId
     * - fromDate (yyyy-MM-dd)
     * - toDate   (yyyy-MM-dd)
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @GetMapping("/reports")
    public Page<AdminInspectionReportSummaryResponse> listInspectionReports(
            @RequestParam(required = false) Long inspectorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        return adminService.listInspectionReports(inspectorId, fromDate, toDate, pageable);
    }

    /**
     * Get a single inspection report by id with full details.
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @GetMapping("/reports/{reportId}")
    public AdminInspectionReportDetailsResponse getInspectionReport(@PathVariable Long reportId) {
        return adminService.getInspectionReportById(reportId);
    }

    /**
     * Initial document review of a submitted application.
     * decision=APPROVE → UNDER_REVIEW
     * decision=REJECT  → REJECTED
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @PostMapping("/applications/{applicationId}/review")
    public AdminDecisionResponse reviewApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody AdminDecisionRequest request) {
        return adminService.reviewApplication(applicationId, request);
    }

    /**
     * Assign an inspector to an application.
     * Application must be in UNDER_REVIEW status.
     * Transitions application to PENDING_INSPECTOR_SLOTS.
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @PostMapping("/applications/{applicationId}/assign-inspector")
    public PropertyApplicationResponse assignInspector(
            @PathVariable Long applicationId,
            @Valid @RequestBody AssignInspectorRequest request) {
        return adminService.assignInspector(applicationId, request);
    }

    /**
     * Review the inspector's inspection report.
     * decision=APPROVE             → PENDING_OWNER_CONSENT
     * decision=REJECT              → REJECTED
     * decision=REQUEST_REINSPECTION → INSPECTION_COMPLETED (awaiting new inspection)
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @PostMapping("/applications/{applicationId}/review-report")
    public AdminDecisionResponse reviewInspectionReport(
            @PathVariable Long applicationId,
            @Valid @RequestBody AdminDecisionRequest request) {
        return adminService.reviewInspectionReport(applicationId, request);
    }

    /**
     * Activate a listing once the owner has provided consent.
     * Application must be in CONSENT_PROVIDED status.
     * Transitions application to LISTED and creates a Listing record.
     */
    @RequiresLogin
    @RequiresRole("ADMIN")
    @PostMapping("/applications/{applicationId}/activate")
    public ListingResponse activateListing(
            @PathVariable Long applicationId,
            @Valid @RequestBody ActivateListingRequest request) {
        return adminService.activateListing(applicationId, request);
    }
}
