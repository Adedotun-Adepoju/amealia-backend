package com.backend.amealia.modules.authentication.controller;

import com.backend.amealia.modules.authentication.dto.request.LoginRequest;
import com.backend.amealia.modules.authentication.dto.request.Verify2FARequest;
import com.backend.amealia.modules.authentication.dto.response.AuthResponse;
import com.backend.amealia.modules.authentication.dto.response.LoginResponse;
import com.backend.amealia.modules.authentication.service.AuthenticationService;
import com.backend.amealia.modules.onboarding.dto.OnboardingRequest;
import com.backend.amealia.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> createUser(@Valid @RequestBody LoginRequest loginRequest) {
        ApiResponse<LoginResponse> response = authenticationService.authenticate(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@Valid @RequestBody Verify2FARequest verify2FARequest) {
        ApiResponse<AuthResponse> response = authenticationService.verify2fa(verify2FARequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/resend-2fa")
    public ResponseEntity<?> resend2fa(@Valid @RequestBody OnboardingRequest onboardingRequest) {
        ApiResponse<Void> response = authenticationService.resend2faVerificationCode(onboardingRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
        ApiResponse<AuthResponse> response = authenticationService.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String refreshToken) {
        ApiResponse<AuthResponse> response = authenticationService.logout(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
