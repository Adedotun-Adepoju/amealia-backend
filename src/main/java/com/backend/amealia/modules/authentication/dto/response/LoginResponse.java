package com.backend.amealia.modules.authentication.dto.response;

import com.backend.amealia.modules.user.enums.OnboardingStep;
import com.backend.amealia.modules.user.enums.UserStatus;

public record LoginResponse(
        String userCode,
        Boolean emailVerified,
        Boolean phoneVerified,
        UserStatus userStatus,
        OnboardingStep onboardingStep
) {
}
