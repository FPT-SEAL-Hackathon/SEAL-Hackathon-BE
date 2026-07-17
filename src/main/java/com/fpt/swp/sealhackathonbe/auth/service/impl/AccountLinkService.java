package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.AccountLinkTicket;
import com.fpt.swp.sealhackathonbe.auth.oauth.OAuthUserInfo;
import com.fpt.swp.sealhackathonbe.auth.repository.AccountLinkTicketRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.core.utils.TokenHashUtil;
import com.fpt.swp.sealhackathonbe.notification.service.EmailService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserOAuthAccount;
import com.fpt.swp.sealhackathonbe.user.repository.UserOAuthAccountRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * Liên kết đăng nhập Google với tài khoản hiện có (và ngược lại) theo nguyên tắc
 * MỘT NGƯỜI = MỘT bản ghi Users:
 * - GOOGLE_LINK: Google sub mới + email đã tồn tại → không tạo user thứ hai;
 *   xác minh chủ sở hữu (mật khẩu local hoặc OTP email) rồi ghi UserOAuthAccounts
 *   trỏ vào user hiện có.
 * - LOCAL_SETUP: đăng ký local với email của user Google-only → không tạo user mới;
 *   xác minh OTP email rồi thiết lập PasswordHash cho user hiện có.
 * Quy tắc an toàn: chỉ lưu hash của linkingToken/OTP; token một lần, hết hạn ngắn;
 * user đích được resolve từ ticket ở server — client không được gửi UserID;
 * sau khi liên kết/đổi phương thức đăng nhập thì thu hồi mọi refresh token cũ.
 */
@Service
public class AccountLinkService {

