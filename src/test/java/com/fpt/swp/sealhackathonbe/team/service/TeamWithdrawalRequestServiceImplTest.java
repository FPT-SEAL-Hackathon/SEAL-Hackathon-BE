package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.team.dto.HandleTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamWithdrawalRequestRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.impl.TeamWithdrawalRequestServiceImpl;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamWithdrawalRequestServiceImplTest {

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @Mock
    private TeamWithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TeamEventRegistrationService teamEventRegistrationService;

    @InjectMocks
    private TeamWithdrawalRequestServiceImpl service;

    @Test
    void rejectingWithdrawalRequestRequiresResponseNote() {
        HandleTeamWithdrawalRequest request = new HandleTeamWithdrawalRequest();
        request.setAction("REJECTED");
        request.setResponseNote("   ");

        assertThrows(
                BadRequestException.class,
                () -> service.handleWithdrawalRequest(UUID.randomUUID(), request, UUID.randomUUID())
        );

        verifyNoInteractions(withdrawalRequestRepository, teamsRepository, auditLogRepository);
    }

    @Test
    void rejectingWithdrawalRequestKeepsTeamActiveAndNotifiesActiveMembers() {
        UUID requestId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Teams team = team(teamId, eventId);
        TeamWithdrawalRequest withdrawalRequest = withdrawalRequest(requestId, teamId, team, leaderId);
        HandleTeamWithdrawalRequest request = new HandleTeamWithdrawalRequest();
        request.setAction("REJECTED");
        request.setResponseNote("Team is already checked in.");

        when(withdrawalRequestRepository.findByRequestIdAndRequestStatus(requestId, "PENDING"))
                .thenReturn(Optional.of(withdrawalRequest));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(withdrawalRequestRepository.save(withdrawalRequest)).thenReturn(withdrawalRequest);
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId))
                .thenReturn(List.of(member(teamId, leaderId), member(teamId, memberId)));

        service.handleWithdrawalRequest(requestId, request, organizerId);

        assertEquals(TeamStatusConstants.ACTIVE, team.getTeamStatusId());
        assertEquals("REJECTED", withdrawalRequest.getRequestStatus());
        assertEquals("Team is already checked in.", withdrawalRequest.getResponseNote());
        verify(teamsRepository, never()).save(team);
        verify(notificationService).sendBroadcastNotification(
                List.of(leaderId, memberId),
                organizerId,
                eventId,
                "Team Withdrawal Rejected",
                "Seal Team's withdrawal request for the event has been rejected. Reason: Team is already checked in."
        );
    }

    @Test
    void approvingWithdrawalRequestWithdrawsTeamAndNotifiesActiveMembers() {
        UUID requestId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();

        Teams team = team(teamId, eventId);
        TeamWithdrawalRequest withdrawalRequest = withdrawalRequest(requestId, teamId, team, leaderId);
        HandleTeamWithdrawalRequest request = new HandleTeamWithdrawalRequest();
        request.setAction("APPROVED");

        when(withdrawalRequestRepository.findByRequestIdAndRequestStatus(requestId, "PENDING"))
                .thenReturn(Optional.of(withdrawalRequest));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(withdrawalRequestRepository.save(withdrawalRequest)).thenReturn(withdrawalRequest);
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId))
                .thenReturn(List.of(member(teamId, leaderId)));

        service.handleWithdrawalRequest(requestId, request, organizerId);

        assertEquals(TeamStatusConstants.WITHDRAWN, team.getTeamStatusId());
        assertEquals("APPROVED", withdrawalRequest.getRequestStatus());
        verify(teamsRepository).save(team);
        verify(teamEventRegistrationService).markTeamParticipantsWithdrawn(teamId, organizerId);
        verify(notificationService).sendBroadcastNotification(
                List.of(leaderId),
                organizerId,
                eventId,
                "Team Withdrawal Approved",
                "Seal Team's withdrawal request for the event has been approved."
        );
    }

    private Teams team(UUID teamId, UUID eventId) {
        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);
        team.setTeamName("Seal Team");
        team.setTeamStatusId(TeamStatusConstants.ACTIVE);
        return team;
    }

    private TeamWithdrawalRequest withdrawalRequest(UUID requestId, UUID teamId, Teams team, UUID requestedById) {
        TeamWithdrawalRequest request = new TeamWithdrawalRequest();
        request.setRequestId(requestId);
        request.setTeamId(teamId);
        request.setTeam(team);
        request.setRequestedById(requestedById);
        request.setReason("Need to withdraw");
        request.setRequestStatus("PENDING");
        return request;
    }

    private TeamMembers member(UUID teamId, UUID userId) {
        TeamMembers member = new TeamMembers();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setActive(true);
        return member;
    }
}
