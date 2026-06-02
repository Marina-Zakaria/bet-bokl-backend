package com.bokl.homerental.controller;

import com.bokl.homerental.controller.dto.AreaDto;
import com.bokl.homerental.controller.dto.GovernorateDto;
import com.bokl.homerental.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Returns all governorates in the language specified by Accept-Language header.
     * Defaults to English if header is absent or unsupported.
     */
    @GetMapping("/governorates")
    public List<GovernorateDto> getGovernorates() {
        return locationService.getGovernorates();
    }

    /**
     * Returns all areas for a given governorate in the language specified by Accept-Language header.
     * Defaults to English if header is absent or unsupported.
     *
     * @param governorateId the governorate ID
     * @return list of areas sorted by name
     */
    @GetMapping("/governorates/{governorateId}/areas")
    public List<AreaDto> getAreas(@PathVariable Integer governorateId) {
        return locationService.getAreas(governorateId);
    }
}
