package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionHistoryService;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamWithdrawalRequestResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamWithdrawalRequestRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.impl.TeamWithdrawalRequestServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamWithdrawalRequestServiceImplTest {

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private TeamWithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private SubmissionsRepository submissionsRepository;

    @Mock
    private SubmissionHistoryService submissionHistoryService;

    @Mock
    private JudgingRepository judgingRepository;

    @Mock
    private DisqualificationsRepository disqualificationsRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private TeamEventRegistrationService teamEventRegistrationService;

    @InjectMocks
    private TeamWithdrawalRequestServiceImpl service;

    @Test
    void requestingWithdrawalWithdrawsTeamImmediatelyAndStoresApprovedRecord() {
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();

        Teams team = team(teamId, eventId, leaderId);
        CreateTeamWithdrawalRequest request = request("Need to withdraw");

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(roundRepository.findRoundIdsByCategoryIdAndStatusNames(
                team.getCategoryId(),
                List.of("submission open", "judging")
        )).thenReturn(List.of(UUID.randomUUID()));
        when(submissionsRepository.findByTeamIdAndRoundIdIn(
                org.mockito.ArgumentMatchers.eq(teamId),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(List.of());
        when(withdrawalRequestRepository.save(any(TeamWithdrawalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TeamWithdrawalRequestResponse response = service.requestWithdrawal(teamId, request, leaderId);

        assertEquals(TeamStatusConstants.WITHDRAWN, team.getTeamStatusId());
        assertEquals("APPROVED", response.getRequestStatus());
        assertEquals("Need to withdraw", response.getReason());
        verify(teamsRepository).save(team);
        verify(teamEventRegistrationService).markTeamParticipantsWithdrawn(teamId, leaderId);
    }

    @Test
    void requestingWithdrawalDisqualifiesSubmittedSubmissionWhenNotJudgedInCurrentRound() {
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        Teams team = team(teamId, eventId, leaderId);
        Submissions submission = submittedSubmission(submissionId, teamId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(roundRepository.findRoundIdsByCategoryIdAndStatusNames(
                team.getCategoryId(),
                List.of("submission open", "judging")
        )).thenReturn(List.of(roundId));
        when(submissionsRepository.findByTeamIdAndRoundIdIn(teamId, List.of(roundId))).thenReturn(List.of(submission));
        when(judgingRepository.existsBySubmission_SubmissionIdAndIsActiveTrue(submissionId)).thenReturn(false);
        when(disqualificationsRepository.findBySubmissionId(submissionId)).thenReturn(List.of());
        when(withdrawalRequestRepository.save(any(TeamWithdrawalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.requestWithdrawal(teamId, request("Need to withdraw"), leaderId);

        assertEquals(SubmissionStatusConstants.DISQUALIFIED, submission.getSubmissionStatusId());
        verify(submissionsRepository).save(submission);
        verify(submissionHistoryService).recordSnapshot(submission);
        verify(disqualificationsRepository).save(argThat(disqualification ->
                submissionId.equals(disqualification.getSubmissionId())
                        && disqualification.getTeamId() == null
                        && leaderId.equals(disqualification.getDisqualifiedById())
                        && "Team voluntarily withdrew".equals(disqualification.getReason())
                        && Boolean.FALSE.equals(disqualification.getReversed())
        ));
    }

    @Test
    void requestingWithdrawalDoesNotDisqualifyJudgedSubmission() {
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        Teams team = team(teamId, eventId, leaderId);
        Submissions submission = submittedSubmission(submissionId, teamId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(roundRepository.findRoundIdsByCategoryIdAndStatusNames(
                team.getCategoryId(),
                List.of("submission open", "judging")
        )).thenReturn(List.of(roundId));
        when(submissionsRepository.findByTeamIdAndRoundIdIn(teamId, List.of(roundId))).thenReturn(List.of(submission));
        when(judgingRepository.existsBySubmission_SubmissionIdAndIsActiveTrue(submissionId)).thenReturn(true);
        when(withdrawalRequestRepository.save(any(TeamWithdrawalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.requestWithdrawal(teamId, request("Need to withdraw"), leaderId);

        assertEquals(SubmissionStatusConstants.SUBMITTED, submission.getSubmissionStatusId());
        verify(submissionsRepository, never()).save(submission);
        verify(submissionHistoryService, never()).recordSnapshot(any(Submissions.class));
        verify(disqualificationsRepository, never()).save(any(Disqualifications.class));
    }

    @Test
    void organizerListReturnsWithdrawalHistoryForEvent() {
        UUID eventId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();

        Teams team = team(teamId, eventId, leaderId);
        TeamWithdrawalRequest withdrawal = withdrawalRequest(teamId, team, leaderId);
        when(withdrawalRequestRepository.findByTeam_EventIdOrderByRequestedAtDesc(eventId))
                .thenReturn(List.of(withdrawal));

        List<TeamWithdrawalRequestResponse> response = service.getWithdrawalRequests(eventId);

        assertEquals(1, response.size());
        assertEquals(teamId, response.get(0).getTeamId());
        assertEquals("APPROVED", response.get(0).getRequestStatus());
        assertEquals("Need to withdraw", response.get(0).getReason());
    }

    private CreateTeamWithdrawalRequest request(String reason) {
        CreateTeamWithdrawalRequest request = new CreateTeamWithdrawalRequest();
        request.setReason(reason);
        return request;
    }

    private Teams team(UUID teamId, UUID eventId, UUID leaderId) {
        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);
        team.setCategoryId(UUID.randomUUID());
        team.setLeaderUserId(leaderId);
        team.setTeamName("Seal Team");
        team.setTeamStatusId(TeamStatusConstants.ACTIVE);
        return team;
    }

    private TeamWithdrawalRequest withdrawalRequest(UUID teamId, Teams team, UUID requestedById) {
        TeamWithdrawalRequest request = new TeamWithdrawalRequest();
        request.setRequestId(UUID.randomUUID());
        request.setTeamId(teamId);
        request.setTeam(team);
        request.setRequestedById(requestedById);
        request.setReason("Need to withdraw");
        request.setRequestStatus("APPROVED");
        request.setRequestedAt(LocalDateTime.now());
        return request;
    }

    private Submissions submittedSubmission(UUID submissionId, UUID teamId) {
        Submissions submission = new Submissions();
        submission.setSubmissionId(submissionId);
        submission.setTeamId(teamId);
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        return submission;
    }
}
