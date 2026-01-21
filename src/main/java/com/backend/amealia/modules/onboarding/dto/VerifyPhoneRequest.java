package com.backend.amealia.modules.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record VerifyPhoneRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @NotEmpty
        String phone,

        @NotBlank
        @NotEmpty
        String otp
) {
}
