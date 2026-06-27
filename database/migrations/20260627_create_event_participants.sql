IF OBJECT_ID(N'dbo.ParticipantStatus', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ParticipantStatus (
        StatusID UNIQUEIDENTIFIER NOT NULL,
        StatusName NVARCHAR(50) NOT NULL,
        CONSTRAINT PK_ParticipantStatus PRIMARY KEY (StatusID),
        CONSTRAINT UQ_ParticipantStatus_Name UNIQUE (StatusName),
        CONSTRAINT CK_ParticipantStatus_Name CHECK (
            StatusName IN (
                N'PENDING_APPROVAL',
                N'ACTIVE',
                N'REJECTED',
                N'SUSPENDED',
                N'TEMPORARY',
                N'UNVERIFIED'
            )
        )
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'PENDING_APPROVAL')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000001', N'PENDING_APPROVAL');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'ACTIVE')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000002', N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'REJECTED')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000003', N'REJECTED');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'SUSPENDED')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000004', N'SUSPENDED');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'TEMPORARY')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000005', N'TEMPORARY');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'UNVERIFIED')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000006', N'UNVERIFIED');
GO

IF OBJECT_ID(N'dbo.EventParticipants', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.EventParticipants (
        EventParticipantID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT DF_EventParticipants_ID DEFAULT NEWID(),
        EventID UNIQUEIDENTIFIER NOT NULL,
        UserID UNIQUEIDENTIFIER NOT NULL,
        ParticipantStatusID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT DF_EventParticipants_Status
            DEFAULT '80000000-0000-0000-0000-000000000001',
        AppliedAt DATETIME2 NOT NULL
            CONSTRAINT DF_EventParticipants_AppliedAt DEFAULT SYSUTCDATETIME(),
        ApprovedAt DATETIME2 NULL,
        ApprovedBy UNIQUEIDENTIFIER NULL,
        RejectedReason NVARCHAR(1000) NULL,
        CreatedAt DATETIME2 NOT NULL
            CONSTRAINT DF_EventParticipants_CreatedAt DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL
            CONSTRAINT DF_EventParticipants_UpdatedAt DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_EventParticipants PRIMARY KEY (EventParticipantID),
        CONSTRAINT UQ_EventParticipants_Event_User UNIQUE (EventID, UserID),
        CONSTRAINT FK_EventParticipants_Event
            FOREIGN KEY (EventID) REFERENCES dbo.Events(EventID),
        CONSTRAINT FK_EventParticipants_User
            FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID),
        CONSTRAINT FK_EventParticipants_Status
            FOREIGN KEY (ParticipantStatusID) REFERENCES dbo.ParticipantStatus(StatusID),
        CONSTRAINT FK_EventParticipants_ApprovedBy
            FOREIGN KEY (ApprovedBy) REFERENCES dbo.Users(UserID)
    );

    CREATE INDEX IX_EventParticipants_Event_Status
        ON dbo.EventParticipants(EventID, ParticipantStatusID);

    CREATE INDEX IX_EventParticipants_User_Status
        ON dbo.EventParticipants(UserID, ParticipantStatusID);

    CREATE INDEX IX_EventParticipants_Status_AppliedAt
        ON dbo.EventParticipants(ParticipantStatusID, AppliedAt DESC);

    CREATE INDEX IX_EventParticipants_ApprovedBy
        ON dbo.EventParticipants(ApprovedBy)
        WHERE ApprovedBy IS NOT NULL;
END;
GO
