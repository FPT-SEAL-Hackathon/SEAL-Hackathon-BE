package com.fpt.swp.sealhackathonbe.integration.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRepositoryResponse {
    private UUID submissionRepositoryId;
    private UUID submissionId;
    private String provider;
    private String externalId;
    private String repositoryUrl;
    private String owner;
    private String repositoryName;
    private String fullName;
    private String description;
    private String visibility;
    private String defaultBranch;
    private String primaryLanguage;
    private LocalDateTime repositoryCreatedAt;
    private LocalDateTime repositoryUpdatedAt;
    private LocalDateTime lastPushedAt;
    private String externalUrl;
    private String lastSyncStatus;
    private LocalDateTime lastSynchronizedAt;
    private String errorCode;
    private String errorMessage;
    private Integer starCount;
    private Integer forkCount;
    private Integer openIssuesCount;
    // Activity (co the null neu call phu that bai)
    private String languagesJson;
    private Integer contributorCount;
    private String topContributorsJson;
    private Integer commitCount;
    private String lastCommitSha;
    // Ghim phien ban de cham
    private String pinnedCommitSha;
    private LocalDateTime pinnedAt;
    private UUID pinnedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
