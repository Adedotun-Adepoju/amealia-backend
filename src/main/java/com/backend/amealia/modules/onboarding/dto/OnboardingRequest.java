package com.backend.amealia.modules.onboarding.dto;

import com.backend.amealia.constants.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OnboardingRequest(
        @Email
        @NotBlank
        String email
) {
}
