/****** Object:  Table [dbo].[Events]    Script Date: 7/14/2026 9:01:54 PM ******/
USE master;
GO

IF DB_ID('SWP_SEAL_HackathonDB') IS NULL
BEGIN
    CREATE DATABASE SWP_SEAL_HackathonDB;
END
GO

USE SWP_SEAL_HackathonDB;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Events](
	[EventID] [uniqueidentifier] NOT NULL,
	[EventName] [nvarchar](255) NULL,
	[Description] [nvarchar](max) NULL,
	[Location] [nvarchar](255) NULL,
	[BannerImageURL] [varchar](255) NULL,
	[EventStatusID] [uniqueidentifier] NOT NULL,
	[RegistrationStart] [datetime2](7) NULL,
	[RegistrationEnd] [datetime2](7) NULL,
	[EventStartDate] [date] NULL,
	[EventEndDate] [date] NULL,
	[MaxTeamSize] [tinyint] NOT NULL,
	[MinTeamSize] [tinyint] NOT NULL,
	[CreatedByID] [uniqueidentifier] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
	[IsDeleted] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[EventID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Categories]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Categories](
	[CategoryID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[CategoryName] [nvarchar](300) NOT NULL,
	[Description] [varchar](255) NULL,
	[SortOrder] [tinyint] NOT NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[CategoryID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Rounds]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Rounds](
	[RoundID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[RoundName] [varchar](255) NULL,
	[RoundOrder] [tinyint] NOT NULL,
	[RoundStatusID] [uniqueidentifier] NOT NULL,
	[SubmissionDeadline] [datetime2](7) NULL,
	[JudgingDeadline] [datetime2](7) NULL,
	[StartDate] [datetime2](7) NULL,
	[EndDate] [datetime2](7) NULL,
	[AdvancementTopN] [int] NULL,
	[IsCalibrationRound] [bit] NOT NULL,
	[Description] [varchar](255) NULL,
	[AppealStartTime] [datetime2](7) NULL,
	[AppealEndTime] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[RoundID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Teams]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Teams](
	[TeamID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[TeamName] [nvarchar](300) NOT NULL,
	[TeamStatusID] [uniqueidentifier] NOT NULL,
	[LeaderUserID] [uniqueidentifier] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TeamID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SubmissionStatus]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SubmissionStatus](
	[StatusID] [uniqueidentifier] NOT NULL,
	[StatusName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[StatusID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Submissions]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Submissions](
	[SubmissionID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NULL,
	[RoundID] [uniqueidentifier] NOT NULL,
	[SubmissionStatusID] [uniqueidentifier] NOT NULL,
	[RepositoryURL] [nvarchar](500) NULL,
	[DemoURL] [nvarchar](500) NULL,
	[ReportURL] [nvarchar](500) NULL,
	[SlideURL] [nvarchar](500) NULL,
	[RepoMetadataJSON] [nvarchar](max) NULL,
	[RepoLastCommitAt] [datetime2](7) NULL,
	[RepoStarCount] [int] NULL,
	[RepoForkCount] [int] NULL,
	[SubmittedAt] [datetime2](7) NULL,
	[LastUpdatedAt] [datetime2](7) NOT NULL,
	[SubmittedByUserID] [uniqueidentifier] NOT NULL,
	[Notes] [nvarchar](max) NULL,
	[IsScoreApproved] [bit] NOT NULL,
	[IsSampleSubmission] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[SubmissionID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SubmissionHistory]    Script Date: 7/17/2026 12:45:00 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SubmissionHistory](
	[SubmissionHistoryID] [uniqueidentifier] NOT NULL CONSTRAINT [DF_SubmissionHistory_ID] DEFAULT (newid()),
	[SubmissionID] [uniqueidentifier] NOT NULL,
	[VersionNumber] [int] NOT NULL,
	[TeamID] [uniqueidentifier] NULL,
	[RoundID] [uniqueidentifier] NOT NULL,
	[SubmissionStatusID] [uniqueidentifier] NOT NULL,
	[RepositoryURL] [nvarchar](500) NULL,
	[DemoURL] [nvarchar](500) NULL,
	[ReportURL] [nvarchar](500) NULL,
	[SlideURL] [nvarchar](500) NULL,
	[RepoMetadataJSON] [nvarchar](max) NULL,
	[RepoLastCommitAt] [datetime2](7) NULL,
	[RepoStarCount] [int] NULL,
	[RepoForkCount] [int] NULL,
	[SubmittedAt] [datetime2](7) NULL,
	[LastUpdatedAt] [datetime2](7) NOT NULL,
	[SubmittedByUserID] [uniqueidentifier] NOT NULL,
	[Notes] [nvarchar](max) NULL,
	[IsScoreApproved] [bit] NOT NULL CONSTRAINT [DF_SubmissionHistory_IsScoreApproved] DEFAULT ((0)),
	[IsSampleSubmission] [bit] NOT NULL CONSTRAINT [DF_SubmissionHistory_IsSampleSubmission] DEFAULT ((0)),
	[SnapshotCreatedAt] [datetime2](7) NOT NULL CONSTRAINT [DF_SubmissionHistory_SnapshotCreatedAt] DEFAULT (getutcdate()),
 CONSTRAINT [PK_SubmissionHistory] PRIMARY KEY CLUSTERED 
(
	[SubmissionHistoryID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_SubmissionHistory_Submission_Version] UNIQUE NONCLUSTERED 
(
	[SubmissionID] ASC,
	[VersionNumber] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Users]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[UserID] [uniqueidentifier] NOT NULL,
	[Email] [nvarchar](255) NOT NULL,
	[PasswordHash] [nvarchar](512) NOT NULL,
	[FullName] [nvarchar](200) NOT NULL,
	[Phone] [nvarchar](20) NULL,
	[UserTypeID] [uniqueidentifier] NOT NULL,
	[AccountStatusID] [uniqueidentifier] NOT NULL,
	[FPTStudentCode] [nvarchar](20) NULL,
	[ExternalStudentCode] [nvarchar](50) NULL,
	[UniversityName] [nvarchar](200) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
	[ApprovedAt] [datetime2](7) NULL,
	[ApprovedByUserID] [uniqueidentifier] NULL,
	[AccountExpiresAt] [datetime2](7) NULL,
	[IsDeleted] [bit] NOT NULL,
	[LocalLoginEnabled] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[UserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_SubmissionDetails]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   VIEW [dbo].[vw_SubmissionDetails] AS
SELECT
    s.SubmissionID,
    e.EventID,
    e.EventName,
    r.RoundID,
    r.RoundName,
    r.RoundOrder,
    c.CategoryID,
    c.CategoryName,
    t.TeamID,
    t.TeamName,
    ss.StatusName AS SubmissionStatus,
    s.RepositoryURL,
    s.DemoURL,
    s.ReportURL,
    s.SlideURL,
    s.SubmittedAt,
    u.FullName AS SubmittedBy
FROM Submissions s
         JOIN Teams t ON t.TeamID = s.TeamID
         JOIN Categories c ON c.CategoryID = t.CategoryID
         JOIN Events e ON e.EventID = c.EventID  -- Lấy EventID qua Category
         JOIN Rounds r ON r.RoundID = s.RoundID
         JOIN SubmissionStatus ss ON ss.StatusID = s.SubmissionStatusID
         JOIN Users u ON u.UserID = s.SubmittedByUserID
GO
/****** Object:  Table [dbo].[EventCriteria]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[EventCriteria](
	[EventCriterionID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[TemplateID] [uniqueidentifier] NULL,
	[CriterionName] [nvarchar](200) NOT NULL,
	[Description] [varchar](255) NULL,
	[Weight] [decimal](5, 2) NOT NULL,
	[MaxScore] [decimal](6, 2) NOT NULL,
	[SortOrder] [tinyint] NOT NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[EventCriterionID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoundJudges]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoundJudges](
	[RoundJudgeID] [uniqueidentifier] NOT NULL,
	[RoundID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[AssignedAt] [datetime2](7) NOT NULL,
	[AssignedByID] [uniqueidentifier] NOT NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RoundJudgeID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoundCriteria]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoundCriteria](
	[RoundCriterionID] [uniqueidentifier] NOT NULL,
	[RoundID] [uniqueidentifier] NOT NULL,
	[EventCriterionID] [uniqueidentifier] NOT NULL,
	[Weight] [numeric](38, 2) NULL,
	[criterionName] [nvarchar](255) NULL,
	[description] [varchar](255) NULL,
	[MaxScore] [numeric](38, 2) NULL,
	[SortOrder] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[RoundCriterionID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserType]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserType](
	[UserTypeID] [uniqueidentifier] NOT NULL,
	[TypeName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[UserTypeID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Judging]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Judging](
	[JudgingID] [uniqueidentifier] NOT NULL,
	[SubmissionID] [uniqueidentifier] NOT NULL,
	[RoundJudgeID] [uniqueidentifier] NOT NULL,
	[RoundCriterionID] [uniqueidentifier] NOT NULL,
	[ScoreValue] [decimal](6, 2) NOT NULL,
	[Comment] [nvarchar](max) NULL,
	[JudgedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
	[IsCalibration] [bit] NOT NULL,
	[IsActive] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[JudgingID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_JudgeScoreSheet]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   VIEW [dbo].[vw_JudgeScoreSheet] AS
SELECT
    sc.JudgingID,
    e.EventID,
    e.EventName,
    r.RoundID,
    r.RoundName,
    t.TeamID,
    t.TeamName,
    c.CategoryName,
    s.SubmissionID,
    rj.UserID AS JudgeUserID,       -- Lấy từ bảng trung gian RoundJudges
    u.FullName AS JudgeName,
    ut.TypeName AS JudgeType,
    rc.RoundCriterionID,
    ec.EventCriterionID,
    ec.CriterionName,
    COALESCE(rc.Weight, ec.Weight) AS Weight,
    ec.MaxScore,
    sc.ScoreValue,
    sc.ScoreValue * COALESCE(rc.Weight, ec.Weight) AS WeightedScore,
    sc.Comment,
    sc.JudgedAt,
    sc.IsCalibration
FROM Judging sc
         JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
         JOIN Teams t ON t.TeamID = s.TeamID
         JOIN Categories c ON c.CategoryID = t.CategoryID
         JOIN Events e ON e.EventID = c.EventID
         JOIN Rounds r ON r.RoundID = s.RoundID
         JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID       -- Đi qua trung gian
         JOIN Users u ON u.UserID = rj.UserID
         JOIN UserType ut ON ut.UserTypeID = u.UserTypeID
         JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID -- Đi qua trung gian
         JOIN EventCriteria ec ON ec.EventCriterionID = rc.EventCriterionID
GO
/****** Object:  View [dbo].[vw_JudgeVariancePerCriterion]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   VIEW [dbo].[vw_JudgeVariancePerCriterion] AS
SELECT
    s.RoundID,
    rc.RoundCriterionID,
    ec.CriterionName,
    sc.SubmissionID,
    COUNT(DISTINCT rj.UserID) AS JudgeCount,
    AVG(sc.ScoreValue) AS MeanScore,
    STDEV(sc.ScoreValue) AS StdDevScore,
    MAX(sc.ScoreValue) - MIN(sc.ScoreValue) AS ScoreRange,
    VAR(sc.ScoreValue) AS VarianceScore
FROM Judging sc
         JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
         JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
         JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
         JOIN EventCriteria ec ON ec.EventCriterionID = rc.EventCriterionID
WHERE sc.IsCalibration = 0
GROUP BY s.RoundID, rc.RoundCriterionID, ec.CriterionName, sc.SubmissionID
GO
/****** Object:  Table [dbo].[TeamStatus]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TeamStatus](
	[StatusID] [uniqueidentifier] NOT NULL,
	[StatusName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[StatusID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoundRankings]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoundRankings](
	[RankingID] [uniqueidentifier] NOT NULL,
	[RoundID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[SubmissionID] [uniqueidentifier] NOT NULL,
	[TotalScore] [decimal](10, 4) NOT NULL,
	[AverageScore] [decimal](10, 4) NOT NULL,
	[RankPosition] [int] NOT NULL,
	[IsAdvanced] [bit] NOT NULL,
	[ComputedAt] [datetime2](7) NOT NULL,
	[IsPublished] [bit] NOT NULL,
	[IsApproved] [bit] NOT NULL DEFAULT 0,
PRIMARY KEY CLUSTERED 
(
	[RankingID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_RoundLeaderboard]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   VIEW [dbo].[vw_RoundLeaderboard] AS
SELECT
    rr.RoundID,
    r.RoundName,
    e.EventID,
    e.EventName,
    rr.CategoryID,
    c.CategoryName,
    rr.TeamID,
    t.TeamName,
    rr.TotalScore,
    rr.AverageScore,
    rr.RankPosition,
    rr.IsAdvanced,
    ts.StatusName AS TeamStatus
FROM RoundRankings rr
         JOIN Rounds r ON r.RoundID = rr.RoundID
         JOIN Categories c ON c.CategoryID = r.CategoryID
         JOIN Events e ON e.EventID = c.EventID
         JOIN Teams t ON t.TeamID = rr.TeamID
         JOIN TeamStatus ts ON ts.StatusID = t.TeamStatusID
GO
/****** Object:  View [dbo].[vw_AnonymizedScores]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   VIEW [dbo].[vw_AnonymizedScores] AS
SELECT
    sc.JudgingID,
    r.RoundID,
    r.RoundName,
    c.CategoryID,
    c.CategoryName,
    HASHBYTES('SHA2_256', CAST(sc.SubmissionID AS NVARCHAR(36))) AS AnonymousSubmissionID,
    HASHBYTES('SHA2_256', CAST(rj.UserID AS NVARCHAR(36))) AS AnonymousJudgeID, -- Hash từ UserID gốc
    ec.CriterionName,
    COALESCE(rc.Weight, ec.Weight) AS Weight,
    ec.MaxScore,
    sc.ScoreValue,
    sc.JudgedAt,
    sc.IsCalibration
FROM Judging sc
         JOIN Submissions s ON s.SubmissionID = sc.SubmissionID
         JOIN Teams t ON t.TeamID = s.TeamID
         JOIN Categories c ON c.CategoryID = t.CategoryID
         JOIN Rounds r ON r.RoundID = s.RoundID
         JOIN RoundJudges rj ON rj.RoundJudgeID = sc.RoundJudgeID
         JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID
         JOIN EventCriteria ec ON ec.EventCriterionID = rc.EventCriterionID
GO
/****** Object:  Table [dbo].[AccountStatus]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AccountStatus](
	[StatusID] [uniqueidentifier] NOT NULL,
	[StatusName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[StatusID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[AuditLog]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AuditLog](
	[LogID] [uniqueidentifier] NOT NULL,
	[ActionType] [nvarchar](100) NOT NULL,
	[EntityType] [nvarchar](100) NOT NULL,
	[EntityID] [uniqueidentifier] NULL,
	[EntityKey] [nvarchar](200) NULL,
	[ActorUserID] [uniqueidentifier] NULL,
	[OldValueJSON] [varchar](255) NULL,
	[NewValueJSON] [varchar](255) NULL,
	[IPAddress] [nvarchar](50) NULL,
	[OccurredAt] [datetime2](7) NOT NULL,
	[Notes] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[LogID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[AwardPatterns]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AwardPatterns](
	[PatternID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[RankPosition] [int] NOT NULL,
	[AwardTierID] [uniqueidentifier] NOT NULL,
	[AwardTitle] [nvarchar](300) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[PrizeValue] [decimal](12, 2) NULL,
	[PrizeCurrency] [nchar](3) NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[PatternID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Awards]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Awards](
	[AwardID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[AwardTierID] [uniqueidentifier] NOT NULL,
	[AwardTitle] [nvarchar](300) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[PrizeValue] [decimal](12, 2) NULL,
	[PrizeCurrency] [nchar](3) NULL,
	[AwardedAt] [datetime2](7) NOT NULL,
	[AwardedByID] [uniqueidentifier] NOT NULL,
	[IsPublished] [bit] NOT NULL,
	[PublishedAt] [datetimeoffset](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[AwardID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[AwardTier]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AwardTier](
	[TierID] [uniqueidentifier] NOT NULL,
	[TierName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TierID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CalibrationSamples]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CalibrationSamples](
	[SampleID] [uniqueidentifier] NOT NULL,
	[RoundID] [uniqueidentifier] NOT NULL,
	[SubmissionID] [uniqueidentifier] NOT NULL,
	[ReferenceScoreJSON] [nvarchar](max) NULL,
	[AddedByID] [uniqueidentifier] NOT NULL,
	[AddedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[SampleID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CategoryMentors]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CategoryMentors](
	[CategoryMentorID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[MentorUserID] [uniqueidentifier] NOT NULL,
	[AssignedAt] [datetime2](7) NOT NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[CategoryMentorID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Certificates]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Certificates](
	[CertificateID] [uniqueidentifier] NOT NULL,
	[AwardID] [uniqueidentifier] NOT NULL,
	[CertificateCode] [nvarchar](100) NOT NULL,
	[GeneratedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[CertificateID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ConsultationMessages]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ConsultationMessages](
	[MessageID] [uniqueidentifier] NOT NULL,
	[RequestID] [uniqueidentifier] NOT NULL,
	[SenderID] [uniqueidentifier] NOT NULL,
	[Content] [nvarchar](max) NULL,
	[AttachmentUrl] [nvarchar](500) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[SeenAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[MessageID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ConsultationRequests]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ConsultationRequests](
	[RequestID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[MentorUserID] [uniqueidentifier] NOT NULL,
	[CreatedByUserID] [uniqueidentifier] NOT NULL,
	[Title] [nvarchar](150) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[Priority] [nvarchar](20) NOT NULL,
	[Status] [nvarchar](20) NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
	[ClosedAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[RequestID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CriterionTemplate]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CriterionTemplate](
	[TemplateID] [uniqueidentifier] NOT NULL,
	[CriterionName] [varchar](255) NULL,
	[Description] [varchar](255) NULL,
	[DefaultWeight] [decimal](5, 2) NOT NULL,
	[MaxScore] [decimal](6, 2) NOT NULL,
	[IsActive] [bit] NOT NULL,
	[CreatedByID] [uniqueidentifier] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TemplateID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DataExportLog]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DataExportLog](
	[ExportID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[ExportedByID] [uniqueidentifier] NOT NULL,
	[ExportedAt] [datetime2](7) NOT NULL,
	[FileFormat] [nvarchar](10) NOT NULL,
	[RowCount] [int] NULL,
	[Notes] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[ExportID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Disqualifications]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Disqualifications](
	[DisqualificationID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NULL,
	[SubmissionID] [uniqueidentifier] NULL,
	[Reason] [nvarchar](max) NULL,
	[DisqualifiedByID] [uniqueidentifier] NOT NULL,
	[DisqualifiedAt] [datetime2](7) NOT NULL,
	[IsReversed] [bit] NOT NULL,
	[ReversedAt] [datetime2](7) NULL,
	[ReversedByID] [uniqueidentifier] NULL,
	[ReversalReason] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
	[DisqualificationID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[EvaluationAuditLogs]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[EvaluationAuditLogs](
	[EvaluationAuditLogID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[ActionType] [nvarchar](50) NOT NULL,
	[ActorUserID] [uniqueidentifier] NOT NULL,
	[JudgingID] [uniqueidentifier] NULL,
	[TeamID] [uniqueidentifier] NULL,
	[SubmissionID] [uniqueidentifier] NULL,
	[OldValue] [nvarchar](max) NULL,
	[NewValue] [nvarchar](max) NULL,
	[Reason] [nvarchar](max) NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[EvaluationAuditLogID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[EventParticipants]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[EventParticipants](
	[EventParticipantID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[ParticipantStatusID] [uniqueidentifier] NOT NULL,
	[AppliedAt] [datetime2](7) NOT NULL,
	[ApprovedAt] [datetime2](7) NULL,
	[ApprovedBy] [uniqueidentifier] NULL,
	[RejectedReason] [nvarchar](1000) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_EventParticipants] PRIMARY KEY CLUSTERED 
(
	[EventParticipantID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[EventRankings]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[EventRankings](
	[EventRankingID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NOT NULL,
	[CategoryID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[FinalScore] [decimal](10, 4) NOT NULL,
	[RankPosition] [int] NOT NULL,
	[ComputedAt] [datetime2](7) NOT NULL,
	[IsPublished] [bit] NOT NULL,
	[IsApproved] [bit] NOT NULL DEFAULT 0,
PRIMARY KEY CLUSTERED 
(
	[EventRankingID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[EventStatus]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[EventStatus](
	[StatusID] [uniqueidentifier] NOT NULL,
	[StatusName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[StatusID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Notifications]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Notifications](
	[NotificationID] [uniqueidentifier] NOT NULL,
	[EventID] [uniqueidentifier] NULL,
	[RecipientUserID] [uniqueidentifier] NULL,
	[Title] [nvarchar](300) NOT NULL,
	[Body] [nvarchar](max) NOT NULL,
	[SentAt] [datetime2](7) NOT NULL,
	[SentByUserID] [uniqueidentifier] NOT NULL,
	[IsRead] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[NotificationID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ParticipantStatus]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ParticipantStatus](
	[StatusID] [uniqueidentifier] NOT NULL,
	[StatusName] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_ParticipantStatus] PRIMARY KEY CLUSTERED 
(
	[StatusID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PasswordResetTokens]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PasswordResetTokens](
	[TokenID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[TokenHash] [nvarchar](512) NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[ExpiresAt] [datetime2](7) NOT NULL,
	[UsedAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[TokenID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RefreshTokens]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RefreshTokens](
	[TokenID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[TokenHash] [nvarchar](512) NOT NULL,
	[IssuedAt] [datetime2](7) NOT NULL,
	[ExpiresAt] [datetime2](7) NOT NULL,
	[RevokedAt] [datetime2](7) NULL,
	[DeviceInfo] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[TokenID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RoundStatus]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoundStatus](
	[StatusID] [uniqueidentifier] NOT NULL,
	[StatusName] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[StatusID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SystemSettings]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SystemSettings](
	[SettingKey] [nvarchar](100) NOT NULL,
	[SettingValue] [nvarchar](1000) NOT NULL,
	[SettingType] [nvarchar](20) NULL,
	[Description] [nvarchar](500) NULL,
	[UpdatedAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[SettingKey] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TeamJoinRequests]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TeamJoinRequests](
	[RequestID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[RequestStatus] [nvarchar](20) NOT NULL,
	[RequestedAt] [datetime2](7) NOT NULL,
	[RespondedAt] [datetime2](7) NULL,
	[RespondedByID] [uniqueidentifier] NULL,
	[ResponseNote] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[RequestID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TeamMembers]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TeamMembers](
	[TeamMemberID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[JoinedAt] [datetime2](7) NOT NULL,
	[LeftAt] [datetime2](7) NULL,
	[IsActive] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TeamMemberID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TeamMilestones]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TeamMilestones](
	[MilestoneID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[MentorUserID] [uniqueidentifier] NOT NULL,
	[Label] [nvarchar](255) NOT NULL,
	[IsDone] [bit] NOT NULL,
	[SortOrder] [int] NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[MilestoneID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UserOAuthAccounts]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserOAuthAccounts](
	[OAuthAccountID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[Provider] [nvarchar](50) NOT NULL,
	[ProviderUserID] [nvarchar](255) NOT NULL,
	[Email] [nvarchar](255) NULL,
	[EmailVerified] [bit] NOT NULL,
	[DisplayName] [nvarchar](255) NULL,
	[AvatarUrl] [nvarchar](1000) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[UpdatedAt] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_UserOAuthAccounts] PRIMARY KEY CLUSTERED 
(
	[OAuthAccountID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[VerificationTokens]    Script Date: 7/14/2026 9:01:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[VerificationTokens](
	[TokenID] [uniqueidentifier] NOT NULL,
	[UserID] [uniqueidentifier] NOT NULL,
	[TokenHash] [nvarchar](512) NOT NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
	[ExpiresAt] [datetime2](7) NOT NULL,
	[UsedAt] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[TokenID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
-- Base lookup/config seed only; demo users, events, teams, submissions, judging and audit data are intentionally omitted.
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000002', N'Active')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000001', N'Pending Approval')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000003', N'Rejected')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000004', N'Suspended')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000005', N'Temporary')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000006', N'Unverified')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000005', N'Best Innovation')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000006', N'Best Presentation')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000001', N'First Place')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000004', N'Honorable Mention')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000002', N'Second Place')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000007', N'Special Award')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000003', N'Third Place')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000005', N'Cancelled')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000004', N'Completed')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000001', N'Draft')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000003', N'Ongoing')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000002', N'Registration Open')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000002', N'ACTIVE')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000001', N'PENDING')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000003', N'REJECTED')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000004', N'SUSPENDED')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000005', N'TEMPORARY')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000006', N'UNVERIFIED')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000004', N'Completed')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000003', N'Judging')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000002', N'Submission Open')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000001', N'Upcoming')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000004', N'Disqualified')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000001', N'Draft')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000006', N'In Progress')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000005', N'Scored')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000002', N'Submitted')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000003', N'Under Review')
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'allowLateSubmissions', N'true', N'BOOLEAN', N'Allow submissions after deadline', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'contactEmail', N'seal@fpt.edu.vn', N'STRING', N'Platform support contact email', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'enablePublicLeaderboard', N'true', N'BOOLEAN', N'Show leaderboard to public', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'maxTeamSize', N'5', N'INTEGER', N'Maximum allowed team members', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'minTeamSize', N'2', N'INTEGER', N'Minimum required team members', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'platformName', N'SEAL FPT Hackathon Platform', N'STRING', N'Display name of the platform', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'requireEmailVerification', N'true', N'BOOLEAN', N'Require email verification on register', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'submissionGracePeriod', N'30', N'INTEGER', N'Grace period in minutes after deadline', NULL)
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000002', N'Active')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000003', N'Disqualified')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000001', N'Forming')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000004', N'Withdrawn')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000005', N'Pending')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000006', N'Rejected')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'11111111-1111-1111-1111-111111111111', N'Admin')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'55555555-5555-5555-5555-555555555555', N'Competitor')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'33333333-3333-3333-3333-333333333333', N'Expert')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'10000000-0000-0000-0000-000000000002', N'External Student')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'10000000-0000-0000-0000-000000000001', N'FPT Student')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'10000000-0000-0000-0000-000000000005', N'Guest Judge')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'10000000-0000-0000-0000-000000000004', N'Internal Judge')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'44444444-4444-4444-4444-444444444444', N'Judge')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'10000000-0000-0000-0000-000000000007', N'Mentor')
INSERT [dbo].[UserType] ([UserTypeID], [TypeName]) VALUES (N'10000000-0000-0000-0000-000000000003', N'Organizer')
GO
ALTER TABLE [dbo].[AccountStatus] ADD  CONSTRAINT [UKon5lvd4jlhyt5pyp5npbj6cv9] UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__AccountS__05E7698A5EBD0195]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[AccountStatus] ADD UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_AwardPatterns_Category_Rank]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[AwardPatterns] ADD  CONSTRAINT [UQ_AwardPatterns_Category_Rank] UNIQUE NONCLUSTERED 
(
	[CategoryID] ASC,
	[RankPosition] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__AwardTie__DB82ACA15DB9DEFF]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[AwardTier] ADD UNIQUE NONCLUSTERED 
(
	[TierName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_Categories_Event_Name]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[Categories] ADD  CONSTRAINT [UQ_Categories_Event_Name] UNIQUE NONCLUSTERED 
(
	[EventID] ASC,
	[CategoryName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_CategoryMentors]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[CategoryMentors] ADD  CONSTRAINT [UQ_CategoryMentors] UNIQUE NONCLUSTERED 
(
	[CategoryID] ASC,
	[MentorUserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Certific__9B85583070C066CE]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[Certificates] ADD UNIQUE NONCLUSTERED 
(
	[CertificateCode] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Certific__B08935DF3D3F9069]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[Certificates] ADD UNIQUE NONCLUSTERED 
(
	[AwardID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_EventCriteria_Event_Name]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[EventCriteria] ADD  CONSTRAINT [UQ_EventCriteria_Event_Name] UNIQUE NONCLUSTERED 
(
	[EventID] ASC,
	[CriterionName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_EventParticipants_Event_User]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[EventParticipants] ADD  CONSTRAINT [UQ_EventParticipants_Event_User] UNIQUE NONCLUSTERED 
(
	[EventID] ASC,
	[UserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_EventRankings]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[EventRankings] ADD  CONSTRAINT [UQ_EventRankings] UNIQUE NONCLUSTERED 
(
	[EventID] ASC,
	[CategoryID] ASC,
	[TeamID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__EventSta__05E7698AFC73DF5F]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[EventStatus] ADD UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_Judging_Sub_Judge_Criterion]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[Judging] ADD  CONSTRAINT [UQ_Judging_Sub_Judge_Criterion] UNIQUE NONCLUSTERED 
(
	[SubmissionID] ASC,
	[RoundJudgeID] ASC,
	[RoundCriterionID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_ParticipantStatus_Name]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[ParticipantStatus] ADD  CONSTRAINT [UQ_ParticipantStatus_Name] UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Password__BCB33F92EB758CED]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[PasswordResetTokens] ADD UNIQUE NONCLUSTERED 
(
	[TokenHash] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__RefreshT__BCB33F9215C99F4F]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[RefreshTokens] ADD UNIQUE NONCLUSTERED 
(
	[TokenHash] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_RoundCriteria]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[RoundCriteria] ADD  CONSTRAINT [UQ_RoundCriteria] UNIQUE NONCLUSTERED 
(
	[RoundID] ASC,
	[EventCriterionID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_RoundJudges]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[RoundJudges] ADD  CONSTRAINT [UQ_RoundJudges] UNIQUE NONCLUSTERED 
(
	[RoundID] ASC,
	[UserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_RoundRankings]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[RoundRankings] ADD  CONSTRAINT [UQ_RoundRankings] UNIQUE NONCLUSTERED 
(
	[RoundID] ASC,
	[CategoryID] ASC,
	[TeamID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_Rounds_Category_Order]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[Rounds] ADD  CONSTRAINT [UQ_Rounds_Category_Order] UNIQUE NONCLUSTERED 
(
	[CategoryID] ASC,
	[RoundOrder] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__RoundSta__05E7698A283A7BE9]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[RoundStatus] ADD UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_Submissions_Team_Round]    Script Date: 7/17/2026 12:45:00 AM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_Submissions_Team_Round] ON [dbo].[Submissions]
(
	[TeamID] ASC,
	[RoundID] ASC
)
WHERE [IsSampleSubmission] = 0 AND [TeamID] IS NOT NULL
WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_SubmissionHistory_SubmissionID_Version]    Script Date: 7/17/2026 12:45:00 AM ******/
CREATE NONCLUSTERED INDEX [IX_SubmissionHistory_SubmissionID_Version] ON [dbo].[SubmissionHistory]
(
	[SubmissionID] ASC,
	[VersionNumber] DESC
)WITH (STATISTICS_NORECOMPUTE = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_SubmissionHistory_Team_Round_Version]    Script Date: 7/17/2026 12:45:00 AM ******/
CREATE NONCLUSTERED INDEX [IX_SubmissionHistory_Team_Round_Version] ON [dbo].[SubmissionHistory]
(
	[TeamID] ASC,
	[RoundID] ASC,
	[VersionNumber] DESC
)WITH (STATISTICS_NORECOMPUTE = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Submissi__05E7698A4F949C5C]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[SubmissionStatus] ADD UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_TeamJoinRequests_Pending]    Script Date: 7/17/2026 12:45:00 AM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_TeamJoinRequests_Pending] ON [dbo].[TeamJoinRequests]
(
	[TeamID] ASC,
	[UserID] ASC
)
WHERE [RequestStatus] = N'PENDING'
WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_TeamMembers]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[TeamMembers] ADD  CONSTRAINT [UQ_TeamMembers] UNIQUE NONCLUSTERED 
(
	[TeamID] ASC,
	[UserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_Teams_Event_Name_NotRejected]    Script Date: 7/17/2026 12:45:00 AM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_Teams_Event_Name_NotRejected] ON [dbo].[Teams]
(
	[EventID] ASC,
	[TeamName] ASC
)
WHERE [TeamStatusID] <> '60000000-0000-0000-0000-000000000006'
WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__TeamStat__05E7698A11D78BE0]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[TeamStatus] ADD UNIQUE NONCLUSTERED 
(
	[StatusName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UKmjdh3rsktps3lho5jbvi6kon5]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [UKmjdh3rsktps3lho5jbvi6kon5] UNIQUE NONCLUSTERED 
(
	[Provider] ASC,
	[ProviderUserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UKrvqf76s09ovntn7dadqllq9v6]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [UKrvqf76s09ovntn7dadqllq9v6] UNIQUE NONCLUSTERED 
(
	[UserID] ASC,
	[Provider] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_UserOAuthAccounts_Provider_User]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [UQ_UserOAuthAccounts_Provider_User] UNIQUE NONCLUSTERED 
(
	[Provider] ASC,
	[ProviderUserID] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_UserOAuthAccounts_User_Provider]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [UQ_UserOAuthAccounts_User_Provider] UNIQUE NONCLUSTERED 
(
	[UserID] ASC,
	[Provider] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UKlw4x3jwjvnku0f2srr889g90n]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[UserType] ADD  CONSTRAINT [UKlw4x3jwjvnku0f2srr889g90n] UNIQUE NONCLUSTERED 
(
	[TypeName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__UserType__D4E7DFA89CD91E78]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[UserType] ADD UNIQUE NONCLUSTERED 
(
	[TypeName] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Verifica__BCB33F92F94DF2AC]    Script Date: 7/14/2026 9:01:57 PM ******/
ALTER TABLE [dbo].[VerificationTokens] ADD UNIQUE NONCLUSTERED 
(
	[TokenHash] ASC
)WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[AccountStatus] ADD  DEFAULT (newid()) FOR [StatusID]
GO
ALTER TABLE [dbo].[AuditLog] ADD  DEFAULT (newid()) FOR [LogID]
GO
ALTER TABLE [dbo].[AuditLog] ADD  DEFAULT (getutcdate()) FOR [OccurredAt]
GO
ALTER TABLE [dbo].[AwardPatterns] ADD  DEFAULT (newid()) FOR [PatternID]
GO
ALTER TABLE [dbo].[AwardPatterns] ADD  DEFAULT ('VND') FOR [PrizeCurrency]
GO
ALTER TABLE [dbo].[AwardPatterns] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[Awards] ADD  DEFAULT (newid()) FOR [AwardID]
GO
ALTER TABLE [dbo].[Awards] ADD  DEFAULT ('VND') FOR [PrizeCurrency]
GO
ALTER TABLE [dbo].[Awards] ADD  DEFAULT (getutcdate()) FOR [AwardedAt]
GO
ALTER TABLE [dbo].[Awards] ADD  DEFAULT ((0)) FOR [IsPublished]
GO
ALTER TABLE [dbo].[AwardTier] ADD  DEFAULT (newid()) FOR [TierID]
GO
ALTER TABLE [dbo].[CalibrationSamples] ADD  DEFAULT (newid()) FOR [SampleID]
GO
ALTER TABLE [dbo].[CalibrationSamples] ADD  DEFAULT (getutcdate()) FOR [AddedAt]
GO
ALTER TABLE [dbo].[Categories] ADD  DEFAULT (newid()) FOR [CategoryID]
GO
ALTER TABLE [dbo].[Categories] ADD  DEFAULT ((0)) FOR [SortOrder]
GO
ALTER TABLE [dbo].[Categories] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[CategoryMentors] ADD  DEFAULT (newid()) FOR [CategoryMentorID]
GO
ALTER TABLE [dbo].[CategoryMentors] ADD  DEFAULT (getutcdate()) FOR [AssignedAt]
GO
ALTER TABLE [dbo].[CategoryMentors] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[Certificates] ADD  DEFAULT (newid()) FOR [CertificateID]
GO
ALTER TABLE [dbo].[Certificates] ADD  DEFAULT (getutcdate()) FOR [GeneratedAt]
GO
ALTER TABLE [dbo].[ConsultationMessages] ADD  DEFAULT (newid()) FOR [MessageID]
GO
ALTER TABLE [dbo].[ConsultationMessages] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[ConsultationRequests] ADD  DEFAULT (newid()) FOR [RequestID]
GO
ALTER TABLE [dbo].[ConsultationRequests] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[ConsultationRequests] ADD  DEFAULT (getutcdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[CriterionTemplate] ADD  DEFAULT (newid()) FOR [TemplateID]
GO
ALTER TABLE [dbo].[CriterionTemplate] ADD  DEFAULT ((1.00)) FOR [DefaultWeight]
GO
ALTER TABLE [dbo].[CriterionTemplate] ADD  DEFAULT ((10.00)) FOR [MaxScore]
GO
ALTER TABLE [dbo].[CriterionTemplate] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[CriterionTemplate] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[DataExportLog] ADD  DEFAULT (newid()) FOR [ExportID]
GO
ALTER TABLE [dbo].[DataExportLog] ADD  DEFAULT (getutcdate()) FOR [ExportedAt]
GO
ALTER TABLE [dbo].[DataExportLog] ADD  DEFAULT ('CSV') FOR [FileFormat]
GO
ALTER TABLE [dbo].[Disqualifications] ADD  DEFAULT (newid()) FOR [DisqualificationID]
GO
ALTER TABLE [dbo].[Disqualifications] ADD  DEFAULT (getutcdate()) FOR [DisqualifiedAt]
GO
ALTER TABLE [dbo].[Disqualifications] ADD  DEFAULT ((0)) FOR [IsReversed]
GO
ALTER TABLE [dbo].[EvaluationAuditLogs] ADD  DEFAULT (newid()) FOR [EvaluationAuditLogID]
GO
ALTER TABLE [dbo].[EvaluationAuditLogs] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[EventCriteria] ADD  DEFAULT (newid()) FOR [EventCriterionID]
GO
ALTER TABLE [dbo].[EventCriteria] ADD  DEFAULT ((1.00)) FOR [Weight]
GO
ALTER TABLE [dbo].[EventCriteria] ADD  DEFAULT ((10.00)) FOR [MaxScore]
GO
ALTER TABLE [dbo].[EventCriteria] ADD  DEFAULT ((0)) FOR [SortOrder]
GO
ALTER TABLE [dbo].[EventCriteria] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[EventParticipants] ADD  CONSTRAINT [DF_EventParticipants_ID]  DEFAULT (newid()) FOR [EventParticipantID]
GO
ALTER TABLE [dbo].[EventParticipants] ADD  CONSTRAINT [DF_EventParticipants_Status]  DEFAULT ('80000000-0000-0000-0000-000000000001') FOR [ParticipantStatusID]
GO
ALTER TABLE [dbo].[EventParticipants] ADD  CONSTRAINT [DF_EventParticipants_AppliedAt]  DEFAULT (sysutcdatetime()) FOR [AppliedAt]
GO
ALTER TABLE [dbo].[EventParticipants] ADD  CONSTRAINT [DF_EventParticipants_CreatedAt]  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[EventParticipants] ADD  CONSTRAINT [DF_EventParticipants_UpdatedAt]  DEFAULT (sysutcdatetime()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[EventRankings] ADD  DEFAULT (newid()) FOR [EventRankingID]
GO
ALTER TABLE [dbo].[EventRankings] ADD  DEFAULT (getutcdate()) FOR [ComputedAt]
GO
ALTER TABLE [dbo].[EventRankings] ADD  DEFAULT ((0)) FOR [IsPublished]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT (newid()) FOR [EventID]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT ('30000000-0000-0000-0000-000000000001') FOR [EventStatusID]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT ((5)) FOR [MaxTeamSize]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT ((3)) FOR [MinTeamSize]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT (getutcdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[Events] ADD  DEFAULT ((0)) FOR [IsDeleted]
GO
ALTER TABLE [dbo].[EventStatus] ADD  DEFAULT (newid()) FOR [StatusID]
GO
ALTER TABLE [dbo].[Judging] ADD  DEFAULT (newid()) FOR [JudgingID]
GO
ALTER TABLE [dbo].[Judging] ADD  DEFAULT (getutcdate()) FOR [JudgedAt]
GO
ALTER TABLE [dbo].[Judging] ADD  DEFAULT (getutcdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[Judging] ADD  DEFAULT ((0)) FOR [IsCalibration]
GO
ALTER TABLE [dbo].[Notifications] ADD  DEFAULT (newid()) FOR [NotificationID]
GO
ALTER TABLE [dbo].[Notifications] ADD  DEFAULT (getutcdate()) FOR [SentAt]
GO
ALTER TABLE [dbo].[Notifications] ADD  DEFAULT ((0)) FOR [IsRead]
GO
ALTER TABLE [dbo].[ParticipantStatus] ADD  CONSTRAINT [DF_ParticipantStatus_ID]  DEFAULT (newid()) FOR [StatusID]
GO
ALTER TABLE [dbo].[PasswordResetTokens] ADD  DEFAULT (newid()) FOR [TokenID]
GO
ALTER TABLE [dbo].[PasswordResetTokens] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[RefreshTokens] ADD  DEFAULT (newid()) FOR [TokenID]
GO
ALTER TABLE [dbo].[RefreshTokens] ADD  DEFAULT (getutcdate()) FOR [IssuedAt]
GO
ALTER TABLE [dbo].[RoundCriteria] ADD  DEFAULT (newid()) FOR [RoundCriterionID]
GO
ALTER TABLE [dbo].[RoundJudges] ADD  DEFAULT (newid()) FOR [RoundJudgeID]
GO
ALTER TABLE [dbo].[RoundJudges] ADD  DEFAULT (getutcdate()) FOR [AssignedAt]
GO
ALTER TABLE [dbo].[RoundJudges] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[RoundRankings] ADD  DEFAULT (newid()) FOR [RankingID]
GO
ALTER TABLE [dbo].[RoundRankings] ADD  DEFAULT ((0)) FOR [IsAdvanced]
GO
ALTER TABLE [dbo].[RoundRankings] ADD  DEFAULT (getutcdate()) FOR [ComputedAt]
GO
ALTER TABLE [dbo].[RoundRankings] ADD  DEFAULT ((0)) FOR [IsPublished]
GO
ALTER TABLE [dbo].[Rounds] ADD  DEFAULT (newid()) FOR [RoundID]
GO
ALTER TABLE [dbo].[Rounds] ADD  DEFAULT ('40000000-0000-0000-0000-000000000001') FOR [RoundStatusID]
GO
ALTER TABLE [dbo].[Rounds] ADD  DEFAULT ((0)) FOR [IsCalibrationRound]
GO
ALTER TABLE [dbo].[RoundStatus] ADD  DEFAULT (newid()) FOR [StatusID]
GO
ALTER TABLE [dbo].[Submissions] ADD  DEFAULT (newid()) FOR [SubmissionID]
GO
ALTER TABLE [dbo].[Submissions] ADD  DEFAULT ('50000000-0000-0000-0000-000000000001') FOR [SubmissionStatusID]
GO
ALTER TABLE [dbo].[Submissions] ADD  DEFAULT (getutcdate()) FOR [LastUpdatedAt]
GO
ALTER TABLE [dbo].[Submissions] ADD  DEFAULT ((0)) FOR [IsScoreApproved]
GO
ALTER TABLE [dbo].[Submissions] ADD  CONSTRAINT [DF_Submissions_IsSampleSubmission]  DEFAULT ((0)) FOR [IsSampleSubmission]
GO
ALTER TABLE [dbo].[SubmissionStatus] ADD  DEFAULT (newid()) FOR [StatusID]
GO
ALTER TABLE [dbo].[TeamJoinRequests] ADD  DEFAULT (newid()) FOR [RequestID]
GO
ALTER TABLE [dbo].[TeamJoinRequests] ADD  DEFAULT (N'PENDING') FOR [RequestStatus]
GO
ALTER TABLE [dbo].[TeamJoinRequests] ADD  DEFAULT (getutcdate()) FOR [RequestedAt]
GO
ALTER TABLE [dbo].[TeamMembers] ADD  DEFAULT (newid()) FOR [TeamMemberID]
GO
ALTER TABLE [dbo].[TeamMembers] ADD  DEFAULT (getutcdate()) FOR [JoinedAt]
GO
ALTER TABLE [dbo].[TeamMembers] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[TeamMilestones] ADD  DEFAULT (newid()) FOR [MilestoneID]
GO
ALTER TABLE [dbo].[TeamMilestones] ADD  DEFAULT ((0)) FOR [IsDone]
GO
ALTER TABLE [dbo].[TeamMilestones] ADD  DEFAULT ((0)) FOR [SortOrder]
GO
ALTER TABLE [dbo].[TeamMilestones] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[TeamMilestones] ADD  DEFAULT (getdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[Teams] ADD  DEFAULT (newid()) FOR [TeamID]
GO
ALTER TABLE [dbo].[Teams] ADD  DEFAULT ('60000000-0000-0000-0000-000000000001') FOR [TeamStatusID]
GO
ALTER TABLE [dbo].[Teams] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Teams] ADD  DEFAULT (getutcdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[TeamStatus] ADD  DEFAULT (newid()) FOR [StatusID]
GO
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [DF_UserOAuthAccounts_ID]  DEFAULT (newid()) FOR [OAuthAccountID]
GO
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [DF_UserOAuthAccounts_EmailVerified]  DEFAULT ((0)) FOR [EmailVerified]
GO
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [DF_UserOAuthAccounts_CreatedAt]  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[UserOAuthAccounts] ADD  CONSTRAINT [DF_UserOAuthAccounts_UpdatedAt]  DEFAULT (getutcdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT (newid()) FOR [UserID]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT ('20000000-0000-0000-0000-000000000001') FOR [AccountStatusID]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT (getutcdate()) FOR [UpdatedAt]
GO
ALTER TABLE [dbo].[Users] ADD  DEFAULT ((0)) FOR [IsDeleted]
GO
ALTER TABLE [dbo].[Users] ADD  CONSTRAINT [DF_Users_LocalLoginEnabled]  DEFAULT ((1)) FOR [LocalLoginEnabled]
GO
ALTER TABLE [dbo].[UserType] ADD  DEFAULT (newid()) FOR [UserTypeID]
GO
ALTER TABLE [dbo].[VerificationTokens] ADD  DEFAULT (newid()) FOR [TokenID]
GO
ALTER TABLE [dbo].[VerificationTokens] ADD  DEFAULT (getutcdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[AuditLog]  WITH CHECK ADD FOREIGN KEY([ActorUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[AwardPatterns]  WITH CHECK ADD FOREIGN KEY([AwardTierID])
REFERENCES [dbo].[AwardTier] ([TierID])
GO
ALTER TABLE [dbo].[AwardPatterns]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[AwardPatterns]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[Awards]  WITH CHECK ADD FOREIGN KEY([AwardedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Awards]  WITH CHECK ADD FOREIGN KEY([AwardTierID])
REFERENCES [dbo].[AwardTier] ([TierID])
GO
ALTER TABLE [dbo].[Awards]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[Awards]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[Awards]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[CalibrationSamples]  WITH CHECK ADD FOREIGN KEY([AddedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[CalibrationSamples]  WITH CHECK ADD FOREIGN KEY([RoundID])
REFERENCES [dbo].[Rounds] ([RoundID])
GO
ALTER TABLE [dbo].[CalibrationSamples]  WITH CHECK ADD FOREIGN KEY([SubmissionID])
REFERENCES [dbo].[Submissions] ([SubmissionID])
GO
ALTER TABLE [dbo].[Categories]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[CategoryMentors]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[CategoryMentors]  WITH CHECK ADD FOREIGN KEY([MentorUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Certificates]  WITH CHECK ADD FOREIGN KEY([AwardID])
REFERENCES [dbo].[Awards] ([AwardID])
GO
ALTER TABLE [dbo].[ConsultationMessages]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationMessage_Request] FOREIGN KEY([RequestID])
REFERENCES [dbo].[ConsultationRequests] ([RequestID])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ConsultationMessages] CHECK CONSTRAINT [FK_ConsultationMessage_Request]
GO
ALTER TABLE [dbo].[ConsultationMessages]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationMessage_Sender] FOREIGN KEY([SenderID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[ConsultationMessages] CHECK CONSTRAINT [FK_ConsultationMessage_Sender]
GO
ALTER TABLE [dbo].[ConsultationRequests]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationRequest_Category] FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[ConsultationRequests] CHECK CONSTRAINT [FK_ConsultationRequest_Category]
GO
ALTER TABLE [dbo].[ConsultationRequests]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationRequest_CreatedBy] FOREIGN KEY([CreatedByUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[ConsultationRequests] CHECK CONSTRAINT [FK_ConsultationRequest_CreatedBy]
GO
ALTER TABLE [dbo].[ConsultationRequests]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationRequest_Event] FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[ConsultationRequests] CHECK CONSTRAINT [FK_ConsultationRequest_Event]
GO
ALTER TABLE [dbo].[ConsultationRequests]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationRequest_Mentor] FOREIGN KEY([MentorUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[ConsultationRequests] CHECK CONSTRAINT [FK_ConsultationRequest_Mentor]
GO
ALTER TABLE [dbo].[ConsultationRequests]  WITH CHECK ADD  CONSTRAINT [FK_ConsultationRequest_Team] FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[ConsultationRequests] CHECK CONSTRAINT [FK_ConsultationRequest_Team]
GO
ALTER TABLE [dbo].[CriterionTemplate]  WITH CHECK ADD FOREIGN KEY([CreatedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[DataExportLog]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[DataExportLog]  WITH CHECK ADD FOREIGN KEY([ExportedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Disqualifications]  WITH CHECK ADD FOREIGN KEY([DisqualifiedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Disqualifications]  WITH CHECK ADD FOREIGN KEY([ReversedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Disqualifications]  WITH CHECK ADD FOREIGN KEY([SubmissionID])
REFERENCES [dbo].[Submissions] ([SubmissionID])
GO
ALTER TABLE [dbo].[Disqualifications]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD FOREIGN KEY([ActorUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD FOREIGN KEY([JudgingID])
REFERENCES [dbo].[Judging] ([JudgingID])
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD FOREIGN KEY([SubmissionID])
REFERENCES [dbo].[Submissions] ([SubmissionID])
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[EventCriteria]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[EventCriteria]  WITH CHECK ADD FOREIGN KEY([TemplateID])
REFERENCES [dbo].[CriterionTemplate] ([TemplateID])
GO
ALTER TABLE [dbo].[EventParticipants]  WITH CHECK ADD  CONSTRAINT [FK_EventParticipants_ApprovedBy] FOREIGN KEY([ApprovedBy])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[EventParticipants] CHECK CONSTRAINT [FK_EventParticipants_ApprovedBy]
GO
ALTER TABLE [dbo].[EventParticipants]  WITH CHECK ADD  CONSTRAINT [FK_EventParticipants_Event] FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[EventParticipants] CHECK CONSTRAINT [FK_EventParticipants_Event]
GO
ALTER TABLE [dbo].[EventParticipants]  WITH CHECK ADD  CONSTRAINT [FK_EventParticipants_Status] FOREIGN KEY([ParticipantStatusID])
REFERENCES [dbo].[ParticipantStatus] ([StatusID])
GO
ALTER TABLE [dbo].[EventParticipants] CHECK CONSTRAINT [FK_EventParticipants_Status]
GO
ALTER TABLE [dbo].[EventParticipants]  WITH CHECK ADD  CONSTRAINT [FK_EventParticipants_User] FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[EventParticipants] CHECK CONSTRAINT [FK_EventParticipants_User]
GO
ALTER TABLE [dbo].[EventRankings]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[EventRankings]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[EventRankings]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[Events]  WITH CHECK ADD FOREIGN KEY([CreatedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Events]  WITH CHECK ADD FOREIGN KEY([EventStatusID])
REFERENCES [dbo].[EventStatus] ([StatusID])
GO
ALTER TABLE [dbo].[Judging]  WITH CHECK ADD FOREIGN KEY([RoundCriterionID])
REFERENCES [dbo].[RoundCriteria] ([RoundCriterionID])
GO
ALTER TABLE [dbo].[Judging]  WITH CHECK ADD FOREIGN KEY([RoundJudgeID])
REFERENCES [dbo].[RoundJudges] ([RoundJudgeID])
GO
ALTER TABLE [dbo].[Judging]  WITH CHECK ADD FOREIGN KEY([SubmissionID])
REFERENCES [dbo].[Submissions] ([SubmissionID])
GO
ALTER TABLE [dbo].[Notifications]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[Notifications]  WITH CHECK ADD FOREIGN KEY([RecipientUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Notifications]  WITH CHECK ADD FOREIGN KEY([SentByUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[PasswordResetTokens]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[RefreshTokens]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[RoundCriteria]  WITH CHECK ADD FOREIGN KEY([EventCriterionID])
REFERENCES [dbo].[EventCriteria] ([EventCriterionID])
GO
ALTER TABLE [dbo].[RoundCriteria]  WITH CHECK ADD FOREIGN KEY([RoundID])
REFERENCES [dbo].[Rounds] ([RoundID])
GO
ALTER TABLE [dbo].[RoundJudges]  WITH CHECK ADD FOREIGN KEY([AssignedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[RoundJudges]  WITH CHECK ADD FOREIGN KEY([RoundID])
REFERENCES [dbo].[Rounds] ([RoundID])
GO
ALTER TABLE [dbo].[RoundJudges]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[RoundRankings]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[RoundRankings]  WITH CHECK ADD FOREIGN KEY([RoundID])
REFERENCES [dbo].[Rounds] ([RoundID])
GO
ALTER TABLE [dbo].[RoundRankings]  WITH CHECK ADD FOREIGN KEY([SubmissionID])
REFERENCES [dbo].[Submissions] ([SubmissionID])
GO
ALTER TABLE [dbo].[RoundRankings]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[Rounds]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[Rounds]  WITH CHECK ADD FOREIGN KEY([RoundStatusID])
REFERENCES [dbo].[RoundStatus] ([StatusID])
GO
ALTER TABLE [dbo].[SubmissionHistory]  WITH CHECK ADD  CONSTRAINT [FK_SubmissionHistory_Rounds] FOREIGN KEY([RoundID])
REFERENCES [dbo].[Rounds] ([RoundID])
GO
ALTER TABLE [dbo].[SubmissionHistory] CHECK CONSTRAINT [FK_SubmissionHistory_Rounds]
GO
ALTER TABLE [dbo].[SubmissionHistory]  WITH CHECK ADD  CONSTRAINT [FK_SubmissionHistory_Submissions] FOREIGN KEY([SubmissionID])
REFERENCES [dbo].[Submissions] ([SubmissionID])
GO
ALTER TABLE [dbo].[SubmissionHistory] CHECK CONSTRAINT [FK_SubmissionHistory_Submissions]
GO
ALTER TABLE [dbo].[SubmissionHistory]  WITH CHECK ADD  CONSTRAINT [FK_SubmissionHistory_SubmissionStatus] FOREIGN KEY([SubmissionStatusID])
REFERENCES [dbo].[SubmissionStatus] ([StatusID])
GO
ALTER TABLE [dbo].[SubmissionHistory] CHECK CONSTRAINT [FK_SubmissionHistory_SubmissionStatus]
GO
ALTER TABLE [dbo].[SubmissionHistory]  WITH CHECK ADD  CONSTRAINT [FK_SubmissionHistory_SubmittedBy] FOREIGN KEY([SubmittedByUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[SubmissionHistory] CHECK CONSTRAINT [FK_SubmissionHistory_SubmittedBy]
GO
ALTER TABLE [dbo].[SubmissionHistory]  WITH CHECK ADD  CONSTRAINT [FK_SubmissionHistory_Teams] FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[SubmissionHistory] CHECK CONSTRAINT [FK_SubmissionHistory_Teams]
GO
ALTER TABLE [dbo].[Submissions]  WITH CHECK ADD FOREIGN KEY([RoundID])
REFERENCES [dbo].[Rounds] ([RoundID])
GO
ALTER TABLE [dbo].[Submissions]  WITH CHECK ADD FOREIGN KEY([SubmissionStatusID])
REFERENCES [dbo].[SubmissionStatus] ([StatusID])
GO
ALTER TABLE [dbo].[Submissions]  WITH CHECK ADD FOREIGN KEY([SubmittedByUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Submissions]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[TeamJoinRequests]  WITH CHECK ADD FOREIGN KEY([RespondedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[TeamJoinRequests]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[TeamJoinRequests]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[TeamMembers]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[TeamMembers]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[TeamMilestones]  WITH CHECK ADD  CONSTRAINT [FK_TeamMilestones_Mentor] FOREIGN KEY([MentorUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[TeamMilestones] CHECK CONSTRAINT [FK_TeamMilestones_Mentor]
GO
ALTER TABLE [dbo].[TeamMilestones]  WITH CHECK ADD  CONSTRAINT [FK_TeamMilestones_Team] FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
GO
ALTER TABLE [dbo].[TeamMilestones] CHECK CONSTRAINT [FK_TeamMilestones_Team]
GO
ALTER TABLE [dbo].[Teams]  WITH CHECK ADD FOREIGN KEY([CategoryID])
REFERENCES [dbo].[Categories] ([CategoryID])
GO
ALTER TABLE [dbo].[Teams]  WITH CHECK ADD FOREIGN KEY([EventID])
REFERENCES [dbo].[Events] ([EventID])
GO
ALTER TABLE [dbo].[Teams]  WITH CHECK ADD FOREIGN KEY([LeaderUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Teams]  WITH CHECK ADD FOREIGN KEY([TeamStatusID])
REFERENCES [dbo].[TeamStatus] ([StatusID])
GO
ALTER TABLE [dbo].[UserOAuthAccounts]  WITH CHECK ADD  CONSTRAINT [FK_UserOAuthAccounts_User] FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[UserOAuthAccounts] CHECK CONSTRAINT [FK_UserOAuthAccounts_User]
GO
ALTER TABLE [dbo].[Users]  WITH CHECK ADD FOREIGN KEY([AccountStatusID])
REFERENCES [dbo].[AccountStatus] ([StatusID])
GO
ALTER TABLE [dbo].[Users]  WITH CHECK ADD FOREIGN KEY([UserTypeID])
REFERENCES [dbo].[UserType] ([UserTypeID])
GO
ALTER TABLE [dbo].[Users]  WITH CHECK ADD  CONSTRAINT [FK_Users_ApprovedBy] FOREIGN KEY([ApprovedByUserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[Users] CHECK CONSTRAINT [FK_Users_ApprovedBy]
GO
ALTER TABLE [dbo].[VerificationTokens]  WITH CHECK ADD FOREIGN KEY([UserID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[AuditLog]  WITH CHECK ADD  CONSTRAINT [CK_AuditLog_Entity] CHECK  (([EntityID] IS NOT NULL OR [EntityKey] IS NOT NULL))
GO
ALTER TABLE [dbo].[AuditLog] CHECK CONSTRAINT [CK_AuditLog_Entity]
GO
ALTER TABLE [dbo].[AwardPatterns]  WITH CHECK ADD  CONSTRAINT [CK_AwardPatterns_RankPosition] CHECK  (([RankPosition]>=(1) AND [RankPosition]<=(10)))
GO
ALTER TABLE [dbo].[AwardPatterns] CHECK CONSTRAINT [CK_AwardPatterns_RankPosition]
GO
ALTER TABLE [dbo].[Disqualifications]  WITH CHECK ADD  CONSTRAINT [CK_Disq_Target] CHECK  (([TeamID] IS NOT NULL AND [SubmissionID] IS NULL OR [TeamID] IS NULL AND [SubmissionID] IS NOT NULL))
GO
ALTER TABLE [dbo].[Disqualifications] CHECK CONSTRAINT [CK_Disq_Target]
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD  CONSTRAINT [CK_EvaluationAuditLogs_ActionType] CHECK  (([ActionType]=N'SUBMISSION_DISQUALIFICATION_REVERSED' OR [ActionType]=N'SUBMISSION_DISQUALIFIED' OR [ActionType]=N'TEAM_DISQUALIFICATION_REVERSED' OR [ActionType]=N'TEAM_DISQUALIFIED' OR [ActionType]=N'SCORE_DELETED' OR [ActionType]=N'SCORE_UPDATED' OR [ActionType]=N'SCORE_CREATED'))
GO
ALTER TABLE [dbo].[EvaluationAuditLogs] CHECK CONSTRAINT [CK_EvaluationAuditLogs_ActionType]
GO
ALTER TABLE [dbo].[EvaluationAuditLogs]  WITH CHECK ADD  CONSTRAINT [CK_EvaluationAuditLogs_Target] CHECK  (([JudgingID] IS NOT NULL OR [TeamID] IS NOT NULL OR [SubmissionID] IS NOT NULL))
GO
ALTER TABLE [dbo].[EvaluationAuditLogs] CHECK CONSTRAINT [CK_EvaluationAuditLogs_Target]
GO
ALTER TABLE [dbo].[Judging]  WITH CHECK ADD  CONSTRAINT [CK_Judging_Value] CHECK  (([ScoreValue]>=(0)))
GO
ALTER TABLE [dbo].[Judging] CHECK CONSTRAINT [CK_Judging_Value]
GO
ALTER TABLE [dbo].[ParticipantStatus]  WITH CHECK ADD  CONSTRAINT [CK_ParticipantStatus_Name] CHECK  (([StatusName]=N'UNVERIFIED' OR [StatusName]=N'TEMPORARY' OR [StatusName]=N'SUSPENDED' OR [StatusName]=N'REJECTED' OR [StatusName]=N'ACTIVE' OR [StatusName]=N'PENDING'))
GO
ALTER TABLE [dbo].[ParticipantStatus] CHECK CONSTRAINT [CK_ParticipantStatus_Name]
GO
ALTER TABLE [dbo].[TeamJoinRequests]  WITH CHECK ADD  CONSTRAINT [CK_TeamJoinRequests_Status] CHECK  (([RequestStatus]=N'CANCELLED' OR [RequestStatus]=N'REJECTED' OR [RequestStatus]=N'APPROVED' OR [RequestStatus]=N'PENDING'))
GO
ALTER TABLE [dbo].[TeamJoinRequests] CHECK CONSTRAINT [CK_TeamJoinRequests_Status]
GO
ALTER TABLE [dbo].[UserOAuthAccounts]  WITH CHECK ADD  CONSTRAINT [CK_UserOAuthAccounts_Provider] CHECK  (([Provider]=N'GITHUB' OR [Provider]=N'GOOGLE'))
GO
ALTER TABLE [dbo].[UserOAuthAccounts] CHECK CONSTRAINT [CK_UserOAuthAccounts_Provider]
GO
/****** Object:  StoredProcedure [dbo].[sp_ApproveUser]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[sp_ApproveUser]
    @UserID UNIQUEIDENTIFIER,
    @ApproverID UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;

UPDATE Users
SET AccountStatusID = '20000000-0000-0000-0000-000000000002',
    ApprovedAt = GETUTCDATE(),
    ApprovedByUserID = @ApproverID,
    UpdatedAt = GETUTCDATE()
WHERE UserID = @UserID
  AND AccountStatusID = '20000000-0000-0000-0000-000000000001'
  AND IsDeleted = 0;

INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID, NewValueJSON)
VALUES (N'ACCOUNT_APPROVED', N'Users', @UserID, @ApproverID, N'{"status":"Active"}');
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_ComputeEventRankings]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_ComputeEventRankings]
    @EventID UNIQUEIDENTIFIER,
    @CategoryID UNIQUEIDENTIFIER
    AS
BEGIN
    SET NOCOUNT ON;

DELETE FROM EventRankings WHERE EventID = @EventID AND CategoryID = @CategoryID;

;WITH FinalScores AS (
    SELECT rr.TeamID, AVG(rr.TotalScore) AS FinalScore
    FROM RoundRankings rr
             JOIN Rounds r ON r.RoundID = rr.RoundID
             JOIN Categories c ON c.CategoryID = r.CategoryID -- Nối qua Categories vì Rounds đã đổi tham chiếu
    WHERE c.EventID = @EventID
      AND rr.CategoryID = @CategoryID
    GROUP BY rr.TeamID
)
 INSERT INTO EventRankings (EventID, CategoryID, TeamID, FinalScore, RankPosition)
SELECT
    @EventID,
    @CategoryID,
    TeamID,
    FinalScore,
    RANK() OVER (ORDER BY FinalScore DESC)
FROM FinalScores;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_ComputeRoundRankings]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_ComputeRoundRankings]
    @RoundID UNIQUEIDENTIFIER,
    @CategoryID UNIQUEIDENTIFIER
    AS
BEGIN
    SET NOCOUNT ON;

DELETE FROM RoundRankings WHERE RoundID = @RoundID AND CategoryID = @CategoryID;

;WITH ScoreSummary AS (
    SELECT
        s.SubmissionID,
        s.TeamID,
        SUM(sc.ScoreValue * COALESCE(rc.Weight, ec.Weight)) AS WeightedTotal,
        AVG(sc.ScoreValue) AS AverageScore
    FROM Submissions s
             JOIN Teams t ON t.TeamID = s.TeamID
             JOIN Judging sc ON sc.SubmissionID = s.SubmissionID
             JOIN RoundCriteria rc ON rc.RoundCriterionID = sc.RoundCriterionID    -- Đã cập nhật logic nối
             JOIN EventCriteria ec ON ec.EventCriterionID = rc.EventCriterionID
    WHERE s.RoundID = @RoundID
      AND t.CategoryID = @CategoryID
      AND s.SubmissionStatusID != '50000000-0000-0000-0000-000000000004'
     AND t.TeamStatusID != '60000000-0000-0000-0000-000000000003'
     AND sc.IsCalibration = 0
 GROUP BY s.SubmissionID, s.TeamID
     ),
     Ranked AS (
 SELECT *, RANK() OVER (ORDER BY WeightedTotal DESC) AS RankPosition
 FROM ScoreSummary
     )
 INSERT INTO RoundRankings (RoundID, CategoryID, TeamID, SubmissionID, TotalScore, AverageScore, RankPosition, IsAdvanced)
SELECT
    @RoundID,
    @CategoryID,
    r.TeamID,
    r.SubmissionID,
    r.WeightedTotal,
    r.AverageScore,
    r.RankPosition,
    CASE WHEN rnd.AdvancementTopN IS NOT NULL AND r.RankPosition <= rnd.AdvancementTopN THEN 1 ELSE 0 END
FROM Ranked r
         CROSS JOIN Rounds rnd
WHERE rnd.RoundID = @RoundID;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_CreateGuestJudge]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_CreateGuestJudge]
    @Email NVARCHAR(255),
    @FullName NVARCHAR(200),
    @PasswordHash NVARCHAR(512),
    @ExpiresAt DATETIME2,
    @CreatedByID UNIQUEIDENTIFIER,
    @NewUserID UNIQUEIDENTIFIER OUTPUT
    AS
BEGIN
    SET NOCOUNT ON;

    SET @NewUserID = NEWID();

INSERT INTO Users (UserID, Email, PasswordHash, FullName, UserTypeID, AccountStatusID, AccountExpiresAt)
VALUES (@NewUserID, @Email, @PasswordHash, @FullName,
        '10000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000005',
        @ExpiresAt);

INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID, NewValueJSON)
VALUES (N'GUEST_JUDGE_CREATED', N'Users', @NewUserID, @CreatedByID, N'{"type":"GuestJudge"}');
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_DisqualifySubmission]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_DisqualifySubmission]
    @SubmissionID UNIQUEIDENTIFIER,
    @Reason NVARCHAR(MAX),
    @DisqualifiedByID UNIQUEIDENTIFIER
    AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @EventID UNIQUEIDENTIFIER;

SELECT @EventID = t.EventID
FROM Submissions s
         JOIN Teams t ON t.TeamID = s.TeamID
WHERE s.SubmissionID = @SubmissionID;

UPDATE Submissions
SET SubmissionStatusID = '50000000-0000-0000-0000-000000000004',
    LastUpdatedAt = GETUTCDATE()
WHERE SubmissionID = @SubmissionID;

INSERT INTO Disqualifications (SubmissionID, Reason, DisqualifiedByID)
VALUES (@SubmissionID, @Reason, @DisqualifiedByID);

INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID, NewValueJSON)
VALUES (N'SUBMISSION_DISQUALIFIED', N'Submissions', @SubmissionID, @DisqualifiedByID,
        N'{"reason":"' + REPLACE(@Reason, '"', '\"') + N'"}');

INSERT INTO EvaluationAuditLogs (EventID, ActionType, ActorUserID, SubmissionID, NewValue, Reason)
VALUES (@EventID, N'SUBMISSION_DISQUALIFIED', @DisqualifiedByID, @SubmissionID,
        N'{"status":"Disqualified"}', @Reason);
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_DisqualifyTeam]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_DisqualifyTeam]
    @TeamID UNIQUEIDENTIFIER,
    @Reason NVARCHAR(MAX),
    @DisqualifiedByID UNIQUEIDENTIFIER
    AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @EventID UNIQUEIDENTIFIER;
SELECT @EventID = EventID FROM Teams WHERE TeamID = @TeamID;

UPDATE Teams
SET TeamStatusID = '60000000-0000-0000-0000-000000000003',
    UpdatedAt = GETUTCDATE()
WHERE TeamID = @TeamID;

UPDATE Submissions
SET SubmissionStatusID = '50000000-0000-0000-0000-000000000004',
    LastUpdatedAt = GETUTCDATE()
WHERE TeamID = @TeamID;

INSERT INTO Disqualifications (TeamID, Reason, DisqualifiedByID)
VALUES (@TeamID, @Reason, @DisqualifiedByID);

INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID, NewValueJSON)
VALUES (N'TEAM_DISQUALIFIED', N'Teams', @TeamID, @DisqualifiedByID,
        N'{"reason":"' + REPLACE(@Reason, '"', '\"') + N'"}');

INSERT INTO EvaluationAuditLogs (EventID, ActionType, ActorUserID, TeamID, NewValue, Reason)
VALUES (@EventID, N'TEAM_DISQUALIFIED', @DisqualifiedByID, @TeamID,
        N'{"status":"Disqualified"}', @Reason);
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_RecordScore]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_RecordScore]
    @SubmissionID UNIQUEIDENTIFIER,
    @RoundJudgeID UNIQUEIDENTIFIER,       -- Đã cập nhật
    @RoundCriterionID UNIQUEIDENTIFIER,   -- Đã cập nhật
    @ScoreValue DECIMAL(6,2),
    @Comment NVARCHAR(MAX) = NULL,
    @IsCalibration BIT = 0
    AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @JudgeUserID UNIQUEIDENTIFIER;
    DECLARE @MaxScore DECIMAL(6,2);
    DECLARE @EventCriterionID UNIQUEIDENTIFIER;
    DECLARE @JudgingID UNIQUEIDENTIFIER;
    DECLARE @EventID UNIQUEIDENTIFIER;
    DECLARE @OldValue NVARCHAR(MAX);
    DECLARE @ActionType NVARCHAR(50);

    -- 1. Xác thực Giám khảo có thuộc đúng vòng của bài nộp không và lấy UserID
SELECT @JudgeUserID = rj.UserID, @EventID = t.EventID
FROM Submissions s
         JOIN Teams t ON t.TeamID = s.TeamID
         JOIN RoundJudges rj ON rj.RoundID = s.RoundID
WHERE s.SubmissionID = @SubmissionID AND rj.RoundJudgeID = @RoundJudgeID;

IF @JudgeUserID IS NULL
        THROW 52000, N'Giám khảo không hợp lệ hoặc không được phân công chấm vòng thi này.', 1;

    -- 2. Xác thực Tiêu chí có thuộc đúng vòng của bài nộp không và lấy MaxScore
SELECT @MaxScore = ec.MaxScore, @EventCriterionID = ec.EventCriterionID
FROM RoundCriteria rc
         JOIN EventCriteria ec ON ec.EventCriterionID = rc.EventCriterionID
         JOIN Submissions s ON s.RoundID = rc.RoundID
WHERE s.SubmissionID = @SubmissionID AND rc.RoundCriterionID = @RoundCriterionID;

IF @MaxScore IS NULL
        THROW 52002, N'Tiêu chí không hợp lệ hoặc không thuộc vòng thi này.', 1;

    IF @ScoreValue > @MaxScore
        THROW 52001, N'Điểm số vượt quá giới hạn tối đa cho phép của tiêu chí này.', 1;

    -- 3. Kiểm tra xem điểm đã tồn tại chưa để Insert hoặc Update
SELECT @JudgingID = JudgingID,
       @OldValue = N'{"score":' + CAST(ScoreValue AS NVARCHAR(30)) + N'}'
FROM Judging
WHERE SubmissionID = @SubmissionID
  AND RoundJudgeID = @RoundJudgeID
  AND RoundCriterionID = @RoundCriterionID;

IF @JudgingID IS NULL
BEGIN
        SET @JudgingID = NEWID();
        SET @ActionType = N'SCORE_CREATED';

INSERT INTO Judging (JudgingID, SubmissionID, RoundJudgeID, RoundCriterionID, ScoreValue, Comment, IsCalibration)
VALUES (@JudgingID, @SubmissionID, @RoundJudgeID, @RoundCriterionID, @ScoreValue, @Comment, @IsCalibration);
END
ELSE
BEGIN
        SET @ActionType = N'SCORE_UPDATED';

UPDATE Judging
SET ScoreValue = @ScoreValue,
    Comment = @Comment,
    UpdatedAt = GETUTCDATE()
WHERE JudgingID = @JudgingID;
END

    -- 4. Ghi Audit Logs
INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID, NewValueJSON)
VALUES (N'SCORE_RECORDED', N'Judging', @JudgingID, @JudgeUserID,
        N'{"round_criterion":"' + CAST(@RoundCriterionID AS NVARCHAR(36)) +
        N'","score":' + CAST(@ScoreValue AS NVARCHAR(30)) + N'}');

INSERT INTO EvaluationAuditLogs (EventID, ActionType, ActorUserID, JudgingID, SubmissionID, OldValue, NewValue, Reason)
VALUES (@EventID, @ActionType, @JudgeUserID, @JudgingID, @SubmissionID, @OldValue,
        N'{"score":' + CAST(@ScoreValue AS NVARCHAR(30)) + N'}',
        N'Giám khảo đã ghi nhận/cập nhật điểm');
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_UpsertSubmission]    Script Date: 7/14/2026 9:01:57 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE   PROCEDURE [dbo].[sp_UpsertSubmission]
    @TeamID UNIQUEIDENTIFIER,
    @RoundID UNIQUEIDENTIFIER,
    @RepositoryURL NVARCHAR(500),
    @DemoURL NVARCHAR(500),
    @ReportURL NVARCHAR(500),
    @SlideURL NVARCHAR(500),
    @Notes NVARCHAR(MAX),
    @RepoMetadataJSON NVARCHAR(MAX) = NULL,
    @RepoLastCommitAt DATETIME2 = NULL,
    @RepoStarCount INT = NULL,
    @RepoForkCount INT = NULL,
    @SubmittedByUserID UNIQUEIDENTIFIER
    AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @SubID UNIQUEIDENTIFIER;
    DECLARE @TeamStatus UNIQUEIDENTIFIER;
    DECLARE @Deadline DATETIME2;

SELECT @TeamStatus = TeamStatusID FROM Teams WHERE TeamID = @TeamID;

IF @TeamStatus IS NULL
        THROW 51002, N'Team not found.', 1;

IF @TeamStatus <> '60000000-0000-0000-0000-000000000002'
        THROW 51001, N'Only active teams can submit work.', 1;

SELECT @Deadline = SubmissionDeadline FROM Rounds WHERE RoundID = @RoundID;

IF @Deadline IS NOT NULL AND GETUTCDATE() > @Deadline
        THROW 51000, N'Submission deadline has passed.', 1;

    IF EXISTS (SELECT 1 FROM Submissions WHERE TeamID = @TeamID AND RoundID = @RoundID)
BEGIN
UPDATE Submissions
SET RepositoryURL = @RepositoryURL,
    DemoURL = @DemoURL,
    ReportURL = @ReportURL,
    SlideURL = @SlideURL,
    Notes = @Notes,
    RepoMetadataJSON = @RepoMetadataJSON,
    RepoLastCommitAt = @RepoLastCommitAt,
    RepoStarCount = @RepoStarCount,
    RepoForkCount = @RepoForkCount,
    SubmissionStatusID = '50000000-0000-0000-0000-000000000002',
    SubmittedAt = GETUTCDATE(),
    LastUpdatedAt = GETUTCDATE(),
    SubmittedByUserID = @SubmittedByUserID
WHERE TeamID = @TeamID AND RoundID = @RoundID;

SELECT @SubID = SubmissionID FROM Submissions WHERE TeamID = @TeamID AND RoundID = @RoundID;

INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID)
VALUES (N'SUBMISSION_UPDATED', N'Submissions', @SubID, @SubmittedByUserID);
END
ELSE
BEGIN
        SET @SubID = NEWID();

INSERT INTO Submissions (
    SubmissionID, TeamID, RoundID, RepositoryURL, DemoURL, ReportURL, SlideURL,
    Notes, RepoMetadataJSON, RepoLastCommitAt, RepoStarCount, RepoForkCount,
    SubmissionStatusID, SubmittedAt, SubmittedByUserID
)
VALUES (
           @SubID, @TeamID, @RoundID, @RepositoryURL, @DemoURL, @ReportURL, @SlideURL,
           @Notes, @RepoMetadataJSON, @RepoLastCommitAt, @RepoStarCount, @RepoForkCount,
           '50000000-0000-0000-0000-000000000002', GETUTCDATE(), @SubmittedByUserID
       );

INSERT INTO AuditLog (ActionType, EntityType, EntityID, ActorUserID)
VALUES (N'SUBMISSION_CREATED', N'Submissions', @SubID, @SubmittedByUserID);
END;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM dbo.EventStatus
    WHERE StatusID = '30000000-0000-0000-0000-000000000006'
)
BEGIN
INSERT INTO dbo.EventStatus
(
    StatusID,
    StatusName
)
VALUES
    (
        '30000000-0000-0000-0000-000000000006',
        'Upcoming'
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM dbo.EventStatus
    WHERE StatusID = '30000000-0000-0000-0000-000000000007'
)
BEGIN
INSERT INTO dbo.EventStatus
(
    StatusID,
    StatusName
)
VALUES
    (
        '30000000-0000-0000-0000-000000000007',
        'Registration Closed'
    );
END
GO
-- Create Appeals table
CREATE TABLE Appeals (
    AppealID UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    TeamID UNIQUEIDENTIFIER NOT NULL,
    EventID UNIQUEIDENTIFIER NOT NULL,
    CategoryID UNIQUEIDENTIFIER NOT NULL,
    Title NVARCHAR(255) NOT NULL,
    Reason NVARCHAR(MAX) NOT NULL,
    Status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
    AppealType NVARCHAR(50) NOT NULL,
    RoundID UNIQUEIDENTIFIER,
    ResolutionNote NVARCHAR(MAX),
    ResolvedByUserID UNIQUEIDENTIFIER,
    CreatedByUserID UNIQUEIDENTIFIER NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETUTCDATE(),
    UpdatedAt DATETIME2 DEFAULT GETUTCDATE(),
    CONSTRAINT FK_Appeals_Teams FOREIGN KEY (TeamID) REFERENCES Teams(TeamID),
    CONSTRAINT FK_Appeals_Events FOREIGN KEY (EventID) REFERENCES Events(EventID),
    CONSTRAINT FK_Appeals_Categories FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    CONSTRAINT FK_Appeals_Rounds FOREIGN KEY (RoundID) REFERENCES Rounds(RoundID),
    CONSTRAINT FK_Appeals_ResolvedBy FOREIGN KEY (ResolvedByUserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Appeals_CreatedBy FOREIGN KEY (CreatedByUserID) REFERENCES Users(UserID)
);
GO

-- ##########################################################################
-- ## BO SUNG TU MIGRATIONS (hop nhat ngay 2026-07-27)
-- ##
-- ## Cac migration duoi day tung bi quen cap nhat vao file snapshot nay.
-- ## Tat ca deu IDEMPOTENT (IF NOT EXISTS) nen chay lai nhieu lan van an toan,
-- ## va dat o CUOI file de moi bang goc + khoa ngoai da ton tai truoc do.
-- ##
-- ## Sau khi hop nhat: chi can chay RIENG file nay la tao du toan bo database.
-- ##########################################################################


-- ==========================================================================
-- 1) Users: cac cot ho so bo sung     (nguon: 20260719_add_user_profile_fields.sql)
-- ==========================================================================
IF COL_LENGTH(N'dbo.Users', N'Bio') IS NULL
    ALTER TABLE dbo.Users ADD Bio NVARCHAR(1000) NULL;
GO
IF COL_LENGTH(N'dbo.Users', N'Github') IS NULL
    ALTER TABLE dbo.Users ADD Github NVARCHAR(500) NULL;
GO
IF COL_LENGTH(N'dbo.Users', N'Portfolio') IS NULL
    ALTER TABLE dbo.Users ADD Portfolio NVARCHAR(500) NULL;
GO


-- ==========================================================================
-- 2) AccountLinkTickets               (nguon: 20260716_account_link_tickets.sql)
--    Lien ket tai khoan Google <-> local. Chi luu HASH cua token va OTP.
-- ==========================================================================
IF OBJECT_ID(N'dbo.AccountLinkTickets', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.AccountLinkTickets (
        TicketID              UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        UserID                UNIQUEIDENTIFIER NOT NULL REFERENCES dbo.Users(UserID),
        -- GOOGLE_LINK: gan dinh danh Google vao user local hien co.
        -- LOCAL_SETUP: thiet lap mat khau local cho user Google-only hien co.
        Purpose               NVARCHAR(30)   NOT NULL,
        Provider              NVARCHAR(30)   NULL,
        ProviderUserID        NVARCHAR(255)  NULL,
        ProviderEmail         NVARCHAR(255)  NULL,
        ProviderEmailVerified BIT            NULL,
        ProviderDisplayName   NVARCHAR(255)  NULL,
        ProviderAvatarUrl     NVARCHAR(1000) NULL,
        TokenHash             NVARCHAR(128)  NOT NULL UNIQUE,
        OtpHash               NVARCHAR(128)  NULL,
        OtpExpiresAt          DATETIME2      NULL,
        OtpAttempts           INT            NOT NULL DEFAULT 0,
        OtpLastSentAt         DATETIME2      NULL,
        OtpVerifiedAt         DATETIME2      NULL,
        CreatedAt             DATETIME2      NOT NULL DEFAULT GETUTCDATE(),
        ExpiresAt             DATETIME2      NOT NULL,
        ConsumedAt            DATETIME2      NULL
    );

    CREATE NONCLUSTERED INDEX IX_AccountLinkTickets_UserID
        ON dbo.AccountLinkTickets(UserID);
END
GO


-- ==========================================================================
-- 3) DeletedUserTombstones       (nguon: 20260717_create_deleted_user_tombstones.sql)
--    KHONG co FK toi Users: organizer thuc hien xoa sau nay cung co the bi xoa.
-- ==========================================================================
IF OBJECT_ID(N'dbo.DeletedUserTombstones', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DeletedUserTombstones (
        [TombstoneID]     [uniqueidentifier] NOT NULL DEFAULT NEWID(),
        [Email]           [nvarchar](255)    NOT NULL,
        [FullName]        [nvarchar](200)    NULL,
        [DeletedByUserID] [uniqueidentifier] NULL,
        [Reason]          [nvarchar](500)    NULL,
        [DeletedAt]       [datetime2](7)     NOT NULL,
        [ExpiresAt]       [datetime2](7)     NOT NULL,
        CONSTRAINT [PK_DeletedUserTombstones] PRIMARY KEY CLUSTERED ([TombstoneID])
    );

    CREATE NONCLUSTERED INDEX [IX_DeletedUserTombstones_Email]
        ON dbo.DeletedUserTombstones ([Email]) INCLUDE ([ExpiresAt]);
END
GO


-- ==========================================================================
-- 4) TeamMentorNotes               (nguon: 20260718_create_team_mentor_notes.sql)
-- ==========================================================================
IF OBJECT_ID(N'dbo.TeamMentorNotes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TeamMentorNotes (
        NoteID    UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        TeamID    UNIQUEIDENTIFIER NOT NULL,
        MentorID  UNIQUEIDENTIFIER NOT NULL,
        Note      NVARCHAR(MAX),
        CreatedAt DATETIME2 DEFAULT CURRENT_TIMESTAMP,
        UpdatedAt DATETIME2,
        CONSTRAINT FK_TeamMentorNotes_Teams FOREIGN KEY (TeamID)   REFERENCES dbo.Teams(TeamID),
        CONSTRAINT FK_TeamMentorNotes_Users FOREIGN KEY (MentorID) REFERENCES dbo.Users(UserID),
        CONSTRAINT UQ_TeamMentorNotes_Team_Mentor UNIQUE (TeamID, MentorID)
    );
END
GO


-- ==========================================================================
-- 5) AI_Knowledge_Base             (nguon: 20260721_create_ai_knowledge_base.sql)
-- ==========================================================================
IF OBJECT_ID(N'dbo.AI_Knowledge_Base', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.AI_Knowledge_Base (
        ID              UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        EventID         UNIQUEIDENTIFIER NOT NULL,
        CategoryID      UNIQUEIDENTIFIER DEFAULT NULL,
        QuestionPattern NVARCHAR(MAX) NOT NULL,
        StandardAnswer  NVARCHAR(MAX) NOT NULL,
        MentorID        UNIQUEIDENTIFIER NOT NULL,
        CreatedAt       DATETIME2 DEFAULT SYSUTCDATETIME(),
        UpdatedAt       DATETIME2 DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_AI_KB_Event    FOREIGN KEY (EventID)    REFERENCES dbo.Events(EventID) ON DELETE CASCADE,
        CONSTRAINT FK_AI_KB_Category FOREIGN KEY (CategoryID) REFERENCES dbo.Categories(CategoryID),
        CONSTRAINT FK_AI_KB_Mentor   FOREIGN KEY (MentorID)   REFERENCES dbo.Users(UserID)
    );

    CREATE INDEX IX_AI_KB_Event    ON dbo.AI_Knowledge_Base(EventID);
    CREATE INDEX IX_AI_KB_Category ON dbo.AI_Knowledge_Base(CategoryID);
END
GO


-- ==========================================================================
-- 6) SubmissionRepositories
--    Gop 3 migration: 20260722 (bang goc)
--                   + 20260723_submission_repositories_counts (star/fork/issue)
--                   + 20260726_submission_repositories_activity (activity + pin)
--    LUU Y: cac cot Pinned* hien DORMANT (khong con duoc ghi) - giu lai de
--    khong phai tao migration xoa cot. Xem AGENTS.md muc GitHub scoring-aid.
-- ==========================================================================
IF OBJECT_ID(N'dbo.SubmissionRepositories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionRepositories (
        SubmissionRepositoryID UNIQUEIDENTIFIER NOT NULL DEFAULT (NEWID()),
        SubmissionID           UNIQUEIDENTIFIER NOT NULL,
        Provider               VARCHAR(20)    NOT NULL,
        ExternalRepositoryID   NVARCHAR(100)  NULL,
        RepositoryUrl          NVARCHAR(500)  NOT NULL,
        Owner                  NVARCHAR(200)  NULL,
        RepositoryName         NVARCHAR(200)  NULL,
        FullName               NVARCHAR(400)  NULL,
        Description            NVARCHAR(MAX)  NULL,
        Visibility             VARCHAR(20)    NULL,
        DefaultBranch          NVARCHAR(200)  NULL,
        PrimaryLanguage        NVARCHAR(100)  NULL,
        RepositoryCreatedAt    DATETIME2 NULL,
        RepositoryUpdatedAt    DATETIME2 NULL,
        LastPushedAt           DATETIME2 NULL,
        ExternalUrl            NVARCHAR(500)  NULL,
        LastSyncStatus         VARCHAR(30)    NOT NULL DEFAULT ('NOT_SYNCHRONIZED'),
        LastSynchronizedAt     DATETIME2 NULL,
        ErrorCode              NVARCHAR(100)  NULL,
        ErrorMessage           NVARCHAR(1000) NULL,
        -- 20260723: so lieu tham khao tu GitHub
        StarCount              INT NULL,
        ForkCount              INT NULL,
        OpenIssuesCount        INT NULL,
        -- 20260726: hoat dong phat trien (best-effort, co the NULL)
        LanguagesJson          NVARCHAR(MAX) NULL,
        ContributorCount       INT NULL,
        TopContributorsJson    NVARCHAR(MAX) NULL,
        CommitCount            INT NULL,
        LastCommitSha          VARCHAR(64) NULL,
        -- 20260726: ghim phien ban (DORMANT - khong con duoc ghi)
        PinnedCommitSha        VARCHAR(64) NULL,
        PinnedAt               DATETIME2(7) NULL,
        PinnedByUserID         UNIQUEIDENTIFIER NULL,
        CreatedAt              DATETIME2 NOT NULL DEFAULT (SYSUTCDATETIME()),
        UpdatedAt              DATETIME2 NOT NULL DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT PK_SubmissionRepositories PRIMARY KEY (SubmissionRepositoryID),
        CONSTRAINT FK_SubmissionRepositories_Submissions FOREIGN KEY (SubmissionID) REFERENCES dbo.Submissions(SubmissionID),
        CONSTRAINT UQ_SubmissionRepositories_SubmissionID UNIQUE (SubmissionID),
        CONSTRAINT CK_SubmissionRepositories_Provider CHECK (Provider IN ('GITHUB', 'GITLAB', 'UNKNOWN')),
        CONSTRAINT CK_SubmissionRepositories_LastSyncStatus CHECK (LastSyncStatus IN ('NOT_SYNCHRONIZED', 'RUNNING', 'SUCCESS', 'FAILED', 'PARTIAL_SUCCESS'))
    );

    CREATE NONCLUSTERED INDEX IX_SubmissionRepositories_Provider_ExternalID
        ON dbo.SubmissionRepositories (Provider, ExternalRepositoryID);
END
GO


-- ==========================================================================
-- 7) Index chong trung ten event dang hoat dong
--                              (nguon: 20260628_add_unique_active_event_name.sql)
-- ==========================================================================
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UQ_Events_EventName_Active' AND object_id = OBJECT_ID('dbo.Events')
)
BEGIN
    CREATE UNIQUE INDEX UQ_Events_EventName_Active
        ON dbo.Events(EventName)
        WHERE IsDeleted = 0;
END
GO


-- ==========================================================================
-- 8) Index chong trung email tai khoan LOCAL
--                                     (nguon: 20260718_unique_local_email.sql)
--    Chi ap cho account local chua xoa; KHONG dung toi account OAuth-only,
--    nen local + Google cung email van song song duoc theo thiet ke hien tai.
-- ==========================================================================
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_Users_Email_LocalActive')
BEGIN
    CREATE UNIQUE INDEX [UQ_Users_Email_LocalActive]
        ON dbo.Users ([Email])
        WHERE [LocalLoginEnabled] = 1 AND [IsDeleted] = 0;
END
GO


-- ==========================================================================
-- 9) Go account status "Pending Approval" da loi thoi
--                           (nguon: 20260725_remove_pending_approval_status.sql)
--    Luong duyet nay da chuyen sang cap TEAM. Users.AccountStatusID la FK
--    NOT NULL nen phai reassign truoc khi xoa row.
-- ==========================================================================
IF EXISTS (SELECT 1 FROM dbo.AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000001')
BEGIN
    UPDATE dbo.Users
        SET AccountStatusID = '20000000-0000-0000-0000-000000000006'   -- Unverified
        WHERE AccountStatusID = '20000000-0000-0000-0000-000000000001';

    DELETE FROM dbo.AccountStatus
        WHERE StatusID = '20000000-0000-0000-0000-000000000001';
END
GO

-- ################## HET PHAN BO SUNG TU MIGRATIONS ########################
