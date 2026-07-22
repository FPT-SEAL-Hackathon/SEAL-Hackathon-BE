package com.fpt.swp.sealhackathonbe.integration.repository.mapper;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncStatus;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.SubmissionRepositoryEntity;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class SubmissionRepositoryMapper {

    public void applyMetadata(RepositoryMetadata metadata, SubmissionRepositoryEntity entity) {
        if (entity == null) {
            return;
        }

        if (metadata != null) {
            entity.setProvider(metadata.getProvider() != null ? metadata.getProvider() : RepositoryProvider.GITHUB);
            entity.setExternalId(metadata.getExternalRepositoryId());
            if (metadata.getRepositoryUrl() != null && !metadata.getRepositoryUrl().trim().isEmpty()) {
                entity.setRepositoryUrl(metadata.getRepositoryUrl());
            }
            entity.setExternalUrl(metadata.getExternalUrl());
            entity.setOwner(metadata.getOwner());
            entity.setRepositoryName(metadata.getRepositoryName());
            entity.setFullName(metadata.getFullName());
            entity.setDescription(metadata.getDescription());
            entity.setVisibility(metadata.getVisibility());
            entity.setDefaultBranch(metadata.getDefaultBranch());
            entity.setPrimaryLanguage(metadata.getPrimaryLanguage());
            entity.setRepositoryCreatedAt(metadata.getRepositoryCreatedAt());
            entity.setRepositoryUpdatedAt(metadata.getRepositoryUpdatedAt());
            entity.setLastPushedAt(metadata.getLastPushedAt());
            entity.setLastSyncStatus(RepositorySyncStatus.SUCCESS);
            entity.setLastSynchronizedAt(LocalDateTime.now(ZoneOffset.UTC));
            entity.setErrorCode(null);
            entity.setErrorMessage(null);
        } else {
            entity.setLastSyncStatus(RepositorySyncStatus.NOT_SYNCHRONIZED);
        }
    }

    public SubmissionRepositoryEntity toEntity(RepositoryMetadata metadata, Submissions submission) {
        SubmissionRepositoryEntity entity = SubmissionRepositoryEntity.builder()
                .submission(submission)
                .repositoryUrl(submission != null ? submission.getRepositoryUrl() : null)
                .build();
        applyMetadata(metadata, entity);
        return entity;
    }
}
