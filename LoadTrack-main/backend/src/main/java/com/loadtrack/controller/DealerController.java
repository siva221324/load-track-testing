package com.loadtrack.controller;

import com.loadtrack.dto.CreateLoginRequest;
import com.loadtrack.dto.DealerRequest;
import com.loadtrack.dto.DealerResponse;
import com.loadtrack.dto.LoginInfoResponse;
import com.loadtrack.service.DealerService;
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
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;
    private final UserManagementService userManagementService;

    @GetMapping
    public Page<DealerResponse> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return dealerService.list(search, pageable);
    }

    @GetMapping("/{id}")
    public DealerResponse get(@PathVariable Long id) {
        return dealerService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DealerResponse create(@Valid @RequestBody DealerRequest req) {
        return dealerService.create(req);
    }

    @PutMapping("/{id}")
    public DealerResponse update(@PathVariable Long id, @Valid @RequestBody DealerRequest req) {
        return dealerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dealerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/login")
    public ResponseEntity<LoginInfoResponse> getLogin(@PathVariable Long id) {
        LoginInfoResponse info = userManagementService.getLoginForDealer(id);
        return info == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(info);
    }

    @PostMapping("/{id}/login")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginInfoResponse createLogin(@PathVariable Long id, @Valid @RequestBody CreateLoginRequest req) {
        return userManagementService.createLoginForDealer(id, req);
    }
}
