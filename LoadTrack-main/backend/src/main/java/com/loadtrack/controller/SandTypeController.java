package com.loadtrack.controller;

import com.loadtrack.dto.SandTypeRequest;
import com.loadtrack.dto.SandTypeResponse;
import com.loadtrack.service.SandTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sand-types")
@RequiredArgsConstructor
public class SandTypeController {

    private final SandTypeService sandTypeService;

    @GetMapping
    public List<SandTypeResponse> list() {
        return sandTypeService.list();
    }

    @GetMapping("/{id}")
    public SandTypeResponse get(@PathVariable Long id) {
        return sandTypeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SandTypeResponse create(@Valid @RequestBody SandTypeRequest req) {
        return sandTypeService.create(req);
    }

    @PutMapping("/{id}")
    public SandTypeResponse update(@PathVariable Long id, @Valid @RequestBody SandTypeRequest req) {
        return sandTypeService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sandTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
