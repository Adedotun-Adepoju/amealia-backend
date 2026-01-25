package com.backend.amealia.modules.onboarding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record VerifyEmailRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @NotEmpty
        String otp
) {
}
