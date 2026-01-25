package com.backend.amealia.modules.authentication.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
