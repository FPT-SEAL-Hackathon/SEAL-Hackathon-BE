package com.fpt.swp.sealhackathonbe.integration.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RepositoryIssues", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Repository_ExternalIssue", columnNames = {"RepositoryID", "ExternalIssueID"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IssueID")
    private UUID issueId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RepositoryID", nullable = false)
    private RepositoryEntity repository;

    @Column(name = "ExternalIssueID", nullable = false)
    private String externalId;

    @Column(name = "IssueNumber", nullable = false)
    private Integer number;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Body", columnDefinition = "NVARCHAR(MAX)")
    private String body;

    @Column(name = "State", nullable = false)
    private String state; // 'open', 'closed'

    @Column(name = "Url")
    private String url;

    @Column(name = "AuthorUsername")
    private String authorUsername;

    @Column(name = "AssigneeUsername")
    private String assigneeUsername;

    @Column(name = "Labels", columnDefinition = "NVARCHAR(MAX)")
    private String labels;

    @Column(name = "Milestone")
    private String milestone;

    @Column(name = "CommentCount", nullable = false)
    private Integer commentCount;

    @Column(name = "ExternalCreatedAt", nullable = false)
    private LocalDateTime externalCreatedAt;

    @Column(name = "ExternalUpdatedAt", nullable = false)
    private LocalDateTime externalUpdatedAt;

    @Column(name = "ExternalClosedAt")
    private LocalDateTime externalClosedAt;

    @Column(name = "LastSynchronizedAt", nullable = false)
    private LocalDateTime lastSynchronizedAt;

    @PrePersist
    protected void onCreate() {
        if (commentCount == null) commentCount = 0;
        lastSynchronizedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastSynchronizedAt = LocalDateTime.now();
    }
}
