package com.fpt.swp.sealhackathonbe.integration.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "SubmissionRepositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRepositoryEntity {

    @Id
    @Column(name = "SubmissionRepositoryID")
    private UUID submissionRepositoryId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SubmissionID", nullable = false, unique = true)
    private Submissions submission;

    @Enumerated(EnumType.STRING)
    @Column(name = "Provider", nullable = false, length = 20)
    private RepositoryProvider provider; // GITHUB, GITLAB, UNKNOWN

    @Column(name = "ExternalRepositoryID", length = 100)
    private String externalId;

    @Column(name = "RepositoryUrl", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "Owner", length = 200)
    private String owner;

    @Column(name = "RepositoryName", length = 200)
    private String repositoryName;

    @Column(name = "FullName", length = 400)
    private String fullName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "Visibility", length = 20)
    private String visibility; // PUBLIC, PRIVATE

    @Column(name = "DefaultBranch", length = 200)
    private String defaultBranch;

    @Column(name = "PrimaryLanguage", length = 100)
    private String primaryLanguage;

    @Column(name = "RepositoryCreatedAt")
    private LocalDateTime repositoryCreatedAt;

    @Column(name = "RepositoryUpdatedAt")
    private LocalDateTime repositoryUpdatedAt;

    @Column(name = "LastPushedAt")
    private LocalDateTime lastPushedAt;

    @Column(name = "ExternalUrl", length = 500)
    private String externalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "LastSyncStatus", nullable = false, length = 30)
    private RepositorySyncStatus lastSyncStatus; // NOT_SYNCHRONIZED, RUNNING, SUCCESS, FAILED, PARTIAL_SUCCESS

    @Column(name = "LastSynchronizedAt")
    private LocalDateTime lastSynchronizedAt;

    @Column(name = "ErrorCode", length = 100)
    private String errorCode;

    @Column(name = "ErrorMessage", length = 1000)
    private String errorMessage;

    @Column(name = "StarCount")
    private Integer starCount;

    @Column(name = "ForkCount")
    private Integer forkCount;

    @Column(name = "OpenIssuesCount")
    private Integer openIssuesCount;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (submissionRepositoryId == null) {
            submissionRepositoryId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (lastSyncStatus == null) {
            lastSyncStatus = RepositorySyncStatus.NOT_SYNCHRONIZED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
