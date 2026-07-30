IF OBJECT_ID(N'dbo.ParticipantStatus', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ParticipantStatus (
        StatusID UNIQUEIDENTIFIER NOT NULL,
        StatusName NVARCHAR(50) NOT NULL,
        CONSTRAINT PK_ParticipantStatus PRIMARY KEY (StatusID),
        CONSTRAINT UQ_ParticipantStatus_Name UNIQUE (StatusName),
        CONSTRAINT CK_ParticipantStatus_Name CHECK (
            StatusName IN (
                N'PENDING',
                N'ACTIVE',
                N'REJECTED',
                N'SUSPENDED',
                N'WITHDRAWN'
            )
        )
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'PENDING')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000001', N'PENDING');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'ACTIVE')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000002', N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'REJECTED')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000003', N'REJECTED');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'SUSPENDED')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000004', N'SUSPENDED');

IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'WITHDRAWN')
    INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
    VALUES ('80000000-0000-0000-0000-000000000005', N'WITHDRAWN');
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
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UQ_Events_EventName_Active'
      AND object_id = OBJECT_ID('dbo.Events')
)
BEGIN
    CREATE UNIQUE INDEX UQ_Events_EventName_Active
        ON dbo.Events(EventName)
        WHERE IsDeleted = 0;
END;
IF OBJECT_ID(N'dbo.ParticipantStatus', N'U') IS NOT NULL
BEGIN
    DECLARE @ConstraintName sysname;
    DECLARE @DropConstraintSql nvarchar(max);

    SELECT @ConstraintName = cc.name
    FROM sys.check_constraints cc
    WHERE cc.parent_object_id = OBJECT_ID(N'dbo.ParticipantStatus')
      AND cc.name = N'CK_ParticipantStatus_Name';

    IF @ConstraintName IS NOT NULL
    BEGIN
        SET @DropConstraintSql = N'ALTER TABLE dbo.ParticipantStatus DROP CONSTRAINT '
            + QUOTENAME(@ConstraintName);
        EXEC sp_executesql @DropConstraintSql;
    END

    IF EXISTS (
        SELECT 1
        FROM dbo.ParticipantStatus
        WHERE StatusID = '80000000-0000-0000-0000-000000000001'
    )
    BEGIN
        UPDATE dbo.ParticipantStatus
        SET StatusName = N'PENDING'
        WHERE StatusID = '80000000-0000-0000-0000-000000000001'
          AND StatusName IN (N'PENDING_APPROVAL', N'Pending Approval', N'PENDING');
    END
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'PENDING')
    BEGIN
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000001', N'PENDING');
    END

    IF EXISTS (
        SELECT 1
        FROM dbo.ParticipantStatus
        WHERE StatusID = '80000000-0000-0000-0000-000000000002'
    )
        UPDATE dbo.ParticipantStatus
        SET StatusName = N'ACTIVE'
        WHERE StatusID = '80000000-0000-0000-0000-000000000002';
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'ACTIVE')
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000002', N'ACTIVE');

    IF EXISTS (
        SELECT 1
        FROM dbo.ParticipantStatus
        WHERE StatusID = '80000000-0000-0000-0000-000000000003'
    )
        UPDATE dbo.ParticipantStatus
        SET StatusName = N'REJECTED'
        WHERE StatusID = '80000000-0000-0000-0000-000000000003';
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'REJECTED')
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000003', N'REJECTED');

    IF EXISTS (
        SELECT 1
        FROM dbo.ParticipantStatus
        WHERE StatusID = '80000000-0000-0000-0000-000000000004'
    )
        UPDATE dbo.ParticipantStatus
        SET StatusName = N'SUSPENDED'
        WHERE StatusID = '80000000-0000-0000-0000-000000000004';
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'SUSPENDED')
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000004', N'SUSPENDED');

    IF EXISTS (
        SELECT 1
        FROM dbo.ParticipantStatus
        WHERE StatusID = '80000000-0000-0000-0000-000000000005'
    )
        UPDATE dbo.ParticipantStatus
        SET StatusName = N'WITHDRAWN'
        WHERE StatusID = '80000000-0000-0000-0000-000000000005';
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'WITHDRAWN')
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000005', N'WITHDRAWN');

    DELETE FROM dbo.ParticipantStatus
    WHERE StatusName IN (N'TEMPORARY', N'UNVERIFIED');

    ALTER TABLE dbo.ParticipantStatus
    ADD CONSTRAINT CK_ParticipantStatus_Name CHECK (
        StatusName IN (
            N'PENDING',
            N'ACTIVE',
            N'REJECTED',
            N'SUSPENDED',
            N'WITHDRAWN'
        )
    );
END;
GO
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
CREATE TABLE TeamMilestones (
    MilestoneID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TeamID UNIQUEIDENTIFIER NOT NULL,
    MentorUserID UNIQUEIDENTIFIER NOT NULL,
    Label NVARCHAR(255) NOT NULL,
    IsDone BIT NOT NULL DEFAULT 0,
    SortOrder INT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_TeamMilestones_Team FOREIGN KEY (TeamID) REFERENCES Teams(TeamID),
    CONSTRAINT FK_TeamMilestones_Mentor FOREIGN KEY (MentorUserID) REFERENCES Users(UserID)
);
GO
/*
 * Allow a user to submit and complete multiple join requests for the same team
 * over time, while still allowing only one PENDING request at a time.
 *
 * TeamMembers keeps UQ_TeamMembers (TeamID, UserID): the application reactivates
 * that row instead of inserting a second membership.
 */
IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = N'UQ_TeamJoinRequests_Pending'
      AND parent_object_id = OBJECT_ID(N'dbo.TeamJoinRequests')
)
    EXEC sys.sp_executesql
        N'ALTER TABLE dbo.TeamJoinRequests DROP CONSTRAINT UQ_TeamJoinRequests_Pending;';

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_TeamJoinRequests_Pending'
      AND object_id = OBJECT_ID(N'dbo.TeamJoinRequests')
)
    EXEC sys.sp_executesql
        N'CREATE UNIQUE INDEX UQ_TeamJoinRequests_Pending
          ON dbo.TeamJoinRequests(TeamID, UserID)
          WHERE RequestStatus = N''PENDING'';';
-- ==========================================================
-- OAuth (Google) + Password Reset foundation
-- Ngày: 2026-07-04
-- Mục đích:
--   1. Cho phép tài khoản LOCAL và tài khoản GOOGLE tồn tại song song
--      với cùng một email (bỏ UNIQUE toàn cục trên Users.Email).
--   2. Thêm cờ LocalLoginEnabled để phân biệt tài khoản có mật khẩu local.
--   3. Tạo bảng UserOAuthAccounts: định danh OAuth bằng (Provider, ProviderUserID),
--      KHÔNG dùng email làm định danh OAuth.
--   4. Tạo bảng PasswordResetTokens: lưu HASH của token reset, không lưu token thô.
--   5. Bỏ 2 CHECK constraint bắt buộc student code theo UserType vì user OAuth
--      TEMPORARY được tạo trước khi hoàn thiện hồ sơ; validation chuyển về
--      tầng service (applyRoleSpecificFields / complete-profile).
-- ==========================================================

-- 1. Bỏ UNIQUE toàn cục trên Users.Email (tên constraint được sinh tự động nên phải tra cứu động)
DECLARE @uqEmail NVARCHAR(256);
SELECT @uqEmail = kc.name
FROM sys.key_constraints kc
         JOIN sys.index_columns ic
              ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
         JOIN sys.columns c
              ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID(N'Users')
  AND kc.type = 'UQ'
  AND c.name = N'Email';

IF @uqEmail IS NOT NULL
    EXEC(N'ALTER TABLE Users DROP CONSTRAINT [' + @uqEmail + N']');
GO

-- Giữ tra cứu email nhanh bằng index KHÔNG unique (IX_Users_Email đã tồn tại thì thôi)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Users_Email' AND object_id = OBJECT_ID(N'Users'))
    CREATE NONCLUSTERED INDEX IX_Users_Email ON Users(Email) WHERE IsDeleted = 0;
GO

-- 2. Thêm cờ LocalLoginEnabled (mặc định 1: mọi tài khoản hiện có đều là local)
IF COL_LENGTH(N'Users', N'LocalLoginEnabled') IS NULL
    ALTER TABLE Users ADD LocalLoginEnabled BIT NOT NULL CONSTRAINT DF_Users_LocalLoginEnabled DEFAULT 1;
GO

-- 3. Bỏ CHECK constraint student-code cứng theo UserType.
--    User OAuth TEMPORARY được gán role tạm EXTERNAL_STUDENT nhưng chưa có
--    student code; ràng buộc nghiệp vụ này được service tầng trên đảm bảo
--    khi accountStatus chuyển sang ACTIVE (complete-profile).
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Users_FPTCode')
    ALTER TABLE Users DROP CONSTRAINT CK_Users_FPTCode;
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Users_ExternalCode')
    ALTER TABLE Users DROP CONSTRAINT CK_Users_ExternalCode;
GO

-- 4. Bảng định danh OAuth: một user có thể có nhiều provider,
--    nhưng một (Provider, ProviderUserID) chỉ thuộc về đúng một user.
IF OBJECT_ID(N'UserOAuthAccounts', N'U') IS NULL
BEGIN
    CREATE TABLE UserOAuthAccounts (
        OAuthAccountID UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID),
        Provider NVARCHAR(30) NOT NULL,
        ProviderUserID NVARCHAR(255) NOT NULL,
        Email NVARCHAR(255) NULL,
        EmailVerified BIT NULL,
        DisplayName NVARCHAR(255) NULL,
        AvatarUrl NVARCHAR(1000) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        UpdatedAt DATETIME2 NULL,
        CONSTRAINT UQ_UserOAuthAccounts_Provider_ProviderUserID UNIQUE (Provider, ProviderUserID),
        CONSTRAINT UQ_UserOAuthAccounts_User_Provider UNIQUE (UserID, Provider)
    );

    CREATE NONCLUSTERED INDEX IX_UserOAuthAccounts_UserID ON UserOAuthAccounts(UserID);
END
GO

