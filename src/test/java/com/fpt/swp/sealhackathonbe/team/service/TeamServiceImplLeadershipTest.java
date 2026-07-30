package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.ParticipantStatus;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamEligibilityReviewResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.event.TeamRegistrationRejectedEvent;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamWithdrawalRequestRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.impl.TeamServiceImpl;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplLeadershipTest {
    private static final UUID FORMING_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final UUID ACTIVE_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000002");
    private static final UUID DISQUALIFIED_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000003");
    private static final UUID WITHDRAWN_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000004");
    private static final UUID PENDING_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000005");
    private static final UUID REJECTED_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000006");

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @Mock
    private TeamJoinRequestsRepository teamJoinRequestsRepository;

    @Mock
    private TeamEventRegistrationService teamEventRegistrationService;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    // TeamServiceImpl.rejectTeam KHONG con publish TeamRegistrationRejectedEvent nua ma goi
    // thang notificationService.sendBroadcastNotification. Thieu mock nay thi dependency bi
    // inject null, NPE bi nuot boi try/catch trong sendEligibilityRejectedNotification va test
    // chi thay "khong co tuong tac nao" — rat kho lan ra nguyen nhan.
    @Mock
    private com.fpt.swp.sealhackathonbe.notification.service.NotificationService notificationService;

    @Mock
    private com.fpt.swp.sealhackathonbe.team.service.impl.TeamJoinRequestCleaner teamJoinRequestCleaner;

    @Mock
    private DisqualificationsRepository disqualificationsRepository;

    @Mock
    private TeamWithdrawalRequestRepository teamWithdrawalRequestRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    void createTeamAllowsReusingNameFromRejectedTeam() {
        UUID eventId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID rejectedTeamId = UUID.randomUUID();
        Event event = event(eventId);
        CreateTeamRequest request = createTeamRequest(eventId, categoryId, "RejectTeam");
        Teams rejectedTeam = rejectedTeam(rejectedTeamId, eventId, leaderId, "RejectTeam");

        when(eventRepository.findByEventIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
        when(categoryRepository.existsByCategoryIdAndEventEventIdAndIsActiveTrue(categoryId, eventId))
                .thenReturn(true);
        when(teamsRepository.findByEventIdAndTeamNameIgnoreCaseForUpdate(eventId, "RejectTeam"))
                .thenReturn(List.of(rejectedTeam));
        when(teamMembersRepository.existsActiveMembershipInEvent(leaderId, eventId))
                .thenReturn(false);
        when(teamsRepository.save(any(Teams.class))).thenAnswer(invocation -> {
            Teams saved = invocation.getArgument(0);
            saved.setTeamId(teamId);
            return saved;
        });
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of());

        TeamResponse response = teamService.createTeam(request, leaderId);

        assertEquals(teamId, response.getTeamId());
        assertEquals("RejectTeam", response.getTeamName());
        assertEquals("RejectTeam [rejected:" + rejectedTeamId + "]", rejectedTeam.getTeamName());
        verify(teamsRepository).saveAll(List.of(rejectedTeam));
        verify(teamsRepository).save(any(Teams.class));
    }

    @Test
    void createTeamStillRejectsNameUsedByNonRejectedTeam() {
        UUID eventId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        Event event = event(eventId);
        CreateTeamRequest request = createTeamRequest(eventId, categoryId, "Seal Squad");

        when(eventRepository.findByEventIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
        when(categoryRepository.existsByCategoryIdAndEventEventIdAndIsActiveTrue(categoryId, eventId))
                .thenReturn(true);
        Teams existingTeam = team(UUID.randomUUID(), UUID.randomUUID());
        existingTeam.setEventId(eventId);
        existingTeam.setTeamName("Seal Squad");
        TeamMembers existingMember = member(existingTeam, existingTeam.getLeaderUserId());
        when(teamsRepository.findByEventIdAndTeamNameIgnoreCaseForUpdate(eventId, "Seal Squad"))
                .thenReturn(List.of(existingTeam));
        when(teamMembersRepository.findByTeamIdAndActiveTrue(existingTeam.getTeamId()))
                .thenReturn(List.of(existingMember));
        when(eventParticipantRepository.findParticipantStatusNameByEventIdAndUserId(eventId, existingMember.getUserId()))
                .thenReturn(Optional.empty());

        assertThrows(BusinessConflictException.class, () -> teamService.createTeam(request, leaderId));
        verify(teamsRepository, never()).save(any(Teams.class));
    }

    @Test
    void createTeamReleasesNameWhenOldTeamHasRejectedParticipantsButStaleTeamStatus() {
        UUID eventId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID oldLeaderId = UUID.randomUUID();
        UUID oldTeamId = UUID.randomUUID();
        UUID newTeamId = UUID.randomUUID();
        Event event = event(eventId);
        CreateTeamRequest request = createTeamRequest(eventId, categoryId, "RejectTeam");
        Teams staleTeam = team(oldTeamId, oldLeaderId);
        staleTeam.setEventId(eventId);
        staleTeam.setTeamName("RejectTeam");
        TeamMembers oldLeader = member(staleTeam, oldLeaderId);

        when(eventRepository.findByEventIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
        when(categoryRepository.existsByCategoryIdAndEventEventIdAndIsActiveTrue(categoryId, eventId))
                .thenReturn(true);
        when(teamsRepository.findByEventIdAndTeamNameIgnoreCaseForUpdate(eventId, "RejectTeam"))
                .thenReturn(List.of(staleTeam));
        when(teamMembersRepository.findByTeamIdAndActiveTrue(oldTeamId)).thenReturn(List.of(oldLeader));
        when(eventParticipantRepository.findParticipantStatusNameByEventIdAndUserId(eventId, oldLeaderId))
                .thenReturn(Optional.of("REJECTED"));
        when(teamMembersRepository.existsActiveMembershipInEvent(leaderId, eventId))
                .thenReturn(false);
        when(teamsRepository.save(any(Teams.class))).thenAnswer(invocation -> {
            Teams saved = invocation.getArgument(0);
            if (saved.getTeamId() == null) {
                saved.setTeamId(newTeamId);
            }
            return saved;
        });
        when(teamMembersRepository.findByTeamIdAndActiveTrue(newTeamId)).thenReturn(List.of());

        TeamResponse response = teamService.createTeam(request, leaderId);

        assertEquals(newTeamId, response.getTeamId());
        assertEquals(REJECTED_STATUS, staleTeam.getTeamStatusId());
        assertFalse(oldLeader.getActive());
        verify(teamsRepository).saveAll(List.of(staleTeam));
        verify(teamMembersRepository).saveAll(List.of(oldLeader));
    }

    @Test
    void reviewTeamsEligibilityDoesNotReturnRejectedTeams() {
        UUID eventId = UUID.randomUUID();
        UUID activeTeamId = UUID.randomUUID();
        UUID rejectedTeamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        Event event = event(eventId);
        Teams activeTeam = team(activeTeamId, leaderId);
        activeTeam.setEventId(eventId);
        activeTeam.setTeamName("Active Team");
        activeTeam.setTeamStatusId(ACTIVE_STATUS);
        Teams rejectedTeam = rejectedTeam(rejectedTeamId, eventId, UUID.randomUUID(), "Rejected Team");

        when(eventRepository.findByEventIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
        when(teamsRepository.findByEventId(eventId)).thenReturn(List.of(activeTeam, rejectedTeam));
        when(teamMembersRepository.findByTeamIdOrderByJoinedAtAsc(activeTeamId)).thenReturn(List.of());

        List<TeamEligibilityReviewResponse> response = teamService.reviewTeamsEligibility(eventId);

        assertEquals(1, response.size());
        assertEquals(activeTeamId, response.get(0).getTeamId());
    }

    @Test
    void leaderLeavingTransfersLeadershipToFirstRemainingMember() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID successorId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers leader = member(team, leaderId);
        TeamMembers successor = member(team, successorId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, leaderId))
                .thenReturn(Optional.of(leader));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(successor));

        teamService.removeMember(teamId, leaderId, leaderId, null);

        assertFalse(leader.getActive());
        assertNotNull(leader.getLeftAt());
        assertEquals(successorId, team.getLeaderUserId());
        verify(teamsRepository).save(team);
    }

    @Test
    void lastLeaderLeavingDeletesEmptyFormingTeam() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers leader = member(team, leaderId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, leaderId))
                .thenReturn(Optional.of(leader));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of());

        teamService.removeMember(teamId, leaderId, leaderId, null);

        verify(teamJoinRequestsRepository).deleteByTeamId(teamId);
        verify(teamMembersRepository).deleteByTeamId(teamId);
        verify(teamsRepository).delete(team);
        verify(teamsRepository, never()).save(team);
    }

    @Test
    void leaderCanDisbandFormingTeam() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers leader = member(team, leaderId);
        TeamMembers other = member(team, memberId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId))
                .thenReturn(List.of(leader, other));

        teamService.disbandTeam(teamId, leaderId);

        verify(teamEventRegistrationService).removePendingRegistration(team.getEventId(), leaderId);
        verify(teamEventRegistrationService).removePendingRegistration(team.getEventId(), memberId);
        verify(teamJoinRequestsRepository).deleteByTeamId(teamId);
        verify(teamMembersRepository).deleteByTeamId(teamId);
        verify(teamsRepository).delete(team);
    }

    @Test
    void nonLeaderCannotDisbandTeam() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));

        assertThrows(
                AccessDeniedException.class,
                () -> teamService.disbandTeam(teamId, UUID.randomUUID())
        );
        verify(teamsRepository, never()).delete(team);
    }

    @Test
    void nonFormingTeamCannotBeDisbanded() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        team.setTeamStatusId(ACTIVE_STATUS);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));

        assertThrows(
                BusinessConflictException.class,
                () -> teamService.disbandTeam(teamId, leaderId)
        );
        verify(teamsRepository, never()).delete(team);
    }

    @Test
    void regularMemberCanLeaveWithoutMinimumSizeCheck() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers member = member(team, memberId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, memberId))
                .thenReturn(Optional.of(member));

        teamService.removeMember(teamId, memberId, memberId, null);

        assertFalse(member.getActive());
        verify(teamsRepository, never()).save(team);
        verify(teamMembersRepository, never()).countByTeamIdAndActiveTrue(teamId);
    }

    @Test
    void activeTeamRosterCannotBeChanged() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        team.setTeamStatusId(ACTIVE_STATUS);
        TeamMembers member = member(team, memberId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, memberId))
                .thenReturn(Optional.of(member));

        assertThrows(
                BusinessConflictException.class,
                () -> teamService.removeMember(teamId, memberId, leaderId, "Roster is locked")
        );
    }

    @Test
    void currentLeaderCanTransferLeadershipToActiveMember() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID newLeaderId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers newLeader = member(team, newLeaderId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, newLeaderId))
                .thenReturn(Optional.of(newLeader));
        when(teamsRepository.save(team)).thenReturn(team);
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of(newLeader));

        TeamResponse response = teamService.transferLeadership(teamId, newLeaderId, leaderId);

        assertEquals(newLeaderId, team.getLeaderUserId());
        assertEquals(newLeaderId, response.getLeaderUserId());
    }

    @Test
    void nonLeaderCannotTransferLeadership() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));

        assertThrows(
                AccessDeniedException.class,
                () -> teamService.transferLeadership(teamId, UUID.randomUUID(), requesterId)
        );
    }

    @Test
    void leadershipCannotBeTransferredToInactiveOrExternalUser() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID externalUserId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, externalUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessConflictException.class,
                () -> teamService.transferLeadership(teamId, externalUserId, leaderId)
        );
    }

    @Test
    void disqualifiedTeamMemberDetailExposesSuspendedParticipantStatus() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        team.setTeamStatusId(UUID.fromString("60000000-0000-0000-0000-000000000003"));
        TeamMembers leader = member(team, leaderId);
        TeamMembers member = member(team, memberId);
        User user = new User();
        user.setUserId(memberId);
        member.setUser(user);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, leaderId))
                .thenReturn(Optional.of(leader));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, memberId))
                .thenReturn(Optional.of(member));

        TeamMemberDetailResponse response = teamService.getTeamMemberDetail(teamId, memberId, leaderId, false);

        assertEquals("Suspended", response.getParticipantStatus());
        assertEquals("Suspended", response.getParticipantStatusName());
    }

    @Test
    void organizerCanViewTeamMemberDetailWithoutBelongingToTeam() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers member = member(team, memberId);
        User user = new User();
        user.setUserId(memberId);
        user.setFullName("Member One");
        member.setUser(user);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, memberId))
                .thenReturn(Optional.of(member));
        // KHONG stub eventParticipantRepository o day: resolveParticipantStatusName suy trang
        // thai tu TEAM STATUS truoc, chi tra bang EventParticipants khi team roi vao trang thai
        // ngoai danh sach da biet. Team nay dang FORMING nen tra ve "Pending" ma khong cham vao
        // repository — stub cu tro thanh thua va lam Mockito bao UnnecessaryStubbing.

        TeamMemberDetailResponse response = teamService.getTeamMemberDetail(teamId, memberId, organizerId, true);

        assertEquals(memberId, response.getUserId());
        assertEquals("Member One", response.getFullName());
        assertEquals("Pending", response.getParticipantStatus());
        verify(teamMembersRepository, never()).findByTeamIdAndUserIdAndActiveTrue(teamId, organizerId);
        verify(eventParticipantRepository, never())
                .findParticipantStatusNameByEventIdAndUserId(any(), any());
    }

    @Test
    void disqualifiedTeamResponseMembersExposeSuspendedParticipantStatus() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        team.setTeamStatusId(DISQUALIFIED_STATUS);
        TeamMembers leader = member(team, leaderId);
        TeamMembers member = member(team, memberId);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of(leader, member));

        TeamResponse response = teamService.getById(teamId);

        assertEquals("Suspended", response.getMembers().get(0).getParticipantStatus());
        assertEquals("Suspended", response.getMembers().get(0).getParticipantStatusName());
        assertEquals("Suspended", response.getMembers().get(1).getParticipantStatus());
        assertEquals("Suspended", response.getMembers().get(1).getParticipantStatusName());
    }

    @Test
    void disqualifiedTeamResponseIncludesLatestDisqualificationDetail() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        LocalDateTime disqualifiedAt = LocalDateTime.now().minusDays(1);
        Teams team = team(teamId, leaderId);
        team.setTeamStatusId(DISQUALIFIED_STATUS);
        Disqualifications disqualification = new Disqualifications();
        disqualification.setTeamId(teamId);
        disqualification.setReason("Policy violation");
        disqualification.setDisqualifiedById(adminId);
        disqualification.setDisqualifiedAt(disqualifiedAt);
        User admin = new User();
        admin.setUserId(adminId);
        admin.setFullName("Organizer One");
        admin.setEmail("organizer@seal.test");
        disqualification.setDisqualifiedBy(admin);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of());
        when(disqualificationsRepository.findTopByTeamIdAndReversedFalseOrderByDisqualifiedAtDesc(teamId))
                .thenReturn(Optional.of(disqualification));

        TeamResponse response = teamService.getById(teamId);

        assertEquals("Policy violation", response.getDisqualifiedReason());
        assertEquals(adminId, response.getDisqualifiedById());
        assertEquals("Organizer One", response.getDisqualifiedByName());
        assertEquals("organizer@seal.test", response.getDisqualifiedByEmail());
        assertEquals(disqualifiedAt, response.getDisqualifiedAt());
    }

    @Test
    void withdrawnTeamResponseIncludesLatestWithdrawalDetail() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        LocalDateTime withdrawnAt = LocalDateTime.now().minusHours(2);
        Teams team = team(teamId, leaderId);
        team.setTeamStatusId(WITHDRAWN_STATUS);
        TeamWithdrawalRequest withdrawal = new TeamWithdrawalRequest();
        withdrawal.setTeamId(teamId);
        withdrawal.setReason("Schedule conflict");
        withdrawal.setRequestedById(leaderId);
        withdrawal.setRequestedAt(withdrawnAt);
        User leader = new User();
        leader.setUserId(leaderId);
        leader.setFullName("Leader One");
        leader.setEmail("leader@seal.test");
        withdrawal.setRequestedBy(leader);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of());
        when(teamWithdrawalRequestRepository.findTopByTeamIdOrderByRequestedAtDesc(teamId))
                .thenReturn(Optional.of(withdrawal));

        TeamResponse response = teamService.getById(teamId);

        assertEquals("Schedule conflict", response.getWithdrawnReason());
        assertEquals(leaderId, response.getWithdrawnById());
        assertEquals("Leader One", response.getWithdrawnByName());
        assertEquals("leader@seal.test", response.getWithdrawnByEmail());
        assertEquals(withdrawnAt, response.getWithdrawnAt());
    }

    @Test
    void rejectingPendingTeamReturnsToFormingWithoutDeactivatingMembers() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        team.setEventId(eventId);
        team.setTeamName("Seal Squad");
        team.setTeamStatusId(PENDING_STATUS);
        TeamMembers leader = member(team, leaderId);
        TeamMembers member = member(team, memberId);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamsRepository.save(team)).thenReturn(team);
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of(leader, member));

        TeamResponse response = teamService.rejectTeam(teamId, "Missing member profile", adminId);

        assertEquals(FORMING_STATUS, team.getTeamStatusId());
        assertTrue(leader.getActive());
        assertTrue(member.getActive());
        assertEquals(FORMING_STATUS, response.getTeamStatusId());
        verify(teamMembersRepository, never()).saveAll(any());
        verify(teamJoinRequestCleaner, never()).rejectPendingRequestsForTeam(any(), any(), any());

        // Bao cho ca doi biet team bi tu choi. Production da chuyen tu publish event sang goi
        // truc tiep notificationService, nen test bam theo hanh vi hien tai cua production.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> recipientsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).sendBroadcastNotification(
                recipientsCaptor.capture(),
                eq(adminId),
                eq(eventId),
                eq("Team Registration Rejected"),
                messageCaptor.capture()
        );
        assertEquals(List.of(leaderId, memberId), recipientsCaptor.getValue());
        assertTrue(messageCaptor.getValue().contains("Seal Squad"));
        assertTrue(messageCaptor.getValue().contains("Missing member profile"));
    }

    private Teams team(UUID teamId, UUID leaderId) {
        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setLeaderUserId(leaderId);
        team.setTeamStatusId(FORMING_STATUS);
        return team;
    }

    private Teams rejectedTeam(UUID teamId, UUID eventId, UUID leaderId, String teamName) {
        Teams team = team(teamId, leaderId);
        team.setEventId(eventId);
        team.setTeamName(teamName);
        team.setTeamStatusId(REJECTED_STATUS);
        return team;
    }

    private EventParticipant rejectedParticipant() {
        ParticipantStatus status = new ParticipantStatus();
        status.setStatusName("REJECTED");
        EventParticipant participant = new EventParticipant();
        participant.setParticipantStatus(status);
        return participant;
    }

    private Event event(UUID eventId) {
        Event event = new Event();
        event.setEventId(eventId);
        event.setMinTeamSize(1);
        event.setMaxTeamSize(5);
        event.setIsDeleted(false);
        return event;
    }

    private CreateTeamRequest createTeamRequest(UUID eventId, UUID categoryId, String teamName) {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setEventId(eventId);
        request.setCategoryId(categoryId);
        request.setTeamName(teamName);
        return request;
    }

    private TeamMembers member(Teams team, UUID userId) {
        TeamMembers member = new TeamMembers();
        member.setTeamId(team.getTeamId());
        member.setTeam(team);
        member.setUserId(userId);
        member.setActive(true);
        return member;
    }
}
