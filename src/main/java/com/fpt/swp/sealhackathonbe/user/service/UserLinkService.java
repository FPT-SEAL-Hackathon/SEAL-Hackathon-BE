package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.MergeBlockedException;
import com.fpt.swp.sealhackathonbe.user.dto.LinkCandidateResponse;
import com.fpt.swp.sealhackathonbe.user.dto.LinkLocalAccountRequest;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserOAuthAccount;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserOAuthAccountRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Liên kết tài khoản OAuth TEMPORARY vào tài khoản local có sẵn.
 * Bắt buộc xác minh mật khẩu local; không bao giờ auto-link theo email.
 */
@Service
public class UserLinkService {

    // Các bảng nghiệp vụ tham chiếu UserID: nếu user tạm đã có dữ liệu ở đây
    // thì KHÔNG cho gộp tự động (trả 409 MERGE_BLOCKED, cần xử lý thủ công).
    private static final Map<String, String> BUSINESS_TABLES = Map.of(
            "EventParticipants", "UserID",
            "Teams", "LeaderUserID",
            "TeamMembers", "UserID",
            "TeamJoinRequests", "UserID",
            "Submissions", "SubmittedByUserID",
            "RoundJudges", "UserID",
            "ConsultationRequests", "CreatedByUserID",
            "CategoryMentors", "MentorUserID"
    );

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountStatusRepository accountStatusRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @PersistenceContext
    private EntityManager entityManager;

    public UserLinkService(
            UserRepository userRepository,
            UserOAuthAccountRepository userOAuthAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            AccountStatusRepository accountStatusRepository,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountStatusRepository = accountStatusRepository;
        this.userService = userService;
    }

    /**
     * Gợi ý tài khoản local trùng email với user hiện tại (không lộ dữ liệu nhạy cảm).
     */
    @Transactional(readOnly = true)
    public List<LinkCandidateResponse> linkCandidates(User currentUser) {
        if (currentUser.getEmail() == null) {
            return List.of();
        }
        return userRepository.findByEmailAndIsDeletedFalse(currentUser.getEmail()).stream()
                .filter(candidate -> !candidate.getUserId().equals(currentUser.getUserId()))
                .filter(candidate -> Boolean.TRUE.equals(candidate.getLocalLoginEnabled()))
                .map(candidate -> LinkCandidateResponse.builder()
                        .email(candidate.getEmail())
                        .fullName(candidate.getFullName())
                        .role(toApiName(candidate.getUserType() != null
                                ? candidate.getUserType().getTypeName() : null))
                        .roleName(candidate.getUserType() != null
                                ? candidate.getUserType().getTypeName() : null)
                        .matchedBy("EMAIL")
                        .build())
                .toList();
    }

    /**
     * Gộp tài khoản:
     * 1. User hiện tại phải là OAuth TEMPORARY.
     * 2. Tài khoản đích phải local-enabled, không SUSPENDED/REJECTED.
     * 3. Mật khẩu local phải đúng (không auto-link theo email).
     * 4. User tạm không được có dữ liệu nghiệp vụ (merge safety).
     * 5. Chuyển định danh OAuth sang tài khoản đích, thu hồi phiên user tạm,
     *    vô hiệu hóa user tạm, cấp phiên mới cho tài khoản đích.
     */
    @Transactional
    public LoginResponse linkLocalAccount(User currentUser, LinkLocalAccountRequest request) {

        String currentStatus = currentUser.getAccountStatus() != null
                ? currentUser.getAccountStatus().getStatusName()
                : "";
        if (!"Temporary".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Only temporary OAuth accounts can be linked.");
        }

        List<UserOAuthAccount> oauthAccounts =
                userOAuthAccountRepository.findByUser_UserId(currentUser.getUserId());
        if (oauthAccounts.isEmpty()) {
            throw new BadRequestException("Current account has no OAuth identity to link.");
        }

        User target = userRepository
                .findFirstByEmailAndLocalLoginEnabledTrueAndIsDeletedFalseOrderByCreatedAtAsc(
                        request.getTargetEmail().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String targetStatus = target.getAccountStatus() != null
                ? target.getAccountStatus().getStatusName()
                : "";
        if ("Suspended".equalsIgnoreCase(targetStatus) || "Rejected".equalsIgnoreCase(targetStatus)) {
            throw new AccessDeniedException("Target account is not eligible for linking.");
        }

        // Xác minh chủ sở hữu bằng mật khẩu local — bước bắt buộc trước khi gộp.
        if (!encoder.matches(request.getPassword(), target.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Định danh OAuth chỉ được liên kết vào một tài khoản cho mỗi provider.
        for (UserOAuthAccount oauthAccount : oauthAccounts) {
            if (userOAuthAccountRepository.existsByUser_UserIdAndProvider(
                    target.getUserId(), oauthAccount.getProvider())) {
                throw new MergeBlockedException(
                        "Target account already has a linked " + oauthAccount.getProvider() + " identity."
                );
            }
        }

        assertNoBusinessData(currentUser.getUserId());

        // Chuyển định danh OAuth từ user tạm sang tài khoản đích.
        for (UserOAuthAccount oauthAccount : oauthAccounts) {
            oauthAccount.setUser(target);
            oauthAccount.setUpdatedAt(LocalDateTime.now());
            userOAuthAccountRepository.save(oauthAccount);
        }

        // Thu hồi mọi phiên của user tạm.
        refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(currentUser.getUserId())
                .forEach(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });

        // Vô hiệu hóa user tạm (soft delete, giống luồng quản lý user).
        currentUser.setIsDeleted(true);
        accountStatusRepository.findByStatusNameIgnoreCase("Suspended")
                .ifPresent(currentUser::setAccountStatus);
        userRepository.save(currentUser);

        // Cấp phiên mới cho tài khoản đích — cùng hình dạng với login thường.
        return userService.issueSession(target);
    }

    /**
     * Merge safety: user tạm có bất kỳ dòng dữ liệu nghiệp vụ nào thì chặn gộp.
     */
    private void assertNoBusinessData(UUID temporaryUserId) {
        for (Map.Entry<String, String> table : BUSINESS_TABLES.entrySet()) {
            Number count = (Number) entityManager
                    .createNativeQuery(
                            "SELECT COUNT(*) FROM " + table.getKey()
                                    + " WHERE " + table.getValue() + " = :userId")
                    .setParameter("userId", temporaryUserId)
                    .getSingleResult();
            if (count != null && count.longValue() > 0) {
                throw new MergeBlockedException(
                        "This account already has activity (" + table.getKey()
                                + ") and requires manual support to merge."
                );
            }
        }
    }

    private String toApiName(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ROOT);
    }
}
