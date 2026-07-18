package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deactivate chỉ chuyển status sang Suspended và PHẢI giữ isDeleted=false
 * để account còn hiện trong danh sách quản lý và kích hoạt lại được.
 */
@ExtendWith(MockitoExtension.class)
class UserManagementServiceDeactivateTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserTypeRepository userTypeRepository;
    @Mock
    private AccountStatusRepository accountStatusRepository;
    @Mock
    private TeamMembersRepository teamMembersRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private UserManagementService service;

    private User activeUser(UUID userId) {
        UserType type = new UserType();
        type.setTypeName("FPT Student");
        AccountStatus status = new AccountStatus();
        status.setStatusName("Active");
        User user = new User();
        user.setUserId(userId);
        user.setEmail("student@test.com");
        user.setFullName("Student");
        user.setUserType(type);
        user.setAccountStatus(status);
        user.setIsDeleted(false);
        return user;
    }

    @Test
    void deactivateSuspendsAccountWithoutSettingIsDeleted() {
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User user = activeUser(userId);
        AccountStatus suspended = new AccountStatus();
        suspended.setStatusName("Suspended");

        when(userRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(accountStatusRepository.findByStatusNameIgnoreCase("Suspended"))
                .thenReturn(Optional.of(suspended));
        when(userRepository.save(any(User.class))).thenReturn(user);
        RefreshToken liveToken = new RefreshToken();
        when(refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(userId))
                .thenReturn(java.util.List.of(liveToken));

        service.delete(userId, actorId);

        assertEquals("Suspended", user.getAccountStatus().getStatusName());
        assertFalse(user.getIsDeleted());
        verify(userRepository).save(user);
        // Suspend phải thu hồi các phiên refresh còn sống.
        org.junit.jupiter.api.Assertions.assertNotNull(liveToken.getRevokedAt());
        verify(refreshTokenRepository).saveAll(java.util.List.of(liveToken));
    }

    @Test
    void deactivateRejectsSelf() {
        UUID actorId = UUID.randomUUID();
        User user = activeUser(actorId);
        when(userRepository.findByUserIdAndIsDeletedFalse(actorId)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.delete(actorId, actorId));
        verify(userRepository, never()).save(any(User.class));
    }
}
