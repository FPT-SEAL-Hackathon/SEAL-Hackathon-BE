USE SWP_SEAL_HackathonDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @ScoringTestEventId uniqueidentifier;

-- Tìm Event theo tên
SELECT TOP 1 @ScoringTestEventId = EventID 
FROM Events 
WHERE EventName LIKE '%scoring test event%';

IF @ScoringTestEventId IS NOT NULL
BEGIN
    PRINT 'Đã tìm thấy Event "Scoring Test Event" có ID: ' + CAST(@ScoringTestEventId AS NVARCHAR(50));
    
    -- Lấy số lượng award bị xóa
    DECLARE @DeletedCount INT;
    SELECT @DeletedCount = COUNT(*) FROM Awards WHERE EventID = @ScoringTestEventId;

    -- Xóa các Certificate liên quan đến Award trước để tránh lỗi Foreign Key
    DELETE FROM Certificates WHERE AwardID IN (SELECT AwardID FROM Awards WHERE EventID = @ScoringTestEventId);

    -- Xóa các Award của event này
    DELETE FROM Awards WHERE EventID = @ScoringTestEventId;
    
    PRINT 'Đã xóa thành công ' + CAST(@DeletedCount AS NVARCHAR(10)) + ' Award(s) và các Certificate liên quan thuộc Event này.';
END
ELSE
BEGIN
    PRINT 'Không tìm thấy Event nào có tên chứa "scoring test event".';
END

