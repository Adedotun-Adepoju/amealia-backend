package com.backend.amealia.modules.onboarding.dto;

import com.backend.amealia.constants.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Pattern(
                regexp = PasswordPolicy.REGEX,
                message = PasswordPolicy.MESSAGE
        )
        String password,

        @NotBlank
        String confirmPassword
) {
}
