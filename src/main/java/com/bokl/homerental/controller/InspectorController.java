package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.annotation.RequiresRole;
import com.bokl.homerental.controller.dto.listing.*;
import com.bokl.homerental.service.inspection.InspectionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LEGACY — Inspector API for the inspection/approval listing workflow.
 * Isolated from the new unit-upload flow. Base path: /api/legacy/inspector
 */
@RestController
@RequestMapping("/api/legacy/inspector")
public class InspectorController {

    private final InspectionService inspectionService;

    public InspectorController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    /**
     * List all property applications assigned to the current inspector.
     * Optional filter: status
     * Supports pagination and sorting.
     */
    @RequiresLogin
    @RequiresRole("INSPECTOR")
    @GetMapping("/applications")
    public Page<PropertyApplicationResponse> listAssignedApplications(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return inspectionService.listAssignedApplications(status, pageable);
    }

    /**
     * Get a single application assigned to the current inspector.
     */
    @RequiresLogin
    @RequiresRole("INSPECTOR")
    @GetMapping("/applications/{applicationId}")
    public PropertyApplicationResponse getAssignedApplication(@PathVariable Long applicationId) {
        return inspectionService.getAssignedApplicationById(applicationId);
    }

    /**
     * Get the inspection schedules (proposed windows) for an assigned application.
     */
    @RequiresLogin
    @RequiresRole("INSPECTOR")
    @GetMapping("/applications/{applicationId}/schedules")
    public List<InspectionScheduleResponse> getSchedules(@PathVariable Long applicationId) {
        return inspectionService.getSchedulesForApplication(applicationId);
    }

    /**
     * Confirm a proposed inspection slot by setting the exact inspection time.
     * exactTime must be within the proposed window.
     * Transitions application to INSPECTION_SCHEDULED.
     */
    @RequiresLogin
    @RequiresRole("INSPECTOR")
    @PostMapping("/schedules/{scheduleId}/confirm")
    public InspectionScheduleResponse confirmSlot(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ConfirmInspectionRequest request) {
        return inspectionService.confirmInspectionSlot(scheduleId, request);
    }

    /**
     * Submit the inspection report after completing the inspection.
     * Includes property details, condition findings, and photos.
     * Transitions application to INSPECTION_COMPLETED.
     */
    @RequiresLogin
    @RequiresRole("INSPECTOR")
    @PostMapping("/schedules/{scheduleId}/report")
    public InspectionReportResponse submitReport(
            @PathVariable Long scheduleId,
            @Valid @RequestBody InspectionReportRequest request) {
        return inspectionService.submitInspectionReport(scheduleId, request);
    }
}
