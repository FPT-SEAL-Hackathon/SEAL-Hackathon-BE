package com.fpt.swp.sealhackathonbe.submission.entity;

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
@Table(
        name = "Submissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UQ_Submissions_Team_Round",
                        columnNames = {"TeamID", "RoundID"}
                )
        }
)
public class Submissions {
    // Mapping voi bang Submissions trong database.
    // Moi team chi co mot submission trong mot round; rule nay duoc enforce boi UQ_Submissions_Team_Round.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SubmissionID")
    private UUID submissionId;

    @Column(name = "TeamID", nullable = false)
    private UUID teamId;

    @Column(name = "RoundID", nullable = false)
    private UUID roundId;

    @Column(name = "SubmissionStatusID", nullable = false)
    private UUID submissionStatusId;

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

    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;
}
