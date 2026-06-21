package com.loadtrack.controller;

import com.loadtrack.dto.ChangePasswordRequest;
import com.loadtrack.service.AccountService;
import com.loadtrack.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public Map<String, Object> me() {
        var user = currentUserService.getCurrentUser();
        Map<String, Object> body = new HashMap<>();
        body.put("userId", user.getId());
        body.put("username", user.getUsername());
        body.put("role", user.getRole().getName());
        if (user.getLinkedDriver() != null) {
            body.put("linkedToType", "driver");
            body.put("linkedToId", user.getLinkedDriver().getId());
            body.put("linkedToName", user.getLinkedDriver().getName());
        } else if (user.getLinkedDealer() != null) {
            body.put("linkedToType", "dealer");
            body.put("linkedToId", user.getLinkedDealer().getId());
            body.put("linkedToName", user.getLinkedDealer().getName());
        }
        return body;
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        accountService.changePassword(req.getCurrentPassword(), req.getNewPassword());
        return Map.of("message", "Password changed successfully");
    }
}
