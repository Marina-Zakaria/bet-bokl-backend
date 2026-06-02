package com.bokl.homerental.service.listing;

import com.bokl.homerental.controller.dto.listing.AdminDecisionRequest;
import com.bokl.homerental.controller.dto.listing.ActivateListingRequest;
import com.bokl.homerental.controller.dto.listing.AssignInspectorRequest;
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
import com.bokl.homerental.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ListingAdministrationService {

    private final PropertyApplicationRepository applicationRepository;
    private final AdminDecisionRepository adminDecisionRepository;
    private final ListingRepository listingRepository;
    private final InspectionReportRepository inspectionReportRepository;
    private final TermsConsentRepository termsConsentRepository;
    private final AuthUserRepository authUserRepository;

    public ListingAdministrationService(
            PropertyApplicationRepository applicationRepository,
            AdminDecisionRepository adminDecisionRepository,
            ListingRepository listingRepository,
            InspectionReportRepository inspectionReportRepository,
            TermsConsentRepository termsConsentRepository,
            AuthUserRepository authUserRepository) {
        this.applicationRepository = applicationRepository;
        this.adminDecisionRepository = adminDecisionRepository;
        this.listingRepository = listingRepository;
        this.inspectionReportRepository = inspectionReportRepository;
        this.termsConsentRepository = termsConsentRepository;
        this.authUserRepository = authUserRepository;
    }

    public AdminDecision reviewApplication(Long applicationId, AdminDecisionRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property application not found"));

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

        return decision;
    }

    public PropertyApplication assignInspector(Long applicationId, AssignInspectorRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property application not found"));

        AuthUser inspector = authUserRepository.findById(request.getInspectorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspector not found"));

        application.setInspector(inspector);
        application.setInspectorAssignedAt(java.time.Instant.now());
        application.setStatus(PropertyApplication.Status.PENDING_INSPECTOR_SLOTS);
        return applicationRepository.save(application);
    }

    public AdminDecision reviewInspectionReport(Long applicationId, AdminDecisionRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property application not found"));

        InspectionReport report = inspectionReportRepository.findFirstByScheduleApplicationIdOrderByCreatedAtDesc(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection report not found"));

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

        return decision;
    }

    public Listing activateListing(Long applicationId, ActivateListingRequest request) {
        PropertyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property application not found"));

        if (application.getStatus() != PropertyApplication.Status.CONSENT_PROVIDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The application is not ready for listing activation.");
        }

        TermsConsent termsConsent = termsConsentRepository.findAll().stream()
                .filter(consent -> consent.getApplication().getId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner consent not found."));

        InspectionReport report = inspectionReportRepository.findFirstByScheduleApplicationIdOrderByCreatedAtDesc(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection report not found"));

        Listing listing = new Listing();
        listing.setPropertyDetail(report.getPropertyDetail());
        listing.setOwner(application.getUser());
        listing.setApplication(application);
        listing.setDecision(adminDecisionRepository.findByApplicationId(applicationId).stream()
                .filter(decision -> decision.getReport() != null)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin decision for report review not found.")));
        listing.setFinalRent(request.getFinalRent());
        listing.setStatus(Listing.Status.ACTIVE);
        listingRepository.save(listing);

        application.setStatus(PropertyApplication.Status.LISTED);
        applicationRepository.save(application);

        return listing;
    }
}
