package com.backend.amealia.modules.onboarding.service;

import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.entity.VerificationToken;
import com.backend.amealia.modules.user.enums.VerificationStatus;
import com.backend.amealia.modules.user.enums.VerificationType;
import com.backend.amealia.modules.user.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${spring.profiles.active}")
    private String springProfile;

    @Transactional
    public String createNewToken(User user, VerificationType type) {
        verificationTokenRepository.deactivateTokens(user.getId(), type);

        VerificationToken emailVerificationToken = new VerificationToken();

        if (VerificationType.PHONE_VERIFICATION.equals(type) && springProfile.equals("development")) {
            emailVerificationToken.setToken("123456");
        } else {
            emailVerificationToken.setToken(generateOtp());
        }

        emailVerificationToken.setUser(user);
        emailVerificationToken.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
        emailVerificationToken.setStatus(VerificationStatus.OPEN);
        emailVerificationToken.setActive(true);
        emailVerificationToken.setType(type);

        verificationTokenRepository.save(emailVerificationToken);

        return emailVerificationToken.getToken();
    }

    public void verifyToken(User user, VerificationType type, String token) {
        // get Open verifications
        VerificationToken verificationToken = verificationTokenRepository.findOneByUserAndTypeAndStatusAndActiveOrderByCreatedAtDesc(user, type, VerificationStatus.OPEN, true)
                .orElseThrow(() -> new BusinessException("Code is not valid, Please send another"));

        verificationToken.setAttempt(verificationToken.getAttempt() + 1);

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            verificationToken.setStatus(VerificationStatus.EXPIRED);
            verificationToken.setActive(false);
            verificationTokenRepository.save(verificationToken);
            throw new BusinessException("Code has expired");
        }

        if (!verificationToken.getToken().equals(token)) {
            int maxAttempts = 3;
            if (verificationToken.getAttempt() == maxAttempts) {
                verificationToken.setStatus(VerificationStatus.FAILED_ATTEMPTS_REACHED);
                verificationToken.setActive(false);
                verificationTokenRepository.save(verificationToken);
                throw new BusinessException("Invalid Code, You have reached the maximum number of attempt(s)");
            } else {
                verificationTokenRepository.save(verificationToken);
                throw new BusinessException(String.format("Invalid Code, You have %d more attempts", maxAttempts - verificationToken.getAttempt()));
            }
        }

        verificationToken.setStatus(VerificationStatus.USED);
        verificationToken.setActive(false);
        verificationTokenRepository.save(verificationToken);
    }


    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit
        return String.valueOf(otp);
    }
}
