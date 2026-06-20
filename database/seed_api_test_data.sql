SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;

BEGIN TRY
    BEGIN TRANSACTION;

    /*
      API test data for SWP_SEAL_HackathonDB.
      Safe to run repeatedly: every row has a deterministic UUID and is inserted only once.

      Shared password for every seeded user: Test@123
      BCrypt hash strength: 10
    */

    DECLARE @PasswordHash nvarchar(512) =
        N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm';

    DECLARE @OrganizerId uniqueidentifier = 'A1000000-0000-0000-0000-000000000001';
    DECLARE @JudgeOneId uniqueidentifier = 'A1000000-0000-0000-0000-000000000002';
    DECLARE @JudgeTwoId uniqueidentifier = 'A1000000-0000-0000-0000-000000000003';
    DECLARE @MentorId uniqueidentifier = 'A1000000-0000-0000-0000-000000000004';
    DECLARE @AlphaLeaderId uniqueidentifier = 'A1000000-0000-0000-0000-000000000010';
    DECLARE @AlphaMemberId uniqueidentifier = 'A1000000-0000-0000-0000-000000000011';
    DECLARE @BetaLeaderId uniqueidentifier = 'A1000000-0000-0000-0000-000000000012';
    DECLARE @BetaMemberId uniqueidentifier = 'A1000000-0000-0000-0000-000000000013';
    DECLARE @GreenLeaderId uniqueidentifier = 'A1000000-0000-0000-0000-000000000014';
    DECLARE @GreenMemberId uniqueidentifier = 'A1000000-0000-0000-0000-000000000015';
    DECLARE @ApplicantId uniqueidentifier = 'A1000000-0000-0000-0000-000000000016';
    DECLARE @GuestJudgeId uniqueidentifier = 'A1000000-0000-0000-0000-000000000017';
    DECLARE @JoinMemberId uniqueidentifier = 'A1000000-0000-0000-0000-000000000018';
    DECLARE @JoinLeaderId uniqueidentifier = 'A1000000-0000-0000-0000-000000000019';
    DECLARE @JoinGuestId uniqueidentifier = 'A1000000-0000-0000-0000-000000000020';

    DECLARE @LiveEventId uniqueidentifier = 'B1000000-0000-0000-0000-000000000001';
    DECLARE @PastEventId uniqueidentifier = 'B1000000-0000-0000-0000-000000000002';
    DECLARE @AiCategoryId uniqueidentifier = 'C1000000-0000-0000-0000-000000000001';
    DECLARE @GreenCategoryId uniqueidentifier = 'C1000000-0000-0000-0000-000000000002';
    DECLARE @OpenCategoryId uniqueidentifier = 'C1000000-0000-0000-0000-000000000003';

    DECLARE @InnovationTemplateId uniqueidentifier = 'C2000000-0000-0000-0000-000000000001';
    DECLARE @TechnicalTemplateId uniqueidentifier = 'C2000000-0000-0000-0000-000000000002';
    DECLARE @PresentationTemplateId uniqueidentifier = 'C2000000-0000-0000-0000-000000000003';

    DECLARE @LiveInnovationId uniqueidentifier = 'C3000000-0000-0000-0000-000000000001';
    DECLARE @LiveTechnicalId uniqueidentifier = 'C3000000-0000-0000-0000-000000000002';
    DECLARE @LivePresentationId uniqueidentifier = 'C3000000-0000-0000-0000-000000000003';
    DECLARE @PastInnovationId uniqueidentifier = 'C3000000-0000-0000-0000-000000000011';
    DECLARE @PastTechnicalId uniqueidentifier = 'C3000000-0000-0000-0000-000000000012';
    DECLARE @PastPresentationId uniqueidentifier = 'C3000000-0000-0000-0000-000000000013';

    DECLARE @AiQualifierId uniqueidentifier = 'D1000000-0000-0000-0000-000000000001';
    DECLARE @AiFinalId uniqueidentifier = 'D1000000-0000-0000-0000-000000000002';
    DECLARE @GreenQualifierId uniqueidentifier = 'D1000000-0000-0000-0000-000000000003';
    DECLARE @PastFinalId uniqueidentifier = 'D1000000-0000-0000-0000-000000000004';

    DECLARE @AlphaTeamId uniqueidentifier = 'E1000000-0000-0000-0000-000000000001';
    DECLARE @BetaTeamId uniqueidentifier = 'E1000000-0000-0000-0000-000000000002';
    DECLARE @GreenTeamId uniqueidentifier = 'E1000000-0000-0000-0000-000000000003';
    DECLARE @PastWinnerTeamId uniqueidentifier = 'E1000000-0000-0000-0000-000000000004';

    DECLARE @AlphaQualifierSubmissionId uniqueidentifier = 'F1000000-0000-0000-0000-000000000001';
    DECLARE @BetaQualifierSubmissionId uniqueidentifier = 'F1000000-0000-0000-0000-000000000002';
    DECLARE @AlphaFinalSubmissionId uniqueidentifier = 'F1000000-0000-0000-0000-000000000003';
    DECLARE @GreenSubmissionId uniqueidentifier = 'F1000000-0000-0000-0000-000000000004';
    DECLARE @PastWinnerSubmissionId uniqueidentifier = 'F1000000-0000-0000-0000-000000000005';

    -- Lookup tables used by default values, foreign keys, and role authorization.
    IF NOT EXISTS (SELECT 1 FROM UserType WHERE UserTypeID = '10000000-0000-0000-0000-000000000001')
        INSERT INTO UserType (UserTypeID, TypeName) VALUES ('10000000-0000-0000-0000-000000000001', N'FPT Student');
    IF NOT EXISTS (SELECT 1 FROM UserType WHERE UserTypeID = '10000000-0000-0000-0000-000000000002')
        INSERT INTO UserType (UserTypeID, TypeName) VALUES ('10000000-0000-0000-0000-000000000002', N'External Student');
    IF NOT EXISTS (SELECT 1 FROM UserType WHERE UserTypeID = '10000000-0000-0000-0000-000000000003')
        INSERT INTO UserType (UserTypeID, TypeName) VALUES ('10000000-0000-0000-0000-000000000003', N'Organizer');
    IF NOT EXISTS (SELECT 1 FROM UserType WHERE UserTypeID = '10000000-0000-0000-0000-000000000004')
        INSERT INTO UserType (UserTypeID, TypeName) VALUES ('10000000-0000-0000-0000-000000000004', N'Internal Judge');
    IF NOT EXISTS (SELECT 1 FROM UserType WHERE UserTypeID = '10000000-0000-0000-0000-000000000005')
        INSERT INTO UserType (UserTypeID, TypeName) VALUES ('10000000-0000-0000-0000-000000000005', N'Guest Judge');

    IF NOT EXISTS (SELECT 1 FROM AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000001')
        INSERT INTO AccountStatus (StatusID, StatusName) VALUES ('20000000-0000-0000-0000-000000000001', N'Pending Approval');
    IF NOT EXISTS (SELECT 1 FROM AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000002')
        INSERT INTO AccountStatus (StatusID, StatusName) VALUES ('20000000-0000-0000-0000-000000000002', N'Active');
    IF NOT EXISTS (SELECT 1 FROM AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000003')
        INSERT INTO AccountStatus (StatusID, StatusName) VALUES ('20000000-0000-0000-0000-000000000003', N'Rejected');
    IF NOT EXISTS (SELECT 1 FROM AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000004')
        INSERT INTO AccountStatus (StatusID, StatusName) VALUES ('20000000-0000-0000-0000-000000000004', N'Suspended');
    IF NOT EXISTS (SELECT 1 FROM AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000005')
        INSERT INTO AccountStatus (StatusID, StatusName) VALUES ('20000000-0000-0000-0000-000000000005', N'Temporary');

    IF NOT EXISTS (SELECT 1 FROM EventStatus WHERE StatusID = '30000000-0000-0000-0000-000000000001')
        INSERT INTO EventStatus (StatusID, StatusName) VALUES ('30000000-0000-0000-0000-000000000001', N'Draft');
    IF NOT EXISTS (SELECT 1 FROM EventStatus WHERE StatusID = '30000000-0000-0000-0000-000000000002')
        INSERT INTO EventStatus (StatusID, StatusName) VALUES ('30000000-0000-0000-0000-000000000002', N'Registration Open');
    IF NOT EXISTS (SELECT 1 FROM EventStatus WHERE StatusID = '30000000-0000-0000-0000-000000000003')
        INSERT INTO EventStatus (StatusID, StatusName) VALUES ('30000000-0000-0000-0000-000000000003', N'Ongoing');
    IF NOT EXISTS (SELECT 1 FROM EventStatus WHERE StatusID = '30000000-0000-0000-0000-000000000004')
        INSERT INTO EventStatus (StatusID, StatusName) VALUES ('30000000-0000-0000-0000-000000000004', N'Completed');

    IF NOT EXISTS (SELECT 1 FROM RoundStatus WHERE StatusID = '40000000-0000-0000-0000-000000000001')
        INSERT INTO RoundStatus (StatusID, StatusName) VALUES ('40000000-0000-0000-0000-000000000001', N'Upcoming');
    IF NOT EXISTS (SELECT 1 FROM RoundStatus WHERE StatusID = '40000000-0000-0000-0000-000000000002')
        INSERT INTO RoundStatus (StatusID, StatusName) VALUES ('40000000-0000-0000-0000-000000000002', N'Submission Open');
    IF NOT EXISTS (SELECT 1 FROM RoundStatus WHERE StatusID = '40000000-0000-0000-0000-000000000003')
        INSERT INTO RoundStatus (StatusID, StatusName) VALUES ('40000000-0000-0000-0000-000000000003', N'Judging');
    IF NOT EXISTS (SELECT 1 FROM RoundStatus WHERE StatusID = '40000000-0000-0000-0000-000000000004')
        INSERT INTO RoundStatus (StatusID, StatusName) VALUES ('40000000-0000-0000-0000-000000000004', N'Completed');

    IF NOT EXISTS (SELECT 1 FROM SubmissionStatus WHERE StatusID = '50000000-0000-0000-0000-000000000001')
        INSERT INTO SubmissionStatus (StatusID, StatusName) VALUES ('50000000-0000-0000-0000-000000000001', N'Draft');
    IF NOT EXISTS (SELECT 1 FROM SubmissionStatus WHERE StatusID = '50000000-0000-0000-0000-000000000002')
        INSERT INTO SubmissionStatus (StatusID, StatusName) VALUES ('50000000-0000-0000-0000-000000000002', N'Submitted');
    IF NOT EXISTS (SELECT 1 FROM SubmissionStatus WHERE StatusID = '50000000-0000-0000-0000-000000000003')
        INSERT INTO SubmissionStatus (StatusID, StatusName) VALUES ('50000000-0000-0000-0000-000000000003', N'Under Review');
    IF NOT EXISTS (SELECT 1 FROM SubmissionStatus WHERE StatusID = '50000000-0000-0000-0000-000000000004')
        INSERT INTO SubmissionStatus (StatusID, StatusName) VALUES ('50000000-0000-0000-0000-000000000004', N'Disqualified');

    IF NOT EXISTS (SELECT 1 FROM TeamStatus WHERE StatusID = '60000000-0000-0000-0000-000000000001')
        INSERT INTO TeamStatus (StatusID, StatusName) VALUES ('60000000-0000-0000-0000-000000000001', N'Forming');
    IF NOT EXISTS (SELECT 1 FROM TeamStatus WHERE StatusID = '60000000-0000-0000-0000-000000000002')
        INSERT INTO TeamStatus (StatusID, StatusName) VALUES ('60000000-0000-0000-0000-000000000002', N'Active');
    IF NOT EXISTS (SELECT 1 FROM TeamStatus WHERE StatusID = '60000000-0000-0000-0000-000000000003')
        INSERT INTO TeamStatus (StatusID, StatusName) VALUES ('60000000-0000-0000-0000-000000000003', N'Disqualified');

    IF NOT EXISTS (SELECT 1 FROM AwardTier WHERE TierID = '70000000-0000-0000-0000-000000000001')
        INSERT INTO AwardTier (TierID, TierName) VALUES ('70000000-0000-0000-0000-000000000001', N'First Place');
    IF NOT EXISTS (SELECT 1 FROM AwardTier WHERE TierID = '70000000-0000-0000-0000-000000000002')
        INSERT INTO AwardTier (TierID, TierName) VALUES ('70000000-0000-0000-0000-000000000002', N'Second Place');
    IF NOT EXISTS (SELECT 1 FROM AwardTier WHERE TierID = '70000000-0000-0000-0000-000000000005')
        INSERT INTO AwardTier (TierID, TierName) VALUES ('70000000-0000-0000-0000-000000000005', N'Best Innovation');

    -- Users. The organizer is inserted first because other users reference it as approver.
    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @OrganizerId OR Email = N'api.organizer@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             CreatedAt, UpdatedAt, ApprovedAt, IsDeleted)
        VALUES
            (@OrganizerId, N'api.organizer@seal.test', @PasswordHash, N'API Test Organizer', N'0900000001',
             '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002',
             '2026-05-01T08:00:00', '2026-05-01T08:00:00', '2026-05-01T08:00:00', 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @JudgeOneId OR Email = N'api.judge1@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@JudgeOneId, N'api.judge1@seal.test', @PasswordHash, N'API Test Judge One', N'0900000002',
             '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002',
             '2026-05-02T08:00:00', '2026-05-02T08:00:00', '2026-05-02T09:00:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @JudgeTwoId OR Email = N'api.judge2@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@JudgeTwoId, N'api.judge2@seal.test', @PasswordHash, N'API Test Judge Two', N'0900000003',
             '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002',
             '2026-05-02T08:10:00', '2026-05-02T08:10:00', '2026-05-02T09:10:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @MentorId OR Email = N'api.mentor@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@MentorId, N'api.mentor@seal.test', @PasswordHash, N'API Test Mentor', N'0900000004',
             '10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002',
             '2026-05-02T08:20:00', '2026-05-02T08:20:00', '2026-05-02T09:20:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @GuestJudgeId OR Email = N'api.guestjudge@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, AccountExpiresAt, IsDeleted)
        VALUES
            (@GuestJudgeId, N'api.guestjudge@seal.test', @PasswordHash, N'API Test Guest Judge', N'0900000017',
             '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005',
             '2026-05-02T08:30:00', '2026-05-02T08:30:00', '2026-05-02T09:30:00', @OrganizerId,
             '2026-12-31T23:59:59', 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @AlphaLeaderId OR Email = N'api.alpha.leader@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@AlphaLeaderId, N'api.alpha.leader@seal.test', @PasswordHash, N'API Alpha Leader', N'0910000010',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200010',
             N'FPT University', '2026-05-03T08:00:00', '2026-05-03T08:00:00', '2026-05-03T09:00:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @AlphaMemberId OR Email = N'api.alpha.member@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@AlphaMemberId, N'api.alpha.member@seal.test', @PasswordHash, N'API Alpha Member', N'0910000011',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200011',
             N'FPT University', '2026-05-03T08:10:00', '2026-05-03T08:10:00', '2026-05-03T09:10:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @BetaLeaderId OR Email = N'api.beta.leader@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@BetaLeaderId, N'api.beta.leader@seal.test', @PasswordHash, N'API Beta Leader', N'0910000012',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200012',
             N'FPT University', '2026-05-03T08:20:00', '2026-05-03T08:20:00', '2026-05-03T09:20:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @BetaMemberId OR Email = N'api.beta.member@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@BetaMemberId, N'api.beta.member@seal.test', @PasswordHash, N'API Beta Member', N'0910000013',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200013',
             N'FPT University', '2026-05-03T08:30:00', '2026-05-03T08:30:00', '2026-05-03T09:30:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @GreenLeaderId OR Email = N'api.green.leader@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@GreenLeaderId, N'api.green.leader@seal.test', @PasswordHash, N'API Green Leader', N'0910000014',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200014',
             N'FPT University', '2026-05-03T08:40:00', '2026-05-03T08:40:00', '2026-05-03T09:40:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @GreenMemberId OR Email = N'api.green.member@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             ExternalStudentCode, UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@GreenMemberId, N'api.green.member@seal.test', @PasswordHash, N'API Green External Member', N'0910000015',
             '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002',
             N'EXT-200015', N'Ho Chi Minh City University of Technology',
             '2026-05-03T08:50:00', '2026-05-03T08:50:00', '2026-05-03T09:50:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @ApplicantId OR Email = N'api.applicant@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@ApplicantId, N'api.applicant@seal.test', @PasswordHash, N'API Pending Team Applicant', N'0910000016',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200016',
             N'FPT University', '2026-05-03T09:00:00', '2026-05-03T09:00:00', '2026-05-03T10:00:00', @OrganizerId, 0);

    -- Clean accounts for testing POST /api/v1/teams/{teamId}/join.
    -- They intentionally have no TeamMembers or TeamJoinRequests rows.
    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @JoinMemberId OR Email = N'api.join.member@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@JoinMemberId, N'api.join.member@seal.test', @PasswordHash, N'API Join Test Member', N'0910000018',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200018',
             N'FPT University', '2026-05-03T09:10:00', '2026-05-03T09:10:00', '2026-05-03T10:10:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @JoinLeaderId OR Email = N'api.join.leader@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID, FPTStudentCode,
             UniversityName, CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, IsDeleted)
        VALUES
            (@JoinLeaderId, N'api.join.leader@seal.test', @PasswordHash, N'API Join Test Leader', N'0910000019',
             '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', N'SE200019',
             N'FPT University', '2026-05-03T09:20:00', '2026-05-03T09:20:00', '2026-05-03T10:20:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @JoinGuestId OR Email = N'api.join.guest@seal.test')
        INSERT INTO Users
            (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
             CreatedAt, UpdatedAt, ApprovedAt, ApprovedByUserID, AccountExpiresAt, IsDeleted)
        VALUES
            (@JoinGuestId, N'api.join.guest@seal.test', @PasswordHash, N'API Join Test Guest', N'0900000020',
             '10000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005',
             '2026-05-03T09:30:00', '2026-05-03T09:30:00', '2026-05-03T10:30:00', @OrganizerId,
             '2026-12-31T23:59:59', 0);

    -- Events and categories.
    IF NOT EXISTS (SELECT 1 FROM Events WHERE EventID = @LiveEventId)
        INSERT INTO Events
            (EventID, EventName, Description, Location, BannerImageURL, EventStatusID,
             RegistrationStart, RegistrationEnd, EventStartDate, EventEndDate,
             MaxTeamSize, MinTeamSize, CreatedByID, CreatedAt, UpdatedAt, IsDeleted)
        VALUES
            (@LiveEventId, 'API Test Hackathon 2026', 'Live event used for API integration tests.',
             'FPT University HCMC', 'https://example.test/images/api-hackathon-2026.png',
             '30000000-0000-0000-0000-000000000003',
             '2026-05-01T00:00:00', '2026-06-05T23:59:59', '2026-06-10', '2026-06-30',
             5, 2, @OrganizerId, '2026-05-01T08:00:00', '2026-06-10T08:00:00', 0);

    IF NOT EXISTS (SELECT 1 FROM Events WHERE EventID = @PastEventId)
        INSERT INTO Events
            (EventID, EventName, Description, Location, BannerImageURL, EventStatusID,
             RegistrationStart, RegistrationEnd, EventStartDate, EventEndDate,
             MaxTeamSize, MinTeamSize, CreatedByID, CreatedAt, UpdatedAt, IsDeleted)
        VALUES
            (@PastEventId, 'API Test Innovation Showcase 2025', 'Completed event for ranking and hall-of-fame tests.',
             'FPT University HCMC', 'https://example.test/images/api-showcase-2025.png',
             '30000000-0000-0000-0000-000000000004',
             '2025-09-01T00:00:00', '2025-09-30T23:59:59', '2025-10-10', '2025-10-12',
             5, 2, @OrganizerId, '2025-08-15T08:00:00', '2025-10-13T08:00:00', 0);

    IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryID = @AiCategoryId)
        INSERT INTO Categories (CategoryID, EventID, CategoryName, Description, SortOrder, IsActive)
        VALUES (@AiCategoryId, @LiveEventId, N'AI for Education', 'AI products for teaching and learning.', 1, 1);
    IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryID = @GreenCategoryId)
        INSERT INTO Categories (CategoryID, EventID, CategoryName, Description, SortOrder, IsActive)
        VALUES (@GreenCategoryId, @LiveEventId, N'Green Technology', 'Technology for sustainability.', 2, 1);
    IF NOT EXISTS (SELECT 1 FROM Categories WHERE CategoryID = @OpenCategoryId)
        INSERT INTO Categories (CategoryID, EventID, CategoryName, Description, SortOrder, IsActive)
        VALUES (@OpenCategoryId, @PastEventId, N'Open Innovation', 'Open category for the completed showcase.', 1, 1);

    IF NOT EXISTS (SELECT 1 FROM CategoryMentors WHERE CategoryMentorID = 'C1100000-0000-0000-0000-000000000001')
        INSERT INTO CategoryMentors (CategoryMentorID, CategoryID, MentorUserID, AssignedAt, IsActive)
        VALUES ('C1100000-0000-0000-0000-000000000001', @GreenCategoryId, @MentorId, '2026-05-10T08:00:00', 1);

    -- Reusable criterion templates and event-specific criteria.
    IF NOT EXISTS (SELECT 1 FROM CriterionTemplate WHERE TemplateID = @InnovationTemplateId)
        INSERT INTO CriterionTemplate
            (TemplateID, CriterionName, Description, DefaultWeight, MaxScore, IsActive, CreatedByID, CreatedAt)
        VALUES
            (@InnovationTemplateId, 'API Test Innovation', 'Originality and value of the solution.', 0.40, 10.00, 1,
             @OrganizerId, '2026-05-05T08:00:00');
    IF NOT EXISTS (SELECT 1 FROM CriterionTemplate WHERE TemplateID = @TechnicalTemplateId)
        INSERT INTO CriterionTemplate
            (TemplateID, CriterionName, Description, DefaultWeight, MaxScore, IsActive, CreatedByID, CreatedAt)
        VALUES
            (@TechnicalTemplateId, 'API Test Technical Quality', 'Architecture, implementation, and reliability.', 0.35, 10.00, 1,
             @OrganizerId, '2026-05-05T08:05:00');
    IF NOT EXISTS (SELECT 1 FROM CriterionTemplate WHERE TemplateID = @PresentationTemplateId)
        INSERT INTO CriterionTemplate
            (TemplateID, CriterionName, Description, DefaultWeight, MaxScore, IsActive, CreatedByID, CreatedAt)
        VALUES
            (@PresentationTemplateId, 'API Test Presentation', 'Clarity of demo and pitch.', 0.25, 10.00, 1,
             @OrganizerId, '2026-05-05T08:10:00');

    IF NOT EXISTS (SELECT 1 FROM EventCriteria WHERE EventCriterionID = @LiveInnovationId)
        INSERT INTO EventCriteria
            (EventCriterionID, EventID, TemplateID, CriterionName, Description, Weight, MaxScore, SortOrder, IsActive)
        VALUES (@LiveInnovationId, @LiveEventId, @InnovationTemplateId, N'Innovation', 'Originality and user value.', 0.40, 10.00, 1, 1);
    IF NOT EXISTS (SELECT 1 FROM EventCriteria WHERE EventCriterionID = @LiveTechnicalId)
        INSERT INTO EventCriteria
            (EventCriterionID, EventID, TemplateID, CriterionName, Description, Weight, MaxScore, SortOrder, IsActive)
        VALUES (@LiveTechnicalId, @LiveEventId, @TechnicalTemplateId, N'Technical Quality', 'Implementation quality.', 0.35, 10.00, 2, 1);
    IF NOT EXISTS (SELECT 1 FROM EventCriteria WHERE EventCriterionID = @LivePresentationId)
        INSERT INTO EventCriteria
            (EventCriterionID, EventID, TemplateID, CriterionName, Description, Weight, MaxScore, SortOrder, IsActive)
        VALUES (@LivePresentationId, @LiveEventId, @PresentationTemplateId, N'Presentation', 'Demo and pitch quality.', 0.25, 10.00, 3, 1);

    IF NOT EXISTS (SELECT 1 FROM EventCriteria WHERE EventCriterionID = @PastInnovationId)
        INSERT INTO EventCriteria
            (EventCriterionID, EventID, TemplateID, CriterionName, Description, Weight, MaxScore, SortOrder, IsActive)
        VALUES (@PastInnovationId, @PastEventId, @InnovationTemplateId, N'Innovation', 'Originality and user value.', 0.40, 10.00, 1, 1);
    IF NOT EXISTS (SELECT 1 FROM EventCriteria WHERE EventCriterionID = @PastTechnicalId)
        INSERT INTO EventCriteria
            (EventCriterionID, EventID, TemplateID, CriterionName, Description, Weight, MaxScore, SortOrder, IsActive)
        VALUES (@PastTechnicalId, @PastEventId, @TechnicalTemplateId, N'Technical Quality', 'Implementation quality.', 0.35, 10.00, 2, 1);
    IF NOT EXISTS (SELECT 1 FROM EventCriteria WHERE EventCriterionID = @PastPresentationId)
        INSERT INTO EventCriteria
            (EventCriterionID, EventID, TemplateID, CriterionName, Description, Weight, MaxScore, SortOrder, IsActive)
        VALUES (@PastPresentationId, @PastEventId, @PresentationTemplateId, N'Presentation', 'Demo and pitch quality.', 0.25, 10.00, 3, 1);

    -- Rounds.
    IF NOT EXISTS (SELECT 1 FROM Rounds WHERE RoundID = @AiQualifierId)
        INSERT INTO Rounds
            (RoundID, CategoryID, RoundName, Description, RoundOrder, RoundStatusID,
             StartDate, EndDate, SubmissionDeadline, JudgingDeadline, AdvancementTopN, IsCalibrationRound)
        VALUES
            (@AiQualifierId, @AiCategoryId, 'AI Qualifier', 'Completed qualifier with scores and ranking.', 1,
             '40000000-0000-0000-0000-000000000004',
             '2026-06-10T08:00:00', '2026-06-12T18:00:00', '2026-06-11T23:59:59', '2026-06-12T17:00:00', 1, 0);
    IF NOT EXISTS (SELECT 1 FROM Rounds WHERE RoundID = @AiFinalId)
        INSERT INTO Rounds
            (RoundID, CategoryID, RoundName, Description, RoundOrder, RoundStatusID,
             StartDate, EndDate, SubmissionDeadline, JudgingDeadline, AdvancementTopN, IsCalibrationRound)
        VALUES
            (@AiFinalId, @AiCategoryId, 'AI Final', 'Final round ready for judging API tests.', 2,
             '40000000-0000-0000-0000-000000000003',
             '2026-06-13T08:00:00', '2026-06-20T18:00:00', '2026-06-18T23:59:59', '2026-06-20T17:00:00', 1, 0);
    IF NOT EXISTS (SELECT 1 FROM Rounds WHERE RoundID = @GreenQualifierId)
        INSERT INTO Rounds
            (RoundID, CategoryID, RoundName, Description, RoundOrder, RoundStatusID,
             StartDate, EndDate, SubmissionDeadline, JudgingDeadline, AdvancementTopN, IsCalibrationRound)
        VALUES
            (@GreenQualifierId, @GreenCategoryId, 'Green Qualifier', 'Submission and disqualification test round.', 1,
             '40000000-0000-0000-0000-000000000002',
             '2026-06-10T08:00:00', '2026-06-18T18:00:00', '2026-06-17T23:59:59', '2026-06-18T17:00:00', 1, 0);
    IF NOT EXISTS (SELECT 1 FROM Rounds WHERE RoundID = @PastFinalId)
        INSERT INTO Rounds
            (RoundID, CategoryID, RoundName, Description, RoundOrder, RoundStatusID,
             StartDate, EndDate, SubmissionDeadline, JudgingDeadline, AdvancementTopN, IsCalibrationRound)
        VALUES
            (@PastFinalId, @OpenCategoryId, 'Showcase Final', 'Completed final for hall-of-fame tests.', 1,
             '40000000-0000-0000-0000-000000000004',
             '2025-10-10T08:00:00', '2025-10-12T18:00:00', '2025-10-11T23:59:59', '2025-10-12T17:00:00', 1, 0);

    -- Every round receives a snapshot of its event criteria.
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000001')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000001', @AiQualifierId, @LiveInnovationId, 0.40, N'Innovation', 'Originality and user value.', 10.00, 1);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000002')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000002', @AiQualifierId, @LiveTechnicalId, 0.35, N'Technical Quality', 'Implementation quality.', 10.00, 2);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000003')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000003', @AiQualifierId, @LivePresentationId, 0.25, N'Presentation', 'Demo and pitch quality.', 10.00, 3);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000011')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000011', @AiFinalId, @LiveInnovationId, 0.40, N'Innovation', 'Originality and user value.', 10.00, 1);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000012')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000012', @AiFinalId, @LiveTechnicalId, 0.35, N'Technical Quality', 'Implementation quality.', 10.00, 2);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000013')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000013', @AiFinalId, @LivePresentationId, 0.25, N'Presentation', 'Demo and pitch quality.', 10.00, 3);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000021')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000021', @GreenQualifierId, @LiveInnovationId, 0.40, N'Innovation', 'Originality and user value.', 10.00, 1);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000022')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000022', @GreenQualifierId, @LiveTechnicalId, 0.35, N'Technical Quality', 'Implementation quality.', 10.00, 2);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000023')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000023', @GreenQualifierId, @LivePresentationId, 0.25, N'Presentation', 'Demo and pitch quality.', 10.00, 3);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000031')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000031', @PastFinalId, @PastInnovationId, 0.40, N'Innovation', 'Originality and user value.', 10.00, 1);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000032')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000032', @PastFinalId, @PastTechnicalId, 0.35, N'Technical Quality', 'Implementation quality.', 10.00, 2);
    IF NOT EXISTS (SELECT 1 FROM RoundCriteria WHERE RoundCriterionID = 'D2000000-0000-0000-0000-000000000033')
        INSERT INTO RoundCriteria (RoundCriterionID, RoundID, EventCriterionID, Weight, criterionName, description, MaxScore, SortOrder)
        VALUES ('D2000000-0000-0000-0000-000000000033', @PastFinalId, @PastPresentationId, 0.25, N'Presentation', 'Demo and pitch quality.', 10.00, 3);

    -- Teams and members. No user is active in two teams in the same event.
    IF NOT EXISTS (SELECT 1 FROM Teams WHERE TeamID = @AlphaTeamId)
        INSERT INTO Teams VALUES (@AlphaTeamId, @LiveEventId, @AiCategoryId, N'API Alpha', '60000000-0000-0000-0000-000000000002', @AlphaLeaderId, '2026-06-01T08:00:00', '2026-06-10T08:00:00');
    IF NOT EXISTS (SELECT 1 FROM Teams WHERE TeamID = @BetaTeamId)
        INSERT INTO Teams VALUES (@BetaTeamId, @LiveEventId, @AiCategoryId, N'API Beta', '60000000-0000-0000-0000-000000000002', @BetaLeaderId, '2026-06-01T08:10:00', '2026-06-10T08:10:00');
    IF NOT EXISTS (SELECT 1 FROM Teams WHERE TeamID = @GreenTeamId)
        INSERT INTO Teams VALUES (@GreenTeamId, @LiveEventId, @GreenCategoryId, N'API Green', '60000000-0000-0000-0000-000000000003', @GreenLeaderId, '2026-06-01T08:20:00', '2026-06-12T08:20:00');
    IF NOT EXISTS (SELECT 1 FROM Teams WHERE TeamID = @PastWinnerTeamId)
        INSERT INTO Teams VALUES (@PastWinnerTeamId, @PastEventId, @OpenCategoryId, N'API Legacy Winners', '60000000-0000-0000-0000-000000000002', @AlphaLeaderId, '2025-09-15T08:00:00', '2025-10-13T08:00:00');

    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000001')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000001', @AlphaTeamId, @AlphaLeaderId, '2026-06-01T08:00:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000002')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000002', @AlphaTeamId, @AlphaMemberId, '2026-06-01T09:00:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000003')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000003', @BetaTeamId, @BetaLeaderId, '2026-06-01T08:10:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000004')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000004', @BetaTeamId, @BetaMemberId, '2026-06-01T09:10:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000005')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000005', @GreenTeamId, @GreenLeaderId, '2026-06-01T08:20:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000006')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000006', @GreenTeamId, @GreenMemberId, '2026-06-01T09:20:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000011')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000011', @PastWinnerTeamId, @AlphaLeaderId, '2025-09-15T08:00:00', NULL, 1);
    IF NOT EXISTS (SELECT 1 FROM TeamMembers WHERE TeamMemberID = 'E2000000-0000-0000-0000-000000000012')
        INSERT INTO TeamMembers VALUES ('E2000000-0000-0000-0000-000000000012', @PastWinnerTeamId, @BetaLeaderId, '2025-09-15T09:00:00', NULL, 1);

    IF NOT EXISTS (SELECT 1 FROM TeamJoinRequests WHERE RequestID = 'E3000000-0000-0000-0000-000000000001')
        INSERT INTO TeamJoinRequests
            (RequestID, TeamID, UserID, RequestStatus, RequestedAt, RespondedAt, RespondedByID, ResponseNote)
        VALUES
            ('E3000000-0000-0000-0000-000000000001', @AlphaTeamId, @ApplicantId, N'PENDING',
             '2026-06-11T10:00:00', NULL, NULL, NULL);

    -- Judges. The Green Technology mentor is intentionally not assigned to the Green round.
    IF NOT EXISTS (SELECT 1 FROM RoundJudges WHERE RoundJudgeID = 'D3000000-0000-0000-0000-000000000001')
        INSERT INTO RoundJudges VALUES ('D3000000-0000-0000-0000-000000000001', @AiQualifierId, @JudgeOneId, '2026-06-09T08:00:00', @OrganizerId);
    IF NOT EXISTS (SELECT 1 FROM RoundJudges WHERE RoundJudgeID = 'D3000000-0000-0000-0000-000000000002')
        INSERT INTO RoundJudges VALUES ('D3000000-0000-0000-0000-000000000002', @AiQualifierId, @JudgeTwoId, '2026-06-09T08:05:00', @OrganizerId);
    IF NOT EXISTS (SELECT 1 FROM RoundJudges WHERE RoundJudgeID = 'D3000000-0000-0000-0000-000000000003')
        INSERT INTO RoundJudges VALUES ('D3000000-0000-0000-0000-000000000003', @AiFinalId, @JudgeOneId, '2026-06-12T08:00:00', @OrganizerId);
    IF NOT EXISTS (SELECT 1 FROM RoundJudges WHERE RoundJudgeID = 'D3000000-0000-0000-0000-000000000004')
        INSERT INTO RoundJudges VALUES ('D3000000-0000-0000-0000-000000000004', @AiFinalId, @JudgeTwoId, '2026-06-12T08:05:00', @OrganizerId);
    IF NOT EXISTS (SELECT 1 FROM RoundJudges WHERE RoundJudgeID = 'D3000000-0000-0000-0000-000000000005')
        INSERT INTO RoundJudges VALUES ('D3000000-0000-0000-0000-000000000005', @GreenQualifierId, @JudgeOneId, '2026-06-09T08:10:00', @OrganizerId);
    IF NOT EXISTS (SELECT 1 FROM RoundJudges WHERE RoundJudgeID = 'D3000000-0000-0000-0000-000000000006')
        INSERT INTO RoundJudges VALUES ('D3000000-0000-0000-0000-000000000006', @PastFinalId, @GuestJudgeId, '2025-10-09T08:00:00', @OrganizerId);

    -- Submissions cover submitted, draft, and disqualified states.
    IF NOT EXISTS (SELECT 1 FROM Submissions WHERE SubmissionID = @AlphaQualifierSubmissionId)
        INSERT INTO Submissions
            (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, DemoURL, ReportURL, SlideURL,
             RepoMetadataJSON, RepoLastCommitAt, RepoStarCount, RepoForkCount, SubmittedAt, LastUpdatedAt,
             SubmittedByUserID, Notes)
        VALUES
            (@AlphaQualifierSubmissionId, @AlphaTeamId, @AiQualifierId, '50000000-0000-0000-0000-000000000003',
             N'https://github.com/example/api-alpha', N'https://alpha.example.test', N'https://example.test/reports/alpha.pdf',
             N'https://example.test/slides/alpha', N'{"language":"Java","branch":"main"}', '2026-06-11T21:00:00', 12, 3,
             '2026-06-11T21:30:00', '2026-06-12T08:00:00', @AlphaLeaderId, N'Ready for judging.');
    IF NOT EXISTS (SELECT 1 FROM Submissions WHERE SubmissionID = @BetaQualifierSubmissionId)
        INSERT INTO Submissions
            (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, DemoURL, ReportURL, SlideURL,
             RepoMetadataJSON, RepoLastCommitAt, RepoStarCount, RepoForkCount, SubmittedAt, LastUpdatedAt,
             SubmittedByUserID, Notes)
        VALUES
            (@BetaQualifierSubmissionId, @BetaTeamId, @AiQualifierId, '50000000-0000-0000-0000-000000000003',
             N'https://github.com/example/api-beta', N'https://beta.example.test', N'https://example.test/reports/beta.pdf',
             N'https://example.test/slides/beta', N'{"language":"TypeScript","branch":"main"}', '2026-06-11T20:00:00', 8, 2,
             '2026-06-11T20:30:00', '2026-06-12T08:00:00', @BetaLeaderId, N'Ready for judging.');
    IF NOT EXISTS (SELECT 1 FROM Submissions WHERE SubmissionID = @AlphaFinalSubmissionId)
        INSERT INTO Submissions
            (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, LastUpdatedAt, SubmittedByUserID, Notes)
        VALUES
            (@AlphaFinalSubmissionId, @AlphaTeamId, @AiFinalId, '50000000-0000-0000-0000-000000000001',
             N'https://github.com/example/api-alpha', '2026-06-12T10:00:00', @AlphaLeaderId, N'Draft final submission.');
    IF NOT EXISTS (SELECT 1 FROM Submissions WHERE SubmissionID = @GreenSubmissionId)
        INSERT INTO Submissions
            (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, SubmittedAt, LastUpdatedAt,
             SubmittedByUserID, Notes)
        VALUES
            (@GreenSubmissionId, @GreenTeamId, @GreenQualifierId, '50000000-0000-0000-0000-000000000004',
             N'https://github.com/example/api-green', '2026-06-11T19:00:00', '2026-06-12T09:00:00',
             @GreenLeaderId, N'Disqualified sample submission.');
    IF NOT EXISTS (SELECT 1 FROM Submissions WHERE SubmissionID = @PastWinnerSubmissionId)
        INSERT INTO Submissions
            (SubmissionID, TeamID, RoundID, SubmissionStatusID, RepositoryURL, DemoURL, ReportURL, SlideURL,
             SubmittedAt, LastUpdatedAt, SubmittedByUserID, Notes)
        VALUES
            (@PastWinnerSubmissionId, @PastWinnerTeamId, @PastFinalId, '50000000-0000-0000-0000-000000000003',
             N'https://github.com/example/api-legacy-winners', N'https://legacy.example.test',
             N'https://example.test/reports/legacy.pdf', N'https://example.test/slides/legacy',
             '2025-10-11T20:00:00', '2025-10-12T08:00:00', @AlphaLeaderId, N'Winning submission.');

    -- Scores for two AI teams and the completed showcase winner.
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000001')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000001', @AlphaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000001', 'D2000000-0000-0000-0000-000000000001', 9.00, N'Strong user value.', '2026-06-12T10:00:00', '2026-06-12T10:00:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000002')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000002', @AlphaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000001', 'D2000000-0000-0000-0000-000000000002', 8.50, N'Solid architecture.', '2026-06-12T10:05:00', '2026-06-12T10:05:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000003')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000003', @AlphaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000001', 'D2000000-0000-0000-0000-000000000003', 8.00, N'Clear demo.', '2026-06-12T10:10:00', '2026-06-12T10:10:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000004')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000004', @AlphaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000002', 'D2000000-0000-0000-0000-000000000001', 8.50, N'Useful concept.', '2026-06-12T10:15:00', '2026-06-12T10:15:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000005')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000005', @AlphaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000002', 'D2000000-0000-0000-0000-000000000002', 9.00, N'Well implemented.', '2026-06-12T10:20:00', '2026-06-12T10:20:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000006')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000006', @AlphaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000002', 'D2000000-0000-0000-0000-000000000003', 8.50, N'Good answers.', '2026-06-12T10:25:00', '2026-06-12T10:25:00', 0);

    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000011')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000011', @BetaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000001', 'D2000000-0000-0000-0000-000000000001', 7.50, N'Good idea.', '2026-06-12T11:00:00', '2026-06-12T11:00:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000012')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000012', @BetaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000001', 'D2000000-0000-0000-0000-000000000002', 7.00, N'Needs more tests.', '2026-06-12T11:05:00', '2026-06-12T11:05:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000013')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000013', @BetaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000001', 'D2000000-0000-0000-0000-000000000003', 8.00, N'Good pitch.', '2026-06-12T11:10:00', '2026-06-12T11:10:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000014')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000014', @BetaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000002', 'D2000000-0000-0000-0000-000000000001', 7.00, N'Common approach.', '2026-06-12T11:15:00', '2026-06-12T11:15:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000015')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000015', @BetaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000002', 'D2000000-0000-0000-0000-000000000002', 7.50, N'Functional prototype.', '2026-06-12T11:20:00', '2026-06-12T11:20:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000016')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000016', @BetaQualifierSubmissionId, 'D3000000-0000-0000-0000-000000000002', 'D2000000-0000-0000-0000-000000000003', 7.50, N'Clear presentation.', '2026-06-12T11:25:00', '2026-06-12T11:25:00', 0);

    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000021')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000021', @PastWinnerSubmissionId, 'D3000000-0000-0000-0000-000000000006', 'D2000000-0000-0000-0000-000000000031', 9.50, N'Excellent innovation.', '2025-10-12T10:00:00', '2025-10-12T10:00:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000022')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000022', @PastWinnerSubmissionId, 'D3000000-0000-0000-0000-000000000006', 'D2000000-0000-0000-0000-000000000032', 9.20, N'Production-ready prototype.', '2025-10-12T10:05:00', '2025-10-12T10:05:00', 0);
    IF NOT EXISTS (SELECT 1 FROM Judging WHERE JudgingID = 'F2000000-0000-0000-0000-000000000023')
        INSERT INTO Judging VALUES ('F2000000-0000-0000-0000-000000000023', @PastWinnerSubmissionId, 'D3000000-0000-0000-0000-000000000006', 'D2000000-0000-0000-0000-000000000033', 9.40, N'Excellent final pitch.', '2025-10-12T10:10:00', '2025-10-12T10:10:00', 0);

    -- Precomputed leaderboard rows allow GET APIs to work before recomputation is tested.
    IF NOT EXISTS (SELECT 1 FROM RoundRankings WHERE RankingID = 'F3000000-0000-0000-0000-000000000001')
        INSERT INTO RoundRankings VALUES ('F3000000-0000-0000-0000-000000000001', @AiQualifierId, @AiCategoryId, @AlphaTeamId, @AlphaQualifierSubmissionId, 51.50, 8.5833, 1, 1, '2026-06-12T12:00:00');
    IF NOT EXISTS (SELECT 1 FROM RoundRankings WHERE RankingID = 'F3000000-0000-0000-0000-000000000002')
        INSERT INTO RoundRankings VALUES ('F3000000-0000-0000-0000-000000000002', @AiQualifierId, @AiCategoryId, @BetaTeamId, @BetaQualifierSubmissionId, 44.50, 7.4167, 2, 0, '2026-06-12T12:00:00');
    IF NOT EXISTS (SELECT 1 FROM RoundRankings WHERE RankingID = 'F3000000-0000-0000-0000-000000000003')
        INSERT INTO RoundRankings VALUES ('F3000000-0000-0000-0000-000000000003', @PastFinalId, @OpenCategoryId, @PastWinnerTeamId, @PastWinnerSubmissionId, 28.10, 9.3667, 1, 1, '2025-10-12T12:00:00');

    IF NOT EXISTS (SELECT 1 FROM EventRankings WHERE EventRankingID = 'F3100000-0000-0000-0000-000000000001')
        INSERT INTO EventRankings VALUES ('F3100000-0000-0000-0000-000000000001', @LiveEventId, @AiCategoryId, @AlphaTeamId, 8.5833, 1, '2026-06-12T12:05:00');
    IF NOT EXISTS (SELECT 1 FROM EventRankings WHERE EventRankingID = 'F3100000-0000-0000-0000-000000000002')
        INSERT INTO EventRankings VALUES ('F3100000-0000-0000-0000-000000000002', @LiveEventId, @AiCategoryId, @BetaTeamId, 7.4167, 2, '2026-06-12T12:05:00');
    IF NOT EXISTS (SELECT 1 FROM EventRankings WHERE EventRankingID = 'F3100000-0000-0000-0000-000000000003')
        INSERT INTO EventRankings VALUES ('F3100000-0000-0000-0000-000000000003', @PastEventId, @OpenCategoryId, @PastWinnerTeamId, 9.3667, 1, '2025-10-12T12:05:00');

    -- Published award supports the public hall-of-fame endpoint.
    IF NOT EXISTS (SELECT 1 FROM Awards WHERE AwardID = 'F4000000-0000-0000-0000-000000000001')
        INSERT INTO Awards
            (AwardID, EventID, CategoryID, TeamID, AwardTierID, AwardTitle, Description,
             PrizeValue, PrizeCurrency, AwardedAt, AwardedByID, IsPublished, PublishedAt)
        VALUES
            ('F4000000-0000-0000-0000-000000000001', @PastEventId, @OpenCategoryId, @PastWinnerTeamId,
             '70000000-0000-0000-0000-000000000001', N'API Test 2025 Champion',
             N'First place in the API test innovation showcase.', 10000000.00, N'VND',
             '2025-10-12T13:00:00', @OrganizerId, 1, '2025-10-13T08:00:00+07:00');
    IF NOT EXISTS (SELECT 1 FROM Awards WHERE AwardID = 'F4000000-0000-0000-0000-000000000002')
        INSERT INTO Awards
            (AwardID, EventID, CategoryID, TeamID, AwardTierID, AwardTitle, Description,
             PrizeValue, PrizeCurrency, AwardedAt, AwardedByID, IsPublished, PublishedAt)
        VALUES
            ('F4000000-0000-0000-0000-000000000002', @LiveEventId, @AiCategoryId, @AlphaTeamId,
             '70000000-0000-0000-0000-000000000005', N'API Test Innovation Candidate',
             N'Unpublished award for organizer API tests.', 3000000.00, N'VND',
             '2026-06-12T13:00:00', @OrganizerId, 0, NULL);

    -- Disqualification history and notifications.
    IF NOT EXISTS (SELECT 1 FROM Disqualifications WHERE DisqualificationID = 'F6000000-0000-0000-0000-000000000001')
        INSERT INTO Disqualifications
            (DisqualificationID, TeamID, SubmissionID, Reason, DisqualifiedByID, DisqualifiedAt,
             IsReversed, ReversedAt, ReversedByID, ReversalReason)
        VALUES
            ('F6000000-0000-0000-0000-000000000001', @GreenTeamId, NULL,
             N'Team violated the competition eligibility policy.', @OrganizerId,
             '2026-06-12T09:00:00', 0, NULL, NULL, NULL);

    IF NOT EXISTS (SELECT 1 FROM Disqualifications WHERE DisqualificationID = 'F6000000-0000-0000-0000-000000000002')
        INSERT INTO Disqualifications
            (DisqualificationID, TeamID, SubmissionID, Reason, DisqualifiedByID, DisqualifiedAt,
             IsReversed, ReversedAt, ReversedByID, ReversalReason)
        VALUES
            ('F6000000-0000-0000-0000-000000000002', NULL, @GreenSubmissionId,
             N'Repository contained material submitted before the competition window.', @OrganizerId,
             '2026-06-12T09:05:00', 0, NULL, NULL, NULL);

    IF NOT EXISTS (SELECT 1 FROM Notifications WHERE NotificationID = 'F5000000-0000-0000-0000-000000000001')
        INSERT INTO Notifications VALUES ('F5000000-0000-0000-0000-000000000001', @LiveEventId, @AlphaLeaderId,
            N'Qualifier result available', N'API Alpha advanced to the final round.', '2026-06-12T12:10:00', @OrganizerId, 0);
    IF NOT EXISTS (SELECT 1 FROM Notifications WHERE NotificationID = 'F5000000-0000-0000-0000-000000000002')
        INSERT INTO Notifications VALUES ('F5000000-0000-0000-0000-000000000002', @LiveEventId, @BetaLeaderId,
            N'Qualifier result available', N'API Beta placed second in the qualifier.', '2026-06-12T12:11:00', @OrganizerId, 1);
    IF NOT EXISTS (SELECT 1 FROM Notifications WHERE NotificationID = 'F5000000-0000-0000-0000-000000000003')
        INSERT INTO Notifications VALUES ('F5000000-0000-0000-0000-000000000003', @LiveEventId, NULL,
            N'Final round schedule', N'The AI final starts on June 13, 2026.', '2026-06-12T12:12:00', @OrganizerId, 0);

    IF NOT EXISTS (SELECT 1 FROM EvaluationAuditLogs WHERE EvaluationAuditLogID = 'F7000000-0000-0000-0000-000000000001')
        INSERT INTO EvaluationAuditLogs
            (EvaluationAuditLogID, EventID, ActionType, ActorUserID, JudgingID, TeamID, SubmissionID,
             OldValue, NewValue, Reason, CreatedAt)
        VALUES
            ('F7000000-0000-0000-0000-000000000001', @LiveEventId, N'SCORE_UPDATED', @JudgeOneId,
             'F2000000-0000-0000-0000-000000000002', @AlphaTeamId, @AlphaQualifierSubmissionId,
             N'{"score":8.0}', N'{"score":8.5}', N'Corrected after reviewing the architecture demo.',
             '2026-06-12T10:30:00');

    COMMIT TRANSACTION;

    PRINT 'API test data seeded successfully.';
    PRINT 'Shared password: Test@123';

    SELECT UserID, Email, FullName
    FROM Users
    WHERE UserID IN
        (@OrganizerId, @JudgeOneId, @JudgeTwoId, @MentorId, @GuestJudgeId,
         @AlphaLeaderId, @AlphaMemberId, @BetaLeaderId, @BetaMemberId,
         @GreenLeaderId, @GreenMemberId, @ApplicantId,
         @JoinMemberId, @JoinLeaderId, @JoinGuestId)
    ORDER BY Email;

    SELECT EventID, EventName FROM Events WHERE EventID IN (@LiveEventId, @PastEventId);
    SELECT TeamID, TeamName FROM Teams WHERE TeamID IN (@AlphaTeamId, @BetaTeamId, @GreenTeamId, @PastWinnerTeamId);
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
