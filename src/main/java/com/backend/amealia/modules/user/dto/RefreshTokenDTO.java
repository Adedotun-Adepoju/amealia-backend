package com.backend.amealia.modules.user.dto;

public record RefreshTokenDTO(
        String refreshToken,
        String tokenId
) {
}
