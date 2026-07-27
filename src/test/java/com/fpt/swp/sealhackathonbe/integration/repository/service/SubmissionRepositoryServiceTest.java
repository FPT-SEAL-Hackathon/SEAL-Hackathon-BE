package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.client.GitRepositoryMetadataClient;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadataFetchResult;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.SubmissionRepositoryResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncStatus;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.SubmissionRepositoryEntity;
import com.fpt.swp.sealhackathonbe.integration.repository.mapper.SubmissionRepositoryMapper;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.SubmissionRepositoryEntityRepository;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionRepositoryServiceTest {

    @Mock
    private SubmissionRepositoryEntityRepository submissionRepositoryRepository;
    @Mock
    private SubmissionsRepository submissionsRepository;
    @Mock
    private TeamMembersRepository teamMembersRepository;
    @Mock
    private TeamsRepository teamsRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RoundRepository roundRepository;
    @Mock
    private RoundJudgeRepository roundJudgeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GitRepositoryMetadataClient metadataClient;

    private SubmissionRepositoryService service;

    private final UUID submissionId = UUID.randomUUID();
    private final UUID teamId = UUID.randomUUID();
    private final UUID roundId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private Submissions submission;

    @BeforeEach
    void setUp() {
        service = new SubmissionRepositoryService(
                submissionRepositoryRepository,
                submissionsRepository,
                teamMembersRepository,
                teamsRepository,
                eventRepository,
                roundRepository,
                roundJudgeRepository,
                userRepository,
                List.of(metadataClient),
                new SubmissionRepositoryMapper());

        submission = new Submissions();
        submission.setTeamId(teamId);
        submission.setRoundId(roundId);
        submission.setRepositoryUrl("https://github.com/owner/repo");
    }

    private void stubEventLookup() {
        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);
        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
    }

    // ── Authorization ───────────────────────────────────────────────────────

    @Test
    void authorizeView_allowsActiveTeamMember() {
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId))
                .thenReturn(Optional.of(new TeamMembers()));

        assertDoesNotThrow(() -> service.authorizeView(submission, userId));
    }

    @Test
    void authorizeView_allowsOrganizerOfSameEvent() {
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)).thenReturn(Optional.empty());
        stubEventLookup();
        when(eventRepository.existsByEventIdAndCreatedBy_UserId(eventId, userId)).thenReturn(true);

        assertDoesNotThrow(() -> service.authorizeView(submission, userId));
    }

    @Test
    void authorizeView_deniesOrganizerOfOtherEvent() {
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)).thenReturn(Optional.empty());
        stubEventLookup();
        when(eventRepository.existsByEventIdAndCreatedBy_UserId(eventId, userId)).thenReturn(false);
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId)).thenReturn(Optional.empty());

        RepositoryIntegrationException ex = assertThrows(RepositoryIntegrationException.class,
                () -> service.authorizeView(submission, userId));
        assertEquals(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void authorizeView_allowsAssignedJudge() {
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)).thenReturn(Optional.empty());
        stubEventLookup();
        when(eventRepository.existsByEventIdAndCreatedBy_UserId(eventId, userId)).thenReturn(false);
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId))
                .thenReturn(Optional.of(new RoundJudge()));

        assertDoesNotThrow(() -> service.authorizeView(submission, userId));
    }

    @Test
    void authorizeResync_allowsAssignedJudge() {
        // Yeu cau moi: judge duoc phep resync de tu nap ban MOI NHAT cua repo khi cham.
        // authorizeResync kiem ROLE_ORGANIZER qua SecurityContext (khong co auth trong test)
        // roi moi den nhanh judge -> khong can stub eventRepository.
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)).thenReturn(Optional.empty());
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId))
                .thenReturn(Optional.of(new RoundJudge()));

        assertDoesNotThrow(() -> service.authorizeResync(submission, userId));
    }

    @Test
    void authorizeResync_deniesNonParticipant() {
        // Nguoi khong phai team member / khong co ROLE_ORGANIZER / khong duoc phan cong judge -> 403.
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)).thenReturn(Optional.empty());
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId)).thenReturn(Optional.empty());

        RepositoryIntegrationException ex = assertThrows(RepositoryIntegrationException.class,
                () -> service.authorizeResync(submission, userId));
        assertEquals(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_MODIFICATION_NOT_ALLOWED, ex.getErrorCode());
    }

    // ── Persistence ─────────────────────────────────────────────────────────

    private RepositoryMetadata sampleMetadata() {
        return RepositoryMetadata.builder()
                .provider(RepositoryProvider.GITHUB)
                .externalRepositoryId("123")
                .repositoryUrl("https://github.com/owner/repo")
                .externalUrl("https://github.com/owner/repo")
                .owner("owner")
                .repositoryName("repo")
                .fullName("owner/repo")
                .defaultBranch("main")
                .primaryLanguage("Java")
                .starCount(10)
                .forkCount(2)
                .openIssuesCount(1)
                .build();
    }

    @Test
    void saveOrUpdate_createsNewRecordWithSuccessStatus() {
        submission.setRepositoryUrl(null);
        when(submissionsRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(Optional.empty());
        when(submissionRepositoryRepository.save(any(SubmissionRepositoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionRepositoryResponse response = service.saveOrUpdateSubmissionRepository(
                submissionId, "https://github.com/owner/repo",
                RepositoryMetadataFetchResult.success(sampleMetadata()));

        assertEquals("SUCCESS", response.getLastSyncStatus());
        assertEquals("owner/repo", response.getFullName());
        assertEquals(10, response.getStarCount());
        // Dong bo nguoc URL sang Submissions cho tuong thich code cu.
        assertEquals("https://github.com/owner/repo", submission.getRepositoryUrl());
        verify(submissionsRepository).save(submission);
    }

    @Test
    void saveOrUpdate_updatesExistingRecordWithoutDuplicate() {
        SubmissionRepositoryEntity existing = new SubmissionRepositoryEntity();
        existing.setSubmission(submission);
        existing.setRepositoryUrl("https://github.com/owner/old-repo");
        existing.setLastSyncStatus(RepositorySyncStatus.SUCCESS);

        when(submissionsRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(Optional.of(existing));
        when(submissionRepositoryRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveOrUpdateSubmissionRepository(submissionId, "https://github.com/owner/repo",
                RepositoryMetadataFetchResult.success(sampleMetadata()));

        // Khong tao entity moi: save duoc goi voi chinh ban ghi cu.
        verify(submissionRepositoryRepository, times(1)).save(existing);
        assertEquals("https://github.com/owner/repo", existing.getRepositoryUrl());
    }

    @Test
    void saveOrUpdate_persistsFailedStatusWithErrorCode() {
        SubmissionRepositoryEntity existing = new SubmissionRepositoryEntity();
        existing.setSubmission(submission);
        existing.setRepositoryUrl("https://github.com/owner/repo");
        existing.setFullName("owner/repo");
        existing.setLastSyncStatus(RepositorySyncStatus.SUCCESS);

        when(submissionsRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(Optional.of(existing));
        when(submissionRepositoryRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionRepositoryResponse response = service.saveOrUpdateSubmissionRepository(
                submissionId, "https://github.com/owner/repo",
                RepositoryMetadataFetchResult.failure("GITHUB_RATE_LIMITED", "GitHub rate limit exceeded"));

        assertEquals("FAILED", response.getLastSyncStatus());
        assertEquals("GITHUB_RATE_LIMITED", response.getErrorCode());
        // Metadata cu duoc giu lai de nguoi xem van thay lan sync tot gan nhat.
        assertEquals("owner/repo", response.getFullName());
    }

    // ── Concurrency (sync lock) ─────────────────────────────────────────────

    @Test
    void syncSubmissionRepository_returnsConflictWhenAnotherSyncIsRunning() {
        com.fpt.swp.sealhackathonbe.user.entity.User user = new com.fpt.swp.sealhackathonbe.user.entity.User();
        user.setUserId(userId);
        // Phai thay CA context (khong chi setAuthentication): test khac trong suite co the
        // leak mot SecurityContext mock ma setAuthentication tren no la no-op.
        org.springframework.security.core.context.SecurityContext freshContext =
                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        freshContext.setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "leader@example.com", null, java.util.Collections.emptyList()));
        org.springframework.security.core.context.SecurityContextHolder.setContext(freshContext);
        try {
            when(userRepository.findByEmail("leader@example.com")).thenReturn(user);
            when(submissionsRepository.findById(submissionId)).thenReturn(Optional.of(submission));
            when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId))
                    .thenReturn(Optional.of(new TeamMembers()));
            when(submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId))
                    .thenReturn(Optional.of(new SubmissionRepositoryEntity()));
            // markSyncRunning tra 0 row = mot phien sync khac dang giu lock RUNNING.
            when(submissionRepositoryRepository.markSyncRunning(eq(submissionId), any(), any(), eq(RepositorySyncStatus.RUNNING)))
                    .thenReturn(0);

            RepositoryIntegrationException ex = assertThrows(RepositoryIntegrationException.class,
                    () -> service.syncSubmissionRepository(submissionId));
            assertEquals(RepositoryIntegrationException.ErrorCode.REPOSITORY_SYNC_ALREADY_RUNNING, ex.getErrorCode());
            // Khong duoc goi GitHub khi khong lay duoc lock.
            verifyNoInteractions(metadataClient);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
