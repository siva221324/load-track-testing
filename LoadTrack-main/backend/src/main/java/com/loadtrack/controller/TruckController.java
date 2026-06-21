package com.loadtrack.controller;

import com.loadtrack.dto.TruckRequest;
import com.loadtrack.dto.TruckResponse;
import com.loadtrack.service.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;

    @GetMapping
    public Page<TruckResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return truckService.list(status, search, pageable);
    }

    @GetMapping("/{id}")
    public TruckResponse get(@PathVariable Long id) {
        return truckService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TruckResponse create(@Valid @RequestBody TruckRequest req) {
        return truckService.create(req);
    }

    @PutMapping("/{id}")
    public TruckResponse update(@PathVariable Long id, @Valid @RequestBody TruckRequest req) {
        return truckService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        truckService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
