package com.backend.amealia.modules.authentication.service;

import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.authentication.dto.request.LoginRequest;
import com.backend.amealia.modules.authentication.dto.request.Verify2FARequest;
import com.backend.amealia.modules.authentication.dto.response.AuthResponse;
import com.backend.amealia.modules.email.service.EmailService;
import com.backend.amealia.modules.onboarding.service.OtpService;
import com.backend.amealia.modules.user.dto.RefreshTokenDTO;
import com.backend.amealia.modules.user.entity.RefreshToken;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.enums.VerificationType;
import com.backend.amealia.modules.user.repository.RefreshTokenRepository;
import com.backend.amealia.modules.user.repository.UserRepository;
import com.backend.amealia.modules.user.service.UserService;
import com.backend.amealia.util.ApiResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.backend.amealia.constants.Constants.*;

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

    public ApiResponse<Void> authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        String code = otpService.createNewToken(user, VerificationType.TWO_FA_VERIFICATION);
        // Send email verification async.
        emailService.send2faEmail(loginRequest.email(), code, "Ade");

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

    public ApiResponse<AuthResponse> refreshAccessToken(String token) {
        Claims claims = jwtService.parseToken(token);

        String tokenId = claims.getId();
        RefreshToken refreshToken = refreshTokenRepository.findByJti(tokenId)
                .orElseThrow(() -> new BusinessException(INVALID_TOKEN));

        if (refreshToken.isRevoked()) {
            throw new BusinessException(INVALID_TOKEN);
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Token expired");
        }

        if (!passwordEncoder.matches(token, refreshToken.getHashedToken())) {
            throw new BusinessException(INVALID_TOKEN);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        AuthResponse authResponse = new AuthResponse(newAccessToken, token);
        return ApiResponse.success(authResponse);
    }

    private void saveRefreshToken(User user, String token, String tokenId) {
        String hashedToken = passwordEncoder.encode(token);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setHashedToken(hashedToken);
        refreshToken.setJti(tokenId);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpiry, ChronoUnit.SECONDS));

        refreshTokenRepository.save(refreshToken);
    }
}
