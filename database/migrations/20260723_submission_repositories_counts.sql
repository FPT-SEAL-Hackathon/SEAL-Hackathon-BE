-- Migration: Add star/fork/open-issue counts to SubmissionRepositories
-- Date: 2026-07-23
-- GitHub repository endpoint tra ve stargazers_count/forks_count/open_issues_count;
-- cac cot nay phuc vu hien thi tham khao cho Team/Judge/Organizer (khong dung cham diem).

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'StarCount') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD StarCount INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'ForkCount') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD ForkCount INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.SubmissionRepositories', N'OpenIssuesCount') IS NULL
BEGIN
    ALTER TABLE dbo.SubmissionRepositories ADD OpenIssuesCount INT NULL;
END;
GO
