USE SWP_SEAL_HackathonDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;
    
    DECLARE @UserId uniqueidentifier;
    -- Tìm user theo email
    SELECT @UserId = UserID FROM Users WHERE Email = 'vanchien0307206@gmail.com';
    
    IF @UserId IS NULL
    BEGIN
        PRINT 'Không tìm thấy User vanchien0307206@gmail.com!';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    DECLARE @TeamId uniqueidentifier;
    DECLARE @EventId uniqueidentifier;
    DECLARE @CategoryId uniqueidentifier;
    DECLARE @LeaderId uniqueidentifier;
    
    -- Dùng cursor để duyệt qua tất cả các team mà user này đang tham gia
    DECLARE team_cursor CURSOR FOR 
    SELECT DISTINCT
        t.TeamID,
        t.EventID,
        t.CategoryID,
        t.LeaderUserID
    FROM Teams t
    LEFT JOIN TeamMembers tm ON t.TeamID = tm.TeamID
    WHERE t.LeaderUserID = @UserId OR tm.UserID = @UserId;
    
    OPEN team_cursor;
    FETCH NEXT FROM team_cursor INTO @TeamId, @EventId, @CategoryId, @LeaderId;
    
    IF @@FETCH_STATUS <> 0
    BEGIN
        PRINT 'User vanchien0307206@gmail.com chưa tham gia team nào!';
        CLOSE team_cursor;
        DEALLOCATE team_cursor;
        ROLLBACK TRANSACTION;
        RETURN;
    END

    WHILE @@FETCH_STATUS = 0
    BEGIN
        PRINT '---------------------------------------------------';
        PRINT 'Đang xử lý cho Team: ' + CAST(@TeamId AS NVARCHAR(50)) + ' thuộc Event: ' + CAST(@EventId AS NVARCHAR(50));
        
        -- Kiểm tra và tạo Submission nếu chưa có
        DECLARE @SubmissionId uniqueidentifier;
        SELECT TOP 1 @SubmissionId = SubmissionID FROM Submissions WHERE TeamID = @TeamId;
        
        IF @SubmissionId IS NULL
        BEGIN
            DECLARE @FirstRoundId uniqueidentifier;
            SELECT TOP 1 @FirstRoundId = RoundID FROM Rounds WHERE CategoryID = @CategoryId ORDER BY RoundOrder DESC;

            SET @SubmissionId = NEWID();
            INSERT INTO Submissions (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, SubmittedAt, LastUpdatedAt, SubmittedByUserID)
            VALUES (@SubmissionId, @TeamId, @FirstRoundId, '50000000-0000-0000-0000-000000000003', 'https://github.com/chien/hackathon', GETDATE(), GETDATE(), @LeaderId);
            PRINT '  -> Tạo Submission mới cho Team.';
        END
        ELSE
        BEGIN
            PRINT '  -> Team đã có Submission: ' + CAST(@SubmissionId AS NVARCHAR(50));
        END
        
        -- Lấy Rank lớn nhất hiện có
        DECLARE @MaxEventRank INT;
        SELECT @MaxEventRank = ISNULL(MAX(RankPosition), 0) FROM EventRankings WHERE EventID = @EventId;

        -- Nếu team chưa có ranking thì thêm mới với Rank cuối cùng
        IF NOT EXISTS (SELECT 1 FROM EventRankings WHERE TeamID = @TeamId AND EventID = @EventId)
        BEGIN
            DECLARE @EventRankingId uniqueidentifier = NEWID();
            INSERT INTO EventRankings (EventRankingID, EventID, CategoryID, TeamID, FinalScore, RankPosition, ComputedAt, IsPublished)
            VALUES (@EventRankingId, @EventId, @CategoryId, @TeamId, 9.75, @MaxEventRank + 1, GETDATE(), 1);
            PRINT '  -> Tạo EventRankings cho Team (Rank ' + CAST(@MaxEventRank + 1 AS NVARCHAR(10)) + ', Score 9.75).';
        END
        ELSE
        BEGIN
            -- Fix cho những row lỡ bị gán Rank = 1 trùng lặp
            UPDATE EventRankings SET RankPosition = @MaxEventRank + 1 WHERE TeamID = @TeamId AND EventID = @EventId AND RankPosition = 1;
            PRINT '  -> EventRankings cho Team đã tồn tại (đã tự động fix lỗi trùng Rank).';
        END
        
        -- Tạo Round Ranking cho từng vòng thuộc Category
        DECLARE @RoundId uniqueidentifier;
        DECLARE round_cursor CURSOR FOR SELECT RoundID FROM Rounds WHERE CategoryID = @CategoryId;
        
        OPEN round_cursor;
        FETCH NEXT FROM round_cursor INTO @RoundId;
        
        WHILE @@FETCH_STATUS = 0
        BEGIN
            DECLARE @MaxRoundRank INT;
            SELECT @MaxRoundRank = ISNULL(MAX(RankPosition), 0) FROM RoundRankings WHERE RoundID = @RoundId;

            IF NOT EXISTS (SELECT 1 FROM RoundRankings WHERE TeamID = @TeamId AND RoundID = @RoundId)
            BEGIN
                INSERT INTO RoundRankings (RankingID, RoundID, CategoryID, TeamID, SubmissionID, TotalScore, AverageScore, RankPosition, IsAdvanced, ComputedAt, IsPublished)
                VALUES (NEWID(), @RoundId, @CategoryId, @TeamId, @SubmissionId, 58.50, 9.75, @MaxRoundRank + 1, 1, GETDATE(), 1);
                PRINT '  -> Tạo RoundRankings cho vòng ' + CAST(@RoundId AS NVARCHAR(50)) + ' (Rank ' + CAST(@MaxRoundRank + 1 AS NVARCHAR(10)) + ').';
            END
            ELSE
            BEGIN
                UPDATE RoundRankings SET RankPosition = @MaxRoundRank + 1 WHERE TeamID = @TeamId AND RoundID = @RoundId AND RankPosition = 1;
                PRINT '  -> RoundRankings cho vòng ' + CAST(@RoundId AS NVARCHAR(50)) + ' đã tồn tại (đã tự động fix lỗi trùng Rank).';
            END
            
            FETCH NEXT FROM round_cursor INTO @RoundId;
        END
        
        CLOSE round_cursor;
        DEALLOCATE round_cursor;

        FETCH NEXT FROM team_cursor INTO @TeamId, @EventId, @CategoryId, @LeaderId;
    END

    CLOSE team_cursor;
    DEALLOCATE team_cursor;

    COMMIT TRANSACTION;
    PRINT '==== HOÀN TẤT TẠO DATA RANKING CHO TẤT CẢ TEAM CỦA CHIEN ====';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    PRINT ERROR_MESSAGE();
    THROW;
END CATCH;
