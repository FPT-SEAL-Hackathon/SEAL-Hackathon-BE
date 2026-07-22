-- Add AppealStartTime and AppealEndTime to Rounds table
ALTER TABLE Rounds
ADD AppealStartTime DATETIME2(7) NULL,
    AppealEndTime DATETIME2(7) NULL;
GO