-- 5. Bảng token reset mật khẩu: chỉ lưu hash SHA-256, dùng một lần, hết hạn ngắn.
IF OBJECT_ID(N'PasswordResetTokens', N'U') IS NULL
BEGIN
    CREATE TABLE PasswordResetTokens (
        TokenID UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID),
        TokenHash NVARCHAR(128) NOT NULL UNIQUE,
        CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        ExpiresAt DATETIME2 NOT NULL,
        UsedAt DATETIME2 NULL
    );

    CREATE NONCLUSTERED INDEX IX_PasswordResetTokens_UserID ON PasswordResetTokens(UserID);
END
GO
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
IF NOT EXISTS (SELECT 1 FROM dbo.TeamStatus WHERE StatusID = '60000000-0000-0000-0000-000000000005')
BEGIN
    INSERT INTO dbo.TeamStatus (StatusID, StatusName)
    VALUES ('60000000-0000-0000-0000-000000000005', N'Pending');
END;

IF NOT EXISTS (SELECT 1 FROM dbo.TeamStatus WHERE StatusID = '60000000-0000-0000-0000-000000000006')
BEGIN
    INSERT INTO dbo.TeamStatus (StatusID, StatusName)
    VALUES ('60000000-0000-0000-0000-000000000006', N'Rejected');
END;
/*
 * Allow a rejected team name to be reused in the same event while preserving
 * uniqueness for every non-rejected team.
 */
DECLARE @RejectedTeamStatus uniqueidentifier = '60000000-0000-0000-0000-000000000006';

UPDATE tm
SET IsActive = 0,
    LeftAt = COALESCE(tm.LeftAt, SYSUTCDATETIME())
FROM dbo.TeamMembers tm
JOIN dbo.Teams t ON t.TeamID = tm.TeamID
WHERE tm.IsActive = 1
  AND t.TeamStatusID <> @RejectedTeamStatus
  AND EXISTS (
      SELECT 1
      FROM dbo.TeamMembers rejectedMember
      JOIN dbo.EventParticipants ep
        ON ep.EventID = t.EventID
       AND ep.UserID = rejectedMember.UserID
      JOIN dbo.ParticipantStatus ps
        ON ps.StatusID = ep.ParticipantStatusID
      WHERE rejectedMember.TeamID = t.TeamID
        AND ps.StatusName = N'REJECTED'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.TeamMembers activeMember
      JOIN dbo.EventParticipants ep
        ON ep.EventID = t.EventID
       AND ep.UserID = activeMember.UserID
      JOIN dbo.ParticipantStatus ps
        ON ps.StatusID = ep.ParticipantStatusID
      WHERE activeMember.TeamID = t.TeamID
        AND activeMember.IsActive = 1
        AND ps.StatusName <> N'REJECTED'
  );

UPDATE t
SET TeamStatusID = @RejectedTeamStatus,
    UpdatedAt = SYSUTCDATETIME()
FROM dbo.Teams t
WHERE t.TeamStatusID <> @RejectedTeamStatus
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.TeamMembers tm
      WHERE tm.TeamID = t.TeamID
        AND tm.IsActive = 1
  );

IF EXISTS (
    SELECT 1
    FROM dbo.Teams
    WHERE TeamStatusID <> @RejectedTeamStatus
    GROUP BY EventID, TeamName
    HAVING COUNT(*) > 1
)
BEGIN
    THROW 51020, N'Cannot create team-name uniqueness index because duplicate non-rejected teams already exist.', 1;
END;

IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = N'UQ_Teams_Event_Name'
      AND parent_object_id = OBJECT_ID(N'dbo.Teams')
)
BEGIN
    ALTER TABLE dbo.Teams DROP CONSTRAINT UQ_Teams_Event_Name;
END;

IF EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_Teams_Event_Name'
      AND object_id = OBJECT_ID(N'dbo.Teams')
      AND is_unique_constraint = 0
)
BEGIN
    DROP INDEX UQ_Teams_Event_Name ON dbo.Teams;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'UQ_Teams_Event_Name_NotRejected'
      AND object_id = OBJECT_ID(N'dbo.Teams')
)
BEGIN
    CREATE UNIQUE INDEX UQ_Teams_Event_Name_NotRejected
        ON dbo.Teams(EventID, TeamName)
        WHERE TeamStatusID <> '60000000-0000-0000-0000-000000000006';
END;
-- ==========================================================
-- Account Link Tickets (Google <-> Local, một user một người)
-- Ngày: 2026-07-16
-- Mục đích:
--   1. Khi đăng nhập Google với sub MỚI nhưng email ĐÃ tồn tại:
--      KHÔNG tạo user mới — phát linkingToken ngắn hạn (ACCOUNT_LINK_REQUIRED)
--      để user xác minh quyền sở hữu (mật khẩu local hoặc OTP email)
--      rồi mới ghi UserOAuthAccounts trỏ vào user hiện có.
--   2. Khi đăng ký local với email thuộc user Google-only (chưa có mật khẩu):
--      KHÔNG tạo user mới — phát linkingToken, xác minh OTP email,
--      rồi thiết lập PasswordHash cho user hiện có (local/setup-password).
--   3. Chỉ lưu HASH (SHA-256) của linkingToken và OTP; token dùng một lần,
--      hết hạn 15 phút; OTP hết hạn 10 phút, tối đa 5 lần nhập sai.
-- ==========================================================

