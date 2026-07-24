package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.team.dto.LeadershipReassignmentResult;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.user.dto.DeactivateUserResult;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deactivate: chuyển status sang Suspended (GIỮ isDeleted=false), thu hồi session,
 * và ủy quyền cho TeamService chuyển leader để team không bị đóng băng; tóm tắt
 * tác động (team đã chuyển leader + cảnh báo) được trả về cho organizer.
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
    @Mock
    private TeamService teamService;
    @Mock
    private RoundJudgeRepository roundJudgeRepository;
    @Mock
    private EventRepository eventRepository;

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
                .thenReturn(List.of(liveToken));
        when(teamService.reassignLeadershipForDeactivatedUser(userId, actorId))
                .thenReturn(LeadershipReassignmentResult.builder().build());

        service.delete(userId, actorId);

        assertEquals("Suspended", user.getAccountStatus().getStatusName());
        assertFalse(user.getIsDeleted());
        verify(userRepository).save(user);
        // Suspend phải thu hồi các phiên refresh còn sống.
        org.junit.jupiter.api.Assertions.assertNotNull(liveToken.getRevokedAt());
        verify(refreshTokenRepository).saveAll(List.of(liveToken));
        // Ủy quyền chuyển leader cho TeamService.
        verify(teamService).reassignLeadershipForDeactivatedUser(userId, actorId);
    }

    @Test
    void deactivateSurfacesTeamTransfersAndWarnings() {
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User user = activeUser(userId);
        AccountStatus suspended = new AccountStatus();
        suspended.setStatusName("Suspended");

        when(userRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(accountStatusRepository.findByStatusNameIgnoreCase("Suspended"))
                .thenReturn(Optional.of(suspended));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(refreshTokenRepository.findByUser_UserIdAndRevokedAtIsNull(userId)).thenReturn(List.of());

        LeadershipReassignmentResult impact = LeadershipReassignmentResult.builder().build();
        impact.getTransfers().add(LeadershipReassignmentResult.TransferInfo.builder()
                .teamName("Team Alpha").newLeaderName("Bob").build());
        impact.getFrozenTeams().add("Team Solo");
        when(teamService.reassignLeadershipForDeactivatedUser(userId, actorId)).thenReturn(impact);
        when(roundJudgeRepository.existsByJudge_UserId(userId)).thenReturn(true);
        when(eventRepository.existsByCreatedBy_UserId(userId)).thenReturn(true);

        DeactivateUserResult result = service.delete(userId, actorId);

        assertEquals(1, result.getTransferredTeams().size());
        assertTrue(result.getTransferredTeams().get(0).contains("Team Alpha"));
        assertTrue(result.getTransferredTeams().get(0).contains("Bob"));
        // 1 team đóng băng + 1 cảnh báo judge + 1 cảnh báo organizer.
        assertEquals(3, result.getWarnings().size());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("Team Solo")));
    }

    @Test
    void deactivateRejectsSelf() {
        UUID actorId = UUID.randomUUID();
        User user = activeUser(actorId);
        when(userRepository.findByUserIdAndIsDeletedFalse(actorId)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.delete(actorId, actorId));
        verify(userRepository, never()).save(any(User.class));
        // Không đụng team khi tự deactivate bị chặn.
        verify(teamService, never()).reassignLeadershipForDeactivatedUser(any(), any());
    }
}
