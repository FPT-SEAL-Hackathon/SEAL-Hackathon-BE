package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.client.GitRepositoryMetadataClient;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.SubmissionRepositoryResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncStatus;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.SubmissionRepositoryEntity;
import com.fpt.swp.sealhackathonbe.integration.repository.mapper.SubmissionRepositoryMapper;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.SubmissionRepositoryEntityRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionRepositoryService {

    private final SubmissionRepositoryEntityRepository submissionRepositoryRepository;
    private final SubmissionsRepository submissionsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final EventRepository eventRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final UserRepository userRepository;
    private final List<GitRepositoryMetadataClient> gitRepositoryMetadataClients;
    private final SubmissionRepositoryMapper submissionRepositoryMapper;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.EVENT_REPOSITORY_ACCESS_DENIED, "User not authenticated");
        }
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.EVENT_REPOSITORY_ACCESS_DENIED, "User not found");
        }
        return user;
    }

    private GitRepositoryMetadataClient selectClient(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Repository URL cannot be empty");
        }
        return gitRepositoryMetadataClients.stream()
                .filter(client -> client.supports(repositoryUrl))
                .findFirst()
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_REPOSITORY_URL, "Unsupported repository URL provider"));
    }

    public SubmissionRepositoryResponse validateRepositoryUrl(String repositoryUrl) {
        GitRepositoryMetadataClient client = selectClient(repositoryUrl);
        RepositoryMetadata metadata = client.fetchPublicMetadata(repositoryUrl);
        return mapMetadataToResponse(metadata);
    }

    public RepositoryMetadata fetchMetadataOutsideTx(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            return null;
        }
        try {
            GitRepositoryMetadataClient client = selectClient(repositoryUrl);
            return client.fetchPublicMetadata(repositoryUrl);
        } catch (Exception e) {
            log.warn("Failed to fetch repository metadata for URL {}: {}", repositoryUrl, e.getMessage());
            return RepositoryMetadata.builder()
                    .provider(RepositoryProvider.GITHUB)
                    .repositoryUrl(repositoryUrl)
                    .build();
        }
    }

    @Transactional
    public SubmissionRepositoryResponse saveOrUpdateSubmissionRepository(UUID submissionId, String repositoryUrl, RepositoryMetadata fetchedMetadata) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            return null;
        }

        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));

        SubmissionRepositoryEntity repoEntity = submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)
                .orElseGet(() -> {
                    SubmissionRepositoryEntity newEntity = new SubmissionRepositoryEntity();
                    newEntity.setSubmission(submission);
                    return newEntity;
                });

        repoEntity.setRepositoryUrl(repositoryUrl);
        submissionRepositoryMapper.applyMetadata(fetchedMetadata, repoEntity);

        repoEntity = submissionRepositoryRepository.save(repoEntity);

        // Synchronize Submissions.RepositoryURL for backward compatibility
        if (!repositoryUrl.equals(submission.getRepositoryUrl())) {
            submission.setRepositoryUrl(repositoryUrl);
            submissionsRepository.save(submission);
        }

        return mapToResponse(repoEntity);
    }

    @Transactional(readOnly = true)
    public void authorizeView(Submissions submission, UUID currentUserId) {
        if (submission.getTeamId() != null) {
            boolean isTeamMember = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(submission.getTeamId(), currentUserId).isPresent();
            if (isTeamMember) return;
        }

        // Event Organizer check
        boolean isOrganizer = eventRepository.findAll().stream()
                .anyMatch(e -> e.getCreatedBy() != null && e.getCreatedBy().getUserId().equals(currentUserId));
        if (isOrganizer) return;

        // Judge check
        boolean isJudge = roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(currentUserId, submission.getRoundId()).isPresent();
        if (isJudge) return;

        throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_ACCESS_DENIED, "Access denied to submission repository metadata");
    }

    @Transactional(readOnly = true)
    public void authorizeResync(Submissions submission, UUID currentUserId) {
        if (submission.getTeamId() != null) {
            boolean isTeamMember = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(submission.getTeamId(), currentUserId).isPresent();
            if (isTeamMember) return;
        }

        boolean isOrganizer = eventRepository.findAll().stream()
                .anyMatch(e -> e.getCreatedBy() != null && e.getCreatedBy().getUserId().equals(currentUserId));
        if (isOrganizer) return;

        throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_MODIFICATION_NOT_ALLOWED, "Only team members or event organizers may resynchronize repository metadata");
    }

    @Transactional(readOnly = true)
    public SubmissionRepositoryResponse getSubmissionRepository(UUID submissionId) {
        User currentUser = getCurrentUser();
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));

        authorizeView(submission, currentUser.getUserId());

        return submissionRepositoryRepository.findBySubmission_SubmissionId(submissionId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public SubmissionRepositoryResponse syncSubmissionRepository(UUID submissionId) {
        User currentUser = getCurrentUser();
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_NOT_FOUND, "Submission not found"));

        authorizeResync(submission, currentUser.getUserId());

        String repoUrl = submission.getRepositoryUrl();
        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.SUBMISSION_REPOSITORY_NOT_FOUND, "Submission has no repository URL attached");
        }

        RepositoryMetadata fetched = fetchMetadataOutsideTx(repoUrl);
        return saveOrUpdateSubmissionRepository(submissionId, repoUrl, fetched);
    }

    public SubmissionRepositoryResponse mapToResponse(SubmissionRepositoryEntity entity) {
        if (entity == null) return null;
        return SubmissionRepositoryResponse.builder()
                .submissionRepositoryId(entity.getSubmissionRepositoryId())
                .submissionId(entity.getSubmission() != null ? entity.getSubmission().getSubmissionId() : null)
                .provider(entity.getProvider() != null ? entity.getProvider().name() : null)
                .externalId(entity.getExternalId())
                .repositoryUrl(entity.getRepositoryUrl())
                .owner(entity.getOwner())
                .repositoryName(entity.getRepositoryName())
                .fullName(entity.getFullName())
                .description(entity.getDescription())
                .visibility(entity.getVisibility())
                .defaultBranch(entity.getDefaultBranch())
                .primaryLanguage(entity.getPrimaryLanguage())
                .repositoryCreatedAt(entity.getRepositoryCreatedAt())
                .repositoryUpdatedAt(entity.getRepositoryUpdatedAt())
                .lastPushedAt(entity.getLastPushedAt())
                .externalUrl(entity.getExternalUrl())
                .lastSyncStatus(entity.getLastSyncStatus() != null ? entity.getLastSyncStatus().name() : null)
                .lastSynchronizedAt(entity.getLastSynchronizedAt())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SubmissionRepositoryResponse mapMetadataToResponse(RepositoryMetadata metadata) {
        if (metadata == null) return null;
        return SubmissionRepositoryResponse.builder()
                .provider(metadata.getProvider() != null ? metadata.getProvider().name() : "GITHUB")
                .externalId(metadata.getExternalRepositoryId())
                .repositoryUrl(metadata.getRepositoryUrl())
                .externalUrl(metadata.getExternalUrl())
                .owner(metadata.getOwner())
                .repositoryName(metadata.getRepositoryName())
                .fullName(metadata.getFullName())
                .description(metadata.getDescription())
                .visibility(metadata.getVisibility())
                .defaultBranch(metadata.getDefaultBranch())
                .primaryLanguage(metadata.getPrimaryLanguage())
                .repositoryCreatedAt(metadata.getRepositoryCreatedAt())
                .repositoryUpdatedAt(metadata.getRepositoryUpdatedAt())
                .lastPushedAt(metadata.getLastPushedAt())
                .lastSyncStatus(RepositorySyncStatus.SUCCESS.name())
                .build();
    }
}
