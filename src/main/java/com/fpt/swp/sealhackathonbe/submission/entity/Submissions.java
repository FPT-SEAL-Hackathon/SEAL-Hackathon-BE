package com.fpt.swp.sealhackathonbe.submission.entity;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Submissions")
public class Submissions {
    // Mapping voi bang Submissions trong database.
    // Moi team chi co mot submission that trong mot round; sample submission co TeamID null va IsSampleSubmission = true.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SubmissionID")
    private UUID submissionId;

    @Column(name = "TeamID")
    private UUID teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", insertable = false, updatable = false)
    private Teams team;

    @Column(name = "RoundID", nullable = false)
    private UUID roundId;

    @Column(name = "SubmissionStatusID", nullable = false)
    private UUID submissionStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmissionStatusID", nullable = false, insertable = false, updatable = false)
    private SubmissionStatus submissionStatus;

    @Column(name = "RepositoryURL", length = 500)
    private String repositoryUrl;

    @Column(name = "DemoURL", length = 500)
    private String demoUrl;

    @Column(name = "ReportURL", length = 500)
    private String reportUrl;

    @Column(name = "SlideURL", length = 500)
    private String slideUrl;

    @Column(name = "RepoMetadataJSON", columnDefinition = "NVARCHAR(MAX)")
    private String repoMetadataJson;

    @Column(name = "RepoLastCommitAt")
    private LocalDateTime repoLastCommitAt;

    @Column(name = "RepoStarCount")
    private Integer repoStarCount;

    @Column(name = "RepoForkCount")
    private Integer repoForkCount;

    @Column(name = "SubmittedAt")
    private LocalDateTime submittedAt;

    @Column(name = "LastUpdatedAt", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "SubmittedByUserID", nullable = false)
    private UUID submittedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmittedByUserID", nullable = false, insertable = false, updatable = false)
    private User submittedByUser;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "IsScoreApproved", nullable = false)
    private Boolean isScoreApproved = false;

    @Column(name = "IsSampleSubmission", nullable = false)
    private Boolean isSampleSubmission = false;
}
