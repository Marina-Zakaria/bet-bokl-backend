package com.bokl.homerental.service.inspection;

import com.bokl.homerental.controller.dto.listing.ConfirmInspectionRequest;
import com.bokl.homerental.controller.dto.listing.InspectionReportRequest;
import com.bokl.homerental.controller.dto.listing.InspectionReportResponse;
import com.bokl.homerental.controller.dto.listing.InspectionScheduleResponse;
import com.bokl.homerental.controller.dto.listing.PropertyDetailRequest;
import com.bokl.homerental.controller.dto.listing.PropertyApplicationResponse;
import com.bokl.homerental.controller.dto.listing.PropertyDetailResponse;
import com.bokl.homerental.entity.AuthUser;
import com.bokl.homerental.entity.Governorate;
import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.listing.PropertyApplication;
import com.bokl.homerental.entity.listing.Address;
import com.bokl.homerental.entity.listing.PropertyDetail;
import com.bokl.homerental.entity.inspection.InspectionReport;
import com.bokl.homerental.entity.inspection.InspectionSchedule;
import com.bokl.homerental.repository.AreaRepository;
import com.bokl.homerental.repository.GovernorateRepository;
import com.bokl.homerental.repository.listing.AddressRepository;
import com.bokl.homerental.repository.listing.PropertyDetailRepository;
import com.bokl.homerental.repository.listing.PropertyApplicationRepository;
import com.bokl.homerental.repository.inspection.InspectionReportRepository;
import com.bokl.homerental.repository.inspection.InspectionScheduleRepository;
import com.bokl.homerental.service.MessageService;
import com.bokl.homerental.security.SecurityUtils;
import com.bokl.homerental.util.JsonUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class InspectionService {

    private final InspectionScheduleRepository scheduleRepository;
    private final InspectionReportRepository reportRepository;
    private final PropertyApplicationRepository applicationRepository;
    private final PropertyDetailRepository propertyDetailRepository;
    private final AddressRepository addressRepository;
    private final GovernorateRepository governorateRepository;
    private final AreaRepository areaRepository;
    private final MessageService msg;

    public InspectionService(
            InspectionScheduleRepository scheduleRepository,
            InspectionReportRepository reportRepository,
            PropertyApplicationRepository applicationRepository,
            PropertyDetailRepository propertyDetailRepository,
            AddressRepository addressRepository,
            GovernorateRepository governorateRepository,
            AreaRepository areaRepository,
            MessageService msg) {
        this.scheduleRepository = scheduleRepository;
        this.reportRepository = reportRepository;
        this.applicationRepository = applicationRepository;
        this.propertyDetailRepository = propertyDetailRepository;
        this.addressRepository = addressRepository;
        this.governorateRepository = governorateRepository;
        this.areaRepository = areaRepository;
        this.msg = msg;
    }

    public InspectionScheduleResponse confirmInspectionSlot(Long scheduleId, ConfirmInspectionRequest request) {
        InspectionSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("inspection.schedule.not_found")));

        PropertyApplication application = schedule.getApplication();
        AuthUser currentUser = SecurityUtils.currentUser();
        if (application.getInspector() == null || !application.getInspector().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("inspection.slot.confirm.forbidden"));
        }
        if (schedule.getStatus() != InspectionSchedule.Status.PROPOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("inspection.slot.confirm.only_proposed"));
        }

        if (request.getExactTime().isBefore(schedule.getProposedStart())
                || request.getExactTime().isAfter(schedule.getProposedEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("inspection.slot.confirm.time_out_of_range"));
        }

        schedule.setExactTime(request.getExactTime());
        schedule.setStatus(InspectionSchedule.Status.CONFIRMED);
        schedule.setConfirmedAt(java.time.Instant.now());
        scheduleRepository.save(schedule);

        application.setStatus(PropertyApplication.Status.INSPECTION_SCHEDULED);
        applicationRepository.save(application);

        return toScheduleResponse(schedule);
    }

    public InspectionReportResponse submitInspectionReport(Long scheduleId, InspectionReportRequest request) {
        InspectionSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("inspection.schedule.not_found")));

        PropertyApplication application = schedule.getApplication();
        AuthUser currentUser = SecurityUtils.currentUser();
        if (application.getInspector() == null || !application.getInspector().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    msg.get("inspection.report.submit.forbidden"));
        }
        if (schedule.getStatus() != InspectionSchedule.Status.CONFIRMED
                && schedule.getStatus() != InspectionSchedule.Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("inspection.report.submit.only_confirmed"));
        }

        PropertyDetailRequest detailRequest = request.getPropertyDetail();
        Address address = buildAddress(detailRequest.getAddress());
        addressRepository.save(address);

        Governorate governorate = governorateRepository.findById(detailRequest.getGovernorateId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("error.governorate.not_found", detailRequest.getGovernorateId())));
        Area area = areaRepository.findById(detailRequest.getAreaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("error.area.not_found", detailRequest.getAreaId())));

        PropertyDetail finalDetail = new PropertyDetail();
        finalDetail.setAddress(address);
        finalDetail.setGovernorate(governorate);
        finalDetail.setArea(area);
        finalDetail.setRoomsCount(detailRequest.getRoomsCount());
        finalDetail.setAreaSqm(detailRequest.getAreaSqm());
        finalDetail.setFurnishing(PropertyDetail.Furnishing.valueOf(detailRequest.getFurnishing().toUpperCase()));
        finalDetail.setExpectedRent(detailRequest.getExpectedRent());
        finalDetail.setAmenities(JsonUtils.toJson(detailRequest.getAmenities()));
        finalDetail.setPhotos(JsonUtils.toJson(detailRequest.getPhotos()));
        finalDetail.setSourceType(PropertyDetail.SourceType.REPORT);
        finalDetail.setSourceId(0L);
        propertyDetailRepository.save(finalDetail);

        InspectionReport report = new InspectionReport();
        report.setSchedule(schedule);
        report.setInspector(currentUser);
        report.setPropertyDetail(finalDetail);
        report.setRecommendation(InspectionReport.Recommendation.valueOf(request.getRecommendation().toUpperCase()));
        report.setAgreedRent(request.getAgreedRent());
        report.setReportData(JsonUtils.toJson(request.getReportData()));
        report.setEvidencePhotos(JsonUtils.toJson(request.getEvidencePhotos()));
        report.setComments(request.getComments());
        reportRepository.save(report);

        finalDetail.setSourceId(report.getId());
        propertyDetailRepository.save(finalDetail);

        schedule.setStatus(InspectionSchedule.Status.COMPLETED);
        scheduleRepository.save(schedule);

        application.setStatus(PropertyApplication.Status.INSPECTION_COMPLETED);
        applicationRepository.save(application);

        return new InspectionReportResponse(
                report.getId(),
                schedule.getId(),
                currentUser.getId(),
                PropertyDetailResponse.from(finalDetail),
                report.getRecommendation().name(),
                report.getAgreedRent(),
                report.getComments(),
                report.getCreatedAt()
        );
    }

    private Address buildAddress(com.bokl.homerental.controller.dto.listing.AddressRequest request) {
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

    public Page<PropertyApplicationResponse> listAssignedApplications(String status, Pageable pageable) {
        AuthUser inspector = SecurityUtils.currentUser();
        Page<PropertyApplication> page;
        if (status != null && !status.isBlank()) {
            PropertyApplication.Status statusEnum = PropertyApplication.Status.valueOf(status.toUpperCase());
            page = applicationRepository.findByInspectorIdAndStatus(inspector.getId(), statusEnum, pageable);
        } else {
            page = applicationRepository.findByInspectorId(inspector.getId(), pageable);
        }
        return page.map(this::toResponse);
    }

    public PropertyApplicationResponse getAssignedApplicationById(Long applicationId) {
        AuthUser inspector = SecurityUtils.currentUser();
        PropertyApplication application = applicationRepository.findByIdAndInspectorId(applicationId, inspector.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("inspection.application.not_found")));
        return toResponse(application);
    }

    public List<InspectionScheduleResponse> getSchedulesForApplication(Long applicationId) {
        AuthUser inspector = SecurityUtils.currentUser();
        applicationRepository.findByIdAndInspectorId(applicationId, inspector.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                msg.get("inspection.application.not_found")));
        return scheduleRepository.findByApplicationIdOrderBySelectionOrder(applicationId)
                .stream().map(this::toScheduleResponse).collect(java.util.stream.Collectors.toList());
    }

    private InspectionScheduleResponse toScheduleResponse(InspectionSchedule s) {
        return new InspectionScheduleResponse(
                s.getId(),
                s.getApplication().getId(),
                s.getProposedStart(),
                s.getProposedEnd(),
                s.getExactTime(),
                s.getStatus().name(),
                s.getSelectionOrder(),
                s.getConfirmedAt(),
                s.getCreatedAt()
        );
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
}
