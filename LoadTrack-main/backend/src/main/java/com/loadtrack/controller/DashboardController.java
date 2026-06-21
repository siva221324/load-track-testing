package com.loadtrack.controller;

import com.loadtrack.dto.AdminDashboardResponse;
import com.loadtrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public AdminDashboardResponse adminSummary() {
        return dashboardService.adminSummary();
    }
}
