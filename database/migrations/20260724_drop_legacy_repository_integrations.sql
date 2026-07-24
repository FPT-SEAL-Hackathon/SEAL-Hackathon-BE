-- Migration: Drop legacy Event-level Repository Integration (PAT-based)
-- Date: 2026-07-24
-- Tinh nang legacy (Organizer nhap GitHub PAT de dong bo repo/issues cap Event) da bi
-- go bo hoan toan, thay bang luong Submission-level (public metadata, khong can PAT).
-- Drop theo thu tu con -> cha de khong vi pham khoa ngoai:
--   RepositoryIssues, RepositorySyncLogs -> Repositories -> RepositoryIntegrations.
-- Cac guard IF OBJECT_ID giup script idempotent va an toan tren DB moi (no-op).

IF OBJECT_ID(N'dbo.RepositoryIssues', N'U') IS NOT NULL
    DROP TABLE dbo.RepositoryIssues;
GO

IF OBJECT_ID(N'dbo.RepositorySyncLogs', N'U') IS NOT NULL
    DROP TABLE dbo.RepositorySyncLogs;
GO

IF OBJECT_ID(N'dbo.Repositories', N'U') IS NOT NULL
    DROP TABLE dbo.Repositories;
GO

IF OBJECT_ID(N'dbo.RepositoryIntegrations', N'U') IS NOT NULL
    DROP TABLE dbo.RepositoryIntegrations;
GO
