package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserRoleRequest;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Guard: organizer không được tự đổi role của chính mình
 * (tự hạ quyền là mất quyền quản trị vĩnh viễn).
 */
@ExtendWith(MockitoExtension.class)
class UserManagementServiceRoleGuardTest {

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

    private UserType type(String name) {
        UserType type = new UserType();
        type.setTypeName(name);
        return type;
    }

    private User organizer(UUID userId) {
        AccountStatus status = new AccountStatus();
        status.setStatusName("Active");
        User user = new User();
        user.setUserId(userId);
        user.setEmail("organizer@test.com");
        user.setFullName("Organizer");
        user.setUserType(type("Organizer"));
        user.setAccountStatus(status);
        user.setIsDeleted(false);
        return user;
    }

    private void stubUserLookup(User user) {
        when(userRepository.findByUserIdAndIsDeletedFalse(user.getUserId()))
                .thenReturn(Optional.of(user));
    }

    private void stubRoleLookup() {
        when(userTypeRepository.findAll())
                .thenReturn(List.of(type("Organizer"), type("Mentor")));
    }

    @Test
    void updateRoleRejectsChangingOwnRole() {
        UUID actorId = UUID.randomUUID();
        User actor = organizer(actorId);
        stubUserLookup(actor);
        stubRoleLookup();

        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole("MENTOR");

        assertThrows(AccessDeniedException.class,
                () -> service.updateRole(actorId, request, actorId));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRoleAllowsResendingOwnCurrentRole() {
        UUID actorId = UUID.randomUUID();
        User actor = organizer(actorId);
        stubUserLookup(actor);
        stubRoleLookup();
        when(userRepository.save(any(User.class))).thenReturn(actor);
        when(teamMembersRepository.findFirstByUserIdAndActiveTrueOrderByJoinedAtDesc(actorId))
                .thenReturn(Optional.empty());

        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole("ORGANIZER");

        assertDoesNotThrow(() -> service.updateRole(actorId, request, actorId));
    }

    @Test
    void updateRejectsChangingOwnRoleViaPut() {
        UUID actorId = UUID.randomUUID();
        User actor = organizer(actorId);
        stubUserLookup(actor);
        stubRoleLookup();

        UpdateUserManagementRequest request = new UpdateUserManagementRequest();
        request.setRole("MENTOR");

        assertThrows(AccessDeniedException.class,
                () -> service.update(actorId, request, actorId));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRoleAllowsChangingAnotherUsersRole() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User target = organizer(targetId);
        stubUserLookup(target);
        stubRoleLookup();
        when(userRepository.save(any(User.class))).thenReturn(target);
        when(teamMembersRepository.findFirstByUserIdAndActiveTrueOrderByJoinedAtDesc(targetId))
                .thenReturn(Optional.empty());

        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole("MENTOR");

        assertDoesNotThrow(() -> service.updateRole(targetId, request, actorId));
        assertEquals("Mentor", target.getUserType().getTypeName());
        verify(userRepository).save(target);
    }
}
