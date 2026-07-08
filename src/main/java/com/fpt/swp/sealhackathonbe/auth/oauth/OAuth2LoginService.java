package com.fpt.swp.sealhackathonbe.auth.oauth;

import com.fpt.swp.sealhackathonbe.core.constant.UserRoleConstants;

import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserOAuthAccount;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserOAuthAccountRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Đăng nhập/tạo tài khoản qua OAuth.
 * Định danh OAuth = (provider, providerUserId). Email trùng với tài khoản
 * local sẽ KHÔNG được auto-merge — user tự liên kết sau khi xác minh mật khẩu.
 */
@Service
public class OAuth2LoginService {

    private static final UUID EXTERNAL_STUDENT_ID =
            UserRoleConstants.ROLE_USER;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final AccountStatusRepository accountStatusRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public OAuth2LoginService(
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            AccountStatusRepository accountStatusRepository,
            UserOAuthAccountRepository userOAuthAccountRepository
    ) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.accountStatusRepository = accountStatusRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
    }

    /**
     * Tìm user theo định danh OAuth; nếu chưa có thì tạo user TEMPORARY mới.
     * Không bao giờ gộp vào tài khoản local chỉ vì trùng email.
     */
    @Transactional
    public User loginOrCreate(OAuthUserInfo info) {
        if (info.getProviderUserId() == null || info.getProviderUserId().isBlank()) {
            throw new IllegalStateException("OAuth provider did not return a user id");
        }

        UserOAuthAccount existing = userOAuthAccountRepository
                .findByProviderAndProviderUserId(info.getProvider(), info.getProviderUserId())
                .orElse(null);

        if (existing != null) {
            existing.setEmail(info.getEmail());
            existing.setEmailVerified(info.getEmailVerified());
            existing.setDisplayName(info.getDisplayName());
            existing.setAvatarUrl(info.getAvatarUrl());
            existing.setUpdatedAt(LocalDateTime.now());
            userOAuthAccountRepository.save(existing);
            return existing.getUser();
        }

        User user = createTemporaryOAuthUser(info);

        // UpdatedAt được set ngay khi tạo vì một số schema định nghĩa cột này NOT NULL.
        LocalDateTime now = LocalDateTime.now();
        UserOAuthAccount oauthAccount = UserOAuthAccount.builder()
                .user(user)
                .provider(info.getProvider())
                .providerUserId(info.getProviderUserId())
                .email(info.getEmail())
                .emailVerified(info.getEmailVerified())
                .displayName(info.getDisplayName())
                .avatarUrl(info.getAvatarUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();
        userOAuthAccountRepository.save(oauthAccount);

        return user;
    }

    /**
     * User OAuth mới: accountStatus = TEMPORARY, chưa đăng nhập local được,
     * password hash ngẫu nhiên không dùng được (PasswordHash là NOT NULL).
     * Role tạm là EXTERNAL_STUDENT vì UserTypeID bắt buộc; role thật sẽ
     * được chọn ở bước complete-profile.
     */
    private User createTemporaryOAuthUser(OAuthUserInfo info) {
        UserType temporaryType = userTypeRepository
                .findById(EXTERNAL_STUDENT_ID)
                .orElseThrow(() -> new IllegalStateException("Default user type not found"));

        AccountStatus temporaryStatus = accountStatusRepository
                .findByStatusNameIgnoreCase("Temporary")
                .orElseThrow(() -> new IllegalStateException("Temporary account status not found"));

        byte[] randomPassword = new byte[32];
        SECURE_RANDOM.nextBytes(randomPassword);

        User user = new User();
        user.setEmail(info.getEmail());
        user.setFullName(info.getDisplayName() != null && !info.getDisplayName().isBlank()
                ? info.getDisplayName()
                : info.getEmail());
        user.setUserType(temporaryType);
        user.setAccountStatus(temporaryStatus);
        user.setPasswordHash(encoder.encode(
                Base64.getUrlEncoder().withoutPadding().encodeToString(randomPassword)
        ));
        user.setLocalLoginEnabled(false);
        user.setIsDeleted(false);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
