package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.SubmissionRepositoryEntityRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionHistoryRepository;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.impl.SubmissionQueryServiceImpl;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionQueryServiceImplTest {
    @Mock
    private SubmissionsRepository submissionsRepository;

    @Mock
    private SubmissionHistoryRepository submissionHistoryRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @Mock
    private DisqualificationsRepository disqualificationsRepository;

    @Mock
    private EventParticipantService eventParticipantService;

    @Mock
    private SubmissionRepositoryEntityRepository submissionRepositoryEntityRepository;

    @Mock
    private SubmissionRepositoryService submissionRepositoryService;

    private SubmissionQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubmissionQueryServiceImpl(
                submissionsRepository,
                submissionHistoryRepository,
                teamMembersRepository,
                disqualificationsRepository,
                eventParticipantService,
                submissionRepositoryEntityRepository,
                submissionRepositoryService
        );
    }

    @Test
    void currentSubmissionAllowsWithdrawnTeamMemberToViewReadOnly() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        TeamMembers membership = membership(teamId, userId, eventId, TeamStatusConstants.WITHDRAWN);
        Submissions submission = submission(teamId, roundId, userId);

        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId))
                .thenReturn(Optional.of(membership));
        when(submissionsRepository.findByTeamIdAndRoundId(teamId, roundId))
                .thenReturn(Optional.of(submission));
        when(submissionRepositoryEntityRepository.findBySubmission_SubmissionId(submission.getSubmissionId()))
                .thenReturn(Optional.empty());
        when(disqualificationsRepository.findActiveBySubmissionIdOrderByDisqualifiedAtDesc(submission.getSubmissionId()))
                .thenReturn(List.of());

        service.getSubmissionByTeamAndRound(teamId, roundId, userId);

        verify(eventParticipantService, never()).assertActiveParticipant(eventId, userId);
    }

    @Test
    void currentSubmissionStillChecksActiveParticipantForActiveTeam() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        TeamMembers membership = membership(teamId, userId, eventId, TeamStatusConstants.ACTIVE);
        Submissions submission = submission(teamId, roundId, userId);

        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId))
                .thenReturn(Optional.of(membership));
        when(submissionsRepository.findByTeamIdAndRoundId(teamId, roundId))
                .thenReturn(Optional.of(submission));
        when(submissionRepositoryEntityRepository.findBySubmission_SubmissionId(submission.getSubmissionId()))
                .thenReturn(Optional.empty());
        when(disqualificationsRepository.findActiveBySubmissionIdOrderByDisqualifiedAtDesc(submission.getSubmissionId()))
                .thenReturn(List.of());

        service.getSubmissionByTeamAndRound(teamId, roundId, userId);

        verify(eventParticipantService).assertActiveParticipant(eventId, userId);
    }

    @Test
    void currentSubmissionReturnsNotFoundWhenTeamRoundHasNoSubmission() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        TeamMembers membership = membership(teamId, userId, eventId, TeamStatusConstants.ACTIVE);

        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId))
                .thenReturn(Optional.of(membership));
        when(submissionsRepository.findByTeamIdAndRoundId(teamId, roundId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getSubmissionByTeamAndRound(teamId, roundId, userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Submission not found", exception.getReason());
        verify(eventParticipantService).assertActiveParticipant(eventId, userId);
    }

    @Test
    void currentSubmissionDeniesUserOutsideTeam() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId))
                .thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.getSubmissionByTeamAndRound(teamId, roundId, userId)
        );

        assertEquals("User does not belong to this team", exception.getMessage());
        verify(eventParticipantService, never()).assertActiveParticipant(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(submissionsRepository, never()).findByTeamIdAndRoundId(teamId, roundId);
    }

    private TeamMembers membership(UUID teamId, UUID userId, UUID eventId, UUID teamStatusId) {
        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);
        team.setTeamStatusId(teamStatusId);

        TeamMembers membership = new TeamMembers();
        membership.setTeamId(teamId);
        membership.setUserId(userId);
        membership.setTeam(team);
        membership.setActive(true);
        return membership;
    }

    private Submissions submission(UUID teamId, UUID roundId, UUID userId) {
        Submissions submission = new Submissions();
        submission.setSubmissionId(UUID.randomUUID());
        submission.setTeamId(teamId);
        submission.setRoundId(roundId);
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setLastUpdatedAt(LocalDateTime.now());
        submission.setSubmittedByUserId(userId);
        return submission;
    }
}
