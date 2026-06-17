-- Tự động thêm cột vào bảng RoundCriteria nếu chưa có (Fix lỗi schema)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'RoundCriteria') AND name = 'criterionName')
BEGIN
    PRINT N'Thêm các cột còn thiếu vào RoundCriteria...';
    ALTER TABLE RoundCriteria ADD 
        criterionName NVARCHAR(255),
        description NVARCHAR(MAX),
        MaxScore DECIMAL(18, 2),
        SortOrder INT;
END
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    PRINT N'Bắt đầu khởi tạo dữ liệu Hackathon quy mô lớn (FPTU Context)...';

    -- Password hash cho 'Test@123'
    DECLARE @PasswordHash NVARCHAR(512) = N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm';

    DECLARE @OrganizerId UNIQUEIDENTIFIER = NEWID();
    DECLARE @Judge1Id UNIQUEIDENTIFIER = NEWID();
    DECLARE @Judge2Id UNIQUEIDENTIFIER = NEWID();
    DECLARE @Judge3Id UNIQUEIDENTIFIER = NEWID();
    
    INSERT INTO Users (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, ApprovedAt, ApprovedByUserID)
    VALUES 
    (@OrganizerId, N'organizer@fpt.edu.vn', @PasswordHash, N'Nguyễn Hoàng Anh', '0912345678', '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', GETUTCDATE(), @OrganizerId),
    (@Judge1Id, N'judge.noibo1@fpt.edu.vn', @PasswordHash, N'Lê Thị Bảo Ngọc', '0987654321', '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', GETUTCDATE(), @OrganizerId),
    (@Judge2Id, N'judge.noibo2@fpt.edu.vn', @PasswordHash, N'Trần Văn Long', '0933445566', '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', GETUTCDATE(), @OrganizerId),
    (@Judge3Id, N'guestjudge@gmail.com', @PasswordHash, N'Phạm Tuấn Khách', '0944556677', '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', GETUTCDATE(), @OrganizerId);

    DECLARE @Template1Id UNIQUEIDENTIFIER = NEWID();
    DECLARE @Template2Id UNIQUEIDENTIFIER = NEWID();
    DECLARE @Template3Id UNIQUEIDENTIFIER = NEWID();
    
    INSERT INTO CriterionTemplate (TemplateID, CriterionName, Description, DefaultWeight, MaxScore, CreatedByID)
    VALUES 
    (@Template1Id, N'Tính Sáng Tạo (Innovation)', N'Đánh giá ý tưởng mới lạ và tính ứng dụng', 1.0, 10.0, @OrganizerId),
    (@Template2Id, N'Độ Phức Tạp Kỹ Thuật (Technical Depth)', N'Đánh giá chất lượng code, kiến trúc hệ thống', 1.5, 10.0, @OrganizerId),
    (@Template3Id, N'Trải nghiệm Người Dùng (UI/UX)', N'Đánh giá giao diện thân thiện, dễ sử dụng', 1.0, 10.0, @OrganizerId);

    DECLARE @e INT = 1, @c INT = 1, @t INT = 1;
    DECLARE @EventID UNIQUEIDENTIFIER, @CategoryID UNIQUEIDENTIFIER, @EventName NVARCHAR(255), @EventStatus UNIQUEIDENTIFIER;
    
    -- Khai báo tất cả các biến vòng lặp ở scope ngoài cùng
    DECLARE @TeamID UNIQUEIDENTIFIER, @LeaderID UNIQUEIDENTIFIER, @MemberID UNIQUEIDENTIFIER;
    DECLARE @IsExternal BIT, @LeaderUserType UNIQUEIDENTIFIER, @MemberUserType UNIQUEIDENTIFIER, @UniversityName NVARCHAR(200);
    DECLARE @LLName NVARCHAR(50), @LMName NVARCHAR(50), @LFName NVARCHAR(50), @LFNameEN NVARCHAR(50), @LLNameEN NVARCHAR(50);
    DECLARE @MLName NVARCHAR(50), @MMName NVARCHAR(50), @MFName NVARCHAR(50), @MFNameEN NVARCHAR(50), @MLNameEN NVARCHAR(50);
    DECLARE @LeaderFullName NVARCHAR(150), @LeaderEmail NVARCHAR(100), @LeaderPhone NVARCHAR(20);
    DECLARE @MemberFullName NVARCHAR(150), @MemberEmail NVARCHAR(100), @MemberPhone NVARCHAR(20);
    DECLARE @LeaderFPTCode NVARCHAR(20), @LeaderExtCode NVARCHAR(50), @MemberFPTCode NVARCHAR(20), @MemberExtCode NVARCHAR(50);
    DECLARE @TeamNames NVARCHAR(50);
    DECLARE @Sub1 UNIQUEIDENTIFIER, @Sub2 UNIQUEIDENTIFIER;
    DECLARE @AdvTeamID UNIQUEIDENTIFIER, @AdvLeaderID UNIQUEIDENTIFIER, @AdvCount INT;
    
    -- Các biến random index
    DECLARE @R_LL INT, @R_LM INT, @R_LF INT, @R_ML INT, @R_MM INT, @R_MF INT, @R_Uni INT;

    WHILE @e <= 4
    BEGIN
        SET @EventID = NEWID();

        IF @e = 1 SELECT @EventName = N'FPT Edu Hackathon 2026', @EventStatus = '30000000-0000-0000-0000-000000000004';
        ELSE IF @e = 2 SELECT @EventName = N'FPT Software AI Hackathon', @EventStatus = '30000000-0000-0000-0000-000000000003';
        ELSE IF @e = 3 SELECT @EventName = N'SEAL Webathon FPTU', @EventStatus = '30000000-0000-0000-0000-000000000004';
        ELSE SELECT @EventName = N'Techwiz 2026 FPT Aptech', @EventStatus = '30000000-0000-0000-0000-000000000003';

        INSERT INTO Events (EventID, EventName, Description, Location, EventStatusID, MaxTeamSize, MinTeamSize, CreatedByID)
        VALUES (@EventID, @EventName, N'Cuộc thi lập trình lớn nhất dành cho sinh viên IT', N'Campus Trường Đại Học FPT', @EventStatus, 5, 2, @OrganizerId);

        DECLARE @EvCrit1 UNIQUEIDENTIFIER = NEWID(), @EvCrit2 UNIQUEIDENTIFIER = NEWID(), @EvCrit3 UNIQUEIDENTIFIER = NEWID();
        INSERT INTO EventCriteria (EventCriterionID, EventID, TemplateID, CriterionName, Weight, MaxScore, IsActive)
        VALUES 
        (@EvCrit1, @EventID, @Template1Id, N'Tính Sáng Tạo (Innovation)', 1.0, 10.0, 1),
        (@EvCrit2, @EventID, @Template2Id, N'Độ Phức Tạp Kỹ Thuật', 1.5, 10.0, 1),
        (@EvCrit3, @EventID, @Template3Id, N'Trải nghiệm Người Dùng (UI/UX)', 1.0, 10.0, 1);

        SET @c = 1;
        WHILE @c <= 3
        BEGIN
            SET @CategoryID = NEWID();
            DECLARE @CatName NVARCHAR(50);
            IF @c = 1 SET @CatName = N'Web Application';
            ELSE IF @c = 2 SET @CatName = N'Mobile Application';
            ELSE SET @CatName = N'Artificial Intelligence';

            INSERT INTO Categories (CategoryID, EventID, CategoryName, SortOrder, IsActive)
            VALUES (@CategoryID, @EventID, @CatName, @c, 1);

            -- Tạo 2 vòng: Sơ Loại và Chung Kết
            DECLARE @R1_ID UNIQUEIDENTIFIER = NEWID(), @R2_ID UNIQUEIDENTIFIER = NEWID();
            INSERT INTO Rounds (RoundID, CategoryID, RoundName, RoundOrder, RoundStatusID, AdvancementTopN)
            VALUES 
            (@R1_ID, @CategoryID, N'Vòng Sơ Loại', 1, '40000000-0000-0000-0000-000000000004', 3),
            (@R2_ID, @CategoryID, N'Vòng Chung Kết', 2, IIF(@e IN (1, 3), '40000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000001'), 3);

            DECLARE @R1_C1 UNIQUEIDENTIFIER = NEWID(), @R1_C2 UNIQUEIDENTIFIER = NEWID(), @R1_C3 UNIQUEIDENTIFIER = NEWID();
            INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, criterionName, description, Weight, MaxScore, SortOrder) VALUES 
            (@R1_C1, @R1_ID, @EvCrit1, N'Tính Sáng Tạo (Innovation)', N'Đánh giá ý tưởng mới lạ và tính ứng dụng', 1.0, 10.0, 1), 
            (@R1_C2, @R1_ID, @EvCrit2, N'Độ Phức Tạp Kỹ Thuật', N'Đánh giá chất lượng code, kiến trúc hệ thống', 1.5, 10.0, 2), 
            (@R1_C3, @R1_ID, @EvCrit3, N'Trải nghiệm Người Dùng (UI/UX)', N'Đánh giá giao diện thân thiện, dễ sử dụng', 1.0, 10.0, 3);
            
            DECLARE @R2_C1 UNIQUEIDENTIFIER = NEWID(), @R2_C2 UNIQUEIDENTIFIER = NEWID(), @R2_C3 UNIQUEIDENTIFIER = NEWID();
            INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, criterionName, description, Weight, MaxScore, SortOrder) VALUES 
            (@R2_C1, @R2_ID, @EvCrit1, N'Tính Sáng Tạo (Innovation)', N'Đánh giá ý tưởng mới lạ và tính ứng dụng', 1.0, 10.0, 1), 
            (@R2_C2, @R2_ID, @EvCrit2, N'Độ Phức Tạp Kỹ Thuật', N'Đánh giá chất lượng code, kiến trúc hệ thống', 1.5, 10.0, 2), 
            (@R2_C3, @R2_ID, @EvCrit3, N'Trải nghiệm Người Dùng (UI/UX)', N'Đánh giá giao diện thân thiện, dễ sử dụng', 1.0, 10.0, 3);

            DECLARE @R1_J1 UNIQUEIDENTIFIER = NEWID(), @R1_J2 UNIQUEIDENTIFIER = NEWID();
            INSERT INTO RoundJudges (RoundJudgeID, RoundID, UserID, AssignedByID) VALUES (@R1_J1, @R1_ID, @Judge1Id, @OrganizerId), (@R1_J2, @R1_ID, @Judge3Id, @OrganizerId);
            
            DECLARE @R2_J1 UNIQUEIDENTIFIER = NEWID(), @R2_J2 UNIQUEIDENTIFIER = NEWID();
            INSERT INTO RoundJudges (RoundJudgeID, RoundID, UserID, AssignedByID) VALUES (@R2_J1, @R2_ID, @Judge2Id, @OrganizerId), (@R2_J2, @R2_ID, @Judge3Id, @OrganizerId);

            -- TẠO 8 ĐỘI THI
            SET @t = 1;
            WHILE @t <= 8
            BEGIN
                SET @TeamID = NEWID();
                SET @LeaderID = NEWID();
                SET @MemberID = NEWID();
                
                -- Sinh index ngẫu nhiên lưu vào biến TĨNH (int)
                -- Khắc phục lỗi CHOOSE chạy nhiều lần đánh giá lại NEWID() gây ra NULL
                SET @R_LL = ABS(CHECKSUM(NEWID())) % 5 + 1;
                SET @R_LM = ABS(CHECKSUM(NEWID())) % 5 + 1;
                SET @R_LF = ABS(CHECKSUM(NEWID())) % 8 + 1;
                
                SET @R_ML = ABS(CHECKSUM(NEWID())) % 5 + 1;
                SET @R_MM = ABS(CHECKSUM(NEWID())) % 5 + 1;
                SET @R_MF = ABS(CHECKSUM(NEWID())) % 8 + 1;
                
                SET @R_Uni = ABS(CHECKSUM(NEWID())) % 3 + 1;
                
                SET @IsExternal = IIF(@t % 3 = 0, 1, 0); -- Cứ mỗi 3 đội sẽ có 1 đội dùng @gmail.com
                SET @LeaderUserType = IIF(@IsExternal = 1, '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001');
                SET @MemberUserType = IIF(@IsExternal = 1, '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001');
                SET @UniversityName = NULL;

                IF @IsExternal = 1
                BEGIN
                    SET @UniversityName = CHOOSE(@R_Uni, N'Trường Đại học Bách khoa (VNUHCM-UT)', N'Trường Đại học Khoa học Tự nhiên (VNUHCM-US)', N'Trường Đại học Công nghệ Thông tin (VNUHCM-UIT)');
                END

                -- Generate random names for Leader
                SET @LLName = CHOOSE(@R_LL, N'Nguyễn', N'Trần', N'Lê', N'Phạm', N'Hoàng');
                SET @LLNameEN = CHOOSE(@R_LL, 'nguyen', 'tran', 'le', 'pham', 'hoang');
                SET @LMName = CHOOSE(@R_LM, N'Văn', N'Thị', N'Hải', N'Ngọc', N'Minh');
                SET @LFName = CHOOSE(@R_LF, N'Anh', N'Khoa', N'Đăng', N'Hùng', N'Linh', N'Đức', N'Phát', N'Quân');
                SET @LFNameEN = CHOOSE(@R_LF, 'anh', 'khoa', 'dang', 'hung', 'linh', 'duc', 'phat', 'quan');

                SET @LeaderFullName = @LLName + ' ' + @LMName + ' ' + @LFName;
                SET @LeaderEmail = @LFNameEN + '.' + @LLNameEN + CAST(ABS(CHECKSUM(NEWID())) % 10000 AS VARCHAR(4)) + IIF(@IsExternal = 1, '@gmail.com', '@fpt.edu.vn');
                SET @LeaderPhone = '09' + CAST(10000000 + (ABS(CHECKSUM(NEWID())) % 90000000) AS VARCHAR(8));

                -- Generate random names for Member
                SET @MLName = CHOOSE(@R_ML, N'Nguyễn', N'Trần', N'Lê', N'Phạm', N'Hoàng');
                SET @MLNameEN = CHOOSE(@R_ML, 'nguyen', 'tran', 'le', 'pham', 'hoang');
                SET @MMName = CHOOSE(@R_MM, N'Văn', N'Thị', N'Hải', N'Ngọc', N'Minh');
                SET @MFName = CHOOSE(@R_MF, N'Anh', N'Khoa', N'Đăng', N'Hùng', N'Linh', N'Đức', N'Phát', N'Quân');
                SET @MFNameEN = CHOOSE(@R_MF, 'anh', 'khoa', 'dang', 'hung', 'linh', 'duc', 'phat', 'quan');

                SET @MemberFullName = @MLName + ' ' + @MMName + ' ' + @MFName;
                SET @MemberEmail = @MFNameEN + '.' + @MLNameEN + CAST(ABS(CHECKSUM(NEWID())) % 10000 AS VARCHAR(4)) + IIF(@IsExternal = 1, '@gmail.com', '@fpt.edu.vn');
                SET @MemberPhone = '03' + CAST(10000000 + (ABS(CHECKSUM(NEWID())) % 90000000) AS VARCHAR(8));

                SET @LeaderFPTCode = NULL; SET @LeaderExtCode = NULL;
                SET @MemberFPTCode = NULL; SET @MemberExtCode = NULL;

                IF @IsExternal = 1
                BEGIN
                    SET @LeaderExtCode = 'EXT' + CAST(@e*1000 + @c*100 + @t*10 + 1 AS VARCHAR);
                    SET @MemberExtCode = 'EXT' + CAST(@e*1000 + @c*100 + @t*10 + 2 AS VARCHAR);
                END
                ELSE
                BEGIN
                    SET @LeaderFPTCode = 'SE' + CAST(@e*1000 + @c*100 + @t*10 + 1 AS VARCHAR);
                    SET @MemberFPTCode = 'SE' + CAST(@e*1000 + @c*100 + @t*10 + 2 AS VARCHAR);
                END

                INSERT INTO Users (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode, ExternalStudentCode, UniversityName, ApprovedAt, ApprovedByUserID)
                VALUES
                (@LeaderID, @LeaderEmail, @PasswordHash, @LeaderFullName, @LeaderPhone, @LeaderUserType, '20000000-0000-0000-0000-000000000002', @LeaderFPTCode, @LeaderExtCode, @UniversityName, GETUTCDATE(), @OrganizerId),
                (@MemberID, @MemberEmail, @PasswordHash, @MemberFullName, @MemberPhone, @MemberUserType, '20000000-0000-0000-0000-000000000002', @MemberFPTCode, @MemberExtCode, @UniversityName, GETUTCDATE(), @OrganizerId);

                IF @t = 1 SET @TeamNames = N'Cóc Ngoạm Cỏ';
                ELSE IF @t = 2 SET @TeamNames = N'F-Code Elite';
                ELSE IF @t = 3 SET @TeamNames = N'JS Lovers';
                ELSE IF @t = 4 SET @TeamNames = N'Bất Bại Coder';
                ELSE IF @t = 5 SET @TeamNames = N'Bug Hunters';
                ELSE IF @t = 6 SET @TeamNames = N'Chuyên Gia Fix Bug';
                ELSE IF @t = 7 SET @TeamNames = N'Mất Gốc C';
                ELSE SET @TeamNames = N'Java Lỏ';

                INSERT INTO Teams (TeamID, EventID, CategoryID, TeamName, TeamStatusID, LeaderUserID)
                VALUES (@TeamID, @EventID, @CategoryID, @TeamNames + ' E' + CAST(@e AS VARCHAR) + 'C' + CAST(@c AS VARCHAR), '60000000-0000-0000-0000-000000000002', @LeaderID);

                INSERT INTO TeamMembers (TeamMemberID, TeamID, UserID, IsActive) VALUES (NEWID(), @TeamID, @LeaderID, 1), (NEWID(), @TeamID, @MemberID, 1);

                -- Round 1: Teams 1-7 nộp bài, Team 8 bỏ thi
                IF @t <= 7
                BEGIN
                    SET @Sub1 = NEWID();
                    INSERT INTO Submissions (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, SubmittedByUserID)
                    VALUES (@Sub1, @TeamID, @R1_ID, '50000000-0000-0000-0000-000000000003', 'https://github.com/student/project-r1-t' + CAST(@t AS VARCHAR), @LeaderID);

                    -- Giám khảo chấm 3 tiêu chí
                    INSERT INTO Judging (JudgingID, SubmissionID, RoundJudgeID, RoundCriterionID, ScoreValue) VALUES 
                    (NEWID(), @Sub1, @R1_J1, @R1_C1, 6.0 + (@t * 0.4)), (NEWID(), @Sub1, @R1_J1, @R1_C2, 5.0 + (@t * 0.5)), (NEWID(), @Sub1, @R1_J1, @R1_C3, 7.0 + (@t * 0.3)),
                    (NEWID(), @Sub1, @R1_J2, @R1_C1, 6.5 + (@t * 0.4)), (NEWID(), @Sub1, @R1_J2, @R1_C2, 5.5 + (@t * 0.5)), (NEWID(), @Sub1, @R1_J2, @R1_C3, 7.5 + (@t * 0.3));
                END

                SET @t = @t + 1;
            END

            -- TÍNH RANK ROUND 1 ĐỂ TÌM TEAM VÀO CHUNG KẾT
            EXEC sp_ComputeRoundRankings @R1_ID, @CategoryID;

            -- DỰA VÀO RANK ROUND 1, TẠO SUBMISSION ROUND 2 CHO NHỮNG ĐỘI ĐƯỢC THĂNG VÒNG (IsAdvanced = 1)
            SET @AdvCount = 0;
            
            DECLARE cur CURSOR LOCAL FAST_FORWARD FOR
            SELECT rr.TeamID, t.LeaderUserID
            FROM RoundRankings rr
            JOIN Teams t ON t.TeamID = rr.TeamID
            WHERE rr.RoundID = @R1_ID AND rr.IsAdvanced = 1;

            OPEN cur;
            FETCH NEXT FROM cur INTO @AdvTeamID, @AdvLeaderID;
            WHILE @@FETCH_STATUS = 0
            BEGIN
                SET @AdvCount = @AdvCount + 1;
                -- Trong số các đội advance (3 đội), 2 đội nộp bài, 1 đội bỏ cuộc
                IF @AdvCount <= 2 AND @e IN (1, 3)
                BEGIN
                    SET @Sub2 = NEWID();
                    INSERT INTO Submissions (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, SubmittedByUserID)
                    VALUES (@Sub2, @AdvTeamID, @R2_ID, '50000000-0000-0000-0000-000000000003', 'https://github.com/student/project-final-t' + CAST(@AdvCount AS VARCHAR), @AdvLeaderID);

                    INSERT INTO Judging (JudgingID, SubmissionID, RoundJudgeID, RoundCriterionID, ScoreValue) VALUES 
                    (NEWID(), @Sub2, @R2_J1, @R2_C1, 7.0 + (@AdvCount * 0.4)), (NEWID(), @Sub2, @R2_J1, @R2_C2, 6.0 + (@AdvCount * 0.5)), (NEWID(), @Sub2, @R2_J1, @R2_C3, 8.0 + (@AdvCount * 0.3)),
                    (NEWID(), @Sub2, @R2_J2, @R2_C1, 7.5 + (@AdvCount * 0.4)), (NEWID(), @Sub2, @R2_J2, @R2_C2, 6.5 + (@AdvCount * 0.5)), (NEWID(), @Sub2, @R2_J2, @R2_C3, 8.5 + (@AdvCount * 0.3));
                END
                FETCH NEXT FROM cur INTO @AdvTeamID, @AdvLeaderID;
            END
            CLOSE cur;
            DEALLOCATE cur;

            IF @e IN (1, 3)
            BEGIN
                EXEC sp_ComputeRoundRankings @R2_ID, @CategoryID;
                EXEC sp_ComputeEventRankings @EventID, @CategoryID;
            END

            SET @c = @c + 1;
        END
        SET @e = @e + 1;
    END

    COMMIT TRANSACTION;
    PRINT N'==================================================';
    PRINT N'ĐÃ TẠO THÀNH CÔNG DỮ LIỆU KIỂM THỬ HACKATHON FPTU!';
    PRINT N'==================================================';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT ERROR_MESSAGE();
    THROW;
END CATCH;