IF OBJECT_ID(N'AccountLinkTickets', N'U') IS NULL
BEGIN
    CREATE TABLE AccountLinkTickets (
        TicketID UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        -- User đích sẽ được liên kết / thiết lập mật khẩu. Backend tự resolve,
        -- frontend không bao giờ được gửi UserID tùy ý.
        UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID),
        -- GOOGLE_LINK: gắn định danh Google vào user local hiện có.
        -- LOCAL_SETUP: thiết lập mật khẩu local cho user Google-only hiện có.
        Purpose NVARCHAR(30) NOT NULL,
        -- Snapshot định danh Google đã xác thực (chỉ dùng cho GOOGLE_LINK).
        Provider NVARCHAR(30) NULL,
        ProviderUserID NVARCHAR(255) NULL,
        ProviderEmail NVARCHAR(255) NULL,
        ProviderEmailVerified BIT NULL,
        ProviderDisplayName NVARCHAR(255) NULL,
        ProviderAvatarUrl NVARCHAR(1000) NULL,
        -- Hash SHA-256 của linkingToken thô (token thô chỉ nằm ở client).
        TokenHash NVARCHAR(128) NOT NULL UNIQUE,
        -- OTP email (hash SHA-256), phát khi user chọn xác minh bằng OTP.
        OtpHash NVARCHAR(128) NULL,
        OtpExpiresAt DATETIME2 NULL,
        OtpAttempts INT NOT NULL DEFAULT 0,
        OtpLastSentAt DATETIME2 NULL,
        OtpVerifiedAt DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        ExpiresAt DATETIME2 NOT NULL,
        ConsumedAt DATETIME2 NULL
    );

    CREATE NONCLUSTERED INDEX IX_AccountLinkTickets_UserID ON AccountLinkTickets(UserID);
END
GO
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
-- Tombstone cho tài khoản bị organizer xóa cứng (hard delete).
-- Chỉ dùng để hiển thị thông báo "tài khoản đã bị gỡ, hãy tạo tài khoản mới"
-- khi user cũ đăng nhập trong vòng 7 ngày; KHÔNG chặn đăng ký lại bằng email này.
-- Bản ghi hết hạn được scheduled job xóa định kỳ.
-- Không FK tới Users: organizer thực hiện xóa sau này cũng có thể bị xóa.

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'DeletedUserTombstones')
BEGIN
    CREATE TABLE [dbo].[DeletedUserTombstones] (
        [TombstoneID]     [uniqueidentifier] NOT NULL DEFAULT NEWID(),
        [Email]           [nvarchar](255)    NOT NULL,
        [FullName]        [nvarchar](200)    NULL,
        [DeletedByUserID] [uniqueidentifier] NULL,
        [Reason]          [nvarchar](500)    NULL,
        [DeletedAt]       [datetime2](7)     NOT NULL,
        [ExpiresAt]       [datetime2](7)     NOT NULL,
        CONSTRAINT [PK_DeletedUserTombstones] PRIMARY KEY CLUSTERED ([TombstoneID])
    );

    CREATE NONCLUSTERED INDEX [IX_DeletedUserTombstones_Email]
        ON [dbo].[DeletedUserTombstones] ([Email]) INCLUDE ([ExpiresAt]);
END
GO
CREATE TABLE TeamMentorNotes (
    NoteID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TeamID UNIQUEIDENTIFIER NOT NULL,
    MentorID UNIQUEIDENTIFIER NOT NULL,
    Note NVARCHAR(MAX),
    CreatedAt DATETIME2 DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME2,
    
    CONSTRAINT FK_TeamMentorNotes_Teams FOREIGN KEY (TeamID) REFERENCES Teams(TeamID),
    CONSTRAINT FK_TeamMentorNotes_Users FOREIGN KEY (MentorID) REFERENCES Users(UserID),
    CONSTRAINT UQ_TeamMentorNotes_Team_Mentor UNIQUE (TeamID, MentorID)
);
GO
-- Chống tạo trùng tài khoản LOCAL cùng email (double-submit / 2 request đồng thời).
-- Chỉ áp cho account đăng nhập local, chưa xóa; KHÔNG đụng account OAuth-only
-- (LocalLoginEnabled=0) hay account đã soft-delete (IsDeleted=1) — vẫn cho phép
-- local + Google cùng email song song theo thiết kế hiện tại.
--
-- LƯU Ý: nếu DB đang có sẵn 2+ account local cùng email (IsDeleted=0), lệnh tạo
-- index sẽ FAIL. Chạy truy vấn kiểm tra trùng trước và xử lý thủ công:
--
--   SELECT Email, COUNT(*) FROM dbo.Users
--   WHERE LocalLoginEnabled = 1 AND IsDeleted = 0
--   GROUP BY Email HAVING COUNT(*) > 1;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = 'UQ_Users_Email_LocalActive'
)
BEGIN
    CREATE UNIQUE INDEX [UQ_Users_Email_LocalActive]
        ON [dbo].[Users] ([Email])
        WHERE [LocalLoginEnabled] = 1 AND [IsDeleted] = 0;
END
GO
ALTER TABLE Users
ADD Bio NVARCHAR(1000) NULL,
    Github NVARCHAR(500) NULL,
    Portfolio NVARCHAR(500) NULL;
