package com.fpt.swp.sealhackathonbe.integration.repository.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Ket qua cua mot lan goi GitHub lay metadata, thuc hien NGOAI transaction DB.
 * - Thanh cong: metadata != null, errorCode/errorMessage null.
 * - That bai: metadata null, errorCode/errorMessage la thong tin an toan (khong chua
 *   raw response cua GitHub) de persist vao SubmissionRepositories cho Organizer xem.
 * Tach rieng khoi RepositoryMetadata de service persist duoc ca trang thai FAILED
 * thay vi nuot loi nhu truoc.
 */
@Getter
@Builder
public class RepositoryMetadataFetchResult {

    private final RepositoryMetadata metadata;
    private final String errorCode;
    private final String errorMessage;

    public boolean isSuccess() {
        return metadata != null;
    }

    public static RepositoryMetadataFetchResult success(RepositoryMetadata metadata) {
        return RepositoryMetadataFetchResult.builder().metadata(metadata).build();
    }

    public static RepositoryMetadataFetchResult failure(String errorCode, String errorMessage) {
        return RepositoryMetadataFetchResult.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
