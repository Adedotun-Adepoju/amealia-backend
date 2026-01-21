package com.backend.amealia.modules.email.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async("emailExecutor")
    public void sendVerificationEmail(String to, String token, String name) {
        try {
            log.info("Sending verification mail to {}", to);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariable("verificationCode", token);
            context.setVariable("name", name);
            context.setVariable("expiryMinutes", 5);

            String html = templateEngine.process(
                    "email/verification-email",
                    context
            );

            helper.setFrom("amealia@amealia.com");
            helper.setTo(to);
            helper.setSubject("Verify Your Email");
            helper.setText(html, true);
            javaMailSender.send(message);
            log.info("Mail successfully sent to {}", to);
        } catch (Exception e) {
            log.info("An exception occurred while attempting to send verification email {}", e.getMessage(), e);
        }
    }
}
