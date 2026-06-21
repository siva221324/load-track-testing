package com.loadtrack.controller;

import com.loadtrack.dto.TripRequest;
import com.loadtrack.dto.TripResponse;
import com.loadtrack.dto.TripStatusRequest;
import com.loadtrack.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public Page<TripResponse> list(
            @RequestParam(required = false) Long truckId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return tripService.list(truckId, driverId, dealerId, status, from, to, pageable);
    }

    @GetMapping("/{id}")
    public TripResponse get(@PathVariable Long id) {
        return tripService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse create(@Valid @RequestBody TripRequest req) {
        return tripService.create(req);
    }

    @PutMapping("/{id}")
    public TripResponse update(@PathVariable Long id, @Valid @RequestBody TripRequest req) {
        return tripService.update(id, req);
    }

    @PutMapping("/{id}/status")
    public TripResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TripStatusRequest req) {
        return tripService.changeStatus(id, req.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
