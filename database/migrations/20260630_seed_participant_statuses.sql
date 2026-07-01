IF OBJECT_ID(N'dbo.ParticipantStatus', N'U') IS NOT NULL
BEGIN
    DECLARE @ConstraintName sysname;

    SELECT @ConstraintName = cc.name
    FROM sys.check_constraints cc
    WHERE cc.parent_object_id = OBJECT_ID(N'dbo.ParticipantStatus')
      AND cc.name = N'CK_ParticipantStatus_Name';

    IF @ConstraintName IS NOT NULL
        EXEC(N'ALTER TABLE dbo.ParticipantStatus DROP CONSTRAINT ' + QUOTENAME(@ConstraintName));

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
        SET StatusName = N'TEMPORARY'
        WHERE StatusID = '80000000-0000-0000-0000-000000000005';
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'TEMPORARY')
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000005', N'TEMPORARY');

    IF EXISTS (
        SELECT 1
        FROM dbo.ParticipantStatus
        WHERE StatusID = '80000000-0000-0000-0000-000000000006'
    )
        UPDATE dbo.ParticipantStatus
        SET StatusName = N'UNVERIFIED'
        WHERE StatusID = '80000000-0000-0000-0000-000000000006';
    ELSE IF NOT EXISTS (SELECT 1 FROM dbo.ParticipantStatus WHERE StatusName = N'UNVERIFIED')
        INSERT INTO dbo.ParticipantStatus (StatusID, StatusName)
        VALUES ('80000000-0000-0000-0000-000000000006', N'UNVERIFIED');

    ALTER TABLE dbo.ParticipantStatus
    ADD CONSTRAINT CK_ParticipantStatus_Name CHECK (
        StatusName IN (
            N'PENDING',
            N'ACTIVE',
            N'REJECTED',
            N'SUSPENDED',
            N'TEMPORARY',
            N'UNVERIFIED'
        )
    );
END;
GO
