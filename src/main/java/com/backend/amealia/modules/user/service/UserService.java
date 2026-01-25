package com.backend.amealia.modules.user.service;

import com.backend.amealia.exception.BusinessException;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.enums.OnboardingStep;
import com.backend.amealia.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.backend.amealia.constants.Constants.INVALID_EMAIL;
import static com.backend.amealia.constants.Constants.INVALID_EMAIL_PHONE;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(INVALID_EMAIL));
    }

    public User findByEmailAndPhoneNumber(String email, String phone) {
        return userRepository.findByEmailAndPhoneNumber(email, phone)
                .orElseThrow(() -> new BusinessException(INVALID_EMAIL_PHONE));
    }

    public void assertStep(User user, OnboardingStep expected) {
        if (user.getOnboardingStep() != expected) {
            log.info("Expected step is {}. The User's step is {}", expected.name(), user.getOnboardingStep().name());
            throw new BusinessException("Invalid onboarding step");
        }
    }

    public String generateUserCode() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', '9')   // only digits
                .build();
        String numericPart = generator.generate(7);
        return "A-" + numericPart;
    }
}
