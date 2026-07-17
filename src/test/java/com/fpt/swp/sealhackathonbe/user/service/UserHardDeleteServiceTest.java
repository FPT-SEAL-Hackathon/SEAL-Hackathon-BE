package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AccountLinkTicketRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.PasswordResetTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.award.repository.AwardRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationMessageRepository;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationRequestRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.criteria.repository.CriterionTemplateRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.judging.repository.EvaluationAuditLogRepository;
import com.fpt.swp.sealhackathonbe.notification.Repository.NotificationRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.research.repository.DataExportLogRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMilestoneRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.DeletedUserTombstone;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.DeletedUserTombstoneRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserOAuthAccountRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHardDeleteServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DeletedUserTombstoneRepository tombstoneRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private AccountLinkTicketRepository accountLinkTicketRepository;
    @Mock
    private UserOAuthAccountRepository userOAuthAccountRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TeamMembersRepository teamMembersRepository;
    @Mock
    private TeamsRepository teamsRepository;
    @Mock
    private TeamJoinRequestsRepository teamJoinRequestsRepository;
    @Mock
    private EventParticipantRepository eventParticipantRepository;
    @Mock
    private SubmissionsRepository submissionsRepository;
    @Mock
    private ConsultationRequestRepository consultationRequestRepository;
    @Mock
    private ConsultationMessageRepository consultationMessageRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private AwardRepository awardRepository;
    @Mock
    private DisqualificationsRepository disqualificationsRepository;
    @Mock
    private EvaluationAuditLogRepository evaluationAuditLogRepository;
    @Mock
    private RoundJudgeRepository roundJudgeRepository;
    @Mock
    private CategoryMentorRepository categoryMentorRepository;
    @Mock
    private CriterionTemplateRepository criterionTemplateRepository;
    @Mock
    private DataExportLogRepository dataExportLogRepository;
    @Mock
    private TeamMilestoneRepository teamMilestoneRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserHardDeleteService service;

    private static final String EMAIL = "student@test.com";

    private User user(UUID id, String email, String role) {
        UserType type = new UserType();
        type.setTypeName(role);
        AccountStatus status = new AccountStatus();
        status.setStatusName("Active");
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setFullName("Test Student");
        user.setUserType(type);
        user.setAccountStatus(status);
        user.setIsDeleted(false);
        return user;
    }

    private User actor(UUID actorId) {
        User actor = user(actorId, "organizer@test.com", "Organizer");
        when(userRepository.findByUserIdAndIsDeletedFalse(actorId)).thenReturn(Optional.of(actor));
        return actor;
    }

    @Test
    void rejectsWhenEmailHasNoAccount() {
        when(userRepository.findAllByEmailIgnoreCase(EMAIL)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class,
                () -> service.hardDeleteByEmail(EMAIL, UUID.randomUUID(), null));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void rejectsSelfDelete() {
        UUID actorId = UUID.randomUUID();
        when(userRepository.findAllByEmailIgnoreCase(EMAIL))
                .thenReturn(List.of(user(actorId, EMAIL, "FPT Student")));
        actor(actorId);

        assertThrows(AccessDeniedException.class,
                () -> service.hardDeleteByEmail(EMAIL, actorId, null));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void rejectsOrganizerAndAdminAccounts() {
        UUID actorId = UUID.randomUUID();
        when(userRepository.findAllByEmailIgnoreCase(EMAIL))
                .thenReturn(List.of(user(UUID.randomUUID(), EMAIL, "Organizer")));
        actor(actorId);

        assertThrows(AccessDeniedException.class,
                () -> service.hardDeleteByEmail(EMAIL, actorId, null));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void rejectsWhenUserStillOwnsStaffData() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(userRepository.findAllByEmailIgnoreCase(EMAIL))
                .thenReturn(List.of(user(targetId, EMAIL, "FPT Student")));
        actor(actorId);
        when(eventRepository.existsByCreatedBy_UserId(targetId)).thenReturn(true);

        BusinessConflictException exception = assertThrows(BusinessConflictException.class,
                () -> service.hardDeleteByEmail(EMAIL, actorId, null));
        assertTrue(exception.getMessage().contains("events created"));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deletesMemberAndNotifiesRemainingTeamMembers() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();

        User target = user(targetId, EMAIL, "FPT Student");
        when(userRepository.findAllByEmailIgnoreCase(EMAIL)).thenReturn(List.of(target));
        actor(actorId);

        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);
        team.setLeaderUserId(leaderId);

        when(teamMembersRepository.findAllByUserIdAndActiveTrue(targetId))
                .thenReturn(List.of(membership(teamId, targetId)));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(membership(teamId, leaderId), membership(teamId, targetId)));
        when(submissionsRepository.findBySubmittedByUserId(targetId)).thenReturn(List.of());

        int deleted = service.hardDeleteByEmail(EMAIL, actorId, null);

        assertEquals(1, deleted);
        verify(notificationService).sendNotification(
                eq(leaderId), eq(actorId), eq(eventId), anyString(), anyString());
        verify(teamMembersRepository).deleteByUserId(targetId);
        verify(userRepository).delete(target);
        // Leader không đổi vì user bị xóa không phải leader.
        assertEquals(leaderId, team.getLeaderUserId());
    }

    @Test
    void transfersLeadershipAndReassignsSubmissionsWhenLeaderDeleted() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID successorId = UUID.randomUUID();

        User target = user(targetId, EMAIL, "FPT Student");
        when(userRepository.findAllByEmailIgnoreCase(EMAIL)).thenReturn(List.of(target));
        actor(actorId);

        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(UUID.randomUUID());
        team.setLeaderUserId(targetId);

        when(teamMembersRepository.findAllByUserIdAndActiveTrue(targetId))
                .thenReturn(List.of(membership(teamId, targetId)));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(membership(teamId, targetId), membership(teamId, successorId)));

        Submissions submission = new Submissions();
        submission.setTeamId(teamId);
        submission.setSubmittedByUserId(targetId);
        when(submissionsRepository.findBySubmittedByUserId(targetId)).thenReturn(List.of(submission));
        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));

        service.hardDeleteByEmail(EMAIL, actorId, null);

        assertEquals(successorId, team.getLeaderUserId());
        assertEquals(successorId, submission.getSubmittedByUserId());
        verify(teamsRepository).save(team);
        verify(notificationService).sendNotification(
                eq(successorId), eq(actorId), any(), anyString(), anyString());
    }

    @Test
    void deletesEmptyTeamWhenLeaderIsLastMember() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        User target = user(targetId, EMAIL, "FPT Student");
        when(userRepository.findAllByEmailIgnoreCase(EMAIL)).thenReturn(List.of(target));
        actor(actorId);

        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setLeaderUserId(targetId);

        when(teamMembersRepository.findAllByUserIdAndActiveTrue(targetId))
                .thenReturn(List.of(membership(teamId, targetId)));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(membership(teamId, targetId)));
        when(submissionsRepository.findBySubmittedByUserId(targetId)).thenReturn(List.of());

        service.hardDeleteByEmail(EMAIL, actorId, null);

        verify(teamJoinRequestsRepository).deleteByTeamId(teamId);
        verify(teamMembersRepository).deleteByTeamId(teamId);
        verify(teamsRepository).delete(team);
        verify(notificationService, never()).sendNotification(any(), any(), any(), anyString(), anyString());
    }

    @Test
    void writesTombstoneWithSevenDayExpiryAndDefaultReason() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        User target = user(targetId, EMAIL, "FPT Student");
        when(userRepository.findAllByEmailIgnoreCase(EMAIL)).thenReturn(List.of(target));
        actor(actorId);
        when(teamMembersRepository.findAllByUserIdAndActiveTrue(targetId)).thenReturn(List.of());
        when(submissionsRepository.findBySubmittedByUserId(targetId)).thenReturn(List.of());

        service.hardDeleteByEmail(EMAIL, actorId, "  ");

        ArgumentCaptor<DeletedUserTombstone> tombstoneCaptor =
                ArgumentCaptor.forClass(DeletedUserTombstone.class);
        verify(tombstoneRepository).save(tombstoneCaptor.capture());
        DeletedUserTombstone tombstone = tombstoneCaptor.getValue();
        assertEquals(EMAIL, tombstone.getEmail());
        assertEquals(actorId, tombstone.getDeletedByUserId());
        assertEquals(UserHardDeleteService.DEFAULT_REASON, tombstone.getReason());
        assertNotNull(tombstone.getDeletedAt());
        assertEquals(7, Duration.between(tombstone.getDeletedAt(), tombstone.getExpiresAt()).toDays());

        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        assertEquals("USER_HARD_DELETED", auditCaptor.getValue().getActionType());
        assertEquals(targetId, auditCaptor.getValue().getEntityId());
        assertEquals(actorId, auditCaptor.getValue().getActorUserId());
    }

    @Test
    void deletesAllAccountsSharingTheEmail() {
        UUID actorId = UUID.randomUUID();
        User localAccount = user(UUID.randomUUID(), EMAIL, "FPT Student");
        User oauthAccount = user(UUID.randomUUID(), EMAIL, "External Student");
        when(userRepository.findAllByEmailIgnoreCase(EMAIL))
                .thenReturn(List.of(localAccount, oauthAccount));
        actor(actorId);
        when(teamMembersRepository.findAllByUserIdAndActiveTrue(any())).thenReturn(List.of());
        when(submissionsRepository.findBySubmittedByUserId(any())).thenReturn(List.of());

        int deleted = service.hardDeleteByEmail(EMAIL, actorId, null);

        assertEquals(2, deleted);
        verify(userRepository).delete(localAccount);
        verify(userRepository).delete(oauthAccount);
        verify(refreshTokenRepository).deleteByUser_UserId(localAccount.getUserId());
        verify(refreshTokenRepository).deleteByUser_UserId(oauthAccount.getUserId());
        verify(userOAuthAccountRepository).deleteByUser_UserId(oauthAccount.getUserId());
    }

    private TeamMembers membership(UUID teamId, UUID userId) {
        TeamMembers member = new TeamMembers();
        member.setTeamMemberId(UUID.randomUUID());
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setActive(true);
        member.setJoinedAt(LocalDateTime.now());
        return member;
    }
}
