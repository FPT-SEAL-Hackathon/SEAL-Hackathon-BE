package com.fpt.swp.sealhackathonbe.submission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubmissionHistoryResponse {
    // DTO tra ve tung version snapshot cua submission cho member/admin xem lich su nop bai.
    private UUID submissionHistoryId;
    private UUID submissionId;
    private Integer versionNumber;
    private UUID teamId;
    private String teamName;
    private UUID roundId;
    private UUID submissionStatusId;
    private String submissionStatusName;

    private String repositoryUrl;
    private String demoUrl;
    private String reportUrl;
    private String slideUrl;

    private String repoMetadataJson;
    private LocalDateTime repoLastCommitAt;
    private Integer repoStarCount;
    private Integer repoForkCount;

    private LocalDateTime submittedAt;
    private LocalDateTime lastUpdatedAt;
    private UUID submittedByUserId;
    private String notes;
    private Boolean isScoreApproved;
    private Boolean isSampleSubmission;
    // Thoi diem he thong ghi snapshot, khac voi submittedAt cua bai nop goc.
    private LocalDateTime snapshotCreatedAt;
}
