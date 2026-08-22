package com.bokl.homerental.service.listing;

import com.bokl.homerental.controller.dto.listing.*;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.Governorate;
import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.listing.Address;
import com.bokl.homerental.entity.listing.PropertyApplication;
import com.bokl.homerental.entity.listing.PropertyDetail;
import com.bokl.homerental.entity.listing.TermsConsent;
import com.bokl.homerental.entity.listing.TermsDefinition;
import com.bokl.homerental.repository.AreaRepository;
import com.bokl.homerental.repository.GovernorateRepository;
import com.bokl.homerental.repository.listing.AddressRepository;
import com.bokl.homerental.repository.listing.PropertyApplicationRepository;
import com.bokl.homerental.repository.listing.PropertyDetailRepository;
import com.bokl.homerental.repository.listing.TermsConsentRepository;
import com.bokl.homerental.repository.listing.TermsDefinitionRepository;
import com.bokl.homerental.repository.inspection.InspectionScheduleRepository;
import com.bokl.homerental.service.MessageService;
import com.bokl.homerental.entity.inspection.InspectionSchedule;
import com.bokl.homerental.security.SecurityUtils;
import com.bokl.homerental.util.JsonUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PropertyApplicationService {

    private final AddressRepository addressRepository;
    private final PropertyDetailRepository propertyDetailRepository;
    private final PropertyApplicationRepository propertyApplicationRepository;
    private final InspectionScheduleRepository scheduleRepository;
    private final GovernorateRepository governorateRepository;
    private final AreaRepository areaRepository;
    private final TermsDefinitionRepository termsDefinitionRepository;
    private final TermsConsentRepository termsConsentRepository;
    private final MessageService msg;

    public PropertyApplicationService(
            AddressRepository addressRepository,
            PropertyDetailRepository propertyDetailRepository,
            PropertyApplicationRepository propertyApplicationRepository,
            InspectionScheduleRepository scheduleRepository,
            GovernorateRepository governorateRepository,
            AreaRepository areaRepository,
            TermsDefinitionRepository termsDefinitionRepository,
            TermsConsentRepository termsConsentRepository,
            MessageService msg) {
        this.addressRepository = addressRepository;
        this.propertyDetailRepository = propertyDetailRepository;
        this.propertyApplicationRepository = propertyApplicationRepository;
        this.scheduleRepository = scheduleRepository;
        this.governorateRepository = governorateRepository;
        this.areaRepository = areaRepository;
        this.termsDefinitionRepository = termsDefinitionRepository;
        this.termsConsentRepository = termsConsentRepository;
        this.msg = msg;
    }

    public PropertyApplicationResponse createApplication(SubmitPropertyApplicationRequest request) {
        AuthUser owner = SecurityUtils.currentUser();

        PropertyDetailRequest detailRequest = request.getPropertyDetail();
        Address address = buildAddress(detailRequest.getAddress());
        addressRepository.save(address);

        Governorate governorate = governorateRepository.findById(detailRequest.getGovernorateId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("error.governorate.not_found", detailRequest.getGovernorateId())));
        Area area = areaRepository.findById(detailRequest.getAreaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("error.area.not_found", detailRequest.getAreaId())));

        PropertyDetail propertyDetail = new PropertyDetail();
        propertyDetail.setAddress(address);
        propertyDetail.setGovernorate(governorate);
        propertyDetail.setArea(area);
        propertyDetail.setRoomsCount(detailRequest.getRoomsCount());
        propertyDetail.setAreaSqm(detailRequest.getAreaSqm());
        propertyDetail.setFurnishing(PropertyDetail.Furnishing.valueOf(detailRequest.getFurnishing().toUpperCase()));
        propertyDetail.setExpectedRent(detailRequest.getExpectedRent());
        propertyDetail.setAmenities(JsonUtils.toJson(detailRequest.getAmenities()));
        propertyDetail.setPhotos(JsonUtils.toJson(detailRequest.getPhotos()));
        propertyDetail.setSourceType(PropertyDetail.SourceType.APPLICATION);
        propertyDetail.setSourceId(0L);
        propertyDetailRepository.save(propertyDetail);

        PropertyApplication application = new PropertyApplication();
        application.setUser(owner);
        application.setPropertyDetail(propertyDetail);
        application.setStatus(PropertyApplication.Status.SUBMITTED);
        propertyApplicationRepository.save(application);

        propertyDetail.setSourceId(application.getId());
        propertyDetailRepository.save(propertyDetail);

        return toResponse(application);
    }

    public List<InspectionWindowRequest> submitInspectionWindows(Long applicationId, List<InspectionWindowRequest> windows) {
        if (windows == null || windows.size() < 4 || windows.size() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                msg.get("listing.inspection.windows.count_invalid"));
        }

        PropertyApplication application = propertyApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("listing.application.not_found")));

        AuthUser currentUser = SecurityUtils.currentUser();
        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("listing.application.update.forbidden"));
        }
        if (application.getInspector() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("listing.inspection.windows.inspector_required"));
        }

        application.setStatus(PropertyApplication.Status.PENDING_INSPECTOR_SLOTS);
        propertyApplicationRepository.save(application);

        List<InspectionSchedule> schedules = new ArrayList<>();
        for (int index = 0; index < windows.size(); index++) {
            InspectionWindowRequest window = windows.get(index);
            if (window.getProposedStart().isAfter(window.getProposedEnd())
                    || window.getProposedStart().equals(window.getProposedEnd())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("listing.inspection.windows.invalid_range"));
            }
            InspectionSchedule schedule = new InspectionSchedule();
            schedule.setApplication(application);
            schedule.setProposedStart(window.getProposedStart());
            schedule.setProposedEnd(window.getProposedEnd());
            schedule.setSelectionOrder(index + 1);
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
        return windows;
    }

    public PropertyApplicationResponse submitTermsConsent(Long applicationId, TermsConsentRequest request) {
        PropertyApplication application = propertyApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("listing.application.not_found")));

        AuthUser currentUser = SecurityUtils.currentUser();
        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("listing.consent.forbidden"));
        }
        if (application.getStatus() != PropertyApplication.Status.PENDING_OWNER_CONSENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("listing.consent.status_invalid"));
        }

        TermsDefinition termsDefinition = termsDefinitionRepository.findById(request.getTermsDefinitionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        msg.get("listing.terms.not_found")));

        TermsConsent consent = new TermsConsent();
        consent.setApplication(application);
        consent.setUser(currentUser);
        consent.setTermsDefinition(termsDefinition);
        consent.setAgreedRent(request.getAgreedRent());
        consent.setCommissionPercentage(request.getCommissionPercentage());
        consent.setIpAddress(request.getIpAddress());
        consent.setUserAgent(request.getUserAgent());
        termsConsentRepository.save(consent);

        application.setStatus(PropertyApplication.Status.CONSENT_PROVIDED);
        propertyApplicationRepository.save(application);

        return toResponse(application);
    }

    /**
     * List all applications for the current owner.
     * Supports filtering by status and pagination.
     */
    @Transactional(readOnly = true)
    public Page<PropertyApplicationResponse> listApplicationsByOwner(String status, Pageable pageable) {
        AuthUser owner = SecurityUtils.currentUser();
        Page<PropertyApplication> page;

        if (status != null && !status.isEmpty()) {
            PropertyApplication.Status statusEnum = PropertyApplication.Status.valueOf(status.toUpperCase());
            page = propertyApplicationRepository.findByUserIdAndStatus(owner.getId(), statusEnum, pageable);
        } else {
            page = propertyApplicationRepository.findByUserId(owner.getId(), pageable);
        }

        List<PropertyApplicationResponse> responses = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    /**
     * Get application details for the current owner.
     * Ensures the owner can only access their own applications.
     */
    @Transactional(readOnly = true)
    public PropertyApplicationResponse getApplicationByIdForOwner(Long applicationId) {
        PropertyApplication application = propertyApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("listing.application.not_found")));

        AuthUser currentUser = SecurityUtils.currentUser();
        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("listing.application.view.forbidden"));
        }

        return toResponse(application);
    }

    /**
     * Get inspection schedules for an application.
     * Returns all proposed windows and any confirmed inspection time.
     */
    @Transactional(readOnly = true)
    public List<InspectionScheduleResponse> getInspectionSchedulesByApplication(Long applicationId) {
        PropertyApplication application = propertyApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("listing.application.not_found")));

        AuthUser currentUser = SecurityUtils.currentUser();
        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("listing.application.view.forbidden"));
        }

        List<InspectionSchedule> schedules = scheduleRepository.findByApplicationIdOrderBySelectionOrder(applicationId);
        return schedules.stream()
                .map(s -> new InspectionScheduleResponse(
                        s.getId(),
                        s.getApplication().getId(),
                        s.getProposedStart(),
                        s.getProposedEnd(),
                        s.getExactTime(),
                        s.getStatus() != null ? s.getStatus().name() : null,
                        s.getSelectionOrder(),
                        s.getConfirmedAt(),
                        s.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private Address buildAddress(AddressRequest request) {
        Address address = new Address();
        address.setStreetAddress(request.getStreetAddress());
        address.setBuildingNumber(request.getBuildingNumber());
        address.setApartmentNumber(request.getApartmentNumber());
        address.setLandmark(request.getLandmark());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setGooglePlaceId(request.getGooglePlaceId());
        return address;
    }

    private PropertyApplicationResponse toResponse(PropertyApplication application) {
        return new PropertyApplicationResponse(
                application.getId(),
                application.getStatus().name(),
                application.getSubmittedAt(),
                application.getUpdatedAt(),
                PropertyDetailResponse.from(application.getPropertyDetail())
        );
    }
}