    private static final Logger log = LoggerFactory.getLogger(AccountLinkService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int LINK_TOKEN_BYTES = 32;
    private static final int LINK_TICKET_TTL_MINUTES = 15;
    private static final int OTP_TTL_MINUTES = 10;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final AccountLinkTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AccountLinkService(
            AccountLinkTicketRepository ticketRepository,
            UserRepository userRepository,
            UserOAuthAccountRepository userOAuthAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenHashUtil tokenHashUtil,
            EmailService emailService
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashUtil = tokenHashUtil;
        this.emailService = emailService;
    }

    // ─── Phát ticket ─────────────────────────────────────────────────────────

    /**
     * Google sub mới nhưng email đã thuộc user hiện có:
     * phát linkingToken (một lần, 15 phút) chứa snapshot định danh Google.
     */
    @Transactional
    public String createGoogleLinkTicket(User target, OAuthUserInfo info) {
        return createTicket(target, AccountLinkTicket.PURPOSE_GOOGLE_LINK, info);
    }

    /**
     * Đăng ký local trên email của user Google-only:
     * phát linkingToken để xác minh OTP rồi thiết lập mật khẩu.
     * REQUIRES_NEW: caller (register) sẽ ném AccountLinkRequiredException để
     * trả 409 cho client — transaction ngoài rollback nhưng ticket phải được giữ.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public String createLocalSetupTicket(User target) {
        return createTicket(target, AccountLinkTicket.PURPOSE_LOCAL_SETUP, null);
    }

    private String createTicket(User target, String purpose, OAuthUserInfo info) {
        LocalDateTime now = LocalDateTime.now();

        // Vô hiệu hóa ticket cũ cùng mục đích trước khi phát ticket mới.
        ticketRepository.findByUser_UserIdAndPurposeAndConsumedAtIsNull(target.getUserId(), purpose)
                .forEach(oldTicket -> oldTicket.setConsumedAt(now));

        byte[] tokenBytes = new byte[LINK_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        AccountLinkTicket ticket = AccountLinkTicket.builder()
                .user(target)
                .purpose(purpose)
                .provider(info != null ? info.getProvider() : null)
                .providerUserId(info != null ? info.getProviderUserId() : null)
                .providerEmail(info != null ? info.getEmail() : null)
                .providerEmailVerified(info != null ? info.getEmailVerified() : null)
                .providerDisplayName(info != null ? info.getDisplayName() : null)
                .providerAvatarUrl(info != null ? info.getAvatarUrl() : null)
                .tokenHash(tokenHashUtil.hash(rawToken))
                .otpAttempts(0)
                .createdAt(now)
                .expiresAt(now.plusMinutes(LINK_TICKET_TTL_MINUTES))
                .build();
        ticketRepository.save(ticket);

        return rawToken;
    }

    // ─── OTP ─────────────────────────────────────────────────────────────────

    /**
     * Gửi OTP 6 số tới email của user đích (không phải email client gửi lên).
     * Cooldown 60 giây giữa hai lần gửi; OTP hết hạn sau 10 phút.
     */
    @Transactional
    public void sendOtp(String linkingToken) {
        AccountLinkTicket ticket = requireActiveTicket(linkingToken);
        LocalDateTime now = LocalDateTime.now();

        if (ticket.getOtpLastSentAt() != null
                && ticket.getOtpLastSentAt().plusSeconds(OTP_RESEND_COOLDOWN_SECONDS).isAfter(now)) {
            throw new BadRequestException("Please wait a moment before requesting another code");
        }

        String otp = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        ticket.setOtpHash(tokenHashUtil.hash(otp));
        ticket.setOtpExpiresAt(now.plusMinutes(OTP_TTL_MINUTES));
        ticket.setOtpAttempts(0);
        ticket.setOtpLastSentAt(now);
        ticket.setOtpVerifiedAt(null);
        ticketRepository.save(ticket);

        User target = ticket.getUser();
        sendOtpEmailAfterCommit(target, otp);
    }

    /**
     * Kiểm tra OTP: tối đa 5 lần nhập sai, hết hạn 10 phút.
     * Thành công thì đánh dấu ticket đã xác minh OTP.
     */
    @Transactional
    public void verifyOtp(String linkingToken, String otp) {
        AccountLinkTicket ticket = requireActiveTicket(linkingToken);
        LocalDateTime now = LocalDateTime.now();

        if (ticket.getOtpHash() == null || ticket.getOtpExpiresAt() == null) {
            throw new BadRequestException("No verification code has been requested for this session");
        }
        if (ticket.getOtpExpiresAt().isBefore(now)) {
            throw new BadRequestException("The verification code has expired. Please request a new one");
        }
        if (ticket.getOtpAttempts() >= OTP_MAX_ATTEMPTS) {
            throw new BadRequestException("Too many incorrect attempts. Please request a new code");
        }

        if (otp == null || !tokenHashUtil.hash(otp.trim()).equals(ticket.getOtpHash())) {
            ticket.setOtpAttempts(ticket.getOtpAttempts() + 1);
            ticketRepository.save(ticket);
            throw new BadRequestException("Incorrect verification code");
        }

        ticket.setOtpVerifiedAt(now);
        ticketRepository.save(ticket);
    }

    // ─── Hoàn tất liên kết Google → user hiện có ─────────────────────────────

    /**
     * Gắn định danh Google trong ticket vào user đích sau khi chứng minh
     * quyền sở hữu: mật khẩu local đúng HOẶC OTP email đã xác minh.
     * Trả về user đích để controller phát phiên đăng nhập mới;
     * mọi refresh token cũ của user bị thu hồi.
     */
    @Transactional
    public User completeGoogleLink(String linkingToken, String password) {
        AccountLinkTicket ticket = requireActiveTicket(linkingToken);

        if (!AccountLinkTicket.PURPOSE_GOOGLE_LINK.equals(ticket.getPurpose())) {
            throw new BadRequestException("Invalid or expired linking session");
        }
        if (ticket.getProvider() == null || ticket.getProviderUserId() == null) {
            throw new BadRequestException("Invalid or expired linking session");
        }

        User target = requireLinkableUser(ticket);

        // Xác minh chủ sở hữu — tuyệt đối không auto-link chỉ vì trùng email.
        boolean otpVerified = ticket.getOtpVerifiedAt() != null;
        if (password != null && !password.isBlank()) {
            if (!Boolean.TRUE.equals(target.getLocalLoginEnabled())
                    || !encoder.matches(password, target.getPasswordHash())) {
                throw new BadRequestException("Incorrect password");
            }
        } else if (!otpVerified) {
            throw new BadRequestException(
                    "Please verify ownership with your password or an email verification code");
        }

        // Chống trùng: user đã có Google, hoặc sub này vừa bị gắn vào user khác.
        if (userOAuthAccountRepository.existsByUser_UserIdAndProvider(
                target.getUserId(), ticket.getProvider())) {
            throw new BusinessConflictException(
                    "This account already has a Google login linked");
        }
        if (userOAuthAccountRepository.findByProviderAndProviderUserId(
                ticket.getProvider(), ticket.getProviderUserId()).isPresent()) {
            throw new BusinessConflictException(
                    "This Google account is already linked to another user");
        }

        LocalDateTime now = LocalDateTime.now();
        UserOAuthAccount oauthAccount = UserOAuthAccount.builder()
                .user(target)
                .provider(ticket.getProvider())
                .providerUserId(ticket.getProviderUserId())
                .email(ticket.getProviderEmail())
                .emailVerified(ticket.getProviderEmailVerified())
                .displayName(ticket.getProviderDisplayName())
                .avatarUrl(ticket.getProviderAvatarUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();
        userOAuthAccountRepository.save(oauthAccount);

        ticket.setConsumedAt(now);
        ticketRepository.save(ticket);

        revokeRefreshTokens(target.getUserId());

        log.info("Linked Google identity to user {}", target.getUserId());
        return target;
    }

    // ─── Thiết lập mật khẩu local cho user Google-only ───────────────────────

    /**
     * Sau khi OTP đã xác minh: thiết lập PasswordHash cho user hiện có
     * (không tạo user mới, không đụng role/trạng thái/mã sinh viên).
     */
    @Transactional
    public User setupLocalPassword(String linkingToken, String password, String confirmPassword) {
        AccountLinkTicket ticket = requireActiveTicket(linkingToken);

        if (!AccountLinkTicket.PURPOSE_LOCAL_SETUP.equals(ticket.getPurpose())) {
            throw new BadRequestException("Invalid or expired linking session");
        }
        if (ticket.getOtpVerifiedAt() == null) {
            throw new BadRequestException("Please verify the email code before setting a password");
        }
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!password.equals(confirmPassword)) {
            throw new BadRequestException("Password and Confirm Password do not match");
        }

        User target = requireLinkableUser(ticket);

        if (Boolean.TRUE.equals(target.getLocalLoginEnabled())) {
            throw new BusinessConflictException("This account already has a local password. Please sign in instead");
        }

        target.setPasswordHash(encoder.encode(password));
        target.setLocalLoginEnabled(true);
        userRepository.save(target);

        LocalDateTime now = LocalDateTime.now();
        ticket.setConsumedAt(now);
        ticketRepository.save(ticket);

        revokeRefreshTokens(target.getUserId());

        log.info("Local password set up for OAuth-only user {}", target.getUserId());
        return target;
    }

    // ─── Gỡ liên kết Google ──────────────────────────────────────────────────

    /**
     * Gỡ định danh Google khỏi user hiện tại. Chỉ cho phép khi user còn
     * đăng nhập local (đã có mật khẩu) — và phải xác minh lại mật khẩu —
     * để không tự khóa mình khỏi tài khoản.
     */
    @Transactional
    public void unlinkGoogle(User currentUser, String password) {
        if (!Boolean.TRUE.equals(currentUser.getLocalLoginEnabled())) {
            throw new BusinessConflictException(
                    "Set up a local password before unlinking Google, or you would lose access to this account");
        }
        if (password == null || !encoder.matches(password, currentUser.getPasswordHash())) {
            throw new BadRequestException("Incorrect password");
        }

        UserOAuthAccount account = userOAuthAccountRepository
                .findByUser_UserIdAndProvider(currentUser.getUserId(), GOOGLE_PROVIDER)
                .orElseThrow(() -> new BadRequestException("No Google login is linked to this account"));

        userOAuthAccountRepository.delete(account);
        log.info("Unlinked Google identity from user {}", currentUser.getUserId());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private AccountLinkTicket requireActiveTicket(String linkingToken) {
        if (linkingToken == null || linkingToken.isBlank()) {
            throw new BadRequestException("Invalid or expired linking session");
        }
        AccountLinkTicket ticket = ticketRepository
                .findByTokenHash(tokenHashUtil.hash(linkingToken.trim()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired linking session"));

        if (ticket.getConsumedAt() != null || ticket.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired linking session");
        }
        return ticket;
    }

    private User requireLinkableUser(AccountLinkTicket ticket) {
        User target = ticket.getUser();
        if (target == null || Boolean.TRUE.equals(target.getIsDeleted())) {
            throw new BadRequestException("Invalid or expired linking session");
        }
        String status = target.getAccountStatus() != null
                ? target.getAccountStatus().getStatusName()
                : "";
        if ("Suspended".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            throw new BusinessConflictException("This account cannot be linked. Please contact support");
        }
        return target;
    }

    private void revokeRefreshTokens(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(userId)
                .forEach(token -> {
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                });
    }

    private void sendOtpEmailAfterCommit(User target, String otp) {
        Runnable sendEmail = () -> {
            try {
                emailService.sendVerificationCodeEmail(target.getEmail(), target.getFullName(), otp);
            } catch (Exception ex) {
                // Không log OTP thô; chỉ log định danh user để chẩn đoán.
                log.warn("Account link OTP email could not be sent to user {}", target.getUserId());
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
