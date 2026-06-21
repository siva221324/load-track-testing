package com.loadtrack.controller;

import com.loadtrack.dto.ApproveTripRequestRequest;
import com.loadtrack.dto.CreateTripRequestRequest;
import com.loadtrack.dto.RejectTripRequestRequest;
import com.loadtrack.dto.TripRequestResponse;
import com.loadtrack.service.TripRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TripRequestController {

    private final TripRequestService tripRequestService;

    // ============================================================
    // DEALER endpoints
    // ============================================================
    @PostMapping("/api/me/dealer/trip-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public TripRequestResponse createByDealer(@Valid @RequestBody CreateTripRequestRequest req) {
        return tripRequestService.createByDealer(req);
    }

    @GetMapping("/api/me/dealer/trip-requests")
    public Page<TripRequestResponse> listForDealer(
            @PageableDefault(size = 50, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return tripRequestService.listForDealer(pageable);
    }

    @PostMapping("/api/me/dealer/trip-requests/{id}/cancel")
    public ResponseEntity<Void> cancelByDealer(@PathVariable Long id) {
        tripRequestService.cancelByDealer(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ADMIN endpoints
    // ============================================================
    @GetMapping("/api/trip-requests")
    public Page<TripRequestResponse> listForAdmin(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 50, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return tripRequestService.listForAdmin(status, pageable);
    }

    @GetMapping("/api/trip-requests/pending-count")
    public Map<String, Long> pendingCount() {
        return Map.of("count", tripRequestService.countPendingForAdmin());
    }

    @PostMapping("/api/trip-requests/{id}/approve")
    public TripRequestResponse approve(@PathVariable Long id,
                                       @Valid @RequestBody ApproveTripRequestRequest req) {
        return tripRequestService.approve(id, req);
    }

    @PostMapping("/api/trip-requests/{id}/reject")
    public TripRequestResponse reject(@PathVariable Long id,
                                      @Valid @RequestBody RejectTripRequestRequest req) {
        return tripRequestService.reject(id, req);
    }
}
