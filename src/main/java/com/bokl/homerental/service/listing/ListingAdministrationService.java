package com.bokl.homerental.service.listing;

import com.bokl.homerental.controller.dto.listing.AdminDecisionRequest;
import com.bokl.homerental.controller.dto.listing.ActivateListingRequest;
import com.bokl.homerental.controller.dto.listing.AdminDecisionResponse;
import com.bokl.homerental.controller.dto.listing.AssignInspectorRequest;
import com.bokl.homerental.controller.dto.listing.AdminInspectionReportDetailsResponse;
import com.bokl.homerental.controller.dto.listing.AdminInspectionReportSummaryResponse;
import com.bokl.homerental.controller.dto.listing.ListingResponse;
import com.bokl.homerental.controller.dto.listing.PropertyApplicationResponse;
import com.bokl.homerental.controller.dto.listing.PropertyDetailResponse;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.inspection.InspectionReport;
import com.bokl.homerental.entity.listing.AdminDecision;
import com.bokl.homerental.entity.listing.Listing;
import com.bokl.homerental.entity.listing.PropertyApplication;
import com.bokl.homerental.entity.listing.TermsConsent;
import com.bokl.homerental.repository.AuthUserRepository;
import com.bokl.homerental.repository.listing.AdminDecisionRepository;
import com.bokl.homerental.repository.listing.ListingRepository;
import com.bokl.homerental.repository.listing.PropertyApplicationRepository;
import com.bokl.homerental.repository.listing.TermsConsentRepository;
import com.bokl.homerental.repository.inspection.InspectionReportRepository;
import com.bokl.homerental.service.MessageService;
import com.bokl.homerental.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@Transactional
public class ListingAdministrationService {

    private final PropertyApplicationRepository applicationRepository;
    private final AdminDecisionRepository adminDecisionRepository;
    private final ListingRepository listingRepository;
    private final InspectionReportRepository inspectionReportRepository;
    private final TermsConsentRepository termsConsentRepository;
    private final AuthUserRepository authUserRepository;
    private final MessageService msg;

    public ListingAdministrationService(
            PropertyApplicationRepository applicationRepository,
            AdminDecisionRepository adminDecisionRepository,
            ListingRepository listingRepository,
            InspectionReportRepository inspectionReportRepository,
            TermsConsentRepository termsConsentRepository,
            AuthUserRepository authUserRepository,
            MessageService msg) {
        this.applicationRepository = applicationRepository;
        this.adminDecisionRepository = adminDecisionRepository;
        this.listingRepository = listingRepository;
        this.inspectionReportRepository = inspectionReportRepository;
        this.termsConsentRepository = termsConsentRepository;
        this.authUserRepository = authUserRepository;
        this.msg = msg;
    }

    public Page<PropertyApplicationResponse> listAllApplications(String status, Pageable pageable) {
        Page<PropertyApplication> page;
        if (status != null && !status.isBlank()) {
            PropertyApplication.Status statusEnum = PropertyApplication.Status.valueOf(status.toUpperCase());
            page = applicationRepository.findByStatus(statusEnum, pageable);
        } else {
            page = applicationRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    public PropertyApplicationResponse getApplicationById(Long applicationId) {
        PropertyApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.application.not_found")));
        return toResponse(application);
    }

    public Page<AdminInspectionReportSummaryResponse> listInspectionReports(
            Long inspectorId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("admin.reports.date_range.invalid"));
        }

        Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = toDate != null ? toDate.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC) : null;