-- Create Appeals table
CREATE TABLE Appeals (
    AppealID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TeamID UNIQUEIDENTIFIER NOT NULL,
    EventID UNIQUEIDENTIFIER NOT NULL,
    CategoryID UNIQUEIDENTIFIER NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    Reason NVARCHAR(MAX) NOT NULL,
    Status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
    AppealType NVARCHAR(50) NOT NULL,
    ResolutionNote NVARCHAR(MAX),
    ResolvedByUserID UNIQUEIDENTIFIER,
    CreatedByUserID UNIQUEIDENTIFIER NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETUTCDATE(),
    UpdatedAt DATETIME2 DEFAULT GETUTCDATE(),
    CONSTRAINT FK_Appeals_Teams FOREIGN KEY (TeamID) REFERENCES Teams(TeamID),
    CONSTRAINT FK_Appeals_Events FOREIGN KEY (EventID) REFERENCES Events(EventID),
    CONSTRAINT FK_Appeals_Categories FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    CONSTRAINT FK_Appeals_ResolvedBy FOREIGN KEY (ResolvedByUserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Appeals_CreatedBy FOREIGN KEY (CreatedByUserID) REFERENCES Users(UserID)
);
GO
-- Add AppealStartTime and AppealEndTime to Rounds table
ALTER TABLE Rounds
ADD AppealStartTime DATETIME2(7) NULL,
    AppealEndTime DATETIME2(7) NULL;
GO
-- Add IsApproved to RoundRankings and EventRankings
ALTER TABLE RoundRankings
ADD IsApproved BIT NOT NULL DEFAULT 0;
GO

ALTER TABLE EventRankings
ADD IsApproved BIT NOT NULL DEFAULT 0;
GO
CREATE TABLE dbo.AI_Knowledge_Base (
    ID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    EventID UNIQUEIDENTIFIER NOT NULL,
    CategoryID UNIQUEIDENTIFIER DEFAULT NULL,
    QuestionPattern NVARCHAR(MAX) NOT NULL,
    StandardAnswer NVARCHAR(MAX) NOT NULL,
    MentorID UNIQUEIDENTIFIER NOT NULL,
    CreatedAt DATETIME2 DEFAULT SYSUTCDATETIME(),
    UpdatedAt DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_AI_KB_Event FOREIGN KEY (EventID) REFERENCES dbo.Events(EventID) ON DELETE CASCADE,
    CONSTRAINT FK_AI_KB_Category FOREIGN KEY (CategoryID) REFERENCES dbo.Categories(CategoryID),
    CONSTRAINT FK_AI_KB_Mentor FOREIGN KEY (MentorID) REFERENCES dbo.Users(UserID)
);

CREATE INDEX IX_AI_KB_Event ON dbo.AI_Knowledge_Base(EventID);
CREATE INDEX IX_AI_KB_Category ON dbo.AI_Knowledge_Base(CategoryID);
GO
/*
    Migration:
    Add new EventStatus values
    Date: 2026-07-22
*/

IF NOT EXISTS (
    SELECT 1
    FROM dbo.EventStatus
    WHERE StatusID = '30000000-0000-0000-0000-000000000006'
)
BEGIN
INSERT INTO dbo.EventStatus
(
    StatusID,
    StatusName
)
VALUES
    (
        '30000000-0000-0000-0000-000000000006',
        'Upcoming'
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM dbo.EventStatus
    WHERE StatusID = '30000000-0000-0000-0000-000000000007'
)
BEGIN
INSERT INTO dbo.EventStatus
(
    StatusID,
    StatusName
)
VALUES
    (
        '30000000-0000-0000-0000-000000000007',
        'Registration Closed'
    );
END
GO
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
ALTER TABLE Appeals ADD RoundID UNIQUEIDENTIFIER NULL;
GO
ALTER TABLE Appeals ADD CONSTRAINT FK_Appeals_Rounds FOREIGN KEY (RoundID) REFERENCES Rounds(RoundID);
GO
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
-- Migration: Remove obsolete account status "Pending Approval"
-- Date: 2026-07-25
-- Luồng duyệt trước đây là duyệt TỪNG học sinh (account status Pending Approval);
-- nay đã đổi sang duyệt TEAM (team có status PENDING riêng) nên account status này thừa.
-- Users.AccountStatusID là FK NOT NULL → phải reassign user còn ở Pending Approval
-- trước khi xóa row (nếu không sẽ vi phạm khóa ngoại).
--
-- StatusID:
--   20000000-...0001 = Pending Approval  (xóa)
--   20000000-...0006 = Unverified        (đích reassign — vẫn CHƯA login được cho tới khi verify,
--                                          giữ đúng ngữ nghĩa "chưa được phép vào" của các account cũ)

IF EXISTS (SELECT 1 FROM dbo.AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000001')
BEGIN
    -- 1) Chuyển mọi user còn ở Pending Approval sang Unverified.
    UPDATE dbo.Users
        SET AccountStatusID = '20000000-0000-0000-0000-000000000006'
        WHERE AccountStatusID = '20000000-0000-0000-0000-000000000001';

    -- 2) Xóa row status đã lỗi thời.
    DELETE FROM dbo.AccountStatus
        WHERE StatusID = '20000000-0000-0000-0000-000000000001';
