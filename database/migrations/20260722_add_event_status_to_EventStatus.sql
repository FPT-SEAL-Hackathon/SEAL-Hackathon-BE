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