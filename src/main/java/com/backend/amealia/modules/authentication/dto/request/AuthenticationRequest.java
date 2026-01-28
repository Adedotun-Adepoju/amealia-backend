package com.backend.amealia.modules.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @Email
        @NotBlank
        String email
) {
}
