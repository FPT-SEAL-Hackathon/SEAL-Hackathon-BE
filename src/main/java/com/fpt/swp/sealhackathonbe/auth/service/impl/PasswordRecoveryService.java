package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.PasswordResetToken;
import com.fpt.swp.sealhackathonbe.auth.repository.PasswordResetTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.core.config.AppProperties;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.utils.TokenHashUtil;
import com.fpt.swp.sealhackathonbe.notification.service.EmailService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Quên/đặt lại mật khẩu cho tài khoản LOCAL.
 * - forgot-password luôn trả kết quả chung (không lộ email có tồn tại).
 * - Chỉ lưu HASH của token reset; token dùng một lần, hết hạn 30 phút.
 * - Reset thành công thì thu hồi toàn bộ refresh token của user.
 */
@Service
public class PasswordRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;
    private static final int RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public PasswordRecoveryService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenHashUtil tokenHashUtil,
            EmailService emailService,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashUtil = tokenHashUtil;
        this.emailService = emailService;
        this.appProperties = appProperties;
    }

    /**
     * Quên mật khẩu: chỉ tài khoản LOCAL-enabled mới được cấp token reset.
     * Không tìm thấy user / user OAuth-only thì im lặng (response vẫn chung).
     */
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository
                .findFirstByEmailAndLocalLoginEnabledTrueAndIsDeletedFalseOrderByCreatedAtAsc(
                        email == null ? "" : email.trim())
                .orElse(null);

        if (user == null) {
            // Không lộ thông tin: kết thúc im lặng, controller vẫn trả thông báo chung.
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Vô hiệu hóa token reset cũ chưa dùng trước khi phát token mới.
        passwordResetTokenRepository.findByUserAndUsedAtIsNull(user)
                .forEach(oldToken -> oldToken.setUsedAt(now));

        byte[] tokenBytes = new byte[RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHashUtil.hash(rawToken))
                .createdAt(now)
                .expiresAt(now.plusMinutes(RESET_TOKEN_TTL_MINUTES))
                .build();
        passwordResetTokenRepository.save(tokenEntity);

        String resetLink = UriComponentsBuilder
                .fromUriString(appProperties.getFrontendUrl())
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        sendResetEmailAfterCommit(user, resetLink);
    }

    /**
     * Đặt lại mật khẩu bằng token: kiểm tra hash, hạn dùng, một lần;
     * cập nhật BCrypt hash mới và thu hồi toàn bộ refresh token.
     */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Password and Confirm Password do not match");
        }
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHash(tokenHashUtil.hash(token))
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getUsedAt() != null) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        if (!Boolean.TRUE.equals(user.getLocalLoginEnabled()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // Thu hồi toàn bộ phiên hiện có sau khi đổi mật khẩu.
        refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(user.getUserId())
                .forEach(refreshToken -> {
                    refreshToken.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(refreshToken);
                });
    }

    /**
     * Chỉ gửi email sau khi transaction commit; không log token thô.
     */
    private void sendResetEmailAfterCommit(User user, String resetLink) {
        Runnable sendEmail = () -> {
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
            } catch (Exception ex) {
                // Dev không có mail server: chỉ log placeholder an toàn, không lộ token.
                log.warn("Password reset email could not be sent to user {}", user.getUserId());
            }
        };

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendEmail.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendEmail.run();
                    }
                }
        );
    }
}
