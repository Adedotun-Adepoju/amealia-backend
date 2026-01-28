package com.backend.amealia.modules.authentication.dto.response;

public record PasswordResetResponse(
        String recoveryToken
) {
}
