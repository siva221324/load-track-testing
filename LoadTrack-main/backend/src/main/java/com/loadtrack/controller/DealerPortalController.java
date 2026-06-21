package com.loadtrack.controller;

import com.loadtrack.dto.DealerStatsResponse;
import com.loadtrack.dto.PaymentResponse;
import com.loadtrack.service.DealerPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/dealer")
@RequiredArgsConstructor
public class DealerPortalController {

    private final DealerPortalService service;

    @GetMapping("/payments")
    public Page<PaymentResponse> myPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean overdueOnly,
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return service.myPayments(status, overdueOnly, pageable);
    }

    @GetMapping("/stats")
    public DealerStatsResponse myStats() {
        return service.myStats();
    }
}
