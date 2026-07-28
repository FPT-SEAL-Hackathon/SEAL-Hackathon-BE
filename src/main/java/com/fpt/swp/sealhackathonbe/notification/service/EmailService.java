package com.fpt.swp.sealhackathonbe.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service quản lý luồng gửi Email thông báo.
 * 
 * Kiến trúc & Tối ưu:
 * - Template Engine: Sử dụng Thymeleaf để parse HTML template từ src/main/resources/templates.
 * - Asynchronous: Các method gửi mail được đánh dấu @Async để không block luồng xử lý chính của ứng dụng.
 * - Feature Flag: Hỗ trợ cấu hình `app.notification.mail.enabled` để bật/tắt gửi mail tuỳ môi trường (dev/prod).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Inject Thymeleaf TemplateEngine

    @Value("${app.notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${app.notification.mail.sender-name:SEAL Hackathon}")
    private String senderName;

    @Async
    public void sendEmail(String recipient, String subject, String content) {
        Context context = new Context();
        context.setVariable("title", subject);
        context.setVariable("message", content.replace("\n", "<br>"));
        sendHtmlEmail(recipient, subject, context);
    }

    @Async
    public void sendNotificationEmail(String recipient, String title, String body) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", body.replace("\n", "<br>"));
        sendHtmlEmail(recipient, title, context);
    }

    @Async
    public void sendVerificationCodeEmail(String recipient, String verificationCode) {
        sendVerificationCodeEmail(recipient, null, verificationCode);
    }

    @Async
    public void sendVerificationCodeEmail(String recipient, String recipientName, String verificationCode) {
        Context context = new Context();
        context.setVariable("title", "Verify your SEAL Hackathon account");
        context.setVariable("greeting", buildGreeting(recipientName));
        context.setVariable("message", "Thank you for registering with SEAL Hackathon. To complete your registration and verify your account, please use the following verification code. This code is valid for a limited time. Please do not share it with anyone.");
        context.setVariable("verificationCode", verificationCode);
        
        sendHtmlEmail(recipient, "Verify your SEAL Hackathon account", context);
    }

    //send mail without name of user
    @Async
    public void sendVerificationLinkEmail(String recipient, String verificationLink) {
        sendVerificationLinkEmail(recipient, null, verificationLink);
    }

    //send mail with name of user
    @Async
    public void sendVerificationLinkEmail(String recipient, String recipientName, String verificationLink) {
        Context context = new Context();
        context.setVariable("title", "Verify your SEAL Hackathon account");
        context.setVariable("greeting", buildGreeting(recipientName));
        context.setVariable("message", "Thank you for registering with SEAL Hackathon. To complete your registration and verify your account, please click the button below:");
        context.setVariable("actionUrl", verificationLink);
        context.setVariable("actionText", "Verify Email Address");
        
        sendHtmlEmail(recipient, "Verify your SEAL Hackathon account", context);
    }

    @Async
    public void sendPasswordResetEmail(String recipient, String resetLink) {
        sendPasswordResetEmail(recipient, null, resetLink);
    }

    @Async
    public void sendPasswordResetEmail(String recipient, String recipientName, String resetLink) {
        Context context = new Context();
        context.setVariable("title", "Reset your SEAL Hackathon password");
        context.setVariable("greeting", buildGreeting(recipientName));
        context.setVariable("message", "We received a request to reset the password for your SEAL Hackathon account. Click the button below to set a new password.");
        context.setVariable("actionUrl", resetLink);
        context.setVariable("actionText", "Reset Password");

        sendHtmlEmail(recipient, "Reset your SEAL Hackathon password", context);
    }

    private void sendHtmlEmail(String recipient, String subject, Context context) {
        if (!mailEnabled) {
            // WARN thay vì INFO: đây là nguyên nhân phổ biến của "không nhận
            // được mail verify" trên môi trường dev, cần đập vào mắt trong log.
            log.warn("Email \"{}\" to {} skipped: mail is disabled (set NOTIFICATION_MAIL_ENABLED=true to enable)",
                    subject, recipient);
            return;
        }
        if (recipient == null || recipient.isBlank()) {
            log.warn("Notification email skipped because recipient email is empty");
            return;
        }

        try {
            // Process the Thymeleaf template with the given context variables
            String htmlContent = templateEngine.process("email-template", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            if (senderEmail != null && !senderEmail.isBlank()) {
                helper.setFrom(senderName + " <" + senderEmail + ">");
            }
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML format

            mailSender.send(message);
        } catch (MessagingException exception) {
            log.error("Failed to send HTML email to {}", recipient, exception);
        } catch (Exception ex) {
            log.error("Unexpected error occurred while sending email to {}", recipient, ex);
        }
    }

    private String buildGreeting(String recipientName) {
        if (recipientName == null || recipientName.isBlank()) {
            return "Hi there,";
        }
        return "Hi " + recipientName.trim() + ",";
    }
}
