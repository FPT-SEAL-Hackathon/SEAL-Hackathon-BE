-- Add IsApproved to RoundRankings and EventRankings
ALTER TABLE RoundRankings
ADD IsApproved BIT NOT NULL DEFAULT 0;
GO

ALTER TABLE EventRankings
ADD IsApproved BIT NOT NULL DEFAULT 0;
GO
