package com.backend.amealia.modules.onboarding.service;

import com.backend.amealia.constants.Constants;
import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.onboarding.dto.SignupRequest;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.enums.OnboardingStep;
import com.backend.amealia.modules.user.enums.UserStatus;
import com.backend.amealia.modules.user.repository.UserRepository;
import com.backend.amealia.modules.user.repository.VerificationTokenRepository;
import com.backend.amealia.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.amealia.constants.Constants.USER_CREATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse<Void> signup(SignupRequest signupRequest) {
        if (!signupRequest.password().equals(signupRequest.confirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        if (userRepository.existsByEmail(signupRequest.email())) {
            throw new BusinessException(Constants.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setEmail(signupRequest.email());
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setOnboardingStep(OnboardingStep.SIGNED_UP);
        user.setUserStatus(UserStatus.PENDING);
        user.setPassword(passwordEncoder.encode(signupRequest.password()));

        userRepository.save(user);

        //to-do: Send email verification

        return ApiResponse.success(USER_CREATED);
    }
}
