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
