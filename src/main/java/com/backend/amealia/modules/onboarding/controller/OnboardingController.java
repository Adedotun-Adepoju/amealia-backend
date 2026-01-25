package com.backend.amealia.modules.onboarding.controller;

import com.backend.amealia.modules.onboarding.dto.*;
import com.backend.amealia.modules.onboarding.service.OnboardingService;
import com.backend.amealia.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/email/resend")
    public ResponseEntity<?> resendEmailVerificationCode(@Valid @RequestBody OnboardingRequest onboardingRequest) {
        ApiResponse<Void> response = onboardingService.resendEmailVerificationCode(onboardingRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyEmailRequest) {
        ApiResponse<Void> response = onboardingService.verifyEmail(verifyEmailRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/phone/send")
    public ResponseEntity<?> sendPhoneVerificationCode(@Valid @RequestBody InitiateVerifyPhoneRequest verifyPhoneRequest) {
        ApiResponse<Void> response = onboardingService.sendPhoneVerificationCode(verifyPhoneRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<?> verifyPhone(@Valid @RequestBody VerifyPhoneRequest verifyPhoneRequest) {
        ApiResponse<Void> response = onboardingService.verifyPhone(verifyPhoneRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
