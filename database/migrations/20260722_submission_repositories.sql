-- Migration: Submission-level repository metadata
-- Date: 2026-07-22

IF OBJECT_ID(N'dbo.SubmissionRepositories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionRepositories (
        SubmissionRepositoryID UNIQUEIDENTIFIER NOT NULL DEFAULT (NEWID()),
        SubmissionID UNIQUEIDENTIFIER NOT NULL,
        Provider VARCHAR(20) NOT NULL,
        ExternalRepositoryID NVARCHAR(100) NULL,
        RepositoryUrl NVARCHAR(500) NOT NULL,
        Owner NVARCHAR(200) NULL,
        RepositoryName NVARCHAR(200) NULL,
        FullName NVARCHAR(400) NULL,
        Description NVARCHAR(MAX) NULL,
        Visibility VARCHAR(20) NULL,
        DefaultBranch NVARCHAR(200) NULL,
        PrimaryLanguage NVARCHAR(100) NULL,
        RepositoryCreatedAt DATETIME2 NULL,
        RepositoryUpdatedAt DATETIME2 NULL,
        LastPushedAt DATETIME2 NULL,
        ExternalUrl NVARCHAR(500) NULL,
        LastSyncStatus VARCHAR(30) NOT NULL DEFAULT ('NOT_SYNCHRONIZED'),
        LastSynchronizedAt DATETIME2 NULL,
        ErrorCode NVARCHAR(100) NULL,
        ErrorMessage NVARCHAR(1000) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT (SYSUTCDATETIME()),
        UpdatedAt DATETIME2 NOT NULL DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT PK_SubmissionRepositories PRIMARY KEY (SubmissionRepositoryID),
        CONSTRAINT FK_SubmissionRepositories_Submissions FOREIGN KEY (SubmissionID) REFERENCES dbo.Submissions(SubmissionID),
        CONSTRAINT UQ_SubmissionRepositories_SubmissionID UNIQUE (SubmissionID),
        CONSTRAINT CK_SubmissionRepositories_Provider CHECK (Provider IN ('GITHUB', 'GITLAB', 'UNKNOWN')),
        CONSTRAINT CK_SubmissionRepositories_LastSyncStatus CHECK (LastSyncStatus IN ('NOT_SYNCHRONIZED', 'RUNNING', 'SUCCESS', 'FAILED', 'PARTIAL_SUCCESS'))
    );

    CREATE NONCLUSTERED INDEX IX_SubmissionRepositories_Provider_ExternalID 
        ON dbo.SubmissionRepositories (Provider, ExternalRepositoryID);
END;
GO

-- Idempotent Backfill Script for existing Submissions
IF OBJECT_ID(N'dbo.SubmissionRepositories', N'U') IS NOT NULL
BEGIN
    INSERT INTO dbo.SubmissionRepositories (
        SubmissionRepositoryID,
        SubmissionID,
        Provider,
        RepositoryUrl,
        ExternalUrl,
        LastSyncStatus,
        CreatedAt,
        UpdatedAt
    )
    SELECT 
        NEWID(),
        s.SubmissionID,
        CASE 
            WHEN LOWER(LTRIM(RTRIM(s.RepositoryURL))) LIKE 'https://github.com/%' OR LOWER(LTRIM(RTRIM(s.RepositoryURL))) LIKE 'http://github.com/%' THEN 'GITHUB'
            WHEN LOWER(LTRIM(RTRIM(s.RepositoryURL))) LIKE 'https://gitlab.com/%' OR LOWER(LTRIM(RTRIM(s.RepositoryURL))) LIKE 'http://gitlab.com/%' THEN 'GITLAB'
            ELSE 'UNKNOWN'
        END AS Provider,
        LTRIM(RTRIM(s.RepositoryURL)),
        LTRIM(RTRIM(s.RepositoryURL)),
        'NOT_SYNCHRONIZED',
        SYSUTCDATETIME(),
        SYSUTCDATETIME()
    FROM dbo.Submissions s
    LEFT JOIN dbo.SubmissionRepositories sr ON s.SubmissionID = sr.SubmissionID
    WHERE s.RepositoryURL IS NOT NULL 
      AND LTRIM(RTRIM(s.RepositoryURL)) <> ''
      AND sr.SubmissionRepositoryID IS NULL;
END;
GO

/* 
Development-Only Rollback Script (Do NOT run in production as it destroys submission repository metadata):
IF OBJECT_ID(N'dbo.SubmissionRepositories', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.SubmissionRepositories;
END;
GO
*/
