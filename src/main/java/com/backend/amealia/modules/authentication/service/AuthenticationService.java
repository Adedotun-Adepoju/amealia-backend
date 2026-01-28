package com.backend.amealia.modules.authentication.service;

import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.authentication.dto.request.AuthenticationRequest;
import com.backend.amealia.modules.authentication.dto.request.LoginRequest;
import com.backend.amealia.modules.authentication.dto.request.PasswordResetRequest;
import com.backend.amealia.modules.authentication.dto.request.Verify2FARequest;
import com.backend.amealia.modules.authentication.dto.response.AuthResponse;
import com.backend.amealia.modules.authentication.dto.response.LoginResponse;
import com.backend.amealia.modules.authentication.dto.response.PasswordResetResponse;
import com.backend.amealia.modules.email.service.EmailService;
import com.backend.amealia.modules.onboarding.dto.OnboardingRequest;
import com.backend.amealia.modules.onboarding.service.OtpService;
import com.backend.amealia.modules.user.dto.RefreshTokenDTO;
import com.backend.amealia.modules.user.entity.RefreshToken;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.enums.VerificationType;
import com.backend.amealia.modules.user.repository.RefreshTokenRepository;
import com.backend.amealia.modules.user.repository.UserRepository;
import com.backend.amealia.modules.user.service.UserService;
import com.backend.amealia.util.ApiResponse;
import com.backend.amealia.util.HelperUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

import static com.backend.amealia.constants.Constants.*;
import static com.backend.amealia.modules.user.enums.OnboardingStep.SIGNED_UP;
import static com.backend.amealia.util.HelperUtil.sha256;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Value("${app.security.jwt.refresh.token.expiry}")
    private long refreshTokenExpiry;

    public ApiResponse<LoginResponse> authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        LoginResponse loginResponse = new LoginResponse(user.getUserCode(), user.isEmailVerified(), user.isPhoneVerified(), user.getUserStatus(), user.getOnboardingStep());

        if (user.isEmailVerified() && user.isPhoneVerified()) {
            String code = otpService.createNewToken(user, VerificationType.TWO_FA_VERIFICATION);
            // Send 2fa email verification async.
            emailService.send2faEmail(loginRequest.email(), code, "Ade");

            return ApiResponse.success(EMAIL_VERIFICATION_SENT, loginResponse);
        }


        return ApiResponse.success(USER_VERIFICATION_NEEDED, loginResponse);
    }

    public ApiResponse<Void> resend2faVerificationCode(AuthenticationRequest authenticationRequest) {
        User user = userService.findByEmail(authenticationRequest.email());

        String token = otpService.createNewToken(user, VerificationType.TWO_FA_VERIFICATION);
        // Send email verification async.
        emailService.send2faEmail(authenticationRequest.email(), token, "Ade");

        return ApiResponse.success(EMAIL_VERIFICATION_SENT);
    }

    public ApiResponse<AuthResponse> verify2fa(Verify2FARequest verify2FARequest) {
        User user = userService.findByEmail(verify2FARequest.email());

        otpService.verifyToken(user, VerificationType.TWO_FA_VERIFICATION, verify2FARequest.code());

        String accessToken = jwtService.generateAccessToken(user);

        RefreshTokenDTO refreshTokenDTO = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshTokenDTO.refreshToken(), refreshTokenDTO.tokenId());

        AuthResponse authResponse = new AuthResponse(accessToken, refreshTokenDTO.refreshToken());

        return ApiResponse.success(authResponse);
    }

    public ApiResponse<?> initiatePasswordReset(AuthenticationRequest authenticationRequest) {
        User user = userService.findByEmail(authenticationRequest.email());

        String code = otpService.createNewToken(user, VerificationType.PASSWORD_UPDATE);

        // Send 2fa email verification async.
        emailService.sendPasswordResetEmail(authenticationRequest.email(), code, "Ade");

        return ApiResponse.success(EMAIL_VERIFICATION_SENT);
    }

    public ApiResponse<PasswordResetResponse> validatePasswordResetOtp(Verify2FARequest verify2FARequest) {
        User user = userService.findByEmail(verify2FARequest.email());

        otpService.verifyToken(user, VerificationType.PASSWORD_UPDATE, verify2FARequest.code());

        String passwordRecoveryToken = otpService.createNewToken(user, VerificationType.PASSWORD_RECOVERY);
        PasswordResetResponse passwordResetResponse = new PasswordResetResponse(passwordRecoveryToken);

        return ApiResponse.success(passwordResetResponse);
    }

    public ApiResponse<?> effectPasswordReset(PasswordResetRequest passwordResetRequest) {
        if (!passwordResetRequest.password().equals(passwordResetRequest.confirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        User user = userService.findByEmail(passwordResetRequest.email());

        otpService.verifyToken(user, VerificationType.PASSWORD_RECOVERY, passwordResetRequest.recoveryToken());

        user.setPassword(passwordEncoder.encode(passwordResetRequest.password()));
        userRepository.save(user);

        return ApiResponse.success();
    }

    public ApiResponse<AuthResponse> refreshAccessToken(String token) {
        Claims claims = jwtService.parseToken(token);

        String tokenId = claims.getId();

        if (HelperUtil.isBlank(tokenId)) {
            log.info("Refresh token without JTI used");
            throw new BusinessException(INVALID_TOKEN);
        }
        RefreshToken refreshToken = refreshTokenRepository.findByJti(tokenId)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN));

        if (refreshToken.isRevoked()) {
            throw new BusinessException(INVALID_TOKEN);
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Token expired");
        }

        String hashedToken = sha256(token);

        if (!hashedToken.equals(refreshToken.getHashedToken())) {
            throw new BusinessException(INVALID_TOKEN);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        AuthResponse authResponse = new AuthResponse(newAccessToken, token);
        return ApiResponse.success(authResponse);
    }

    public ApiResponse<AuthResponse> logout(String refreshToken) {
        Claims claims = jwtService.parseToken(refreshToken);

        String tokenId = claims.getId();
        RefreshToken token = refreshTokenRepository.findByJti(tokenId)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN));

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return ApiResponse.success(LOGGED_OUT_SUCCESSFULLY);
    }

    private void saveRefreshToken(User user, String token, String tokenId) {
        String hashedToken = sha256(token);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setHashedToken(hashedToken);
        refreshToken.setJti(tokenId);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpiry, ChronoUnit.SECONDS));

        refreshTokenRepository.save(refreshToken);
    }
}
