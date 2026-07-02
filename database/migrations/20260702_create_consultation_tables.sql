IF OBJECT_ID(N'dbo.ConsultationRequests', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ConsultationRequests (
        RequestID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT DF_ConsultationRequests_RequestID DEFAULT NEWID()
            CONSTRAINT PK_ConsultationRequests PRIMARY KEY,
        EventID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationRequests_Events REFERENCES dbo.Events(EventID),
        CategoryID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationRequests_Categories REFERENCES dbo.Categories(CategoryID),
        TeamID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationRequests_Teams REFERENCES dbo.Teams(TeamID),
        MentorUserID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationRequests_Mentor REFERENCES dbo.Users(UserID),
        CreatedByUserID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationRequests_CreatedBy REFERENCES dbo.Users(UserID),
        Title NVARCHAR(150) NOT NULL,
        Description NVARCHAR(MAX) NOT NULL,
        Priority NVARCHAR(20) NOT NULL,
        Status NVARCHAR(20) NOT NULL
            CONSTRAINT DF_ConsultationRequests_Status DEFAULT N'PENDING',
        CreatedAt DATETIME2 NOT NULL
            CONSTRAINT DF_ConsultationRequests_CreatedAt DEFAULT GETUTCDATE(),
        UpdatedAt DATETIME2 NOT NULL
            CONSTRAINT DF_ConsultationRequests_UpdatedAt DEFAULT GETUTCDATE(),
        ClosedAt DATETIME2 NULL,
        CONSTRAINT CK_ConsultationRequests_Priority CHECK (Priority IN (N'LOW', N'MEDIUM', N'HIGH', N'URGENT')),
        CONSTRAINT CK_ConsultationRequests_Status CHECK (Status IN (N'PENDING', N'ACCEPTED', N'IN_PROGRESS', N'RESOLVED', N'REJECTED', N'CANCELLED'))
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_ConsultationRequests_Mentor' AND object_id = OBJECT_ID(N'dbo.ConsultationRequests'))
    CREATE NONCLUSTERED INDEX IX_ConsultationRequests_Mentor ON dbo.ConsultationRequests(MentorUserID, CreatedAt DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_ConsultationRequests_Team' AND object_id = OBJECT_ID(N'dbo.ConsultationRequests'))
    CREATE NONCLUSTERED INDEX IX_ConsultationRequests_Team ON dbo.ConsultationRequests(TeamID, CreatedAt DESC);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_ConsultationRequests_CategoryStatus' AND object_id = OBJECT_ID(N'dbo.ConsultationRequests'))
    CREATE NONCLUSTERED INDEX IX_ConsultationRequests_CategoryStatus ON dbo.ConsultationRequests(CategoryID, Status);
GO

IF OBJECT_ID(N'dbo.ConsultationMessages', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ConsultationMessages (
        MessageID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT DF_ConsultationMessages_MessageID DEFAULT NEWID()
            CONSTRAINT PK_ConsultationMessages PRIMARY KEY,
        RequestID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationMessages_Requests REFERENCES dbo.ConsultationRequests(RequestID),
        SenderID UNIQUEIDENTIFIER NOT NULL
            CONSTRAINT FK_ConsultationMessages_Sender REFERENCES dbo.Users(UserID),
        Content NVARCHAR(MAX) NULL,
        AttachmentUrl NVARCHAR(500) NULL,
        CreatedAt DATETIME2 NOT NULL
            CONSTRAINT DF_ConsultationMessages_CreatedAt DEFAULT GETUTCDATE(),
        SeenAt DATETIME2 NULL
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_ConsultationMessages_RequestCreatedAt' AND object_id = OBJECT_ID(N'dbo.ConsultationMessages'))
    CREATE NONCLUSTERED INDEX IX_ConsultationMessages_RequestCreatedAt ON dbo.ConsultationMessages(RequestID, CreatedAt ASC);
GO
