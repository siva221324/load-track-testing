package com.loadtrack.controller;

import com.loadtrack.dto.SettingsRequest;
import com.loadtrack.dto.SettingsResponse;
import com.loadtrack.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public SettingsResponse getCurrent() {
        return settingsService.getCurrent();
    }

    @PutMapping
    public SettingsResponse update(@Valid @RequestBody SettingsRequest req) {
        return settingsService.upsert(req);
    }
}
