package com.fpt.swp.sealhackathonbe.integration.repository.mapper;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadataFetchResult;
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
            entity.setStarCount(metadata.getStarCount());
            entity.setForkCount(metadata.getForkCount());
            entity.setOpenIssuesCount(metadata.getOpenIssuesCount());
            entity.setLastSyncStatus(RepositorySyncStatus.SUCCESS);
            entity.setLastSynchronizedAt(LocalDateTime.now(ZoneOffset.UTC));
            // Sync thanh cong phai xoa loi cua lan sync truoc de Organizer khong thay loi cu.
            entity.setErrorCode(null);
            entity.setErrorMessage(null);
        } else {
            entity.setLastSyncStatus(RepositorySyncStatus.NOT_SYNCHRONIZED);
        }
    }

    /**
     * Ap ket qua fetch (co the that bai) vao entity:
     * - Thanh cong: ghi de metadata + SUCCESS + xoa loi cu.
     * - That bai: GIU nguyen metadata cu (neu co) de nguoi xem van thay lan sync tot
     *   gan nhat, chi cap nhat trang thai FAILED + errorCode/errorMessage an toan.
     */
    public void applyFetchResult(RepositoryMetadataFetchResult fetchResult, SubmissionRepositoryEntity entity) {
        if (entity == null) {
            return;
        }
        if (fetchResult == null) {
            entity.setLastSyncStatus(RepositorySyncStatus.NOT_SYNCHRONIZED);
            return;
        }
        if (fetchResult.isSuccess()) {
            applyMetadata(fetchResult.getMetadata(), entity);
            return;
        }
        entity.setLastSyncStatus(RepositorySyncStatus.FAILED);
        entity.setLastSynchronizedAt(LocalDateTime.now(ZoneOffset.UTC));
        entity.setErrorCode(truncate(fetchResult.getErrorCode(), 100));
        entity.setErrorMessage(truncate(fetchResult.getErrorMessage(), 1000));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
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
