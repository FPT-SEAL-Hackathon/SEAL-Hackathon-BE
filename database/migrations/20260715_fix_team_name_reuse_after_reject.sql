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
