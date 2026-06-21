package com.loadtrack.controller;

import com.loadtrack.dto.ForgotPasswordRequest;
import com.loadtrack.dto.LoginInfoResponse;
import com.loadtrack.dto.LoginRequest;
import com.loadtrack.dto.LoginResponse;
import com.loadtrack.dto.SignupRequest;
import com.loadtrack.entity.User;
import com.loadtrack.repository.UserRepository;
import com.loadtrack.security.JwtUtil;
import com.loadtrack.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserManagementService userManagementService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().getName(),
                user.getId()
        );

        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().getName())
                .userId(user.getId())
                .build());
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginInfoResponse signup(@Valid @RequestBody SignupRequest request) {
        return userManagementService.signupAdmin(request.getUsername(), request.getPassword());
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String tempPassword = userManagementService.resetPasswordToDefault(request.getUsername());
        return Map.of(
            "message", "Password reset successfully. Use the temporary password below to log in, " +
                       "then change it from Account Settings.",
            "temporaryPassword", tempPassword
        );
    }
}