END;
GO
USE SWP_SEAL_HackathonDB;
GO

ALTER TABLE dbo.Events
ALTER COLUMN EventName NVARCHAR(255) NULL;
GO

ALTER TABLE dbo.Events
ALTER COLUMN Description NVARCHAR(MAX) NULL;
GO

ALTER TABLE dbo.Events
ALTER COLUMN Location NVARCHAR(255) NULL;
GO
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
-- Migration: Bootstrap tai khoan ADMIN dau tien
-- Date: 2026-07-27
--
-- Boi canh: role "Admin" (UserType) da co san trong schema nhung CHUA co user nao mang role
-- nay. Sau khi quan ly nguoi dung / cau hinh he thong chuyen tu ORGANIZER sang ADMIN, phai co
-- it nhat mot tai khoan Admin de dang nhap, neu khong se khong ai vao duoc User Management.
--
-- !!! DEV ONLY !!!
-- Mat khau khoi tao la mat khau dung chung cua bo seed dev (xem database/seed_api_test_data.sql).
-- PHAI doi mat khau ngay sau lan dang nhap dau tien, va KHONG dung file nay tren moi truong that.
--
-- Idempotent: chay lai nhieu lan khong tao trung.

SET NOCOUNT ON;
GO

DECLARE @AdminUserTypeID  UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
DECLARE @ActiveStatusID   UNIQUEIDENTIFIER = '20000000-0000-0000-0000-000000000002';
DECLARE @AdminUserID      UNIQUEIDENTIFIER = 'A0000000-0000-0000-0000-00000000AD01';
DECLARE @AdminEmail       NVARCHAR(255)    = N'admin@seal.local';

-- BCrypt(10) cua mat khau dev dung chung: Test@123
DECLARE @PasswordHash     NVARCHAR(512)    =
    N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm';

DECLARE @Now DATETIME2(7) = SYSUTCDATETIME();

-- 1) Dam bao UserType 'Admin' ton tai (DB dung tu snapshot cu co the con thieu row nay).
IF NOT EXISTS (SELECT 1 FROM dbo.UserType WHERE TypeName = N'Admin')
BEGIN
    INSERT INTO dbo.UserType (UserTypeID, TypeName) VALUES (@AdminUserTypeID, N'Admin');
END
ELSE
BEGIN
    -- Lay dung ID thuc te neu DB da co san row 'Admin' voi ID khac.
    SELECT @AdminUserTypeID = UserTypeID FROM dbo.UserType WHERE TypeName = N'Admin';
END;

-- 2) Tao tai khoan Admin neu chua co.
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = @AdminEmail OR UserID = @AdminUserID)
BEGIN
    INSERT INTO dbo.Users
        (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
         CreatedAt, UpdatedAt, ApprovedAt, IsDeleted)
    VALUES
        (@AdminUserID, @AdminEmail, @PasswordHash, N'System Administrator', N'0900000099',
         @AdminUserTypeID, @ActiveStatusID,
         @Now, @Now, @Now, 0);
END;

-- 3) Bat dang nhap local neu cot ton tai (them boi 20260718_unique_local_email.sql).
--    Tai khoan da o trang thai Active nen khong can buoc xac minh email.
IF COL_LENGTH(N'dbo.Users', N'LocalLoginEnabled') IS NOT NULL
BEGIN
    EXEC sp_executesql
        N'UPDATE dbo.Users SET LocalLoginEnabled = 1 WHERE UserID = @id AND LocalLoginEnabled <> 1;',
        N'@id UNIQUEIDENTIFIER', @id = @AdminUserID;
END;
GO
-- Ensure submission statuses used by the judging approval flow exist.
-- Flow:
--   Submitted    = team has submitted work
--   In Progress  = judges have started/finished scoring, organizer has not approved yet
--   Scored       = organizer approved the score
--   Disqualified = submission/team was disqualified

MERGE dbo.SubmissionStatus AS target
USING (VALUES
    ('50000000-0000-0000-0000-000000000001', N'Draft'),
    ('50000000-0000-0000-0000-000000000002', N'Submitted'),
    ('50000000-0000-0000-0000-000000000003', N'Under Review'),
    ('50000000-0000-0000-0000-000000000004', N'Disqualified'),
    ('50000000-0000-0000-0000-000000000005', N'Scored'),
    ('50000000-0000-0000-0000-000000000006', N'In Progress')
) AS source (StatusID, StatusName)
ON target.StatusID = source.StatusID
WHEN MATCHED AND target.StatusName <> source.StatusName THEN
    UPDATE SET StatusName = source.StatusName
WHEN NOT MATCHED BY TARGET THEN
    INSERT (StatusID, StatusName)
    VALUES (source.StatusID, source.StatusName);