        return inspectionReportRepository.searchReports(inspectorId, fromInstant, toInstant, pageable)
                .map(this::toReportSummaryResponse);
    }

    public AdminInspectionReportDetailsResponse getInspectionReportById(Long reportId) {
        InspectionReport report = inspectionReportRepository.findById(reportId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.report.not_found")));
        return toReportDetailsResponse(report);
    }

    private PropertyApplicationResponse toResponse(PropertyApplication app) {
        return new PropertyApplicationResponse(
                app.getId(),
                app.getStatus().name(),
                app.getSubmittedAt(),
                app.getUpdatedAt(),
                PropertyDetailResponse.from(app.getPropertyDetail())
        );
    }

    public AdminDecisionResponse reviewApplication(Long applicationId, AdminDecisionRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.application.not_found")));

        AuthUser admin = SecurityUtils.currentUser();
        AdminDecision decision = new AdminDecision();
        decision.setApplication(application);
        decision.setAdmin(admin);
        decision.setDecision(AdminDecision.Decision.valueOf(request.getDecision().toUpperCase()));
        decision.setFinalRent(request.getFinalRent());
        decision.setComments(request.getComments());
        adminDecisionRepository.save(decision);

        if (decision.getDecision() == AdminDecision.Decision.REJECT) {
            application.setStatus(PropertyApplication.Status.REJECTED);
        } else {
            application.setStatus(PropertyApplication.Status.UNDER_REVIEW);
        }
        applicationRepository.save(application);

        return toDecisionResponse(decision);
    }

    public PropertyApplicationResponse assignInspector(Long applicationId, AssignInspectorRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.application.not_found")));

        AuthUser inspector = authUserRepository.findById(request.getInspectorId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.inspector.not_found")));

        application.setInspector(inspector);
        application.setInspectorAssignedAt(java.time.Instant.now());
        application.setStatus(PropertyApplication.Status.PENDING_INSPECTOR_SLOTS);
        return toResponse(applicationRepository.save(application));
    }

    public AdminDecisionResponse reviewInspectionReport(Long applicationId, AdminDecisionRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.application.not_found")));

        InspectionReport report = inspectionReportRepository.findFirstByScheduleApplicationIdOrderByCreatedAtDesc(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.report.not_found")));

        AuthUser admin = SecurityUtils.currentUser();
        AdminDecision decision = new AdminDecision();
        decision.setApplication(application);
        decision.setAdmin(admin);
        decision.setReport(report);
        decision.setDecision(AdminDecision.Decision.valueOf(request.getDecision().toUpperCase()));
        decision.setFinalRent(request.getFinalRent());
        decision.setComments(request.getComments());
        adminDecisionRepository.save(decision);

        if (decision.getDecision() == AdminDecision.Decision.APPROVE) {
            application.setStatus(PropertyApplication.Status.PENDING_OWNER_CONSENT);
        } else if (decision.getDecision() == AdminDecision.Decision.REJECT) {
            application.setStatus(PropertyApplication.Status.REJECTED);
        } else {
            application.setStatus(PropertyApplication.Status.INSPECTION_COMPLETED);
        }
        applicationRepository.save(application);

        return toDecisionResponse(decision);
    }

    public ListingResponse activateListing(Long applicationId, ActivateListingRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.application.not_found")));

        if (application.getStatus() != PropertyApplication.Status.CONSENT_PROVIDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                msg.get("admin.listing.activation.invalid_status"));
        }

        TermsConsent termsConsent = termsConsentRepository.findAll().stream()
                .filter(consent -> consent.getApplication().getId().equals(applicationId))
                .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                msg.get("admin.owner_consent.not_found")));

        InspectionReport report = inspectionReportRepository.findFirstByScheduleApplicationIdOrderByCreatedAtDesc(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("admin.report.not_found")));

        Listing listing = new Listing();
        listing.setPropertyDetail(report.getPropertyDetail());
        listing.setOwner(application.getUser());
        listing.setApplication(application);
        listing.setDecision(adminDecisionRepository.findByApplicationId(applicationId).stream()
                .filter(decision -> decision.getReport() != null)
                .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                msg.get("admin.report_decision.not_found"))));
        listing.setFinalRent(request.getFinalRent());
        listing.setStatus(Listing.Status.ACTIVE);
        listingRepository.save(listing);

        application.setStatus(PropertyApplication.Status.LISTED);
        applicationRepository.save(application);

        return new ListingResponse(
                listing.getId(),
                application.getId(),
                application.getUser().getId(),
                listing.getPropertyDetail().getId(),
                listing.getDecision().getId(),
                listing.getFinalRent(),
                listing.getStatus().name(),
                listing.getActivatedAt()
        );
    }

    private AdminDecisionResponse toDecisionResponse(AdminDecision d) {
        return new AdminDecisionResponse(
                d.getId(),
                d.getApplication().getId(),
                d.getAdmin().getId(),
                d.getReport() != null ? d.getReport().getId() : null,
                d.getDecision().name(),
                d.getFinalRent(),
                d.getComments(),
                d.getCreatedAt()
        );
    }

    private AdminInspectionReportSummaryResponse toReportSummaryResponse(InspectionReport report) {
        return new AdminInspectionReportSummaryResponse(
                report.getId(),
                report.getSchedule().getApplication().getId(),
                report.getSchedule().getId(),
                report.getInspector().getId(),
                report.getRecommendation().name(),
                report.getAgreedRent(),
                report.getComments(),
                report.getCreatedAt()
        );
    }

    private AdminInspectionReportDetailsResponse toReportDetailsResponse(InspectionReport report) {
        return new AdminInspectionReportDetailsResponse(
                report.getId(),
                report.getSchedule().getApplication().getId(),
                report.getSchedule().getId(),
                report.getInspector().getId(),
                PropertyDetailResponse.from(report.getPropertyDetail()),
                report.getRecommendation().name(),
                report.getAgreedRent(),
                readJson(report.getReportData()),
                readJson(report.getEvidencePhotos()),
                report.getComments(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    private Object readJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        return com.bokl.homerental.util.JsonUtils.fromJson(rawJson, Object.class);
    }
}
