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
