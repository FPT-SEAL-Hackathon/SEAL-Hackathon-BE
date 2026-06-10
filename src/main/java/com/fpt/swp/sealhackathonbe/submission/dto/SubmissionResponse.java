package com.fpt.swp.sealhackathonbe.submission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubmissionResponse {
    // DTO dau ra cho tat ca API doc submission.
    // Entity -> SubmissionMapper -> response nay, de controller khong tra truc tiep object persistence.
    private UUID submissionId;
    private UUID teamId;
    private UUID roundId;
    private UUID submissionStatusId;

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
}
