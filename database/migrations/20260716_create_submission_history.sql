IF OBJECT_ID(N'dbo.SubmissionHistory', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionHistory
    (
        SubmissionHistoryID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT DF_SubmissionHistory_ID DEFAULT NEWID(),
        SubmissionID UNIQUEIDENTIFIER NOT NULL,
        VersionNumber INT NOT NULL,
        TeamID UNIQUEIDENTIFIER NULL,
        RoundID UNIQUEIDENTIFIER NOT NULL,
        SubmissionStatusID UNIQUEIDENTIFIER NOT NULL,
        RepositoryURL NVARCHAR(500) NULL,
        DemoURL NVARCHAR(500) NULL,
        ReportURL NVARCHAR(500) NULL,
        SlideURL NVARCHAR(500) NULL,
        RepoMetadataJSON NVARCHAR(MAX) NULL,
        RepoLastCommitAt DATETIME2(7) NULL,
        RepoStarCount INT NULL,
        RepoForkCount INT NULL,
        SubmittedAt DATETIME2(7) NULL,
        LastUpdatedAt DATETIME2(7) NOT NULL,
        SubmittedByUserID UNIQUEIDENTIFIER NOT NULL,
        Notes NVARCHAR(MAX) NULL,
        IsScoreApproved BIT NOT NULL
            CONSTRAINT DF_SubmissionHistory_IsScoreApproved DEFAULT 0,
        IsSampleSubmission BIT NOT NULL
            CONSTRAINT DF_SubmissionHistory_IsSampleSubmission DEFAULT 0,
        SnapshotCreatedAt DATETIME2(7) NOT NULL
            CONSTRAINT DF_SubmissionHistory_SnapshotCreatedAt DEFAULT GETUTCDATE(),

        CONSTRAINT PK_SubmissionHistory PRIMARY KEY (SubmissionHistoryID),
        CONSTRAINT UQ_SubmissionHistory_Submission_Version UNIQUE (SubmissionID, VersionNumber),
        CONSTRAINT FK_SubmissionHistory_Submissions FOREIGN KEY (SubmissionID)
            REFERENCES dbo.Submissions (SubmissionID),
        CONSTRAINT FK_SubmissionHistory_Teams FOREIGN KEY (TeamID)
            REFERENCES dbo.Teams (TeamID),
        CONSTRAINT FK_SubmissionHistory_Rounds FOREIGN KEY (RoundID)
            REFERENCES dbo.Rounds (RoundID),
        CONSTRAINT FK_SubmissionHistory_SubmissionStatus FOREIGN KEY (SubmissionStatusID)
            REFERENCES dbo.SubmissionStatus (StatusID),
        CONSTRAINT FK_SubmissionHistory_SubmittedBy FOREIGN KEY (SubmittedByUserID)
            REFERENCES dbo.Users (UserID)
    );

    CREATE INDEX IX_SubmissionHistory_SubmissionID_Version
        ON dbo.SubmissionHistory (SubmissionID, VersionNumber DESC);

    CREATE INDEX IX_SubmissionHistory_Team_Round_Version
        ON dbo.SubmissionHistory (TeamID, RoundID, VersionNumber DESC);
END
GO

INSERT INTO dbo.SubmissionHistory
(
    SubmissionID,
    VersionNumber,
    TeamID,
    RoundID,
    SubmissionStatusID,
    RepositoryURL,
    DemoURL,
    ReportURL,
    SlideURL,
    RepoMetadataJSON,
    RepoLastCommitAt,
    RepoStarCount,
    RepoForkCount,
    SubmittedAt,
    LastUpdatedAt,
    SubmittedByUserID,
    Notes,
    IsScoreApproved,
    IsSampleSubmission,
    SnapshotCreatedAt
)
SELECT
    s.SubmissionID,
    1,
    s.TeamID,
    s.RoundID,
    s.SubmissionStatusID,
    s.RepositoryURL,
    s.DemoURL,
    s.ReportURL,
    s.SlideURL,
    s.RepoMetadataJSON,
    s.RepoLastCommitAt,
    s.RepoStarCount,
    s.RepoForkCount,
    s.SubmittedAt,
    s.LastUpdatedAt,
    s.SubmittedByUserID,
    s.Notes,
    s.IsScoreApproved,
    s.IsSampleSubmission,
    GETUTCDATE()
FROM dbo.Submissions s
WHERE NOT EXISTS
(
    SELECT 1
    FROM dbo.SubmissionHistory h
    WHERE h.SubmissionID = s.SubmissionID
      AND h.VersionNumber = 1
);
GO
