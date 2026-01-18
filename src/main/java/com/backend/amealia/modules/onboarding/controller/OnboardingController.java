package com.backend.amealia.modules.onboarding.controller;

import com.backend.amealia.modules.onboarding.dto.SignupRequest;
import com.backend.amealia.modules.onboarding.service.OnboardingService;
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
@RequestMapping("api/v1/onboarding/")
@RequiredArgsConstructor
public class OnboardingController {
    private final OnboardingService onboardingService;

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest signupRequest) {
        ApiResponse<Void> response = onboardingService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
