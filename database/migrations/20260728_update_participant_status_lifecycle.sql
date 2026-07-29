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
