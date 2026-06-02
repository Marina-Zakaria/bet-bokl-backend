package com.bokl.homerental.service;

import com.bokl.homerental.controller.dto.AreaDto;
import com.bokl.homerental.controller.dto.GovernorateDto;
import com.bokl.homerental.entity.Area;
import com.bokl.homerental.entity.Governorate;
import com.bokl.homerental.repository.AreaRepository;
import com.bokl.homerental.repository.GovernorateRepository;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
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
        Locale locale = LocaleContextHolder.getLocale();
        boolean useArabic = "ar".equalsIgnoreCase(locale.getLanguage());

        return governorateRepository.findAllByOrderByNameEn().stream()
                .map(gov -> toGovernorateDto(gov, useArabic))
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

        Locale locale = LocaleContextHolder.getLocale();
        boolean useArabic = "ar".equalsIgnoreCase(locale.getLanguage());

        return areaRepository.findAllByGovernorateIdOrderByNameEn(governorateId).stream()
                .map(area -> toAreaDto(area, useArabic))
                .collect(Collectors.toList());
    }

    private GovernorateDto toGovernorateDto(Governorate governorate, boolean useArabic) {
        String name = useArabic ? governorate.getNameAr() : governorate.getNameEn();
        return new GovernorateDto(governorate.getId(), name);
    }

    private AreaDto toAreaDto(Area area, boolean useArabic) {
        String name = useArabic ? area.getNameAr() : area.getNameEn();
        return new AreaDto(area.getId(), name);
    }
}
