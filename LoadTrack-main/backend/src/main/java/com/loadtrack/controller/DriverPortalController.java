package com.loadtrack.controller;

import com.loadtrack.dto.DriverStatsResponse;
import com.loadtrack.dto.TripResponse;
import com.loadtrack.service.DriverPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/driver")
@RequiredArgsConstructor
public class DriverPortalController {

    private final DriverPortalService service;

    @GetMapping("/trips")
    public Page<TripResponse> myTrips(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "tripDate", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return service.myTrips(status, pageable);
    }

    @GetMapping("/stats")
    public DriverStatsResponse myStats() {
        return service.myStats();
    }
}
