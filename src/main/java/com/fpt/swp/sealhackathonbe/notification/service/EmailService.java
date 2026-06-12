package com.fpt.swp.sealhackathonbe.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Async
    public void sendEmail(String recipient, String subject, String content) {
        if (!mailEnabled) {
            return;
        }
        if (recipient == null || recipient.isBlank()) {
            log.warn("Notification email skipped because recipient email is empty");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (senderEmail != null && !senderEmail.isBlank()) {
            message.setFrom(senderEmail);
        }
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.error("Failed to send notification email to {}", recipient, exception);
        }
    }
}
