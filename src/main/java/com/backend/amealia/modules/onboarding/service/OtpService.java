package com.backend.amealia.modules.onboarding.service;

import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.entity.VerificationToken;
import com.backend.amealia.modules.user.enums.VerificationStatus;
import com.backend.amealia.modules.user.enums.VerificationType;
import com.backend.amealia.modules.user.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public String createNewToken(User user, VerificationType type) {
        verificationTokenRepository.deactivateTokens(user.getId(), type);

        VerificationToken emailVerificationToken = new VerificationToken();

        emailVerificationToken.setUser(user);
        emailVerificationToken.setToken(generateOtp());
        emailVerificationToken.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
        emailVerificationToken.setStatus(VerificationStatus.OPEN);
        emailVerificationToken.setActive(true);
        emailVerificationToken.setType(type);

        verificationTokenRepository.save(emailVerificationToken);

        return emailVerificationToken.getToken();
    }


    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit
        return String.valueOf(otp);
    }
}
