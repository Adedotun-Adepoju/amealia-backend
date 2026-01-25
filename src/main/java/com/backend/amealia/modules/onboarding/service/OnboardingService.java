package com.backend.amealia.modules.onboarding.service;

import com.backend.amealia.constants.Constants;
import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.email.service.EmailService;
import com.backend.amealia.modules.onboarding.dto.*;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.enums.OnboardingStep;
import com.backend.amealia.modules.user.enums.UserStatus;
import com.backend.amealia.modules.user.enums.VerificationType;
import com.backend.amealia.modules.user.repository.UserRepository;
import com.backend.amealia.modules.user.repository.VerificationTokenRepository;
import com.backend.amealia.modules.user.service.UserService;
import com.backend.amealia.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.backend.amealia.constants.Constants.*;
import static com.backend.amealia.modules.user.enums.OnboardingStep.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {
    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpService otpService;

    private final PasswordEncoder passwordEncoder;

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

        String token = otpService.createNewToken(user, VerificationType.EMAIL_VERIFICATION);
        // Send email verification async.
        emailService.sendVerificationEmail(signupRequest.email(),token, "Ade");

        return ApiResponse.success(USER_CREATED);
    }

    public ApiResponse<Void> resendEmailVerificationCode(OnboardingRequest onboardingRequest) {
        User user = userService.findByEmail(onboardingRequest.email());

        if (user.isEmailVerified()) {
            throw new BusinessException("Email already verified");
        }

        userService.assertStep(user, SIGNED_UP);

        String token = otpService.createNewToken(user, VerificationType.EMAIL_VERIFICATION);
        // Send email verification async.
        emailService.sendVerificationEmail(onboardingRequest.email(), token, "Ade");

        return ApiResponse.success(VERIFICATION_SENT);
    }

    public ApiResponse<Void> verifyEmail(VerifyEmailRequest verifyEmailRequest) {
        User user = userService.findByEmail(verifyEmailRequest.email());

        if (user.isEmailVerified()) {
            throw new BusinessException("Email already verified");
        }

        userService.assertStep(user, SIGNED_UP);

        otpService.verifyToken(user, VerificationType.EMAIL_VERIFICATION, verifyEmailRequest.otp());
        user.setEmailVerified(true);
        user.setOnboardingStep(EMAIL_VERIFIED);
        userRepository.save(user);

        return ApiResponse.success(EMAIL_VERIFIED_SUCCESS);
    }

    public ApiResponse<Void> sendPhoneVerificationCode(InitiateVerifyPhoneRequest verifyPhoneRequest) {
        User user = userService.findByEmail(verifyPhoneRequest.email());

        if (user.isPhoneVerified()) {
            throw new BusinessException("Phone number already verified");
        }

        userService.assertStep(user, EMAIL_VERIFIED);

        String token = otpService.createNewToken(user, VerificationType.PHONE_VERIFICATION);
        user.setPhoneNumber(verifyPhoneRequest.phone());
        user.setOnboardingStep(PHONE_PROVIDED);
        userRepository.save(user);

        // to-do: Send phone verification token.

        return ApiResponse.success(VERIFICATION_SENT);
    }

    public ApiResponse<Void> verifyPhone(VerifyPhoneRequest verifyPhoneRequest) {
        User user = userService.findByEmailAndPhoneNumber(verifyPhoneRequest.email(), verifyPhoneRequest.phone());

        if (user.isPhoneVerified()) {
            throw new BusinessException("Email already verified");
        }

        userService.assertStep(user, PHONE_PROVIDED);

        otpService.verifyToken(user, VerificationType.PHONE_VERIFICATION, verifyPhoneRequest.otp());
        user.setPhoneVerified(true);
        user.setOnboardingStep(PHONE_VERIFIED);
        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return ApiResponse.success(PHONE_VERIFIED_SUCCESS);
    }

}
