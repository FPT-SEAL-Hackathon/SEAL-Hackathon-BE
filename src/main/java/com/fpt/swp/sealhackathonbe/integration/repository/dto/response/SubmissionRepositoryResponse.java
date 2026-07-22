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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
