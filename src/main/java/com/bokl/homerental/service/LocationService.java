package com.bokl.homerental.service;

import com.bokl.homerental.controller.dto.AreaDto;
import com.bokl.homerental.controller.dto.GovernorateDto;
import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.Governorate;
import com.bokl.homerental.repository.AreaRepository;
import com.bokl.homerental.repository.GovernorateRepository;
import com.bokl.homerental.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final GovernorateRepository governorateRepository;
    private final AreaRepository areaRepository;
    private final MessageService msg;

    public LocationService(
            GovernorateRepository governorateRepository,
            AreaRepository areaRepository,
            MessageService msg) {
        this.governorateRepository = governorateRepository;
        this.areaRepository = areaRepository;
        this.msg = msg;
    }

    @Transactional(readOnly = true)
    public List<GovernorateDto> getGovernorates() {
        return governorateRepository.findAllByOrderByNameEn().stream()
                .map(this::toGovernorateDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaDto> getAreas(Integer governorateId) {
        if (!governorateRepository.existsById(governorateId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    msg.get("error.governorate.not_found", governorateId)
            );
        }
        return areaRepository.findAllByGovernorateIdOrderByNameEn(governorateId).stream()
                .map(this::toAreaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GovernorateDto> searchGovernorates(String q) {
        if (q == null || q.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query q is required");
        }
        return governorateRepository.searchByText(q.trim()).stream()
                .map(this::toGovernorateDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AreaDto> searchAreas(String q) {
        if (q == null || q.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query q is required");
        }
        return areaRepository.searchByText(q.trim()).stream()
                .map(this::toAreaDto)
                .collect(Collectors.toList());
    }

    private GovernorateDto toGovernorateDto(Governorate governorate) {
        return new GovernorateDto(governorate.getId(), governorate.getNameAr(), governorate.getNameEn());
    }

    private AreaDto toAreaDto(Area area) {
        return new AreaDto(
                area.getId(),
                area.getGovernorate().getId(),
                area.getNameAr(),
                area.getNameEn());
    }
}
