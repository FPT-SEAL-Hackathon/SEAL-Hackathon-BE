-- ==========================================
-- 1. Reference Data (Statuses & Types)
-- ==========================================

-- Populate User Types
DECLARE @AdminUserTypeID UNIQUEIDENTIFIER = COALESCE((SELECT UserTypeID FROM UserType WHERE TypeName = N'Admin'), '11111111-1111-1111-1111-111111111111');
DECLARE @OrganizerUserTypeID UNIQUEIDENTIFIER = COALESCE((SELECT UserTypeID FROM UserType WHERE TypeName = N'Organizer'), '22222222-2222-2222-2222-222222222222');
DECLARE @MentorUserTypeID UNIQUEIDENTIFIER = COALESCE((SELECT UserTypeID FROM UserType WHERE TypeName = N'Mentor'), '33333333-3333-3333-3333-333333333333');
DECLARE @JudgeUserTypeID UNIQUEIDENTIFIER = COALESCE((SELECT UserTypeID FROM UserType WHERE TypeName = N'Judge'), '44444444-4444-4444-4444-444444444444');
DECLARE @CompetitorUserTypeID UNIQUEIDENTIFIER = COALESCE((SELECT UserTypeID FROM UserType WHERE TypeName = N'Competitor'), '55555555-5555-5555-5555-555555555555');

IF NOT EXISTS (SELECT 1 FROM UserType WHERE TypeName = N'Admin')
    INSERT INTO UserType (UserTypeID, TypeName) VALUES (@AdminUserTypeID, N'Admin');
IF NOT EXISTS (SELECT 1 FROM UserType WHERE TypeName = N'Organizer')
    INSERT INTO UserType (UserTypeID, TypeName) VALUES (@OrganizerUserTypeID, N'Organizer');
IF NOT EXISTS (SELECT 1 FROM UserType WHERE TypeName = N'Mentor')
    INSERT INTO UserType (UserTypeID, TypeName) VALUES (@MentorUserTypeID, N'Mentor');
IF NOT EXISTS (SELECT 1 FROM UserType WHERE TypeName = N'Judge')
    INSERT INTO UserType (UserTypeID, TypeName) VALUES (@JudgeUserTypeID, N'Judge');
IF NOT EXISTS (SELECT 1 FROM UserType WHERE TypeName = N'Competitor')
    INSERT INTO UserType (UserTypeID, TypeName) VALUES (@CompetitorUserTypeID, N'Competitor');

-- Populate Account Status
DECLARE @ActiveAccountStatusID UNIQUEIDENTIFIER = COALESCE((SELECT StatusID FROM AccountStatus WHERE StatusName = N'Active'), '11111111-2222-3333-4444-555555555555');

IF NOT EXISTS (SELECT 1 FROM AccountStatus WHERE StatusName = N'Active')
    INSERT INTO AccountStatus (StatusID, StatusName) VALUES (@ActiveAccountStatusID, N'Active');

-- Populate Event Status
DECLARE @OngoingEventStatusID UNIQUEIDENTIFIER = COALESCE((SELECT StatusID FROM EventStatus WHERE StatusName = N'Ongoing'), 'E5E5E5E5-E5E5-E5E5-E5E5-E5E5E5E5E5E5');

IF NOT EXISTS (SELECT 1 FROM EventStatus WHERE StatusName = N'Ongoing')
    INSERT INTO EventStatus (StatusID, StatusName) VALUES (@OngoingEventStatusID, N'Ongoing');

-- Populate Team Status
DECLARE @ActiveTeamStatusID UNIQUEIDENTIFIER = COALESCE((SELECT StatusID FROM TeamStatus WHERE StatusName = N'Active'), '60000000-0000-0000-0000-000000000002');

IF NOT EXISTS (SELECT 1 FROM TeamStatus WHERE StatusName = N'Active')
    INSERT INTO TeamStatus (StatusID, StatusName) VALUES (@ActiveTeamStatusID, N'Active');

