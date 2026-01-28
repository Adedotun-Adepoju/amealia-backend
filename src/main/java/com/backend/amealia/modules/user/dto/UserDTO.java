package com.backend.amealia.modules.user.dto;

import com.backend.amealia.modules.user.enums.OnboardingStep;
import com.backend.amealia.modules.user.enums.UserStatus;

public record UserDTO(
        String email,
        String name,
        String phone,
        UserStatus userStatus,
        OnboardingStep onboardingStep,
        Boolean emailVerified,
        Boolean phoneVerified,
        String userCode
) {
}
