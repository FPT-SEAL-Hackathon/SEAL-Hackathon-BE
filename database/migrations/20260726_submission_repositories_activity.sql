-- Migration: Add development-activity + version-pin columns to SubmissionRepositories
-- Date: 2026-07-26
-- Phuc vu cham diem/quan ly (KHONG dung tinh diem):
--   Activity: LanguagesJson, ContributorCount, TopContributorsJson, CommitCount, LastCommitSha
--   Ghim phien ban: PinnedCommitSha, PinnedAt, PinnedByUserID
-- Du lieu activity lay best-effort qua GitHub API phu (languages/contributors/commits).

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'LanguagesJson') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD LanguagesJson NVARCHAR(MAX) NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'ContributorCount') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD ContributorCount INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'TopContributorsJson') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD TopContributorsJson NVARCHAR(MAX) NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'CommitCount') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD CommitCount INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'LastCommitSha') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD LastCommitSha VARCHAR(64) NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'PinnedCommitSha') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD PinnedCommitSha VARCHAR(64) NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'PinnedAt') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD PinnedAt DATETIME2(7) NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'PinnedByUserID') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD PinnedByUserID UNIQUEIDENTIFIER NULL;
END;
GO
