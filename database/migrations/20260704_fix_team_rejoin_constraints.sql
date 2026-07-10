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
