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

    @Value("${app.notification.mail.sender-name:SEAL Hackathon}")
    private String senderName;

    @Async
    public void sendEmail(String recipient, String subject, String content) {
        sendPlainTextEmail(recipient, subject, content);
    }

    @Async
    public void sendNotificationEmail(String recipient, String title, String body) {
        sendPlainTextEmail(recipient, title, body);
    }

    @Async
    public void sendVerificationCodeEmail(String recipient, String verificationCode) {
        sendVerificationCodeEmail(recipient, null, verificationCode);
    }

    @Async
    public void sendVerificationCodeEmail(String recipient, String recipientName, String verificationCode) {
        sendPlainTextEmail(
                recipient,
                "Verify your SEAL Hackathon account",
                buildVerificationCodeContent(recipientName, verificationCode)
        );
    }
    //send mail without name of user
    @Async
    public void sendVerificationLinkEmail(String recipient, String verificationLink) {
        sendVerificationLinkEmail(recipient, null, verificationLink);
    }
    //send mail with name of user
    @Async
    public void sendVerificationLinkEmail(String recipient, String recipientName, String verificationLink) {
        sendPlainTextEmail(
                recipient,
                "Verify your SEAL Hackathon account",
                buildVerificationLinkContent(recipientName, verificationLink)
        );
    }
    //
    @Async
    public void sendPasswordResetEmail(String recipient, String resetLink) {
        sendPasswordResetEmail(recipient, null, resetLink);
    }

    @Async
    public void sendPasswordResetEmail(String recipient, String recipientName, String resetLink) {
        sendPlainTextEmail(
                recipient,
                "Reset your SEAL Hackathon password",
                buildPasswordResetContent(recipientName, resetLink)
        );
    }

    private void sendPlainTextEmail(String recipient, String subject, String content) {
        if (!mailEnabled) {
            return;
        }
        if (recipient == null || recipient.isBlank()) {
            log.warn("Notification email skipped because recipient email is empty");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (senderEmail != null && !senderEmail.isBlank()) {
            message.setFrom(senderName + " <" + senderEmail + ">");
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

    private String buildVerificationCodeContent(String recipientName, String verificationCode) {
        String greeting = buildGreeting(recipientName);
        return greeting + "\n\n"
                + "Use the verification code below to verify your SEAL Hackathon account:\n\n"
                + verificationCode + "\n\n"
                + "If you did not request this email, you can ignore it.\n\n"
                + "Regards,\n"
                + senderName;
    }

    private String buildVerificationLinkContent(String recipientName, String verificationLink) {
        String greeting = buildGreeting(recipientName);
        return greeting + "\n\n"
                + "Click the link below to verify your SEAL Hackathon account:\n\n"
                + verificationLink + "\n\n"
                + "If you did not request this email, you can ignore it.\n\n"
                + "Regards,\n"
                + senderName;
    }

    private String buildPasswordResetContent(String recipientName, String resetLink) {
        String greeting = buildGreeting(recipientName);
        return greeting + "\n\n"
                + "Click the link below to reset your SEAL Hackathon password:\n\n"
                + resetLink + "\n\n"
                + "If you did not request a password reset, you can ignore this email.\n\n"
                + "Regards,\n"
                + senderName;
    }

    private String buildGreeting(String recipientName) {
        if (recipientName == null || recipientName.isBlank()) {
            return "Hi,";
        }
        return "Hi " + recipientName.trim() + ",";
    }
}
