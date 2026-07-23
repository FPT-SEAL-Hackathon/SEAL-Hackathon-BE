package com.fpt.swp.sealhackathonbe.integration.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mot dong trong bang "Submission Repositories" cua Organizer:
 * ngu canh submission (team/category/round) + metadata repository (neu co).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSubmissionRepositoryItemResponse {
    private UUID submissionId;
    private UUID teamId;
    private String teamName;
    private String categoryName;
    private String roundName;
    private LocalDateTime submittedAt;
    private LocalDateTime submissionDeadline;

    // URL tho tren Submissions (fallback khi chua co ban ghi metadata).
    private String repositoryUrl;

    // Metadata co cau truc tu SubmissionRepositories; null = submission chua co repository.
    private SubmissionRepositoryResponse repository;

    /**
     * Chi la chi bao trung lap de Organizer REVIEW thu cong, khong phai ket luan vi pham:
     * true khi lastPushedAt > submissionDeadline; null khi thieu mot trong hai moc thoi gian.
     */
    private Boolean lastPushAfterDeadline;
}