DECLARE @OrganizerUserID UNIQUEIDENTIFIER = COALESCE((SELECT UserID FROM Users WHERE Email = N'organizer@seal.com'), 'A0000000-0000-0000-0000-000000000000');
DECLARE @MentorAiUserID UNIQUEIDENTIFIER = COALESCE((SELECT UserID FROM Users WHERE Email = N'mentor1@seal.com'), 'A0000000-0000-0000-0000-111111111111');
DECLARE @MentorWebUserID UNIQUEIDENTIFIER = COALESCE((SELECT UserID FROM Users WHERE Email = N'mentor2@seal.com'), 'A0000000-0000-0000-0000-222222222222');
DECLARE @LeaderDataUserID UNIQUEIDENTIFIER = COALESCE((SELECT UserID FROM Users WHERE Email = N'leader1@seal.com'), 'A0000000-0000-0000-0000-333333333333');
DECLARE @LeaderWebUserID UNIQUEIDENTIFIER = COALESCE((SELECT UserID FROM Users WHERE Email = N'leader2@seal.com'), 'A0000000-0000-0000-0000-444444444444');
DECLARE @EventID UNIQUEIDENTIFIER = COALESCE((SELECT EventID FROM Events WHERE EventName = N'SEAL Hackathon 2026' AND IsDeleted = 0), 'E0000000-0000-0000-0000-000000000000');
DECLARE @AiCategoryID UNIQUEIDENTIFIER = COALESCE((SELECT CategoryID FROM Categories WHERE EventID = @EventID AND CategoryName = N'AI & Machine Learning'), 'CA000000-0000-0000-0000-111111111111');
DECLARE @WebCategoryID UNIQUEIDENTIFIER = COALESCE((SELECT CategoryID FROM Categories WHERE EventID = @EventID AND CategoryName = N'Web Applications'), 'CA000000-0000-0000-0000-222222222222');
DECLARE @DataTeamID UNIQUEIDENTIFIER = COALESCE((SELECT TeamID FROM Teams WHERE EventID = @EventID AND TeamName = N'Data Ninjas'), 'D0000000-0000-0000-0000-111111111111');
DECLARE @WebTeamID UNIQUEIDENTIFIER = COALESCE((SELECT TeamID FROM Teams WHERE EventID = @EventID AND TeamName = N'Web Wizards'), 'D0000000-0000-0000-0000-222222222222');


-- ==========================================
-- 2. Users (Organizer, Mentors, Leaders)
-- ==========================================
-- Shared password for seeded users: Test@123
DECLARE @PasswordHash NVARCHAR(512) = N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm';

INSERT INTO Users (UserID, Email, PasswordHash, FullName, UserTypeID, AccountStatusID, IsDeleted, CreatedAt, UpdatedAt)
SELECT UserID, Email, PasswordHash, FullName, UserTypeID, AccountStatusID, IsDeleted, GETDATE(), GETDATE()
FROM (VALUES
          (@OrganizerUserID, 'organizer@seal.com', @PasswordHash, 'Alice Organizer', @OrganizerUserTypeID, @ActiveAccountStatusID, 0),
          (@MentorAiUserID, 'mentor1@seal.com', @PasswordHash, 'Bob Mentor (AI)', @MentorUserTypeID, @ActiveAccountStatusID, 0),
          (@MentorWebUserID, 'mentor2@seal.com', @PasswordHash, 'Charlie Mentor (Web)', @MentorUserTypeID, @ActiveAccountStatusID, 0),
          (@LeaderDataUserID, 'leader1@seal.com', @PasswordHash, 'David Leader', @CompetitorUserTypeID, @ActiveAccountStatusID, 0),
          (@LeaderWebUserID, 'leader2@seal.com', @PasswordHash, 'Eve Leader', @CompetitorUserTypeID, @ActiveAccountStatusID, 0)
     ) AS SeedUsers(UserID, Email, PasswordHash, FullName, UserTypeID, AccountStatusID, IsDeleted)
WHERE NOT EXISTS (
    SELECT 1
    FROM Users
    WHERE Users.UserID = SeedUsers.UserID
       OR Users.Email = SeedUsers.Email
);

UPDATE Users
SET PasswordHash = @PasswordHash,
    UpdatedAt = GETDATE()
WHERE Email IN (
    N'organizer@seal.com',
    N'mentor1@seal.com',
    N'mentor2@seal.com',
    N'leader1@seal.com',
    N'leader2@seal.com'
);


-- ==========================================
-- 3. Event & Categories
-- ==========================================
INSERT INTO Events (EventID, EventName, Description, Location, EventStatusID, RegistrationStart, RegistrationEnd, MaxTeamSize, MinTeamSize, CreatedByID, CreatedAt, UpdatedAt, IsDeleted)
SELECT @EventID, 'SEAL Hackathon 2026', 'The ultimate global hackathon focusing on AI and Web solutions.', 'FPT University', @OngoingEventStatusID, GETDATE(), GETDATE(), 5, 3, @OrganizerUserID, GETDATE(), GETDATE(), 0
WHERE NOT EXISTS (
    SELECT 1
    FROM Events
    WHERE EventID = @EventID
       OR (EventName = 'SEAL Hackathon 2026' AND IsDeleted = 0)
);

