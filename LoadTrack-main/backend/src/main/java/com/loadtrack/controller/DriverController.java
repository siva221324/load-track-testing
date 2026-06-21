package com.loadtrack.controller;

import com.loadtrack.dto.CreateLoginRequest;
import com.loadtrack.dto.DriverRequest;
import com.loadtrack.dto.DriverResponse;
import com.loadtrack.dto.LoginInfoResponse;
import com.loadtrack.service.DriverService;
import com.loadtrack.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final UserManagementService userManagementService;

    @GetMapping
    public Page<DriverResponse> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return driverService.list(search, pageable);
    }

    @GetMapping("/{id}")
    public DriverResponse get(@PathVariable Long id) {
        return driverService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse create(@Valid @RequestBody DriverRequest req) {
        return driverService.create(req);
    }

    @PutMapping("/{id}")
    public DriverResponse update(@PathVariable Long id, @Valid @RequestBody DriverRequest req) {
        return driverService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/login")
    public ResponseEntity<LoginInfoResponse> getLogin(@PathVariable Long id) {
        LoginInfoResponse info = userManagementService.getLoginForDriver(id);
        return info == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(info);
    }

    @PostMapping("/{id}/login")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginInfoResponse createLogin(@PathVariable Long id, @Valid @RequestBody CreateLoginRequest req) {
        return userManagementService.createLoginForDriver(id, req);
    }
}