IF OBJECT_ID(N'dbo.FptStudentCodePrefixes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.FptStudentCodePrefixes (
        Prefix NVARCHAR(2) NOT NULL,
        EnglishName NVARCHAR(100) NOT NULL,
        VietnameseName NVARCHAR(200) NOT NULL,
        MajorGroup NVARCHAR(100) NOT NULL,
        MajorCode NVARCHAR(20) NULL,
        Note NVARCHAR(500) NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_FptStudentCodePrefixes_IsActive DEFAULT (1),
        CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_FptStudentCodePrefixes_CreatedAt DEFAULT (SYSUTCDATETIME()),
        UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_FptStudentCodePrefixes_UpdatedAt DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT PK_FptStudentCodePrefixes PRIMARY KEY (Prefix),
        CONSTRAINT CK_FptStudentCodePrefixes_Prefix CHECK (Prefix NOT LIKE '%[^A-Z]%' AND LEN(Prefix) = 2)
    );
END
GO

MERGE dbo.FptStudentCodePrefixes AS target
USING (VALUES
    (N'SE', N'Software Engineering', N'Ky thuat phan mem', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - SE (Software Engineering)'),
    (N'IA', N'Information Assurance', N'An toan thong tin', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - IA (Information Assurance)'),
    (N'AI', N'Artificial Intelligence', N'Tri tue nhan tao', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - AI (Artificial Intelligence)'),
    (N'DS', N'Data Science', N'Khoa hoc du lieu ung dung', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - DS (Data Science)'),
    (N'IC', N'Integrated Circuits', N'Thiet ke vi mach ban dan', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - IC (Integrated Circuits)'),
    (N'AM', N'Automotive', N'Cong nghe o to so', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - AM (Automotive)'),
    (N'IS', N'Information Systems', N'He thong thong tin', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - IS (Information Systems)'),
    (N'GD', N'Graphic Design', N'Thiet ke do hoa va my thuat so', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - GD (Graphic Design)'),
    (N'RA', N'Robotics and AI', N'Robot va Tri tue nhan tao', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - RA (Robotics and AI)'),
    (N'SB', N'Business Administration', N'Quan tri kinh doanh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - SB (Business Administration)'),
    (N'BA', N'Business Administration', N'Quan tri kinh doanh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - BA (Business Administration)'),
    (N'DM', N'Digital Marketing', N'Quan tri truyen thong va Marketing so', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - DM (Digital Marketing)'),
    (N'IB', N'International Business', N'Kinh doanh quoc te', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - IB (International Business)'),
    (N'HM', N'Hotel Management', N'Quan tri khach san', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - HM (Hotel Management)'),
    (N'TM', N'Tourism Management', N'Quan tri dich vu du lich va lu hanh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - TM (Tourism Management)'),
    (N'FI', N'Finance', N'Tai chinh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - FI (Finance)'),
    (N'LS', N'Logistics', N'Logistics va quan ly chuoi cung ung', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - LS (Logistics)'),
    (N'QA', N'English', N'Ngon ngu Anh', N'Ngon ngu va Xa hoi', N'7220201', N'Ngon ngu va Xa hoi - QA (English), Ma nganh: 7220201'),
    (N'EN', N'English', N'Ngon ngu Anh', N'Ngon ngu va Xa hoi', N'7220201', N'Ngon ngu va Xa hoi - EN (English), Ma nganh: 7220201'),
    (N'JA', N'Japanese', N'Ngon ngu Nhat', N'Ngon ngu va Xa hoi', N'7220209', N'Ngon ngu va Xa hoi - JA (Japanese), Ma nganh: 7220209'),
    (N'KR', N'Korean', N'Ngon ngu Han Quoc', N'Ngon ngu va Xa hoi', N'7220210', N'Ngon ngu va Xa hoi - KR (Korean), Ma nganh: 7220210'),
    (N'CH', N'Chinese', N'Ngon ngu Trung Quoc', N'Ngon ngu va Xa hoi', N'7220204', N'Ngon ngu va Xa hoi - CH (Chinese), Ma nganh: 7220204'),
    (N'MC', N'Multimedia Communication', N'Truyen thong da phuong tien', N'Ngon ngu va Xa hoi', N'7320106', N'Ngon ngu va Xa hoi - MC (Multimedia Communication), Ma nganh: 7320106'),
    (N'LE', N'Law', N'Luat / Luat kinh te', N'Ngon ngu va Xa hoi', N'7380101', N'Ngon ngu va Xa hoi - LE (Law), Ma nganh: 7380101')
) AS source (Prefix, EnglishName, VietnameseName, MajorGroup, MajorCode, Note)
ON target.Prefix = source.Prefix
WHEN MATCHED THEN
    UPDATE SET
        EnglishName = source.EnglishName,
        VietnameseName = source.VietnameseName,
        MajorGroup = source.MajorGroup,
        MajorCode = source.MajorCode,
        Note = source.Note,
        IsActive = 1,
        UpdatedAt = SYSUTCDATETIME()
WHEN NOT MATCHED THEN
    INSERT (Prefix, EnglishName, VietnameseName, MajorGroup, MajorCode, Note, IsActive)
    VALUES (source.Prefix, source.EnglishName, source.VietnameseName, source.MajorGroup, source.MajorCode, source.Note, 1);
GO
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
IF OBJECT_ID(N'dbo.ParticipantStatus', N'U') IS NOT NULL
BEGIN
    DECLARE @ConstraintName sysname;
    DECLARE @DropConstraintSql nvarchar(max);
    DECLARE @PendingStatusID uniqueidentifier = '80000000-0000-0000-0000-000000000001';
    DECLARE @WithdrawnStatusID uniqueidentifier = '80000000-0000-0000-0000-000000000005';

    SELECT @ConstraintName = cc.name
    FROM sys.check_constraints cc
    WHERE cc.parent_object_id = OBJECT_ID(N'dbo.ParticipantStatus')
      AND cc.name = N'CK_ParticipantStatus_Name';

    IF @ConstraintName IS NOT NULL
    BEGIN
        SET @DropConstraintSql = N'ALTER TABLE dbo.ParticipantStatus DROP CONSTRAINT '
            + QUOTENAME(@ConstraintName);
        EXEC sp_executesql @DropConstraintSql;
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'WITHDRAWN')
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM dbo.ParticipantStatus
            WHERE StatusID = @WithdrawnStatusID
        )
            UPDATE dbo.ParticipantStatus
            SET StatusName = N'WITHDRAWN'
            WHERE StatusID = @WithdrawnStatusID;
        ELSE
            INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
            VALUES (@WithdrawnStatusID, N'WITHDRAWN');
    END

    IF OBJECT_ID(N'dbo.EventParticipants', N'U') IS NOT NULL
    BEGIN
        UPDATE ep
        SET ParticipantStatusID = @PendingStatusID
        FROM dbo.EventParticipants ep
        JOIN dbo.ParticipantStatus ps
          ON ps.StatusID = ep.ParticipantStatusID
        WHERE ps.StatusName IN (N'TEMPORARY', N'UNVERIFIED');

        IF OBJECT_ID(N'dbo.TeamMembers', N'U') IS NOT NULL
           AND OBJECT_ID(N'dbo.Teams', N'U') IS NOT NULL
        BEGIN
            INSERT INTO dbo.EventParticipants (
                EventParticipantID,
                EventID,
                UserID,
                ParticipantStatusID,
                AppliedAt,
                CreatedAt,
                UpdatedAt
            )
            SELECT
                NEWID(),
                t.EventID,
                tm.UserID,
                CASE t.TeamStatusID
                    WHEN '60000000-0000-0000-0000-000000000002'
                        THEN '80000000-0000-0000-0000-000000000002'
                    WHEN '60000000-0000-0000-0000-000000000003'
                        THEN '80000000-0000-0000-0000-000000000004'
                    WHEN '60000000-0000-0000-0000-000000000004'
                        THEN @WithdrawnStatusID
                    WHEN '60000000-0000-0000-0000-000000000006'
                        THEN '80000000-0000-0000-0000-000000000003'
                    ELSE @PendingStatusID
                END,
                COALESCE(tm.JoinedAt, SYSUTCDATETIME()),
                SYSUTCDATETIME(),
                SYSUTCDATETIME()
            FROM dbo.TeamMembers tm
            JOIN dbo.Teams t
              ON t.TeamID = tm.TeamID
            WHERE tm.IsActive = 1
              AND NOT EXISTS (
                    SELECT 1
                    FROM dbo.EventParticipants ep
                    WHERE ep.EventID = t.EventID
                      AND ep.UserID = tm.UserID
              );

            UPDATE ep
            SET ParticipantStatusID =
                CASE t.TeamStatusID
                    WHEN '60000000-0000-0000-0000-000000000001'
                        THEN @PendingStatusID
                    WHEN '60000000-0000-0000-0000-000000000005'
                        THEN @PendingStatusID
                    WHEN '60000000-0000-0000-0000-000000000002'
                        THEN '80000000-0000-0000-0000-000000000002'
                    WHEN '60000000-0000-0000-0000-000000000003'
                        THEN '80000000-0000-0000-0000-000000000004'
                    WHEN '60000000-0000-0000-0000-000000000004'
                        THEN @WithdrawnStatusID
                    WHEN '60000000-0000-0000-0000-000000000006'
                        THEN '80000000-0000-0000-0000-000000000003'
                    ELSE ep.ParticipantStatusID
                END,
                UpdatedAt = SYSUTCDATETIME()
            FROM dbo.EventParticipants ep
            JOIN dbo.TeamMembers tm
              ON tm.UserID = ep.UserID
             AND tm.IsActive = 1
            JOIN dbo.Teams t
              ON t.TeamID = tm.TeamID
             AND t.EventID = ep.EventID;
        END
    END

    DELETE FROM dbo.ParticipantStatus
    WHERE StatusName IN (N'TEMPORARY', N'UNVERIFIED');

    ALTER TABLE dbo.ParticipantStatus
    ADD CONSTRAINT CK_ParticipantStatus_Name CHECK (
        StatusName IN (
            N'PENDING',
            N'ACTIVE',
            N'REJECTED',
            N'SUSPENDED',
            N'WITHDRAWN'
        )
    );
END;
GO
ALTER TABLE Events
ALTER COLUMN EventStartDate datetime2(7) NULL;

ALTER TABLE Events
ALTER COLUMN EventEndDate datetime2(7) NULL;
-- Alter SystemSettings SettingValue column to support longer strings (e.g., JSON settings for landing page)
ALTER TABLE [dbo].[SystemSettings] ALTER COLUMN [SettingValue] NVARCHAR(MAX) NOT NULL;