INSERT INTO Categories (CategoryID, EventID, CategoryName, Description, SortOrder, IsActive)
SELECT CategoryID, EventID, CategoryName, Description, SortOrder, IsActive
FROM (VALUES
          (@AiCategoryID, @EventID, 'AI & Machine Learning', 'Develop AI-driven solutions to solve real-world problems', 1, 1),
          (@WebCategoryID, @EventID, 'Web Applications', 'Create modern web applications with focus on usability', 2, 1)
     ) AS SeedCategories(CategoryID, EventID, CategoryName, Description, SortOrder, IsActive)
WHERE NOT EXISTS (
    SELECT 1
    FROM Categories
    WHERE Categories.CategoryID = SeedCategories.CategoryID
       OR (Categories.EventID = SeedCategories.EventID AND Categories.CategoryName = SeedCategories.CategoryName)
);


-- ==========================================
-- 4. Category Mentors (Assigning Mentors to Categories)
-- ==========================================
INSERT INTO CategoryMentors (CategoryMentorID, CategoryID, MentorUserID, AssignedAt)
SELECT NEWID(), CategoryID, MentorUserID, GETDATE()
FROM (VALUES
          (@AiCategoryID, @MentorAiUserID),
          (@WebCategoryID, @MentorWebUserID)
     ) AS SeedCategoryMentors(CategoryID, MentorUserID)
WHERE NOT EXISTS (
    SELECT 1
    FROM CategoryMentors
    WHERE CategoryMentors.CategoryID = SeedCategoryMentors.CategoryID
      AND CategoryMentors.MentorUserID = SeedCategoryMentors.MentorUserID
);


-- ==========================================
-- 5. Teams (Competitors joining specific categories)
-- ==========================================
INSERT INTO Teams (TeamID, EventID, CategoryID, TeamName, TeamStatusID, LeaderUserID, CreatedAt, UpdatedAt)
SELECT TeamID, EventID, CategoryID, TeamName, TeamStatusID, LeaderUserID, GETDATE(), GETDATE()
FROM (VALUES
          (@DataTeamID, @EventID, @AiCategoryID, 'Data Ninjas', @ActiveTeamStatusID, @LeaderDataUserID),
          (@WebTeamID, @EventID, @WebCategoryID, 'Web Wizards', @ActiveTeamStatusID, @LeaderWebUserID)
     ) AS SeedTeams(TeamID, EventID, CategoryID, TeamName, TeamStatusID, LeaderUserID)
WHERE NOT EXISTS (
    SELECT 1
    FROM Teams
    WHERE Teams.TeamID = SeedTeams.TeamID
       OR (Teams.EventID = SeedTeams.EventID AND Teams.TeamName = SeedTeams.TeamName)
);


-- ==========================================
-- 6. Consultation Requests
-- ==========================================
INSERT INTO ConsultationRequests (RequestID, EventID, CategoryID, TeamID, MentorUserID, CreatedByUserID, Title, Description, Priority, Status, CreatedAt, UpdatedAt)
SELECT RequestID, EventID, CategoryID, TeamID, MentorUserID, CreatedByUserID, Title, Description, Priority, Status, GETDATE(), GETDATE()
FROM (VALUES
          ('C0000000-0000-0000-0000-111111111111', @EventID, @AiCategoryID, @DataTeamID, @MentorAiUserID, @LeaderDataUserID, 'Need help with TensorFlow Model', 'Our model is overfitting the training data, could you provide some guidance on regularization techniques like Dropout or L2?', 'HIGH', 'PENDING'),
          ('C0000000-0000-0000-0000-222222222222', @EventID, @AiCategoryID, @DataTeamID, @MentorAiUserID, @LeaderDataUserID, 'Dataset Preparation Questions', 'We have some missing values in our image dataset, should we drop them or use augmentation? Looking for best practices.', 'MEDIUM', 'IN_PROGRESS'),
          ('C0000000-0000-0000-0000-333333333333', @EventID, @WebCategoryID, @WebTeamID, @MentorWebUserID, @LeaderWebUserID, 'React State Management Issue', 'Our application state is out of sync between the sidebar and the main map component. Context API feels sluggish here.', 'URGENT', 'RESOLVED')
     ) AS SeedConsultationRequests(RequestID, EventID, CategoryID, TeamID, MentorUserID, CreatedByUserID, Title, Description, Priority, Status)
WHERE NOT EXISTS (
    SELECT 1
    FROM ConsultationRequests
    WHERE ConsultationRequests.RequestID = SeedConsultationRequests.RequestID
);
