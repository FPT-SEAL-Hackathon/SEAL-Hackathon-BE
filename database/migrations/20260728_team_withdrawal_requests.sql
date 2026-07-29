IF NOT EXISTS (SELECT 1 FROM dbo.TeamStatus WHERE StatusID = '60000000-0000-0000-0000-000000000004')
BEGIN
    INSERT INTO dbo.TeamStatus (StatusID, StatusName)
    VALUES ('60000000-0000-0000-0000-000000000004', N'Withdrawn');
END;

IF OBJECT_ID(N'dbo.TeamWithdrawalRequests', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TeamWithdrawalRequests (
        RequestID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT PK_TeamWithdrawalRequests PRIMARY KEY
            CONSTRAINT DF_TeamWithdrawalRequests_RequestID DEFAULT NEWID(),
        TeamID UNIQUEIDENTIFIER NOT NULL,
        RequestedByID UNIQUEIDENTIFIER NOT NULL,
        Reason NVARCHAR(1000) NOT NULL,
        RequestStatus NVARCHAR(20) NOT NULL,
        RequestedAt DATETIME2 NOT NULL
            CONSTRAINT DF_TeamWithdrawalRequests_RequestedAt DEFAULT SYSDATETIME(),
        RespondedAt DATETIME2 NULL,
        RespondedByID UNIQUEIDENTIFIER NULL,
        ResponseNote NVARCHAR(500) NULL,
        CONSTRAINT FK_TeamWithdrawalRequests_Teams
            FOREIGN KEY (TeamID) REFERENCES dbo.Teams(TeamID),
        CONSTRAINT FK_TeamWithdrawalRequests_RequestedBy
            FOREIGN KEY (RequestedByID) REFERENCES dbo.Users(UserID),
        CONSTRAINT FK_TeamWithdrawalRequests_RespondedBy
            FOREIGN KEY (RespondedByID) REFERENCES dbo.Users(UserID),
        CONSTRAINT CK_TeamWithdrawalRequests_Status
            CHECK (RequestStatus IN (N'PENDING', N'APPROVED', N'REJECTED'))
    );
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UX_TeamWithdrawalRequests_OnePendingPerTeam'
      AND object_id = OBJECT_ID(N'dbo.TeamWithdrawalRequests')
)
BEGIN
    CREATE UNIQUE INDEX UX_TeamWithdrawalRequests_OnePendingPerTeam
    ON dbo.TeamWithdrawalRequests(TeamID)
    WHERE RequestStatus = N'PENDING';
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_TeamWithdrawalRequests_EventStatus'
      AND object_id = OBJECT_ID(N'dbo.TeamWithdrawalRequests')
)
BEGIN
    CREATE INDEX IX_TeamWithdrawalRequests_EventStatus
    ON dbo.TeamWithdrawalRequests(RequestStatus, TeamID);
END;
