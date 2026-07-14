IF COL_LENGTH('dbo.Submissions', 'IsSampleSubmission') IS NULL
BEGIN
    ALTER TABLE dbo.Submissions
        ADD IsSampleSubmission BIT NOT NULL
            CONSTRAINT DF_Submissions_IsSampleSubmission DEFAULT 0;
END;
GO

IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = 'UQ_Submissions_Team_Round'
      AND parent_object_id = OBJECT_ID('dbo.Submissions')
)
BEGIN
    ALTER TABLE dbo.Submissions
        DROP CONSTRAINT UQ_Submissions_Team_Round;
END;
GO

IF EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UQ_Submissions_Team_Round'
      AND object_id = OBJECT_ID('dbo.Submissions')
)
BEGIN
    DROP INDEX UQ_Submissions_Team_Round ON dbo.Submissions;
END;
GO

ALTER TABLE dbo.Submissions
    ALTER COLUMN TeamID UNIQUEIDENTIFIER NULL;
GO

CREATE UNIQUE INDEX UQ_Submissions_Team_Round
    ON dbo.Submissions(TeamID, RoundID)
    WHERE IsSampleSubmission = 0 AND TeamID IS NOT NULL;
GO
