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
/****** Object:  Table [dbo].[TeamWithdrawalRequests]    Script Date: 7/28/2026 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TeamWithdrawalRequests](
	[RequestID] [uniqueidentifier] NOT NULL,
	[TeamID] [uniqueidentifier] NOT NULL,
	[RequestedByID] [uniqueidentifier] NOT NULL,
	[Reason] [nvarchar](1000) NOT NULL,
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
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000002', N'Active')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000001', N'Pending Approval')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000003', N'Rejected')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000004', N'Suspended')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000005', N'Temporary')
INSERT [dbo].[AccountStatus] ([StatusID], [StatusName]) VALUES (N'20000000-0000-0000-0000-000000000006', N'Unverified')
GO
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'294a9477-1e4c-4ed3-96c3-1a5570cbe86b', N'USER_CREATED', N'Users', N'1cefeef1-28e1-4897-b4ff-b85942aaad9f', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teamb1@gmail.com","role":"FPT Student"}', NULL, CAST(N'2026-07-04T02:13:06.3618939' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'cc6cab7d-f0fa-49c3-ba9a-1d9367e33c5c', N'USER_ROLE_CHANGED', N'Users', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"ahihine03@gmail.com","role":"Expert","status":"Active","isDeleted":false}', N'{"email":"ahihine03@gmail.com","role":"Internal Judge","status":"Active","isDeleted":false}', NULL, CAST(N'2026-07-09T10:01:37.6631403' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'812081cc-a0d4-4256-927b-1f0409f9af43', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'0c0e1749-1cb7-4b6a-9742-a8f4b159afd1', N'1eb4241e-e471-4395-a61f-43b40fe75af6:fa60133b-5874-4dbf-9299-a0069495b193', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"fa60133b-5874-4dbf-9299-a0069495b193","rejectedReason":""}', NULL, CAST(N'2026-07-02T14:42:38.9817495' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'97132993-d8b1-41c6-a159-2b22e1e8e973', N'SUBMISSION_CREATED', N'Submissions', N'2b708319-e446-44be-8831-75be637591a0', NULL, N'5d565896-2307-4911-939d-94e11e4b1c44', NULL, NULL, NULL, CAST(N'2026-07-04T10:02:37.5366667' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'bdec6a25-823d-4e83-93ae-2b605bbcbd3b', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'f3e8c517-f8df-4371-8957-42426f1fa53b', N'1eb4241e-e471-4395-a61f-43b40fe75af6:6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:14.6716930' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'c698e7c7-47ed-4989-bf9e-2c9618ce2c40', N'SUBMISSION_CREATED', N'Submissions', N'391cb64b-6ff7-4471-b76a-973ade51874f', NULL, N'5181ec59-afee-46ee-95d1-16cde3a8819c', NULL, NULL, NULL, CAST(N'2026-07-09T08:36:26.3933333' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'c1e463ed-ff39-40ba-a9ca-2d7eaf8bfc31', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'0c35b87b-6b37-4555-8362-5a6cabc6a49e', N'1eb4241e-e471-4395-a61f-43b40fe75af6:1cefeef1-28e1-4897-b4ff-b85942aaad9f', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"1cefeef1-28e1-4897-b4ff-b85942aaad9f","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:16.5116401' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'1497b9fa-1da6-4381-a0cf-31c5625c299b', N'TEAM_ELIGIBILITY_APPROVED', N'Teams', N'bab0ac21-0e0e-496a-af83-576b30867164', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"status":"Active","note":""}', NULL, CAST(N'2026-07-08T14:11:01.5489964' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'8324a2ab-1b6a-49da-b63e-36b1fa2c4e76', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'ebfa6d81-7a7a-4ee3-be07-6872de80e48b', N'1eb4241e-e471-4395-a61f-43b40fe75af6:6eda58a7-1866-4f0b-98c8-213c1e07e016', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"6eda58a7-1866-4f0b-98c8-213c1e07e016","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:17.0239660' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'9d337897-113a-4453-98ce-4e2f8065eb5c', N'USER_CREATED', N'Users', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teama2@gmail.com","role":"External Student"}', NULL, CAST(N'2026-07-04T02:11:30.2695364' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'd8885353-e03e-4532-9aef-5fa0d6362d57', N'USER_CREATED', N'Users', N'5181ec59-afee-46ee-95d1-16cde3a8819c', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"team1a@gmail.com","role":"External Student"}', NULL, CAST(N'2026-07-04T02:10:06.8586526' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'ac62dc13-0854-403f-88b0-72dce7ad4fde', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'54d42a91-3826-428b-a90a-29f7b97ac688', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32:5d565896-2307-4911-939d-94e11e4b1c44', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"8fb898cb-dc68-4a6f-b88f-1b3f7305fe32","userId":"5d565896-2307-4911-939d-94e11e4b1c44","rejectedReason":""}', NULL, CAST(N'2026-07-04T11:25:46.5846693' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'01bb6a69-36f5-4027-bd4d-79aa6bd404b3', N'USER_UPDATED', N'Users', N'89abab57-98ab-4927-9a7f-9cdae0d8434f', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"thaovann1405@gmail.com","role":"Internal Judge","status":"Active","isDeleted":false}', N'{"email":"thaovann1405@gmail.com","role":"Internal Judge","status":"Active","isDeleted":false}', NULL, CAST(N'2026-07-01T13:48:13.3087594' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'ad164649-21de-4865-bac0-8d6678192d1b', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'e19bc273-adb6-487a-b23e-9ce3e908f55e', N'1eb4241e-e471-4395-a61f-43b40fe75af6:46f63efa-185a-4a0a-9e9f-934f01c6513c', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"46f63efa-185a-4a0a-9e9f-934f01c6513c","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:17.6950956' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'485983cc-d2f4-4ee4-a5f1-90a791e560a3', N'USER_CREATED', N'Users', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"ahihine03@gmail.com","role":"Internal Judge"}', NULL, CAST(N'2026-06-30T21:24:25.6504158' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'e981affc-cad0-49d1-b4e3-91e65daf0b85', N'USER_UPDATED', N'Users', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"teama2@gmail.com","role":"External Student","status":"Unverified","isDeleted":false}', N'{"email":"teama2@gmail.com","role":"External Student","status":"Active","isDeleted":false}', NULL, CAST(N'2026-07-04T02:11:50.2382455' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'f40c9972-12a4-43b4-a3bb-920743bb6604', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'1c8e7d85-f46f-4022-972d-4b830b419ea4', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32:58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"8fb898cb-dc68-4a6f-b88f-1b3f7305fe32","userId":"58f44b80-6e5d-4db5-aa0d-5c92af46668b","rejectedReason":""}', NULL, CAST(N'2026-07-04T11:25:44.0222780' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'20cf1f42-428a-4a9a-b97a-a8114ecbc728', N'USER_CREATED', N'Users', N'4d2f7a5b-a634-46a9-92ee-ac28fa245a1a', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teame1@gmail.com","role":"External Student"}', NULL, CAST(N'2026-07-04T02:16:01.4026555' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'21ddd655-55ea-448a-9bae-b3d57c84f930', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'061f75eb-37e3-4dfc-b2d6-4412e0f3cceb', N'1eb4241e-e471-4395-a61f-43b40fe75af6:4d2f7a5b-a634-46a9-92ee-ac28fa245a1a', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"4d2f7a5b-a634-46a9-92ee-ac28fa245a1a","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:16.0039401' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'a85682e6-854c-4bf9-a6c4-b3fc6977603a', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'76130b77-9cdc-47c5-9884-b62f9902fbfe', N'1eb4241e-e471-4395-a61f-43b40fe75af6:960a79c4-da41-478b-85bd-61f579e43f5b', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"960a79c4-da41-478b-85bd-61f579e43f5b","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:18.1187451' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'6564f15c-ce5c-4b45-a54a-b7d5aadc75df', N'USER_DEACTIVATED', N'Users', N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"hahachenal@gmail.com","role":"FPT Student","status":"Active","isDeleted":false}', N'{"email":"hahachenal@gmail.com","role":"FPT Student","status":"Suspended","isDeleted":true}', NULL, CAST(N'2026-07-14T06:43:18.3624892' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'31294c42-27f2-41f5-8a47-be3870ccd269', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'303f95e5-c02e-408c-9858-58ba8d537d23', N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038:58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"a2ed1f00-b43f-4592-b5b5-247ab0bcd038","userId":"58f44b80-6e5d-4db5-aa0d-5c92af46668b","rejectedReason":""}', NULL, CAST(N'2026-07-04T11:15:58.2572257' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'21012843-b37e-4f3e-9f05-c0cb38012a6b', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'fa9a42ad-80e8-4ae2-a42a-8569a59ba516', N'1eb4241e-e471-4395-a61f-43b40fe75af6:5181ec59-afee-46ee-95d1-16cde3a8819c', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"5181ec59-afee-46ee-95d1-16cde3a8819c","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:17.3467438' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'064fa4ad-1b45-46b6-a174-c4255cb7471c', N'TEAM_ELIGIBILITY_APPROVED', N'Teams', N'31f337e9-dc45-46bf-b6c5-280f93b482ca', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"status":"Active","note":""}', NULL, CAST(N'2026-07-04T16:16:04.1810746' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'96fa4e61-41ca-44aa-82ee-cc4b0e09c918', N'USER_CREATED', N'Users', N'8c5c3287-cd14-4186-9180-340d0387fc3a', NULL, N'a0000000-0000-0000-0000-000000000000', NULL, N'{"email":"judefortesting123@gmail.com","role":"Internal Judge"}', NULL, CAST(N'2026-07-08T15:53:11.3814121' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'a59a0434-2f91-43a4-a90a-d0d2e681393c', N'USER_CREATED', N'Users', N'e3f9b065-3380-47ff-9705-5d8106188e4e', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teamc1@gmail.com","role":"FPT Student"}', NULL, CAST(N'2026-07-04T02:13:44.7987034' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'4b626893-145b-45dc-8418-d11af60e1b9e', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'd6127ae4-5de2-4820-8b30-a936067ca684', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8:ea969288-a77c-4683-b661-b10955f1df18', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"de33a8f5-07c1-448b-b131-dfdf7fab5ea8","userId":"ea969288-a77c-4683-b661-b10955f1df18","rejectedReason":""}', NULL, CAST(N'2026-07-01T11:48:56.6402754' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'7110982d-6f6b-4592-aeae-d2c0290156ee', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'79f264ae-66f0-4792-8dbb-6aa91462abc3', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32:ea969288-a77c-4683-b661-b10955f1df18', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"REJECTED","eventId":"8fb898cb-dc68-4a6f-b88f-1b3f7305fe32","userId":"ea969288-a77c-4683-b661-b10955f1df18","rejectedReason":"deo cho"}', NULL, CAST(N'2026-07-14T09:39:37.6722349' AS DateTime2), N'deo cho')
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'6dbcc590-cdc7-48a2-9d51-d404e10e6c4c', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'89d4b0be-9576-4477-b87e-914e30265978', N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038:5d565896-2307-4911-939d-94e11e4b1c44', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"a2ed1f00-b43f-4592-b5b5-247ab0bcd038","userId":"5d565896-2307-4911-939d-94e11e4b1c44","rejectedReason":""}', NULL, CAST(N'2026-07-04T11:15:29.5873218' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'e264a638-9bd4-4109-ac4f-d7a7bb218d0f', N'USER_CREATED', N'Users', N'46f63efa-185a-4a0a-9e9f-934f01c6513c', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teamd1@gmail.com","role":"External Student"}', NULL, CAST(N'2026-07-04T02:15:14.5043434' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'f8cec332-d94d-4cc5-8ee4-dbd628ab51dd', N'EVENT_PARTICIPANT_STATUS_UPDATED', N'EventParticipants', N'a95a34b2-d4c3-47d2-ad21-f252fd122153', N'1eb4241e-e471-4395-a61f-43b40fe75af6:e3f9b065-3380-47ff-9705-5d8106188e4e', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"status":"PENDING"}', N'{"status":"ACTIVE","eventId":"1eb4241e-e471-4395-a61f-43b40fe75af6","userId":"e3f9b065-3380-47ff-9705-5d8106188e4e","rejectedReason":""}', NULL, CAST(N'2026-07-04T02:36:18.5117057' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'962487c6-295f-4a05-b7e5-dcfe977b6bc3', N'USER_CREATED', N'Users', N'9a9ef0d0-2bb3-4ed2-8e61-5dd814f4b20d', NULL, N'a0000000-0000-0000-0000-000000000000', NULL, N'{"email":"dellcomatkhau088888@gmail.com","role":"Internal Judge"}', NULL, CAST(N'2026-07-09T14:46:29.5871184' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'6fafd549-d75c-490a-8e50-e5b13c334e43', N'USER_CREATED', N'Users', N'6006f192-3fd1-4647-b145-8ebccb5ebd61', NULL, N'a0000000-0000-0000-0000-000000000000', NULL, N'{"email":"mentorfortesting123@gmail.com","role":"Mentor"}', NULL, CAST(N'2026-07-08T16:26:04.2993433' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'2a45c903-e1b7-4394-815e-efc44e55931b', N'USER_CREATED', N'Users', N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teama3@gmail.com","role":"External Student"}', NULL, CAST(N'2026-07-04T02:12:23.9129545' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'15e998d0-a29c-4db2-960f-f080d7dd65ae', N'USER_UPDATED', N'Users', N'5181ec59-afee-46ee-95d1-16cde3a8819c', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"team1a@gmail.com","role":"External Student","status":"Active","isDeleted":false}', N'{"email":"team1a@gmail.com","role":"External Student","status":"Active","isDeleted":false}', NULL, CAST(N'2026-07-04T02:10:51.3571974' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'ee94e256-af57-4b2d-8203-f3e62e709fee', N'USER_CREATED', N'Users', N'89abab57-98ab-4927-9a7f-9cdae0d8434f', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"thaovann1405@gmail.com","role":"Internal Judge"}', NULL, CAST(N'2026-07-01T13:45:20.1828659' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'9c7e785e-7ba0-417e-acc3-f3ec53754da0', N'USER_CREATED', N'Users', N'960a79c4-da41-478b-85bd-61f579e43f5b', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, N'{"email":"teamc2@gmail.com","role":"FPT Student"}', NULL, CAST(N'2026-07-04T02:14:30.2766979' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'7c6375e0-9d92-4403-874e-f71dc0998c9e', N'USER_UPDATED', N'Users', N'89abab57-98ab-4927-9a7f-9cdae0d8434f', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"thaovann1405@gmail.com","role":"Internal Judge","status":"Active","isDeleted":false}', N'{"email":"thaovann1405@gmail.com","role":"Internal Judge","status":"Active","isDeleted":false}', NULL, CAST(N'2026-07-01T13:45:57.5793118' AS DateTime2), NULL)
INSERT [dbo].[AuditLog] ([LogID], [ActionType], [EntityType], [EntityID], [EntityKey], [ActorUserID], [OldValueJSON], [NewValueJSON], [IPAddress], [OccurredAt], [Notes]) VALUES (N'2b8bd43b-a466-4230-8e76-fc0e3295361c', N'USER_ROLE_CHANGED', N'Users', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', NULL, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'{"email":"ahihine03@gmail.com","role":"Internal Judge","status":"Active","isDeleted":false}', N'{"email":"ahihine03@gmail.com","role":"Expert","status":"Active","isDeleted":false}', NULL, CAST(N'2026-07-09T10:00:37.2903101' AS DateTime2), NULL)
GO
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'f970cae8-8a8f-4e98-923f-02a0cc71efc7', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'97b3e382-2725-4202-b41e-ec637292a634', 1, N'70000000-0000-0000-0000-000000000001', N'1st Place', NULL, CAST(1000000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'29785472-bf1e-4488-9333-2c7812768df0', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', 3, N'70000000-0000-0000-0000-000000000003', N'3rd Place', NULL, CAST(500000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'ca6aa951-dfa6-4771-8928-425650e83db4', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'97b3e382-2725-4202-b41e-ec637292a634', 2, N'70000000-0000-0000-0000-000000000002', N'2nd Place', NULL, CAST(700000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'2467b7e8-26bd-44b5-8231-64008e65887a', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', 3, N'70000000-0000-0000-0000-000000000003', N'3rd Place', NULL, CAST(500000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'c5ba9aee-12af-4f14-af4d-72a0956711ae', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', 4, N'70000000-0000-0000-0000-000000000004', N'4th Place', NULL, CAST(200000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'243f8918-b154-4e02-96e9-89ac8ef08531', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', 1, N'70000000-0000-0000-0000-000000000001', N'1st Place', NULL, CAST(1000000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'2450417e-ac9d-4d85-9132-8c2e317147ea', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', 2, N'70000000-0000-0000-0000-000000000002', N'2nd Place', NULL, CAST(700000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'c649c593-2103-4dd2-a285-b7030d2915c7', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'97b3e382-2725-4202-b41e-ec637292a634', 4, N'70000000-0000-0000-0000-000000000004', N'4th Place', NULL, CAST(200000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'bd969385-96b6-4f34-9752-edb2c97e4ac1', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', 2, N'70000000-0000-0000-0000-000000000002', N'2nd Place', NULL, CAST(700000.00 AS Decimal(12, 2)), N'VND', 1)
INSERT [dbo].[AwardPatterns] ([PatternID], [EventID], [CategoryID], [RankPosition], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [IsActive]) VALUES (N'70d70bd8-fb41-4901-9b4f-f2cdb36391a5', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'97b3e382-2725-4202-b41e-ec637292a634', 3, N'70000000-0000-0000-0000-000000000003', N'3rd Place', NULL, CAST(500000.00 AS Decimal(12, 2)), N'VND', 1)
GO
INSERT [dbo].[Awards] ([AwardID], [EventID], [CategoryID], [TeamID], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [AwardedAt], [AwardedByID], [IsPublished], [PublishedAt]) VALUES (N'f4000000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000002', N'c1000000-0000-0000-0000-000000000003', N'e1000000-0000-0000-0000-000000000004', N'70000000-0000-0000-0000-000000000001', N'API Test 2025 Champion', N'First place in the API test innovation showcase.', CAST(10000000.00 AS Decimal(12, 2)), N'VND', CAST(N'2025-10-12T13:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1, CAST(N'2025-10-13T08:00:00.0000000+07:00' AS DateTimeOffset))
INSERT [dbo].[Awards] ([AwardID], [EventID], [CategoryID], [TeamID], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [AwardedAt], [AwardedByID], [IsPublished], [PublishedAt]) VALUES (N'f4000000-0000-0000-0000-000000000002', N'b1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000001', N'70000000-0000-0000-0000-000000000005', N'API Test Innovation Candidate', N'Unpublished award for organizer API tests.', CAST(3000000.00 AS Decimal(12, 2)), N'VND', CAST(N'2026-06-12T13:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0, NULL)
INSERT [dbo].[Awards] ([AwardID], [EventID], [CategoryID], [TeamID], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [AwardedAt], [AwardedByID], [IsPublished], [PublishedAt]) VALUES (N'831b7269-c69e-4a3e-9862-0aa01219e900', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'70000000-0000-0000-0000-000000000001', N'1st Place', NULL, CAST(1000000.00 AS Decimal(12, 2)), N'VND', CAST(N'2026-07-04T09:15:39.4153268' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1, CAST(N'2026-07-04T09:15:39.4153268+00:00' AS DateTimeOffset))
INSERT [dbo].[Awards] ([AwardID], [EventID], [CategoryID], [TeamID], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [AwardedAt], [AwardedByID], [IsPublished], [PublishedAt]) VALUES (N'0117d63d-f648-4b05-999e-a66ce0f5a97f', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'70000000-0000-0000-0000-000000000003', N'3rd Place', NULL, CAST(500000.00 AS Decimal(12, 2)), N'VND', CAST(N'2026-07-04T09:15:40.1348849' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1, CAST(N'2026-07-04T09:15:40.1348849+00:00' AS DateTimeOffset))
INSERT [dbo].[Awards] ([AwardID], [EventID], [CategoryID], [TeamID], [AwardTierID], [AwardTitle], [Description], [PrizeValue], [PrizeCurrency], [AwardedAt], [AwardedByID], [IsPublished], [PublishedAt]) VALUES (N'701c5473-2ebb-4d16-b865-d87041d1b92c', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'70000000-0000-0000-0000-000000000002', N'2nd Place', NULL, CAST(700000.00 AS Decimal(12, 2)), N'VND', CAST(N'2026-07-04T09:15:39.8930854' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1, CAST(N'2026-07-04T09:15:39.8930854+00:00' AS DateTimeOffset))
GO
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000005', N'Best Innovation')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000006', N'Best Presentation')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000001', N'First Place')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000004', N'Honorable Mention')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000002', N'Second Place')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000007', N'Special Award')
INSERT [dbo].[AwardTier] ([TierID], [TierName]) VALUES (N'70000000-0000-0000-0000-000000000003', N'Third Place')
GO
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'c1000000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000001', N'AI for Education', N'AI products for teaching and learning.', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'c1000000-0000-0000-0000-000000000002', N'b1000000-0000-0000-0000-000000000001', N'Green Technology', N'Technology for sustainability.', 2, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'c1000000-0000-0000-0000-000000000003', N'b1000000-0000-0000-0000-000000000002', N'Open Innovation', N'Open category for the completed showcase.', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'Hạng Mục Test', N'Testing only', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'95e6a07c-4524-4f48-98b8-0bb6f7d26546', N'b1000000-0000-0000-0000-000000000002', N'AI Healthcare Innovation', N'AI solution for healthcare, etc.', 2, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'Web Application', NULL, 2, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'ca000000-0000-0000-0000-111111111111', N'f1230799-7a9e-4325-a999-f49719c5752b', N'AI & Machine Learning', N'Develop AI-driven solutions to solve real-world problems', 1, 0)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'74d82808-67b9-4a10-b5d1-14222bf8cb1e', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'AI Healthcare', N'AI in healthcare, medicine,...', 3, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'91c1abd4-836f-4930-87f4-1d00c9cd74b4', N'f1230799-7a9e-4325-a999-f49719c5752b', N'Web Application', N'Sample category for web application teams', 1, 0)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'ca000000-0000-0000-0000-222222222222', N'f1230799-7a9e-4325-a999-f49719c5752b', N'Web Applications', N'Create modern web applications with focus on usability', 2, 0)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'Web Application', N'AI in real life', 2, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'a9a45c56-4697-4706-88a1-38f8531b3d45', N'c3a85586-6a74-4c1d-8688-34b6359ced09', N'AI Track', N'For AI', 1, 0)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'7d38b882-b988-4822-b8c4-409374e102dd', N'93059e4e-907e-403b-afb2-fbeb049bbdce', N'AI & Machine Learning Track', N'H?ng m?c dành riêng cho các gi?i pháp ?ng d?ng AI.', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'6b8cd8f6-d267-49a5-936d-5ac6411d6c56', N'f1230799-7a9e-4325-a999-f49719c5752b', N'Big Data', N'AI', 3, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'b3c2ae60-2e0c-4bd3-961d-64aeecbd63db', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'Hạng Mục Test', N'Developing breakthrough applications using Large Language Models (LLMs) for text, image, video, or audio generation', 1, 0)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'fcef3ad8-1fd5-40b8-ba88-7ef9023a8ea0', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'Web', N'Contest to build web application', 2, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'd0a97168-eb3a-40d6-b947-848d8cd98346', N'6e411493-9891-4483-9a19-e81e986284e7', N'AI Tracks', N'About AI', 1, 0)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'93084f05-fbf5-4e87-83c9-995cc3e8d57a', N'c3a85586-6a74-4c1d-8688-34b6359ced09', N'AI Healthcare', N'AI Solutions for improving the quality of Healthcare, Medical, etc.', 2, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'dd9d6a12-e7ff-44db-b8b0-b36503ce2d76', N'5effbaf1-a851-435b-a657-0fd220e1083f', N'AI Tracks', N'', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'5aa9426a-966b-4f8d-80fb-df02eca948cd', N'98c6d276-8c18-4bc3-a8de-21483926777a', N'AI & Big Data', N'Focusing on utilizing artificial intelligence to solve complex social problems.', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'86a35a9c-cd51-4819-8569-e3868f4e3353', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'Machine Learning', N'AI', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'97b3e382-2725-4202-b41e-ec637292a634', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'IoT', N'Contest to build software and hardware', 1, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'Mobile Application', NULL, 3, 1)
INSERT [dbo].[Categories] ([CategoryID], [EventID], [CategoryName], [Description], [SortOrder], [IsActive]) VALUES (N'61e47de7-fa5b-4035-9e20-ecd1e8ceb300', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'Game', N'Contest to build a game', 3, 1)
GO
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'c1100000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000004', CAST(N'2026-05-10T08:00:00.0000000' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'ae185a9f-70d6-471e-a1ea-1670834af622', N'5aa9426a-966b-4f8d-80fb-df02eca948cd', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-02T23:55:14.7587148' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'23f3cb6a-0326-43f7-a177-17edfa2cb5fd', N'b3c2ae60-2e0c-4bd3-961d-64aeecbd63db', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-01T10:53:54.0563474' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'8a945784-41c6-475f-a190-1dacd2022a12', N'6b8cd8f6-d267-49a5-936d-5ac6411d6c56', N'a0000000-0000-0000-0000-111111111111', CAST(N'2026-07-09T01:06:09.2214046' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'ec0ec60b-c4c0-48fb-9d81-32e68b4dba45', N'c1000000-0000-0000-0000-000000000003', N'a1000000-0000-0000-0000-000000000004', CAST(N'2026-07-13T23:00:14.3675196' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'6f631e55-eaf7-4335-b101-3df7db9af274', N'95e6a07c-4524-4f48-98b8-0bb6f7d26546', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-09T12:41:19.0827449' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'1a408702-b7f6-40b2-a43b-49ba874c4aba', N'c1000000-0000-0000-0000-000000000003', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-13T22:51:41.7549791' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'0c4a47a0-4720-44ff-afc5-4e27832832b5', N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-02T23:57:50.8651380' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'3feeff81-7868-4dab-9cfc-54b925501ea6', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'a1000000-0000-0000-0000-000000000003', CAST(N'2026-07-08T21:27:06.3704832' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'ad5a7cb9-f6c7-4b21-90eb-5e88302c31c0', N'7d38b882-b988-4822-b8c4-409374e102dd', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-02T23:27:41.0957648' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'39ed7b3f-7e22-4b44-97e4-60baf1cca68c', N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'89abab57-98ab-4927-9a7f-9cdae0d8434f', CAST(N'2026-07-03T00:05:35.3030962' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'22bcd05d-4edd-4d94-91c5-74dc2b872e26', N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-02T23:57:18.6893498' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'e8e47293-5b56-43d9-a53d-836cb1fc715e', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'a0000000-0000-0000-0000-111111111111', CAST(N'2026-07-08T13:29:05.8327586' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'e03ebe7f-1e88-4c10-a88d-a737aa175747', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'8c5c3287-cd14-4186-9180-340d0387fc3a', CAST(N'2026-07-08T21:25:28.8652059' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'c254cc29-88e3-44a8-93c6-b044f7eeb287', N'74d82808-67b9-4a10-b5d1-14222bf8cb1e', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-03T14:06:06.5709406' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'd7078400-4caa-4ab2-af05-b3dd22fef075', N'ca000000-0000-0000-0000-111111111111', N'a0000000-0000-0000-0000-111111111111', CAST(N'2026-07-01T21:40:12.3066667' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'3e285e64-95f4-4b27-a4a2-be72be9d772a', N'ca000000-0000-0000-0000-222222222222', N'a0000000-0000-0000-0000-222222222222', CAST(N'2026-07-01T21:40:12.3066667' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'9d8196e2-15ce-4ef9-8f43-ceef55635da0', N'61e47de7-fa5b-4035-9e20-ecd1e8ceb300', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-09T14:43:43.2560123' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'62318fe7-bec2-4775-95b4-e075dd941707', N'fcef3ad8-1fd5-40b8-ba88-7ef9023a8ea0', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-09T14:43:23.0785693' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'c5feb621-534b-461b-b808-f0dc2633e345', N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'a0000000-0000-0000-0000-222222222222', CAST(N'2026-07-09T13:43:41.2686891' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'd75b3d82-cd5c-47a3-90ab-f161916e2618', N'c1000000-0000-0000-0000-000000000001', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-09T00:05:19.8254800' AS DateTime2), 1)
INSERT [dbo].[CategoryMentors] ([CategoryMentorID], [CategoryID], [MentorUserID], [AssignedAt], [IsActive]) VALUES (N'6ef5f149-87ed-4a86-898d-fd5a60e1b0cd', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-03T00:15:32.8662211' AS DateTime2), 1)
GO
INSERT [dbo].[ConsultationMessages] ([MessageID], [RequestID], [SenderID], [Content], [AttachmentUrl], [CreatedAt], [SeenAt]) VALUES (N'ddac5c7c-c884-4fb4-a888-1e8bb039ca91', N'c0000000-0000-0000-0000-111111111111', N'a0000000-0000-0000-0000-111111111111', N'I think it will be oke', NULL, CAST(N'2026-07-08T13:36:19.4119395' AS DateTime2), NULL)
INSERT [dbo].[ConsultationMessages] ([MessageID], [RequestID], [SenderID], [Content], [AttachmentUrl], [CreatedAt], [SeenAt]) VALUES (N'6881981b-b3d6-4ee9-932a-da32507201a6', N'c0000000-0000-0000-0000-222222222222', N'a0000000-0000-0000-0000-111111111111', N'Oke', NULL, CAST(N'2026-07-08T13:36:33.3752074' AS DateTime2), NULL)
GO
INSERT [dbo].[ConsultationRequests] ([RequestID], [EventID], [CategoryID], [TeamID], [MentorUserID], [CreatedByUserID], [Title], [Description], [Priority], [Status], [CreatedAt], [UpdatedAt], [ClosedAt]) VALUES (N'c0000000-0000-0000-0000-111111111111', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ca000000-0000-0000-0000-111111111111', N'd0000000-0000-0000-0000-111111111111', N'a0000000-0000-0000-0000-111111111111', N'a0000000-0000-0000-0000-333333333333', N'Need help with TensorFlow Model', N'Our model is overfitting the training data, could you provide some guidance on regularization techniques like Dropout or L2?', N'HIGH', N'IN_PROGRESS', CAST(N'2026-07-01T21:40:12.3200000' AS DateTime2), CAST(N'2026-07-08T13:36:24.6147671' AS DateTime2), NULL)
INSERT [dbo].[ConsultationRequests] ([RequestID], [EventID], [CategoryID], [TeamID], [MentorUserID], [CreatedByUserID], [Title], [Description], [Priority], [Status], [CreatedAt], [UpdatedAt], [ClosedAt]) VALUES (N'c0000000-0000-0000-0000-222222222222', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ca000000-0000-0000-0000-111111111111', N'd0000000-0000-0000-0000-111111111111', N'a0000000-0000-0000-0000-111111111111', N'a0000000-0000-0000-0000-333333333333', N'Dataset Preparation Questions', N'We have some missing values in our image dataset, should we drop them or use augmentation? Looking for best practices.', N'MEDIUM', N'RESOLVED', CAST(N'2026-07-01T21:40:12.3200000' AS DateTime2), CAST(N'2026-07-08T13:36:34.6234795' AS DateTime2), CAST(N'2026-07-08T13:36:34.4786269' AS DateTime2))
INSERT [dbo].[ConsultationRequests] ([RequestID], [EventID], [CategoryID], [TeamID], [MentorUserID], [CreatedByUserID], [Title], [Description], [Priority], [Status], [CreatedAt], [UpdatedAt], [ClosedAt]) VALUES (N'c0000000-0000-0000-0000-333333333333', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ca000000-0000-0000-0000-222222222222', N'd0000000-0000-0000-0000-222222222222', N'a0000000-0000-0000-0000-222222222222', N'a0000000-0000-0000-0000-444444444444', N'React State Management Issue', N'Our application state is out of sync between the sidebar and the main map component. Context API feels sluggish here.', N'URGENT', N'RESOLVED', CAST(N'2026-07-01T21:40:12.3200000' AS DateTime2), CAST(N'2026-07-01T21:40:12.3200000' AS DateTime2), NULL)
GO
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'c2000000-0000-0000-0000-000000000001', N'API Test Innovation', N'Originality and value of the solution.', CAST(0.40 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-05-05T08:00:00.0000000' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'c2000000-0000-0000-0000-000000000002', N'API Test Technical Quality', N'Architecture, implementation, and reliability.', CAST(0.35 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-05-05T08:05:00.0000000' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'c2000000-0000-0000-0000-000000000003', N'API Test Presentation', N'Clarity of demo and pitch.', CAST(0.25 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-05-05T08:10:00.0000000' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'777e03e6-3123-48d3-91a0-0402e5e8d693', N'K? Thu?t (Tech)', N'Ðánh giá ch?t lu?ng code', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T08:39:16.9200000' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'c25279f5-024a-4ef3-a585-26affb9612f8', N'Technical Complexity', N'Depth and complexity of technical implementation', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1366667' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'6989925b-66f5-49c6-a9da-65068ee38f95', N'Innovation & Creativity', N'Evaluates creativity, originality, feasibility, and differentiation from existing solutions.', CAST(0.30 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-10T03:20:30.5540647' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'348748fc-1750-42a7-9f5f-7245cb0226c5', N'Impact & Social Value', N'Potential societal or business impact', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1366667' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'4d301618-523f-421b-934b-745995c88030', N'Innovation', N'Evaluates the originality, creativity, and uniqueness of the proposed solution.', CAST(0.30 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-10T03:11:53.4837075' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'35ca3cb7-0cbc-4543-a4d2-904ccb45929c', N'Presentation Quality', N'Clarity and effectiveness of the demo/slide', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1366667' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'082074f1-7507-427c-ab92-a9a5b0978ee3', N'Code Quality', N'Readability, structure, and maintainability', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1366667' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'ae75b9a1-7a90-4721-a488-bfd01ffab2bb', N'Innovation', N'Originality and creativity of the solution', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1366667' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'b83deaf0-0c3e-46d3-8d62-ceb5a55513ce', N'Feasibility', N'Real-world applicability and viability', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1366667' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'70134de8-4d20-49fb-a2a6-dd638d0a6c36', N'Business Impact', N'Assesses the potential business value and real-world impact of the project.', CAST(0.20 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-10T03:09:27.4594425' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'fa26fd55-678c-4079-b525-e19580ef0cd4', N'Technical Implementation', N'Assesses the technical quality, architecture, scalability, and implementation of the solution.', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-10T01:52:18.0078718' AS DateTime2))
INSERT [dbo].[CriterionTemplate] ([TemplateID], [CriterionName], [Description], [DefaultWeight], [MaxScore], [IsActive], [CreatedByID], [CreatedAt]) VALUES (N'c89e0913-468e-470d-be6f-e85462dc9470', N'Idea', N'Ðánh giá tính sáng t?o', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T08:39:16.9200000' AS DateTime2))
GO
INSERT [dbo].[Disqualifications] ([DisqualificationID], [TeamID], [SubmissionID], [Reason], [DisqualifiedByID], [DisqualifiedAt], [IsReversed], [ReversedAt], [ReversedByID], [ReversalReason]) VALUES (N'f6000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000003', NULL, N'Team violated the competition eligibility policy.', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-12T09:00:00.0000000' AS DateTime2), 0, NULL, NULL, NULL)
INSERT [dbo].[Disqualifications] ([DisqualificationID], [TeamID], [SubmissionID], [Reason], [DisqualifiedByID], [DisqualifiedAt], [IsReversed], [ReversedAt], [ReversedByID], [ReversalReason]) VALUES (N'f6000000-0000-0000-0000-000000000002', NULL, N'f1000000-0000-0000-0000-000000000004', N'Repository contained material submitted before the competition window.', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-12T09:05:00.0000000' AS DateTime2), 0, NULL, NULL, NULL)
INSERT [dbo].[Disqualifications] ([DisqualificationID], [TeamID], [SubmissionID], [Reason], [DisqualifiedByID], [DisqualifiedAt], [IsReversed], [ReversedAt], [ReversedByID], [ReversalReason]) VALUES (N'b28db260-bc33-436d-920b-4b322fb31884', N'3dfb2ba7-77a3-4608-bd4d-cc49b619bf54', NULL, N'Sample disqualification record for testing', N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1933333' AS DateTime2), 0, NULL, NULL, NULL)
INSERT [dbo].[Disqualifications] ([DisqualificationID], [TeamID], [SubmissionID], [Reason], [DisqualifiedByID], [DisqualifiedAt], [IsReversed], [ReversedAt], [ReversedByID], [ReversalReason]) VALUES (N'faf10d6c-5cb1-42ac-96f6-9ec4623478c8', N'f8b4d5f2-55b0-4e1c-87f9-ec17d6a619c6', NULL, N'Approve is disabled until every issue above is resolved (team size must be within the event''s 2–5 range and all member profiles must be complete).', N'a0000000-0000-0000-0000-000000000000', CAST(N'2026-07-09T13:55:21.8575675' AS DateTime2), 0, NULL, NULL, NULL)
GO
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'f7000000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000001', N'SCORE_UPDATED', N'a1000000-0000-0000-0000-000000000002', N'f2000000-0000-0000-0000-000000000002', N'e1000000-0000-0000-0000-000000000001', N'f1000000-0000-0000-0000-000000000001', N'{"score":8.0}', N'{"score":8.5}', N'Corrected after reviewing the architecture demo.', CAST(N'2026-06-12T10:30:00.0000000' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'8f3c7054-db80-499b-b1b3-35f1b7c471a5', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'df0544f9-8b6e-4d95-b008-9b462795856a', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'732cb879-0c63-4a36-864e-96a833c243b8', NULL, N'{"score":8,"comment":"good"}', N'Initial score submission', CAST(N'2026-07-02T14:47:05.5107409' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'a49070b3-fbd6-462d-b8f2-4ae32a5e30a7', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'bf8363e0-fd5b-40a4-9e55-9f3b7abaf394', N'bab0ac21-0e0e-496a-af83-576b30867164', N'391cb64b-6ff7-4471-b76a-973ade51874f', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-09T16:35:42.4831229' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'f45b0209-8db0-48cf-943b-4dc148fe7f6a', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'3fd85fba-e200-4f88-81a9-9dc11875abbf', N'bab0ac21-0e0e-496a-af83-576b30867164', N'391cb64b-6ff7-4471-b76a-973ade51874f', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-09T16:35:42.3551947' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'f721355a-eed9-498f-aed9-56938266ec0f', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'f0b30df9-29c1-470d-9cb9-73c66e53a342', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'4fbbe238-1f5c-4579-8be8-cdc6ffe052e2', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-02T15:16:20.5695143' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'ac58f419-8d51-4f3b-bb02-8717f297cc10', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'66eb656e-ef5a-4157-918d-936c402a6692', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-02T15:09:59.2797773' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'798b2b45-8baa-4239-aede-885500cacc9c', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'60433ed3-338b-4824-b18c-a121f527a75d', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-02T15:09:59.2346736' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'f11bc576-57c3-4596-b166-975ff69b93bb', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'3bd8ca00-75de-4c84-9490-1913d21f0e92', N'bab0ac21-0e0e-496a-af83-576b30867164', N'391cb64b-6ff7-4471-b76a-973ade51874f', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-09T16:35:42.4028578' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'8f5026a8-83c8-4d04-b00b-d503676a9dc3', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'f0809dc2-0eff-47c2-9bb7-7f8dd386d02f', N'bab0ac21-0e0e-496a-af83-576b30867164', N'391cb64b-6ff7-4471-b76a-973ade51874f', NULL, N'{"score":10,"comment":""}', N'Initial score submission', CAST(N'2026-07-09T16:35:42.4402826' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'63b5921d-dd80-4050-9eab-e1d8625db443', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'5f28a6cd-838c-4c16-861b-7bd00c194645', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'4fbbe238-1f5c-4579-8be8-cdc6ffe052e2', NULL, N'{"score":9,"comment":""}', N'Initial score submission', CAST(N'2026-07-02T15:16:20.5374249' AS DateTime2))
INSERT [dbo].[EvaluationAuditLogs] ([EvaluationAuditLogID], [EventID], [ActionType], [ActorUserID], [JudgingID], [TeamID], [SubmissionID], [OldValue], [NewValue], [Reason], [CreatedAt]) VALUES (N'4f0c3f0b-27c1-4faa-a4ac-f5976363b477', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'SCORE_CREATED', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'905afe69-7840-439e-96fd-0d559e357001', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'732cb879-0c63-4a36-864e-96a833c243b8', NULL, N'{"score":10,"comment":"perfect"}', N'Initial score submission', CAST(N'2026-07-02T14:47:05.6301727' AS DateTime2))
GO
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c3000000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000001', N'c2000000-0000-0000-0000-000000000001', N'Innovation', N'Originality and user value.', CAST(0.35 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c3000000-0000-0000-0000-000000000002', N'b1000000-0000-0000-0000-000000000001', N'c2000000-0000-0000-0000-000000000002', N'Technical Quality', N'Implementation quality.', CAST(0.40 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c3000000-0000-0000-0000-000000000003', N'b1000000-0000-0000-0000-000000000001', N'c2000000-0000-0000-0000-000000000003', N'Presentation', N'Demo and pitch quality.', CAST(0.25 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c3000000-0000-0000-0000-000000000011', N'b1000000-0000-0000-0000-000000000002', N'c2000000-0000-0000-0000-000000000001', N'Innovation', N'Originality and user value.', CAST(0.40 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c3000000-0000-0000-0000-000000000012', N'b1000000-0000-0000-0000-000000000002', N'c2000000-0000-0000-0000-000000000002', N'Technical Quality', N'Implementation quality.', CAST(0.30 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c3000000-0000-0000-0000-000000000013', N'b1000000-0000-0000-0000-000000000002', N'c2000000-0000-0000-0000-000000000003', N'Presentation', N'Demo and pitch quality.', CAST(0.25 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'123f399b-2d8b-47e3-9766-0000ecbabef3', N'b1000000-0000-0000-0000-000000000002', N'c25279f5-024a-4ef3-a585-26affb9612f8', N'Technical Complexity', N'Depth and complexity of technical implementation', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 4, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'7961cef8-d8b9-403e-b25c-15e8ef495b52', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', NULL, N'Tech', N'Technical Implementation Score', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'21115bb7-e25e-4581-a1ac-15ef5fe39aaa', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'ae75b9a1-7a90-4721-a488-bfd01ffab2bb', N'Innovation', N'Originality and creativity of the solution', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'9a46b529-0ed0-4de6-91c7-1b996fb9f96a', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', NULL, N'Idea', N'Idea and Creativity Score', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 0, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'9729db7d-6664-46c5-bb1d-1c6b6887c29c', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'777e03e6-3123-48d3-91a0-0402e5e8d693', N'K? Thu?t (Tech)', N'Ðánh giá ch?t lu?ng code', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'eaaa45ee-255e-47fe-92a1-1d535742bb32', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ae75b9a1-7a90-4721-a488-bfd01ffab2bb', N'Innovation', N'Originality and creativity of the solution', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'4c7e910f-3be3-4cc8-aa56-24065c7ff835', N'c3a85586-6a74-4c1d-8688-34b6359ced09', N'6989925b-66f5-49c6-a9da-65068ee38f95', N'Innovation & Creativity', N'Evaluates creativity, originality, feasibility, and differentiation from existing solutions.', CAST(0.30 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'9750525e-4037-41d0-9f41-2802d479b06d', N'93059e4e-907e-403b-afb2-fbeb049bbdce', N'fa26fd55-678c-4079-b525-e19580ef0cd4', N'Technical Implementation', N'Assesses the technical quality, architecture, scalability, and implementation of the solution.', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'79c78700-d68a-4454-9a40-397b72b8fd89', N'6e411493-9891-4483-9a19-e81e986284e7', N'fa26fd55-678c-4079-b525-e19580ef0cd4', N'Technical Implementation', N'Assesses the technical quality, architecture, scalability, and implementation of the solution.', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 0)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'02791336-467f-4400-89ef-3986ad66338c', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'b83deaf0-0c3e-46d3-8d62-ceb5a55513ce', N'Feasibility', N'Real-world applicability and viability', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'e230d584-7926-4e6c-a24d-49b6e9cc2f8f', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'35ca3cb7-0cbc-4543-a4d2-904ccb45929c', N'Presentation Quality', N'Clarity and effectiveness of the demo/slide', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'7a89f45e-ac16-4813-a57d-4cf05007e631', N'b1000000-0000-0000-0000-000000000002', N'fa26fd55-678c-4079-b525-e19580ef0cd4', N'Technical Implementation', N'Assesses the technical quality, architecture, scalability, and implementation of the solution.', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 5, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'd20de1d1-1545-4c93-a1f8-52f8c2944a05', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'c25279f5-024a-4ef3-a585-26affb9612f8', N'Technical Complexity', N'Depth and complexity of technical implementation', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 5, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'e18b23f8-5304-44a1-8290-55ab3e39a019', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'70134de8-4d20-49fb-a2a6-dd638d0a6c36', N'Business Impact', N'Assesses the potential business value and real-world impact of the project.', CAST(0.20 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'45edaad9-f280-4169-b875-64ca389f169f', N'f1230799-7a9e-4325-a999-f49719c5752b', N'082074f1-7507-427c-ab92-a9a5b0978ee3', N'Code Quality', N'Readability, structure, and maintainability', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'f1771b9c-2f10-467a-99b6-716873c7cb55', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'b83deaf0-0c3e-46d3-8d62-ceb5a55513ce', N'Feasibility', N'Real-world applicability and viability', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'c4686d88-bcca-4798-ac98-7b0f86183376', N'f1230799-7a9e-4325-a999-f49719c5752b', N'777e03e6-3123-48d3-91a0-0402e5e8d693', N'K? Thu?t (Tech)', N'Ðánh giá ch?t lu?ng code', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 3, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'd10679bf-87f2-448b-b5e7-85c7d02bbd24', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'b83deaf0-0c3e-46d3-8d62-ceb5a55513ce', N'Feasibility', N'Real-world applicability and viability', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'7a7406ab-10c5-4679-99d8-90598b40c564', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'35ca3cb7-0cbc-4543-a4d2-904ccb45929c', N'Presentation Quality', N'Clarity and effectiveness of the demo/slide', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 4, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'7525f797-9f37-4579-ac54-95144fa6c658', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'35ca3cb7-0cbc-4543-a4d2-904ccb45929c', N'Presentation Quality', N'Clarity and effectiveness of the demo/slide', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 4, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'52c004e2-bee1-4809-9376-a4abe265fd10', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'ae75b9a1-7a90-4721-a488-bfd01ffab2bb', N'Innovation', N'Originality and creativity of the solution', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'd5c5d72c-0314-45e4-bb18-ad5474b5d64e', N'f1230799-7a9e-4325-a999-f49719c5752b', N'35ca3cb7-0cbc-4543-a4d2-904ccb45929c', N'Presentation Quality', N'Clarity and effectiveness of the demo/slide', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 6, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'e869aa38-9be3-400e-b24f-ade00070d667', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'6989925b-66f5-49c6-a9da-65068ee38f95', N'Innovation & Creativity', N'Evaluates creativity, originality, feasibility, and differentiation from existing solutions.', CAST(0.30 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'6bc91a1b-c07a-4007-997f-cd3f49d00847', N'f1230799-7a9e-4325-a999-f49719c5752b', N'c25279f5-024a-4ef3-a585-26affb9612f8', N'Technical Complexity', N'Depth and complexity of technical implementation', CAST(2.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 5, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'e920779c-0f66-46b8-b70d-d025dd89cd72', N'6e411493-9891-4483-9a19-e81e986284e7', N'70134de8-4d20-49fb-a2a6-dd638d0a6c36', N'Business Impact', N'Assesses the potential business value and real-world impact of the project.', CAST(0.50 AS Decimal(5, 2)), CAST(20.00 AS Decimal(6, 2)), 2, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'81690c60-a72c-4fd3-a253-e7cfaa2f5f4c', N'f1230799-7a9e-4325-a999-f49719c5752b', N'b83deaf0-0c3e-46d3-8d62-ceb5a55513ce', N'Feasibility', N'Real-world applicability and viability', CAST(1.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 4, 1)
INSERT [dbo].[EventCriteria] ([EventCriterionID], [EventID], [TemplateID], [CriterionName], [Description], [Weight], [MaxScore], [SortOrder], [IsActive]) VALUES (N'4e9514ef-a5d4-4530-9ff8-ee9ce786d17f', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'082074f1-7507-427c-ab92-a9a5b0978ee3', N'Code Quality', N'Readability, structure, and maintainability', CAST(1.00 AS Decimal(5, 2)), CAST(10.00 AS Decimal(6, 2)), 1, 1)
GO
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'54d42a91-3826-428b-a90a-29f7b97ac688', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'5d565896-2307-4911-939d-94e11e4b1c44', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T11:25:01.0498994' AS DateTime2), CAST(N'2026-07-04T11:25:46.5846693' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T11:25:01.0498994' AS DateTime2), CAST(N'2026-07-04T11:25:46.6446974' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'd5da51f1-cfd5-4285-82c3-38ed8d3752a6', N'f1230799-7a9e-4325-a999-f49719c5752b', N'1cefeef1-28e1-4897-b4ff-b85942aaad9f', N'80000000-0000-0000-0000-000000000001', CAST(N'2026-07-04T02:34:10.8648545' AS DateTime2), NULL, NULL, NULL, CAST(N'2026-07-04T02:34:10.8668575' AS DateTime2), CAST(N'2026-07-04T02:34:10.8668575' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'f3e8c517-f8df-4371-8957-42426f1fa53b', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:33:34.8115388' AS DateTime2), CAST(N'2026-07-04T02:36:14.0541141' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:33:34.8125465' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'061f75eb-37e3-4dfc-b2d6-4412e0f3cceb', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'4d2f7a5b-a634-46a9-92ee-ac28fa245a1a', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:35:25.3108469' AS DateTime2), CAST(N'2026-07-04T02:36:14.0944621' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:35:25.3108469' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'1c8e7d85-f46f-4022-972d-4b830b419ea4', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T11:25:22.6529972' AS DateTime2), CAST(N'2026-07-04T11:25:44.0212778' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T11:25:22.6540001' AS DateTime2), CAST(N'2026-07-04T11:25:44.0581478' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'303f95e5-c02e-408c-9858-58ba8d537d23', N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T11:15:43.5581954' AS DateTime2), CAST(N'2026-07-04T11:15:58.2572257' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T11:15:43.5581954' AS DateTime2), CAST(N'2026-07-04T11:15:58.3093038' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'0c35b87b-6b37-4555-8362-5a6cabc6a49e', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'1cefeef1-28e1-4897-b4ff-b85942aaad9f', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:34:18.4442327' AS DateTime2), CAST(N'2026-07-04T02:36:14.1407319' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:34:18.4447381' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'5fb03e19-3382-4e81-9c27-5fe8e23d2e9c', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ea969288-a77c-4683-b661-b10955f1df18', N'80000000-0000-0000-0000-000000000001', CAST(N'2026-07-01T11:48:10.8284461' AS DateTime2), NULL, NULL, NULL, CAST(N'2026-07-01T11:48:10.8284461' AS DateTime2), CAST(N'2026-07-01T11:48:10.8284461' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'ebfa6d81-7a7a-4ee3-be07-6872de80e48b', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:33:19.3581469' AS DateTime2), CAST(N'2026-07-04T02:36:14.2581080' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:33:19.3595986' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'79f264ae-66f0-4792-8dbb-6aa91462abc3', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'ea969288-a77c-4683-b661-b10955f1df18', N'80000000-0000-0000-0000-000000000003', CAST(N'2026-07-02T15:10:44.3537664' AS DateTime2), NULL, NULL, N'deo cho', CAST(N'2026-07-02T15:10:44.3557787' AS DateTime2), CAST(N'2026-07-14T09:39:38.7912325' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'fa9a42ad-80e8-4ae2-a42a-8569a59ba516', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:33:02.1103266' AS DateTime2), CAST(N'2026-07-04T02:36:14.3607573' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:33:02.1103266' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'89d4b0be-9576-4477-b87e-914e30265978', N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038', N'5d565896-2307-4911-939d-94e11e4b1c44', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T11:14:56.6123997' AS DateTime2), CAST(N'2026-07-04T11:15:29.5850205' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T11:14:56.6138668' AS DateTime2), CAST(N'2026-07-04T11:15:29.6635683' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'e19bc273-adb6-487a-b23e-9ce3e908f55e', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'46f63efa-185a-4a0a-9e9f-934f01c6513c', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:35:11.1805433' AS DateTime2), CAST(N'2026-07-04T02:36:14.4631374' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:35:11.1805433' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'0c0e1749-1cb7-4b6a-9742-a8f4b159afd1', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'fa60133b-5874-4dbf-9299-a0069495b193', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-02T14:41:55.2559607' AS DateTime2), CAST(N'2026-07-02T14:42:38.9797395' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-02T14:41:55.2584739' AS DateTime2), CAST(N'2026-07-02T14:42:39.0914014' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'd6127ae4-5de2-4820-8b30-a936067ca684', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'ea969288-a77c-4683-b661-b10955f1df18', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-06-30T20:32:39.7672758' AS DateTime2), CAST(N'2026-07-01T11:48:56.6321292' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-06-30T20:32:39.7762320' AS DateTime2), CAST(N'2026-07-01T11:48:56.7072832' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'92564095-d844-4ff2-8029-b5b096a612cf', N'f1230799-7a9e-4325-a999-f49719c5752b', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'80000000-0000-0000-0000-000000000001', CAST(N'2026-07-04T02:30:02.1355377' AS DateTime2), NULL, NULL, NULL, CAST(N'2026-07-04T02:30:02.1394611' AS DateTime2), CAST(N'2026-07-04T02:30:02.1394611' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'76130b77-9cdc-47c5-9884-b62f9902fbfe', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'960a79c4-da41-478b-85bd-61f579e43f5b', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:34:45.6818798' AS DateTime2), CAST(N'2026-07-04T02:36:14.5659975' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:34:45.6828820' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'1ccc2ca7-ddc9-4aec-bcfc-d348b0034ea8', N'f1230799-7a9e-4325-a999-f49719c5752b', N'5d565896-2307-4911-939d-94e11e4b1c44', N'80000000-0000-0000-0000-000000000001', CAST(N'2026-07-02T21:20:10.3839224' AS DateTime2), NULL, NULL, NULL, CAST(N'2026-07-02T21:20:10.3849245' AS DateTime2), CAST(N'2026-07-02T21:20:10.3849245' AS DateTime2))
INSERT [dbo].[EventParticipants] ([EventParticipantID], [EventID], [UserID], [ParticipantStatusID], [AppliedAt], [ApprovedAt], [ApprovedBy], [RejectedReason], [CreatedAt], [UpdatedAt]) VALUES (N'a95a34b2-d4c3-47d2-ad21-f252fd122153', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'e3f9b065-3380-47ff-9705-5d8106188e4e', N'80000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T02:34:33.7017207' AS DateTime2), CAST(N'2026-07-04T02:36:14.6686673' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, CAST(N'2026-07-04T02:34:33.7017207' AS DateTime2), CAST(N'2026-07-04T02:36:14.8869214' AS DateTime2))
GO
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'f3100000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000001', CAST(8.5833 AS Decimal(10, 4)), 1, CAST(N'2026-06-12T12:05:00.0000000' AS DateTime2), 1)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'f3100000-0000-0000-0000-000000000002', N'b1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000002', CAST(7.4167 AS Decimal(10, 4)), 2, CAST(N'2026-06-12T12:05:00.0000000' AS DateTime2), 1)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'f3100000-0000-0000-0000-000000000003', N'b1000000-0000-0000-0000-000000000002', N'c1000000-0000-0000-0000-000000000003', N'e1000000-0000-0000-0000-000000000004', CAST(9.3667 AS Decimal(10, 4)), 1, CAST(N'2025-10-12T12:05:00.0000000' AS DateTime2), 1)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'fd140495-f43a-40af-b948-2b37324a58a7', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', CAST(29.0000 AS Decimal(10, 4)), 1, CAST(N'2026-07-04T01:57:27.8542103' AS DateTime2), 0)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'29fcbafb-1108-4bb7-8ee2-4f2fac40bf0d', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'f8b4d5f2-55b0-4e1c-87f9-ec17d6a619c6', CAST(0.0000 AS Decimal(10, 4)), 3, CAST(N'2026-07-04T01:57:27.9963530' AS DateTime2), 0)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'630e2cfa-fa0e-4c12-8dca-6fca21e2ec06', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'9db12c83-94fb-487f-8bcb-aee523a60d75', CAST(0.0000 AS Decimal(10, 4)), 3, CAST(N'2026-07-04T01:57:27.9627308' AS DateTime2), 0)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'76e43f8b-d4d7-4382-b7a2-76b8c0861505', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'98c856ef-f5d0-41ce-bab8-6e3747c58da4', CAST(0.0000 AS Decimal(10, 4)), 3, CAST(N'2026-07-04T01:57:27.9255267' AS DateTime2), 0)
INSERT [dbo].[EventRankings] ([EventRankingID], [EventID], [CategoryID], [TeamID], [FinalScore], [RankPosition], [ComputedAt], [IsPublished]) VALUES (N'dd0e81e4-ed81-438e-bd49-ce41e63a28e6', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', CAST(28.0000 AS Decimal(10, 4)), 2, CAST(N'2026-07-04T01:57:27.8918427' AS DateTime2), 0)
GO
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'b1000000-0000-0000-0000-000000000001', N'API Test Hackathon 2026', N'Live event used for API integration tests.', N'FPT University HCMC', N'https://example.test/images/api-hackathon-2026.png', N'30000000-0000-0000-0000-000000000003', CAST(N'2026-05-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-06-05T23:59:59.0000000' AS DateTime2), CAST(N'2026-06-10' AS Date), CAST(N'2026-06-30' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-05-01T08:00:00.0000000' AS DateTime2), CAST(N'2026-06-10T08:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'b1000000-0000-0000-0000-000000000002', N'API Test Innovation Showcase 2025', N'Completed event for ranking and hall-of-fame tests.', N'FPT University HCMC', N'https://example.test/images/api-showcase-2025.png', N'30000000-0000-0000-0000-000000000004', CAST(N'2025-09-01T00:00:00.0000000' AS DateTime2), CAST(N'2025-09-30T23:59:59.0000000' AS DateTime2), CAST(N'2025-10-10' AS Date), CAST(N'2025-10-12' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2025-08-15T08:00:00.0000000' AS DateTime2), CAST(N'2025-10-13T08:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'5effbaf1-a851-435b-a657-0fd220e1083f', N'Seal Dev Marathon 2026', N'Full-stack development challenge.', N'FPT University, Ho Chi Minh City', NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-07-05T08:00:00.0000000' AS DateTime2), CAST(N'2026-07-25T23:59:00.0000000' AS DateTime2), CAST(N'2026-07-03' AS Date), CAST(N'2026-08-20' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-03T15:56:32.8615651' AS DateTime2), CAST(N'2026-07-03T15:56:32.8615651' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'AI Innovation Hackathon 2026', N'A hackathon focused on AI, blockchain, and software innovation for FPT University students.', N'FPT University Hanoi Campus', N'https://example.com/images/seal-ai-hackathon-2027.png', N'30000000-0000-0000-0000-000000000002', CAST(N'2026-06-28T11:30:00.0000000' AS DateTime2), CAST(N'2026-07-28T11:59:00.0000000' AS DateTime2), CAST(N'2026-06-28' AS Date), CAST(N'2026-12-30' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-27T02:29:22.2082716' AS DateTime2), CAST(N'2026-07-03T23:45:15.7754135' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'Scoring Test Event', N'Event for scoring test.', N'Online', NULL, N'30000000-0000-0000-0000-000000000002', CAST(N'2026-07-03T23:34:00.0000000' AS DateTime2), CAST(N'2026-07-25T00:17:00.0000000' AS DateTime2), CAST(N'2026-07-10' AS Date), CAST(N'2026-08-31' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T08:39:16.9200000' AS DateTime2), CAST(N'2026-07-04T00:19:02.0165173' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'98c6d276-8c18-4bc3-a8de-21483926777a', N'Hackathon Talent Fall 2026', N'—', N'FPT University HCMC', NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-06-27T09:53:00.0000000' AS DateTime2), CAST(N'2026-07-28T17:00:00.0000000' AS DateTime2), CAST(N'2026-06-28' AS Date), CAST(N'2026-11-30' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-27T16:10:06.9119429' AS DateTime2), CAST(N'2026-06-28T07:18:16.0506859' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038', N'SWP Hackathon 2026', N'Demo', N'FPTU HCMC', N'', N'30000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T01:00:00.0000000' AS DateTime2), CAST(N'2026-07-04T23:34:00.0000000' AS DateTime2), CAST(N'2026-07-04' AS Date), CAST(N'2026-07-05' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-03T23:35:33.9423908' AS DateTime2), CAST(N'2026-07-03T23:35:33.9423908' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'c3a85586-6a74-4c1d-8688-34b6359ced09', N'Demo', N'Demo', N'FPT', NULL, N'30000000-0000-0000-0000-000000000002', CAST(N'2026-06-28T20:29:00.0000000' AS DateTime2), CAST(N'2026-06-29T20:23:00.0000000' AS DateTime2), CAST(N'2026-06-29' AS Date), CAST(N'2026-06-30' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-28T20:23:35.7991405' AS DateTime2), CAST(N'2026-06-28T20:23:35.7991405' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'487df0aa-a65c-4cde-abc6-394c3025fb0a', N'Hackathon Talent 2027', NULL, NULL, NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-06-28T09:16:00.0000000' AS DateTime2), CAST(N'2026-07-27T08:08:00.0000000' AS DateTime2), CAST(N'2026-06-27' AS Date), CAST(N'2026-12-27' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-27T15:13:31.3956230' AS DateTime2), CAST(N'2026-06-27T15:13:31.3956230' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'SealTest 2026', N'Event to Testing.', N'FPT', N'', N'30000000-0000-0000-0000-000000000003', CAST(N'2026-07-01T10:24:00.0000000' AS DateTime2), CAST(N'2026-07-04T16:24:00.0000000' AS DateTime2), CAST(N'2026-07-09' AS Date), CAST(N'2026-07-10' AS Date), 5, 1, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-30T15:24:57.0997613' AS DateTime2), CAST(N'2026-07-14T13:40:12.1395404' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'383f36c8-354c-4433-b36d-6d7c7e87d9bc', N'FPTU Hackathon Talent', N'—', NULL, NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-06-27T09:48:00.0000000' AS DateTime2), NULL, NULL, NULL, 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-27T16:09:45.2740216' AS DateTime2), CAST(N'2026-06-27T16:47:46.0514679' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'7d627537-1f9a-4bb3-81bc-76e7b92622cc', N'DEMO_EVENT', N'only for testing purpose', N'FPTU', NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-06-30T10:40:00.0000000' AS DateTime2), CAST(N'2026-07-01T10:36:00.0000000' AS DateTime2), CAST(N'2026-07-03' AS Date), CAST(N'2026-07-05' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-30T10:37:22.2598828' AS DateTime2), CAST(N'2026-06-30T10:37:22.2598828' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'e7b085bb-5fa5-402e-b960-bf2ad5abc574', N'Hackathon Talent', N'A competition to find talents in AI, Cybersecurity, etc.', NULL, NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-06-30T09:17:00.0000000' AS DateTime2), CAST(N'2026-12-15T17:51:00.0000000' AS DateTime2), CAST(N'2026-06-29' AS Date), CAST(N'2026-12-31' AS Date), 5, 3, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-27T14:52:36.8915589' AS DateTime2), CAST(N'2026-06-27T16:16:31.0084759' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'af215827-b694-41d8-8a57-cc50f10e54c8', N'AI Research', N'—', N'FPT University, HCM', NULL, N'30000000-0000-0000-0000-000000000001', CAST(N'2026-07-31T15:15:00.0000000' AS DateTime2), CAST(N'2026-08-08T19:00:00.0000000' AS DateTime2), CAST(N'2026-08-10' AS Date), CAST(N'2026-08-15' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T15:15:47.2000721' AS DateTime2), CAST(N'2026-07-01T15:15:58.9921303' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'DEMOEVENT', N'for testing', N'FPTU', NULL, N'30000000-0000-0000-0000-000000000002', CAST(N'2026-06-30T11:09:00.0000000' AS DateTime2), CAST(N'2026-07-01T11:06:00.0000000' AS DateTime2), CAST(N'2026-07-02' AS Date), CAST(N'2026-07-05' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-06-30T11:07:01.9542966' AS DateTime2), CAST(N'2026-06-30T11:07:01.9542966' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'6e411493-9891-4483-9a19-e81e986284e7', N'Test1', N'Test', N'Test', N'', N'30000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T01:01:00.0000000' AS DateTime2), CAST(N'2026-07-04T22:01:00.0000000' AS DateTime2), CAST(N'2026-07-04' AS Date), CAST(N'2026-07-06' AS Date), 5, 2, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-04T00:01:41.8542841' AS DateTime2), CAST(N'2026-07-04T00:01:41.8542841' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'f1230799-7a9e-4325-a999-f49719c5752b', N'SEAL Hackathon 2026', N'Sample event for testing team workflow', N'FPT University', NULL, N'30000000-0000-0000-0000-000000000002', CAST(N'2026-06-27T04:48:00.0000000' AS DateTime2), CAST(N'2026-07-29T04:48:00.0000000' AS DateTime2), CAST(N'2026-06-26' AS Date), CAST(N'2026-12-31' AS Date), 5, 3, N'97027c55-304d-44cd-b99d-4eb09607dc87', CAST(N'2026-06-26T09:48:24.1500000' AS DateTime2), CAST(N'2026-06-28T11:14:58.0366827' AS DateTime2), 0)
INSERT [dbo].[Events] ([EventID], [EventName], [Description], [Location], [BannerImageURL], [EventStatusID], [RegistrationStart], [RegistrationEnd], [EventStartDate], [EventEndDate], [MaxTeamSize], [MinTeamSize], [CreatedByID], [CreatedAt], [UpdatedAt], [IsDeleted]) VALUES (N'93059e4e-907e-403b-afb2-fbeb049bbdce', N'SEAL Hackathon 2025: Tech for Good', N'Gi?i d?u l?n nh?t dành cho sinh viên', N'FPT University Hola Campus', N'https://example.com/banner-hackathon-2025.jpg', N'30000000-0000-0000-0000-000000000003', CAST(N'2026-07-02T20:00:00.0000000' AS DateTime2), CAST(N'2026-07-02T23:59:00.0000000' AS DateTime2), CAST(N'2026-07-04' AS Date), CAST(N'2026-07-05' AS Date), 5, 3, N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-02T13:10:02.2966667' AS DateTime2), CAST(N'2026-07-14T16:37:13.4785985' AS DateTime2), 0)
GO
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000005', N'Cancelled')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000004', N'Completed')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000001', N'Draft')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000003', N'Ongoing')
INSERT [dbo].[EventStatus] ([StatusID], [StatusName]) VALUES (N'30000000-0000-0000-0000-000000000002', N'Registration Open')
GO
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000001', N'f1000000-0000-0000-0000-000000000001', N'd3000000-0000-0000-0000-000000000001', N'd2000000-0000-0000-0000-000000000001', CAST(9.00 AS Decimal(6, 2)), N'Strong user value.', CAST(N'2026-06-12T10:00:00.0000000' AS DateTime2), CAST(N'2026-06-12T10:00:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000002', N'f1000000-0000-0000-0000-000000000001', N'd3000000-0000-0000-0000-000000000001', N'd2000000-0000-0000-0000-000000000002', CAST(8.50 AS Decimal(6, 2)), N'Solid architecture.', CAST(N'2026-06-12T10:05:00.0000000' AS DateTime2), CAST(N'2026-06-12T10:05:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000003', N'f1000000-0000-0000-0000-000000000001', N'd3000000-0000-0000-0000-000000000001', N'd2000000-0000-0000-0000-000000000003', CAST(8.00 AS Decimal(6, 2)), N'Clear demo.', CAST(N'2026-06-12T10:10:00.0000000' AS DateTime2), CAST(N'2026-06-12T10:10:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000004', N'f1000000-0000-0000-0000-000000000001', N'd3000000-0000-0000-0000-000000000002', N'd2000000-0000-0000-0000-000000000001', CAST(8.50 AS Decimal(6, 2)), N'Useful concept.', CAST(N'2026-06-12T10:15:00.0000000' AS DateTime2), CAST(N'2026-06-12T10:15:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000005', N'f1000000-0000-0000-0000-000000000001', N'd3000000-0000-0000-0000-000000000002', N'd2000000-0000-0000-0000-000000000002', CAST(9.00 AS Decimal(6, 2)), N'Well implemented.', CAST(N'2026-06-12T10:20:00.0000000' AS DateTime2), CAST(N'2026-06-12T10:20:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000006', N'f1000000-0000-0000-0000-000000000001', N'd3000000-0000-0000-0000-000000000002', N'd2000000-0000-0000-0000-000000000003', CAST(8.50 AS Decimal(6, 2)), N'Good answers.', CAST(N'2026-06-12T10:25:00.0000000' AS DateTime2), CAST(N'2026-06-12T10:25:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000011', N'f1000000-0000-0000-0000-000000000002', N'd3000000-0000-0000-0000-000000000001', N'd2000000-0000-0000-0000-000000000001', CAST(7.50 AS Decimal(6, 2)), N'Good idea.', CAST(N'2026-06-12T11:00:00.0000000' AS DateTime2), CAST(N'2026-06-12T11:00:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000012', N'f1000000-0000-0000-0000-000000000002', N'd3000000-0000-0000-0000-000000000001', N'd2000000-0000-0000-0000-000000000002', CAST(7.00 AS Decimal(6, 2)), N'Needs more tests.', CAST(N'2026-06-12T11:05:00.0000000' AS DateTime2), CAST(N'2026-06-12T11:05:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000013', N'f1000000-0000-0000-0000-000000000002', N'd3000000-0000-0000-0000-000000000001', N'd2000000-0000-0000-0000-000000000003', CAST(8.00 AS Decimal(6, 2)), N'Good pitch.', CAST(N'2026-06-12T11:10:00.0000000' AS DateTime2), CAST(N'2026-06-12T11:10:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000014', N'f1000000-0000-0000-0000-000000000002', N'd3000000-0000-0000-0000-000000000002', N'd2000000-0000-0000-0000-000000000001', CAST(7.00 AS Decimal(6, 2)), N'Common approach.', CAST(N'2026-06-12T11:15:00.0000000' AS DateTime2), CAST(N'2026-06-12T11:15:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000015', N'f1000000-0000-0000-0000-000000000002', N'd3000000-0000-0000-0000-000000000002', N'd2000000-0000-0000-0000-000000000002', CAST(7.50 AS Decimal(6, 2)), N'Functional prototype.', CAST(N'2026-06-12T11:20:00.0000000' AS DateTime2), CAST(N'2026-06-12T11:20:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000016', N'f1000000-0000-0000-0000-000000000002', N'd3000000-0000-0000-0000-000000000002', N'd2000000-0000-0000-0000-000000000003', CAST(7.50 AS Decimal(6, 2)), N'Clear presentation.', CAST(N'2026-06-12T11:25:00.0000000' AS DateTime2), CAST(N'2026-06-12T11:25:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000021', N'f1000000-0000-0000-0000-000000000005', N'd3000000-0000-0000-0000-000000000006', N'd2000000-0000-0000-0000-000000000031', CAST(9.50 AS Decimal(6, 2)), N'Excellent innovation.', CAST(N'2025-10-12T10:00:00.0000000' AS DateTime2), CAST(N'2025-10-12T10:00:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000022', N'f1000000-0000-0000-0000-000000000005', N'd3000000-0000-0000-0000-000000000006', N'd2000000-0000-0000-0000-000000000032', CAST(9.20 AS Decimal(6, 2)), N'Production-ready prototype.', CAST(N'2025-10-12T10:05:00.0000000' AS DateTime2), CAST(N'2025-10-12T10:05:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f2000000-0000-0000-0000-000000000023', N'f1000000-0000-0000-0000-000000000005', N'd3000000-0000-0000-0000-000000000006', N'd2000000-0000-0000-0000-000000000033', CAST(9.40 AS Decimal(6, 2)), N'Excellent final pitch.', CAST(N'2025-10-12T10:10:00.0000000' AS DateTime2), CAST(N'2025-10-12T10:10:00.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'905afe69-7840-439e-96fd-0d559e357001', N'732cb879-0c63-4a36-864e-96a833c243b8', N'4abf3236-5fb1-45bf-8534-b825b4e0f1b3', N'9c004b17-118f-45e9-8feb-d251091d193d', CAST(10.00 AS Decimal(6, 2)), N'perfect', CAST(N'2026-07-02T14:47:05.3995010' AS DateTime2), CAST(N'2026-07-02T14:47:05.3995010' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'3bd8ca00-75de-4c84-9490-1913d21f0e92', N'391cb64b-6ff7-4471-b76a-973ade51874f', N'681ead49-9f53-4716-8e2f-87da7d1ed0be', N'87b741bc-659a-4ed7-ab4e-9be7f2bbb645', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-09T16:35:42.2359844' AS DateTime2), CAST(N'2026-07-09T16:35:42.2359844' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f0b30df9-29c1-470d-9cb9-73c66e53a342', N'4fbbe238-1f5c-4579-8be8-cdc6ffe052e2', N'4abf3236-5fb1-45bf-8534-b825b4e0f1b3', N'9c004b17-118f-45e9-8feb-d251091d193d', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-02T15:16:20.5033007' AS DateTime2), CAST(N'2026-07-02T15:16:20.5033007' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'5f28a6cd-838c-4c16-861b-7bd00c194645', N'4fbbe238-1f5c-4579-8be8-cdc6ffe052e2', N'4abf3236-5fb1-45bf-8534-b825b4e0f1b3', N'ad1a5dd8-f592-4507-9b46-572e9571d986', CAST(9.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-02T15:16:20.4723573' AS DateTime2), CAST(N'2026-07-02T15:16:20.4723573' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'f0809dc2-0eff-47c2-9bb7-7f8dd386d02f', N'391cb64b-6ff7-4471-b76a-973ade51874f', N'681ead49-9f53-4716-8e2f-87da7d1ed0be', N'0285b0be-586b-4e40-824b-41c773e06c67', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-09T16:35:42.2734211' AS DateTime2), CAST(N'2026-07-09T16:35:42.2734211' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'66eb656e-ef5a-4157-918d-936c402a6692', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', N'e47e067c-3dbf-4405-9416-5c8e51e2e0d5', N'2b053b7e-cbc9-4c0d-9b7c-75f8161e81b3', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-02T15:09:59.1283813' AS DateTime2), CAST(N'2026-07-02T15:09:59.1283813' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'df0544f9-8b6e-4d95-b008-9b462795856a', N'732cb879-0c63-4a36-864e-96a833c243b8', N'4abf3236-5fb1-45bf-8534-b825b4e0f1b3', N'ad1a5dd8-f592-4507-9b46-572e9571d986', CAST(8.00 AS Decimal(6, 2)), N'good', CAST(N'2026-07-02T14:47:05.2188892' AS DateTime2), CAST(N'2026-07-02T14:47:05.2188892' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'3fd85fba-e200-4f88-81a9-9dc11875abbf', N'391cb64b-6ff7-4471-b76a-973ade51874f', N'681ead49-9f53-4716-8e2f-87da7d1ed0be', N'8664194f-021f-421f-834a-550f93ef5b20', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-09T16:35:42.1922024' AS DateTime2), CAST(N'2026-07-09T16:35:42.1922024' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'bf8363e0-fd5b-40a4-9e55-9f3b7abaf394', N'391cb64b-6ff7-4471-b76a-973ade51874f', N'681ead49-9f53-4716-8e2f-87da7d1ed0be', N'5de51f3b-76be-4fa3-8ebe-07ddceae8cfb', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-09T16:35:42.3150576' AS DateTime2), CAST(N'2026-07-09T16:35:42.3150576' AS DateTime2), 0, 1)
INSERT [dbo].[Judging] ([JudgingID], [SubmissionID], [RoundJudgeID], [RoundCriterionID], [ScoreValue], [Comment], [JudgedAt], [UpdatedAt], [IsCalibration], [IsActive]) VALUES (N'60433ed3-338b-4824-b18c-a121f527a75d', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', N'e47e067c-3dbf-4405-9416-5c8e51e2e0d5', N'964a0a74-9323-4209-8437-9125678304c1', CAST(10.00 AS Decimal(6, 2)), N'', CAST(N'2026-07-02T15:09:59.0383543' AS DateTime2), CAST(N'2026-07-02T15:09:59.0383543' AS DateTime2), 0, 1)
GO
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'f5000000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000010', N'Qualifier result available', N'API Alpha advanced to the final round.', CAST(N'2026-06-12T12:10:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'f5000000-0000-0000-0000-000000000002', N'b1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000012', N'Qualifier result available', N'API Beta placed second in the qualifier.', CAST(N'2026-06-12T12:11:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'f5000000-0000-0000-0000-000000000003', N'b1000000-0000-0000-0000-000000000001', NULL, N'Final round schedule', N'The AI final starts on June 13, 2026.', CAST(N'2026-06-12T12:12:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'61d48af1-5980-4a57-8fc3-03a017f0e0b0', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:17.3983530' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'5fe86792-778f-4c99-bdad-117c8ddb4367', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'Event Registration Approved', N'Your registration for AI Innovation Hackathon 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-04T04:25:44.0222780' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'b5bd883c-b7c5-4f0c-9550-11caef5a0c9a', N'b1000000-0000-0000-0000-000000000002', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'New Judge Assignment', N'You have been assigned as a Judge for Round: Showcase Final in Category: Open Innovation, Event: API Test Innovation Showcase 2025.
Event Date: 2025-10-10 to 2025-10-12
Event Link: http://localhost:5173/events/b1000000-0000-0000-0000-000000000002', CAST(N'2026-07-13T12:30:39.0308114' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'7121f628-428b-48ac-a179-28689a5d2bfe', N'b1000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000003', N'New Judge Assignment', N'You have been assigned as a Judge for Round: Round 1 in Category: Open Innovation, Event: API Test Innovation Showcase 2025.
Event Date: 2025-10-10 to 2025-10-12
Event Link: http://localhost:5173/events/b1000000-0000-0000-0000-000000000002', CAST(N'2026-07-13T12:41:38.2918359' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'1985821d-5806-4c0d-9da3-2e5d76424a1f', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'ea969288-a77c-4683-b661-b10955f1df18', N'Event Registration Rejected', N'Your registration for AI Innovation Hackathon 2026 has been rejected. deo cho', CAST(N'2026-07-14T09:39:37.7673084' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'b2bbe9a0-4251-4243-af22-321494c82c11', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'46f63efa-185a-4a0a-9e9f-934f01c6513c', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:17.7407578' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'552362cf-12b4-4823-ae73-35ec7f86353e', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'1cefeef1-28e1-4897-b4ff-b85942aaad9f', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:16.6142245' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'12f023e7-110b-48bb-9bc5-3850d55dec96', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'960a79c4-da41-478b-85bd-61f579e43f5b', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:18.1809488' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'6aeef832-ba81-4af2-ab8f-40c1740519b6', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'WARNING', N'Get back to work bruh', CAST(N'2026-07-03T18:46:59.1873695' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'64dcb915-2819-4d11-8513-5064db468cf1', N'c3a85586-6a74-4c1d-8688-34b6359ced09', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'New Judge Assignment', N'You have been assigned as a Judge for Round: Round 1 in Category: AI Healthcare, Event: Demo.
Event Date: 2026-06-29 to 2026-06-30
Event Link: http://localhost:5173/events/c3a85586-6a74-4c1d-8688-34b6359ced09', CAST(N'2026-07-13T21:45:52.6459917' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'4baffa50-ea59-4333-80e1-54bbd08c175c', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'Team Join Request Approved', N'Your request to join team UIT1 has been approved.', CAST(N'2026-07-03T19:38:30.1735128' AS DateTime2), N'5181ec59-afee-46ee-95d1-16cde3a8819c', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'6b8d2e4f-faa2-44c6-a062-57d8e7cf1fc1', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:17.0809925' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'1eda1188-ff01-4ae0-9898-5953762dc094', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'4d2f7a5b-a634-46a9-92ee-ac28fa245a1a', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:16.0968537' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'21d31078-d34a-4752-b0e8-6f4e2ae10b83', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'ea969288-a77c-4683-b661-b10955f1df18', N'Event Registration Approved', N'Your registration for DEMOEVENT has been approved. You can now participate and create or join a team.', CAST(N'2026-07-01T04:48:56.6452715' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'e5e1e64a-6e2f-42f9-aa5c-7e510133c7d5', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:14.7719785' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'1df3dc91-8098-4a31-8fa2-890092f3db53', N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038', N'5d565896-2307-4911-939d-94e11e4b1c44', N'Event Registration Approved', N'Your registration for SWP Hackathon 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-04T04:15:29.5883381' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'0645f860-27a2-41f9-a778-8a072568078f', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'8c5c3287-cd14-4186-9180-340d0387fc3a', N'New Judge Assignment', N'You have been assigned as a Judge for Round: Round 1 in Category: Machine Learning, Event: DEMOEVENT.
Event Date: 2026-07-02 to 2026-07-05
Event Link: http://localhost:5173/events/de33a8f5-07c1-448b-b131-dfdf7fab5ea8', CAST(N'2026-07-13T17:33:11.0441866' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'2b8f1757-c82b-42fa-8721-a6be79a8658c', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'Team Join Request Approved', N'Your request to join team TeamVN has been approved.', CAST(N'2026-07-04T04:35:34.6250115' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'43384e7c-e6cd-4e28-be5c-aac890890705', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'9a9ef0d0-2bb3-4ed2-8e61-5dd814f4b20d', N'New Judge Assignment', N'You have been assigned as a Judge for Round: Final Round in Category: Hạng Mục Test, Event: Scoring Test Event.
Event Date: 2026-07-10 to 2026-08-31
Event Link: http://localhost:5173/events/3fbbd982-a9f8-4dba-a577-1f77c368d213', CAST(N'2026-07-09T09:16:37.3038903' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'5e3eefe9-3b08-4db5-aa48-b84ecf2f436e', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'5d565896-2307-4911-939d-94e11e4b1c44', N'Event Registration Approved', N'Your registration for AI Innovation Hackathon 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-04T04:25:46.5846693' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'2d7e2857-e275-4555-b8a5-cb0ef1a69c94', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'e3f9b065-3380-47ff-9705-5d8106188e4e', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-03T19:36:18.5546581' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'f057f1fa-4abb-4bee-befc-ccb8660b700a', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'fa60133b-5874-4dbf-9299-a0069495b193', N'Event Registration Approved', N'Your registration for SealTest 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-02T07:42:38.9888159' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'3fe8b438-46f4-447d-8b32-e857733e82fc', N'de33a8f5-07c1-448b-b131-dfdf7fab5ea8', N'a1000000-0000-0000-0000-000000000003', N'New Judge Assignment', N'You have been assigned as a Judge for Round: Round 1 in Category: Machine Learning, Event: DEMOEVENT.
Event Date: 2026-07-02 to 2026-07-05
Event Link: http://localhost:5173/events/de33a8f5-07c1-448b-b131-dfdf7fab5ea8', CAST(N'2026-07-13T17:13:17.0835049' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'36c3af19-e137-4d0e-8cde-fe516db262b9', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', N'Team Join Request Approved', N'Your request to join team UIT1 has been approved.', CAST(N'2026-07-03T19:38:31.6184366' AS DateTime2), N'5181ec59-afee-46ee-95d1-16cde3a8819c', 0)
INSERT [dbo].[Notifications] ([NotificationID], [EventID], [RecipientUserID], [Title], [Body], [SentAt], [SentByUserID], [IsRead]) VALUES (N'e540f4bf-abce-4cf6-9fd6-ffea4b718bdf', N'a2ed1f00-b43f-4592-b5b5-247ab0bcd038', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'Event Registration Approved', N'Your registration for SWP Hackathon 2026 has been approved. You can now participate and create or join a team.', CAST(N'2026-07-04T04:15:58.2572257' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
GO
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000002', N'ACTIVE')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000001', N'PENDING')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000003', N'REJECTED')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000004', N'SUSPENDED')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000005', N'TEMPORARY')
INSERT [dbo].[ParticipantStatus] ([StatusID], [StatusName]) VALUES (N'80000000-0000-0000-0000-000000000006', N'UNVERIFIED')
GO
INSERT [dbo].[PasswordResetTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'2d1e94a0-7050-4557-ab87-0ab45159d060', N'ea969288-a77c-4683-b661-b10955f1df18', N'8e6d16a6c5428b323434be494a3d21530b4f4a8ef673c94f881e1ef5f0cee864', CAST(N'2026-07-03T23:05:04.4215294' AS DateTime2), CAST(N'2026-07-03T23:35:04.4215294' AS DateTime2), CAST(N'2026-07-03T23:05:39.3025647' AS DateTime2))
INSERT [dbo].[PasswordResetTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'5c372ca5-763f-4c58-a33a-542646c72c0b', N'ea969288-a77c-4683-b661-b10955f1df18', N'7c924a690cac555dfe69f680d5eaf7cea0cc27ce2e2e438cbdbb1bfaac7e8691', CAST(N'2026-07-03T21:46:45.8955511' AS DateTime2), CAST(N'2026-07-03T22:16:45.8955511' AS DateTime2), CAST(N'2026-07-04T16:54:55.7960462' AS DateTime2))
INSERT [dbo].[PasswordResetTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'3876eee1-815c-45b7-8232-c634083eb124', N'ea969288-a77c-4683-b661-b10955f1df18', N'404f0088d79059f59c77d09985bc1d58a8a8b561b1af03e61da93f480eb2541d', CAST(N'2026-07-04T16:54:55.7960462' AS DateTime2), CAST(N'2026-07-04T17:24:55.7960462' AS DateTime2), CAST(N'2026-07-04T16:58:02.5096669' AS DateTime2))
GO
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'efda5028-02a6-4f9a-b358-0243560f7caa', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzMDUwOSwiZXhwIjoxNzg0MTE2OTA5fQ.QD_YSF58vVglcPwCBeAUrRBLdn8uVmseIWYNRWkunsM', CAST(N'2026-07-14T19:01:49.5215404' AS DateTime2), CAST(N'2026-07-21T19:01:49.5215404' AS DateTime2), CAST(N'2026-07-14T19:05:57.0034168' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'eddec9d3-3a6c-4405-a51e-057fccd9a3b5', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDk2ODAxLCJleHAiOjE3ODM1ODMyMDF9.RHKLwe0N_Fz-n4CSi2CZZkLRb4njNIjYSuFZsaADjK8', CAST(N'2026-07-08T14:46:41.1920889' AS DateTime2), CAST(N'2026-07-15T14:46:41.1920889' AS DateTime2), CAST(N'2026-07-08T14:47:43.7058552' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b5663e00-1546-4987-b28d-063d2ce0b841', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzY4MzMzNiwiZXhwIjoxNzgzNzY5NzM2fQ.e1A-OvuIsS-IkGYvMIph1SvO8XTIiNU3z4cah3WKRcA', CAST(N'2026-07-10T11:35:36.1517179' AS DateTime2), CAST(N'2026-07-17T11:35:36.1517306' AS DateTime2), CAST(N'2026-07-10T11:48:39.5088716' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'89926515-ac53-4379-a5c4-0c14194be6e5', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg5MTA1LCJleHAiOjE3ODM2NzU1MDV9.OX-Vh55L4ydMWDGEfyxQhfupmIA3HE47siymXFpnPwI', CAST(N'2026-07-09T16:25:05.5786553' AS DateTime2), CAST(N'2026-07-16T16:25:05.5786553' AS DateTime2), CAST(N'2026-07-09T16:25:31.8225204' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'54cbe2ff-cfde-428f-a749-0c3508b7e079', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjg0MTUzLCJleHAiOjE3ODM3NzA1NTN9.fk8SzOeMwtj0MMgh2H7WWPt99AORyWgGwPTztySQNmU', CAST(N'2026-07-10T11:49:13.4343027' AS DateTime2), CAST(N'2026-07-17T11:49:13.4343122' AS DateTime2), CAST(N'2026-07-10T12:23:09.0167372' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8de5e9a1-cf4d-4769-a344-0d290eb120c7', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5NTAwNCwiZXhwIjoxNzgzNTgxNDA0fQ.4DbOT6ifbGy2DZHy_6vBdPNyGT82nV4OKXbEe4wozVg', CAST(N'2026-07-08T14:16:44.9430877' AS DateTime2), CAST(N'2026-07-15T14:16:44.9430877' AS DateTime2), CAST(N'2026-07-08T14:17:09.6561092' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bee61735-e4e0-4e10-b1be-0d56c2a95ff6', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzUyMDQ4MywiZXhwIjoxNzgzNjA2ODgzfQ.Tt4JblV4rj5kpJis8TgAT1TPse8BmXSKsNPlg6SO28E', CAST(N'2026-07-08T21:21:23.4620552' AS DateTime2), CAST(N'2026-07-15T21:21:23.4620552' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f08a2b83-4ab1-40c3-81d0-0f18c6b15e91', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4MzAxMCwiZXhwIjoxNzgzNjY5NDEwfQ.TZfOXRwvBVernBbWav0M1ir8nEfUa6W3reMTH75Aybg', CAST(N'2026-07-09T14:43:30.8990401' AS DateTime2), CAST(N'2026-07-16T14:43:30.8990401' AS DateTime2), CAST(N'2026-07-09T14:43:46.9999027' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ed1babcd-7a38-473d-9405-0fff64fe7e8a', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM0OTE4NzAsImV4cCI6MTc4MzU3ODI3MH0.WX0yZllQvyDCrL0LwrmTeUk5EPrnIQTOmW41SNUiZ1Y', CAST(N'2026-07-08T13:24:30.9636050' AS DateTime2), CAST(N'2026-07-15T13:24:30.9636050' AS DateTime2), CAST(N'2026-07-08T13:29:10.0455919' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b42e1a4b-d6f4-41aa-afdf-13a8c0c68163', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDk1NTk4LCJleHAiOjE3ODM1ODE5OTh9.W-IGbP5vH0o8XqBCrITebAqILPKuD1lwH0F3DvGpOE4', CAST(N'2026-07-08T14:26:38.9358842' AS DateTime2), CAST(N'2026-07-15T14:26:38.9358842' AS DateTime2), CAST(N'2026-07-08T14:28:31.2676069' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'5f5e2129-28db-4f57-bf3a-14295bcd8fd4', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1ODczMDMsImV4cCI6MTc4MzY3MzcwM30.aN1_3KVKFXqlqmAXkq52H6wmKhXtYphKbkCBf96j3oY', CAST(N'2026-07-09T15:55:03.6638505' AS DateTime2), CAST(N'2026-07-16T15:55:03.6638505' AS DateTime2), CAST(N'2026-07-09T16:17:04.3889331' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'28a5d7c0-16c9-4614-87fe-152fb40f50ed', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjkyNDUyLCJleHAiOjE3ODM3Nzg4NTJ9.e6vzG1MqvCgIDO8SjojecRQ2avkzbgE9TxBvowyCLkw', CAST(N'2026-07-10T14:07:32.1409479' AS DateTime2), CAST(N'2026-07-17T14:07:32.1409586' AS DateTime2), CAST(N'2026-07-10T14:07:34.4126040' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'2906488e-94a7-41b8-83cd-15e815f1783e', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjU1Mzc3LCJleHAiOjE3ODM3NDE3Nzd9.khhCZK5C7_ACBb1Wv_ErO9AfaRIQkqnvvuqq9DhobOU', CAST(N'2026-07-10T10:49:37.3276408' AS DateTime2), CAST(N'2026-07-17T10:49:37.3276408' AS DateTime2), CAST(N'2026-07-10T10:57:43.2821134' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'55420db8-0e70-4b50-87ef-174cb586f892', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4MzgzNDg0MywiZXhwIjoxNzgzOTIxMjQzfQ.QvFy3S9w-LE17gGXcbFnJKqk7yBCeVDiK2r5uuabsYA', CAST(N'2026-07-12T12:40:43.3620640' AS DateTime2), CAST(N'2026-07-19T12:40:43.3620640' AS DateTime2), CAST(N'2026-07-12T12:42:30.5922069' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'05e556f9-5568-4dc2-9ff8-1831a4b32ab7', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg0OTUyLCJleHAiOjE3ODM2NzEzNTJ9.uSQ8HVXWUiqki6xRaF0TvTEm11jGHDTmbU-FukEhNHo', CAST(N'2026-07-09T15:15:52.3664311' AS DateTime2), CAST(N'2026-07-16T15:15:52.3664311' AS DateTime2), CAST(N'2026-07-09T15:15:59.2280461' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b829fde0-7264-4271-a5fc-194c5ade8889', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NDY0NCwiZXhwIjoxNzgzNjcxMDQ0fQ.BeqeTdv8gC0w4va-lVZiXQ8JpHKVNHgtyX7s2cfGeVg', CAST(N'2026-07-09T15:10:44.8519000' AS DateTime2), CAST(N'2026-07-16T15:10:44.8519000' AS DateTime2), CAST(N'2026-07-09T15:12:34.2459940' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd214a2db-f668-4e7f-a93e-1aaee9f102a2', N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWhhY2hlbmFsQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAxMDk1MiwiZXhwIjoxNzg0MDk3MzUyfQ.NqmWUhvDEfWNRrgezK1zYNKue4a9xiFI0KFNWhWwLRw', CAST(N'2026-07-14T06:35:52.2059810' AS DateTime2), CAST(N'2026-07-21T06:35:52.2059847' AS DateTime2), CAST(N'2026-07-14T06:36:08.7683329' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ef579241-149d-4971-8df0-1bfc85bb7b03', N'86eb15fb-4af1-4bc1-9bfe-15faa6a6c490', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWhhY2hlbmFsQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAxMTA4OSwiZXhwIjoxNzg0MDk3NDg5fQ.pjDtLZlJIBqT-wN82JJdSBNX0HGFq3MWY7PlVQboeM4', CAST(N'2026-07-14T06:38:09.8002285' AS DateTime2), CAST(N'2026-07-21T06:38:09.8002313' AS DateTime2), CAST(N'2026-07-14T06:39:31.8212579' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'03c42f44-4d0c-4cc0-9016-1cd55dba8524', N'd359de35-5ccb-4e8c-9476-732b6046914f', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4dWFudGFuMDcxMTA2QGdtYWlsLmNvbSIsImlhdCI6MTc4MzQ5NTcyNSwiZXhwIjoxNzgzNTgyMTI1fQ.CSic2NLDJy6o8H9KBYKnK1MzDEBglqNyGZcKhBKIVOc', CAST(N'2026-07-08T14:28:45.7228695' AS DateTime2), CAST(N'2026-07-15T14:28:45.7228695' AS DateTime2), CAST(N'2026-07-08T14:29:31.4603956' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'721ced22-cd23-4fc4-a68b-1d0229e0c811', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjY2NDA1LCJleHAiOjE3ODM3NTI4MDV9.Gl9C3k-gm_BfGNbZdlfyMMVhdfekKt8TccZoaDXqfr8', CAST(N'2026-07-10T13:53:25.3004718' AS DateTime2), CAST(N'2026-07-17T13:53:25.3004718' AS DateTime2), CAST(N'2026-07-10T18:37:07.0514957' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'71b8782d-aadb-44c2-9fb9-1ed870e90dc4', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNjAzNywiZXhwIjoxNzg0MTIyNDM3fQ.73xDiq0AkJE2vHhgo7EVukc1o2O3H5n3xQHNHwNcXs8', CAST(N'2026-07-14T20:33:57.9378953' AS DateTime2), CAST(N'2026-07-21T20:33:57.9378953' AS DateTime2), CAST(N'2026-07-14T20:34:16.9099633' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'109b1411-331c-4e4f-9724-21e462e2f4d4', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg2MjY1LCJleHAiOjE3ODM2NzI2NjV9.ZTxHzsIfuEzw4rIpOsOzlfjuIbkq_tJPIN_IQ0nVn7c', CAST(N'2026-07-09T15:37:45.0619379' AS DateTime2), CAST(N'2026-07-16T15:37:45.0619379' AS DateTime2), CAST(N'2026-07-09T15:37:55.5939337' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd823aa75-ee91-4c49-a530-225d47a7893a', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNDc0NCwiZXhwIjoxNzg0MTIxMTQ0fQ.CKsn-AdLjGnLvUJKPhDiwyXycskb_9QTc5JM-N5Hs7E', CAST(N'2026-07-14T20:12:24.3044076' AS DateTime2), CAST(N'2026-07-21T20:12:24.3044076' AS DateTime2), CAST(N'2026-07-14T20:12:48.9909557' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'559ba152-5993-4069-926d-25111608f0cb', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTgzMzg3LCJleHAiOjE3ODM2Njk3ODd9.46ZAnGUghfLPdclInZQYF26Nbg00z8QnO0igRCrzM9E', CAST(N'2026-07-09T14:49:47.0315921' AS DateTime2), CAST(N'2026-07-16T14:49:47.0315921' AS DateTime2), CAST(N'2026-07-09T15:06:09.4331223' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'a81b12cb-e582-4bfb-b46e-256974e1532b', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMzIwNCwiZXhwIjoxNzg0MTA5NjA0fQ.N6FLSkqeVDyAUMuKHgLQhUjoWy_XzwPGXSjb3AqMj08', CAST(N'2026-07-14T17:00:04.8400511' AS DateTime2), CAST(N'2026-07-21T17:00:04.8400511' AS DateTime2), CAST(N'2026-07-14T17:00:15.5907199' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'771a5079-8f10-49c6-9671-2869e1826f25', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDk0OTc0LCJleHAiOjE3ODM1ODEzNzR9.n-Xzp4R1HuqYg_JpfjOjYTb60aXg5lYh2vYdcKVLfJ4', CAST(N'2026-07-08T14:16:14.5686281' AS DateTime2), CAST(N'2026-07-15T14:16:14.5686281' AS DateTime2), CAST(N'2026-07-08T14:16:40.6044222' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'76727ec4-392d-4c92-8a81-286e004cfccd', N'ea969288-a77c-4683-b661-b10955f1df18', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cm9uZ2hpZXUxNzEwMjZAZ21haWwuY29tIiwiaWF0IjoxNzgzNjY3NTYwLCJleHAiOjE3ODM3NTM5NjB9.9M8528hLs3hx1qtUoXIrd9QXPlXk0Q__F3L2b_-EjPs', CAST(N'2026-07-10T14:12:40.8862221' AS DateTime2), CAST(N'2026-07-17T14:12:40.8862221' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'9debd087-0bf7-4a5d-9ce5-2943125a0d28', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ3OTUwOCwiZXhwIjoxNzgzNTY1OTA4fQ.YgTDR57SUFm7VZQFgL6PGtgVvHA6VSzNe17SdSXRyjE', CAST(N'2026-07-08T09:58:28.6131623' AS DateTime2), CAST(N'2026-07-15T09:58:28.6131623' AS DateTime2), CAST(N'2026-07-08T09:58:30.9260268' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'12be95bc-78ec-49ff-9351-2a37e9e17347', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyMjQwMCwiZXhwIjoxNzg0MTA4ODAwfQ.gjMN3LSaEwIFJ_cT-eWzMoVxMKob9UHPV-ulkHKshcE', CAST(N'2026-07-14T16:46:40.5579515' AS DateTime2), CAST(N'2026-07-21T16:46:40.5579515' AS DateTime2), CAST(N'2026-07-14T16:52:51.3221255' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f00b2584-cce2-49a0-b370-2a51e70b2e74', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNjY1NTc2LCJleHAiOjE3ODM3NTE5NzZ9.LUcKd4NlyS-fTSJfu94C0rgq0xvBSQPBTVBzmqm8wc0', CAST(N'2026-07-10T13:39:36.8127866' AS DateTime2), CAST(N'2026-07-17T13:39:36.8127866' AS DateTime2), CAST(N'2026-07-10T13:45:47.7411459' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0a4a8e55-14cb-4e9e-aba6-2d93e841c5d0', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMTgwMywiZXhwIjoxNzg0MTA4MjAzfQ.d7ILgEq4hAASJC_OVBCmUqMunDKsm3Dpc_7_sRuBngo', CAST(N'2026-07-14T09:36:43.4955641' AS DateTime2), CAST(N'2026-07-21T09:36:43.4955682' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'440c8885-f69d-4450-b525-2ea81e0c1c57', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg3MzIwLCJleHAiOjE3ODM2NzM3MjB9.0yUT9fENGbcPIsBdsx93FuQaTra3-ZQ0rc86_ZLEUZ0', CAST(N'2026-07-09T15:55:20.7458849' AS DateTime2), CAST(N'2026-07-16T15:55:20.7458849' AS DateTime2), CAST(N'2026-07-09T15:59:48.6099553' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'33961d2e-0909-48a4-91f5-307b891107ae', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxMjc4NywiZXhwIjoxNzg0MDk5MTg3fQ.XHDm1W2N4dPKhBvL80nTiilDizbsTfhb2ESUgmHXRY4', CAST(N'2026-07-14T07:06:27.7684056' AS DateTime2), CAST(N'2026-07-21T07:06:27.7684087' AS DateTime2), CAST(N'2026-07-14T07:06:50.5755583' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'eb4b4fd9-4261-4110-8008-30c4fbba4d78', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1NzU1MzQsImV4cCI6MTc4MzY2MTkzNH0.tciqd3xZmHPtNNajr7tkOlorVtK7PuNjl0aXmaRxacI', CAST(N'2026-07-09T12:38:54.9515909' AS DateTime2), CAST(N'2026-07-16T12:38:54.9515909' AS DateTime2), CAST(N'2026-07-09T13:25:43.9412364' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'78bd2219-925b-4320-bbcb-31cb6ab862e4', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyODYzOSwiZXhwIjoxNzg0MTE1MDM5fQ.NBpUMLC2kS_m5cwI77meBB0DYGe7-Vvd2gf1fOOj6jU', CAST(N'2026-07-14T18:30:39.0742189' AS DateTime2), CAST(N'2026-07-21T18:30:39.0742189' AS DateTime2), CAST(N'2026-07-14T18:30:43.0525652' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0650d887-af0f-4ce3-91ae-31e6d39870eb', N'9a9ef0d0-2bb3-4ed2-8e61-5dd814f4b20d', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZWxsY29tYXRraGF1MDg4ODg4QGdtYWlsLmNvbSIsImlhdCI6MTc4MzU4ODY1NCwiZXhwIjoxNzgzNjc1MDU0fQ.0vvEOKfXlLFcPgyR53OAZk3KZvxQvygH610m2Yazqz4', CAST(N'2026-07-09T16:17:34.8510271' AS DateTime2), CAST(N'2026-07-16T16:17:34.8510271' AS DateTime2), CAST(N'2026-07-09T16:33:55.8616445' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'579edd0b-35fa-42b2-bf55-32227277f133', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODQwMjI4NjcsImV4cCI6MTc4NDEwOTI2N30.n3tUeWtznb73mELVmaqgJnyndPWO13WpKRHZLd9YjCM', CAST(N'2026-07-14T16:54:27.1549187' AS DateTime2), CAST(N'2026-07-21T16:54:27.1549187' AS DateTime2), CAST(N'2026-07-14T17:00:30.9542162' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f1d225c9-0877-4c10-804a-38b348ee089c', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU2NjAyNiwiZXhwIjoxNzgzNjUyNDI2fQ.U4rCS9L7_kWPOMy3Ruf8jb8sbYDJD_uzJyZh5NsDoUw', CAST(N'2026-07-09T10:00:26.4856352' AS DateTime2), CAST(N'2026-07-16T10:00:26.4856352' AS DateTime2), CAST(N'2026-07-09T10:00:39.5533088' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'58a61197-b9a6-44ea-92a4-390c7cf58d3d', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTY1OTcyLCJleHAiOjE3ODM2NTIzNzJ9.fH6M3pTXjRPnSQkjTocDTn3u7ds7d08Xy0krUDL3RSk', CAST(N'2026-07-09T09:59:32.2381523' AS DateTime2), CAST(N'2026-07-16T09:59:32.2381523' AS DateTime2), CAST(N'2026-07-09T10:00:22.2503832' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'47766a5b-364e-42a8-8248-397f909c8205', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAzNDg5MSwiZXhwIjoxNzg0MTIxMjkxfQ.3M-PBr4STClqEJrVEpd0JB15F1s3Roe9sAJiVPjXCHM', CAST(N'2026-07-14T20:14:51.6204123' AS DateTime2), CAST(N'2026-07-21T20:14:51.6204123' AS DateTime2), CAST(N'2026-07-14T20:33:54.5879490' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd26fed84-6b57-4db1-ba7f-3ba97158b168', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxMTEzOSwiZXhwIjoxNzg0MDk3NTM5fQ.WkjozHv2TyK2VfPSnGZ_iN8NICN2_Ts3eJDqMWBwRk0', CAST(N'2026-07-14T13:38:59.9064511' AS DateTime2), CAST(N'2026-07-21T13:38:59.9064511' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0645359e-9950-413e-b542-3d1479172485', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NjExNiwiZXhwIjoxNzgzNjcyNTE2fQ.yxscdIxDo5CXfHR89ITyKZiB5KsSr3d63uo37PDiESw', CAST(N'2026-07-09T15:35:16.3073490' AS DateTime2), CAST(N'2026-07-16T15:35:16.3073490' AS DateTime2), CAST(N'2026-07-09T15:35:52.3510476' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'50bdbb0e-feef-4cd6-bba6-3d2b2cece7b3', N'8c5c3287-cd14-4186-9180-340d0387fc3a', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWRlZm9ydGVzdGluZzEyM0BnbWFpbC5jb20iLCJpYXQiOjE3ODM1MDA4MDUsImV4cCI6MTc4MzU4NzIwNX0.w3hujrvFPp7VB1T9dPamTr1UGRZ1T-RjOP23Ixy64AM', CAST(N'2026-07-08T15:53:25.5175056' AS DateTime2), CAST(N'2026-07-15T15:53:25.5175056' AS DateTime2), CAST(N'2026-07-08T16:02:32.9025765' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'6e83455e-ac42-4169-a48b-3e1084a02383', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzg0MDE5OTY3LCJleHAiOjE3ODQxMDYzNjd9.hRgu9MDkMpgRZ-0vTOAzwctZFvGpwhO02lIHcY_MQJ0', CAST(N'2026-07-14T16:06:07.3428423' AS DateTime2), CAST(N'2026-07-21T16:06:07.3428423' AS DateTime2), CAST(N'2026-07-14T16:41:00.2279790' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd5ad6e52-ac78-45bd-80b8-455cdc195551', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg2MTU1LCJleHAiOjE3ODM2NzI1NTV9.z5DkMlyY9xPbvVijBELEhH0R2zBluunPJOaO-htVReY', CAST(N'2026-07-09T15:35:55.4618682' AS DateTime2), CAST(N'2026-07-16T15:35:55.4618682' AS DateTime2), CAST(N'2026-07-09T15:37:36.4654630' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8b756406-02aa-4898-9867-47fa6ef878e3', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5NTgwOSwiZXhwIjoxNzgzNTgyMjA5fQ.NQrXrPZBNYDzGz49u3Lta4H6hC4DXHzQKkswKa_3hcU', CAST(N'2026-07-08T14:30:09.5740794' AS DateTime2), CAST(N'2026-07-15T14:30:09.5740794' AS DateTime2), CAST(N'2026-07-08T14:46:15.2563742' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'e72464cc-8b7e-4cc4-9178-4a0da9a3bfbe', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDc5MjAzLCJleHAiOjE3ODM1NjU2MDN9.Jzw4Rt4HpxHbE6XcOnjqk8ZOAUkvdfn2BSMVu437oLg', CAST(N'2026-07-08T09:53:23.9407799' AS DateTime2), CAST(N'2026-07-15T09:53:23.9407799' AS DateTime2), CAST(N'2026-07-08T09:58:18.1903756' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd4fd6f12-03c4-42ea-b5a7-4b2f2ea119cf', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1NzUwMDEsImV4cCI6MTc4MzY2MTQwMX0.5IPPIPtkKdVbWWoAlfu3_EMxi-D4dbwclAmc6nCnm6A', CAST(N'2026-07-09T12:30:01.0456115' AS DateTime2), CAST(N'2026-07-16T12:30:01.0456115' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd140e8c5-59a2-42a4-a493-4b2fb2720cc2', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzOTM4Nzc0LCJleHAiOjE3ODQwMjUxNzR9.dGNU3C6cKAhSdzyjHI9TPrv5kJ8afDpBoKnYakm1ypU', CAST(N'2026-07-13T10:32:54.7325266' AS DateTime2), CAST(N'2026-07-20T10:32:54.7325310' AS DateTime2), CAST(N'2026-07-14T06:35:42.5226910' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'957f6f77-c773-431d-90ef-4ce0eb091d82', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxMDc1OSwiZXhwIjoxNzg0MDk3MTU5fQ.8NUlPxV5sg12vligyZUZ9MpmSSDARiMfGavoPOPyhuk', CAST(N'2026-07-14T06:32:39.7353246' AS DateTime2), CAST(N'2026-07-21T06:32:39.7353282' AS DateTime2), CAST(N'2026-07-14T06:32:56.0565978' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'768ef983-2d12-4370-a55f-4e7968f5e698', N'fa60133b-5874-4dbf-9299-a0069495b193', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4dWFudGFuMDcxMTA2QGdtYWlsLmNvbSIsImlhdCI6MTc4MzQ5NTc3MSwiZXhwIjoxNzgzNTgyMTcxfQ.9Ohk88Z8fuyhCPZwHYEpJv3hXYHT2YXcdL32PFqM-lQ', CAST(N'2026-07-08T14:29:31.5945834' AS DateTime2), CAST(N'2026-07-15T14:29:31.5945834' AS DateTime2), CAST(N'2026-07-08T14:30:06.0814904' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ef101d5c-220b-4c68-9255-4efe3606637d', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NDk2MywiZXhwIjoxNzgzNjcxMzYzfQ.kdZ3rIGmX-oOIyAIxwHfRVliCgw11Ell7vQoqrTpJCI', CAST(N'2026-07-09T15:16:03.5764070' AS DateTime2), CAST(N'2026-07-16T15:16:03.5764070' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'546c8904-c07b-47cb-b47b-4f82c0800169', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg4NTg2LCJleHAiOjE3ODM2NzQ5ODZ9.GVSht43qx-MseVvse5SpnR_f6gQmgCi6qHy65HN-SZY', CAST(N'2026-07-09T16:16:26.2462760' AS DateTime2), CAST(N'2026-07-16T16:16:26.2462760' AS DateTime2), CAST(N'2026-07-09T16:23:39.1552976' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bdce8ef9-be81-4af4-968c-4fe4904b4227', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5NDE5MSwiZXhwIjoxNzgzNTgwNTkxfQ.NpTB6oSSLwtDuG7BtVgq40Cs3smsTq9a5jZsFSt5MUY', CAST(N'2026-07-08T14:03:11.5136935' AS DateTime2), CAST(N'2026-07-15T14:03:11.5136935' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'7b7e8620-1a81-4222-9089-50efcadefa49', N'a0000000-0000-0000-0000-111111111111', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW50b3IxQHNlYWwuY29tIiwiaWF0IjoxNzgzNDkxNzkzLCJleHAiOjE3ODM1NzgxOTN9.SasA5TuS8ucGYcrDn9pssn6Z1xTUjVinJdX_znnP0H0', CAST(N'2026-07-08T13:23:13.0148214' AS DateTime2), CAST(N'2026-07-15T13:23:13.0148214' AS DateTime2), CAST(N'2026-07-08T13:24:21.5799110' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'46f1c850-de07-4bb6-b1d2-52702dd31fee', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5OTE0MSwiZXhwIjoxNzgzNTg1NTQxfQ.wyhlH6CkeoOaQO_h-aHcZY1cjQRi9yyPmzsBahuOXZc', CAST(N'2026-07-08T15:25:41.7972158' AS DateTime2), CAST(N'2026-07-15T15:25:41.7972158' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'6f173411-24c8-4c46-9154-549fb3cd5a5f', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4Mzk3MTg4NCwiZXhwIjoxNzg0MDU4Mjg0fQ.tJtorkoHfNQ6JikXLVYnm2BwmVkYDcnbOFYf5ZS53cw', CAST(N'2026-07-14T02:44:44.1873073' AS DateTime2), CAST(N'2026-07-21T02:44:44.1873073' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'7c193311-074c-4900-a6f4-54d9bba58989', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTgzMDMxLCJleHAiOjE3ODM2Njk0MzF9.mCu6UStaTiUAz7mSAdo64Lv2DT37VhUSMpl9Ehy5au4', CAST(N'2026-07-09T14:43:51.4994914' AS DateTime2), CAST(N'2026-07-16T14:43:51.4994914' AS DateTime2), CAST(N'2026-07-09T14:47:57.5578032' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0a34fc56-2f84-4abc-98e5-59092333e319', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNjA4MiwiZXhwIjoxNzg0MTIyNDgyfQ.XryVSjEf7R5zthmbZoPBggBRt7Jozlam8Jd71CxOCKk', CAST(N'2026-07-14T20:34:42.8288298' AS DateTime2), CAST(N'2026-07-21T20:34:42.8300179' AS DateTime2), CAST(N'2026-07-14T20:35:03.0966829' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b324499a-9e07-4114-83bb-5a7d0364ab9a', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzYwMTYwMCwiZXhwIjoxNzgzNjg4MDAwfQ.PJfqOQ8gQLGxlcrnXT8UBR2uB9KILbfsBAbvdDlPBk4', CAST(N'2026-07-09T12:53:20.7944884' AS DateTime2), CAST(N'2026-07-16T12:53:20.7944926' AS DateTime2), CAST(N'2026-07-09T12:53:31.5398131' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0887d9f5-a5cb-4479-b9b6-5c6e88e4041d', N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWhhY2hlbmFsQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAxMTE3MSwiZXhwIjoxNzg0MDk3NTcxfQ.ohBHPFJuo-qsj7CkaC0SYVxhCJcDOxC5Oy_b2sAEZQQ', CAST(N'2026-07-14T06:39:31.9844232' AS DateTime2), CAST(N'2026-07-21T06:39:31.9844270' AS DateTime2), CAST(N'2026-07-14T06:40:01.2469276' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bc073854-93de-4d7f-afcf-5d3ada58fd2a', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTgyODE2LCJleHAiOjE3ODM2NjkyMTZ9.kGu3ocHhHrF31PMBzvtkUotSaoExZynGOgtSmWc0f-4', CAST(N'2026-07-09T14:40:16.7493666' AS DateTime2), CAST(N'2026-07-16T14:40:16.7493666' AS DateTime2), CAST(N'2026-07-09T14:40:51.6342523' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'22248146-e0c2-4454-8623-5e252cdfaa09', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU3NDQ4NiwiZXhwIjoxNzgzNjYwODg2fQ.mrd7URp9Ztql3wXz0P9D6quiRvEMDrgu074yC0QbFfE', CAST(N'2026-07-09T12:21:26.5913323' AS DateTime2), CAST(N'2026-07-16T12:21:26.5913323' AS DateTime2), CAST(N'2026-07-09T13:09:55.1459357' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'331d7725-cf5a-4fa4-a8b3-5e3aa39647ce', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxMTM2OCwiZXhwIjoxNzg0MDk3NzY4fQ.5y4CqtCUrSQvj5xSn0B0B5GCFSbL_qPgqhaFWnIyr-w', CAST(N'2026-07-14T06:42:48.0133999' AS DateTime2), CAST(N'2026-07-21T06:42:48.0134042' AS DateTime2), CAST(N'2026-07-14T06:43:24.1975103' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'808774f7-1b0a-418f-9849-5ed9539baf53', N'ea969288-a77c-4683-b661-b10955f1df18', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cm9uZ2hpZXUxNzEwMjZAZ21haWwuY29tIiwiaWF0IjoxNzgzNjgzNTk3LCJleHAiOjE3ODM3Njk5OTd9.N65PfgKEaNI4SOtFD55SLEoKPprIFjSEFGYGr6YbbuQ', CAST(N'2026-07-10T11:39:57.1332801' AS DateTime2), CAST(N'2026-07-17T11:39:57.1332904' AS DateTime2), CAST(N'2026-07-10T11:40:10.9515444' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'cdff1847-50b0-4a69-b8de-6032af980e8c', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjg0MzEzLCJleHAiOjE3ODM3NzA3MTN9.n-fJ1J5VJtkcYrq0kcHqNsLarImrV08Z04sN8A8pZOA', CAST(N'2026-07-10T18:51:53.5700589' AS DateTime2), CAST(N'2026-07-17T18:51:53.5700589' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'c17d5069-e924-4518-a6a6-617431a5b987', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1ODMxNDMsImV4cCI6MTc4MzY2OTU0M30.7GA8sDdRMCH80Wzaq0ArM_bVStI4lFfvy1d2F9HAhzo', CAST(N'2026-07-09T14:45:43.5360000' AS DateTime2), CAST(N'2026-07-16T14:45:43.5360000' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8b10859c-8063-4f73-a009-62710b406878', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODQwMjE2NjEsImV4cCI6MTc4NDEwODA2MX0.bf8k9dJdXGDX77OGcL2vzO6zminxKQB1i2mAM_RKJhw', CAST(N'2026-07-14T16:34:21.0807952' AS DateTime2), CAST(N'2026-07-21T16:34:21.0807952' AS DateTime2), CAST(N'2026-07-14T16:43:08.2482940' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'1c85b3c5-64a5-4630-af8d-6314365129c6', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg4Mzk2LCJleHAiOjE3ODM2NzQ3OTZ9.eW3UNYUb6yXHVjLuZ5pu6GNGwVnluwprZnw7TKIWvSw', CAST(N'2026-07-09T16:13:16.1638030' AS DateTime2), CAST(N'2026-07-16T16:13:16.1638030' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f19a7172-866c-4ba8-8893-6577c89ba827', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzUzMDk1OSwiZXhwIjoxNzgzNjE3MzU5fQ.q-bUwBqQIQo0c6qFCk-PbVH--pWCDekDtFTfkuASsQ4', CAST(N'2026-07-09T00:15:59.6712368' AS DateTime2), CAST(N'2026-07-16T00:15:59.6712368' AS DateTime2), CAST(N'2026-07-09T01:18:12.5556603' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'92100ca0-cc6c-4372-971d-6627d8d5180c', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg1MjYwLCJleHAiOjE3ODM2NzE2NjB9.C0cJvq8qetCw_-kZh7GlLJNd5dGcb4i5oJhG3wa96yc', CAST(N'2026-07-09T15:21:00.1739188' AS DateTime2), CAST(N'2026-07-16T15:21:00.1739188' AS DateTime2), CAST(N'2026-07-09T15:23:00.7040157' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'de997603-5def-4f5a-bd11-67f0d7c18e5b', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg5MTc2LCJleHAiOjE3ODM2NzU1NzZ9.pUKuOZS1pZjp_8ryjkggXSmXBejLOyyocDTo4QNuT7E', CAST(N'2026-07-09T16:26:16.3745437' AS DateTime2), CAST(N'2026-07-16T16:26:16.3745437' AS DateTime2), CAST(N'2026-07-09T16:26:18.3100091' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'25bf4692-ae1f-4502-89b0-6a49f4acd8b1', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTc4MzI2LCJleHAiOjE3ODM2NjQ3MjZ9.xHzMiZ9Ip_B7eu-2XGzGolWiwPPNQbv7FD3fUfcC5jE', CAST(N'2026-07-09T13:25:26.3950588' AS DateTime2), CAST(N'2026-07-16T13:25:26.3950588' AS DateTime2), CAST(N'2026-07-09T13:25:29.2584952' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'eef334fc-edf3-4740-b2b2-6b17c41c4a4d', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyMzM4NiwiZXhwIjoxNzg0MTA5Nzg2fQ._62rRKcGxexHsyHPa_807fRwX9xAB3NHoTY61ZTVa34', CAST(N'2026-07-14T17:03:06.6742128' AS DateTime2), CAST(N'2026-07-21T17:03:06.6742128' AS DateTime2), CAST(N'2026-07-14T18:30:32.0041188' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'46a079a0-9649-4ab7-b3ce-6bd77ebe5e30', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1MDMxNjUsImV4cCI6MTc4MzU4OTU2NX0.X2fSdRNiKUvHRg5MYyukX5XK7B4huORvqPYZ1GdBDsw', CAST(N'2026-07-08T16:32:45.1769518' AS DateTime2), CAST(N'2026-07-15T16:32:45.1769518' AS DateTime2), CAST(N'2026-07-09T12:29:47.1466231' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'5531ded5-4d55-4362-b03d-6e45e75939a8', N'a1000000-0000-0000-0000-000000000015', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuZ3JlZW4ubWVtYmVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjcyMCwiZXhwIjoxNzg0MTA5MTIwfQ.Bd4XQCcHBUZCmfjO1A2l9L7GRaQ_QWaffdUHEmguha8', CAST(N'2026-07-14T16:52:00.7004402' AS DateTime2), CAST(N'2026-07-21T16:52:00.7004402' AS DateTime2), CAST(N'2026-07-14T16:52:35.4347606' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'af50850f-9794-41ef-a0bb-6f86f3d787d2', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzUwNDY3NSwiZXhwIjoxNzgzNTkxMDc1fQ.2j7U0DeqaGS9cY2p1UG5_JiaBhaXiB6WCJKTiKP_IBo', CAST(N'2026-07-08T16:57:55.8182491' AS DateTime2), CAST(N'2026-07-15T16:57:55.8182491' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'185736da-d54a-48e4-93e2-6ff71481329a', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxOTkxOSwiZXhwIjoxNzg0MTA2MzE5fQ.blWiFKOUXHKhY0lrkDGTZCmb6G9KL1QKWPzWBWj3bq0', CAST(N'2026-07-14T16:05:19.6764427' AS DateTime2), CAST(N'2026-07-21T16:05:19.6769488' AS DateTime2), CAST(N'2026-07-14T16:06:03.6282076' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'880fc50e-a050-4a1b-a90f-700f54556ae4', N'3644121c-cb57-4576-ad3e-03e9315f5896', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzOTM4NzE1LCJleHAiOjE3ODQwMjUxMTV9.fwI1wCXgVOgqMhVE2ij9Cqk4J37JhNS_sfwXj5wzKYk', CAST(N'2026-07-13T10:31:55.7742457' AS DateTime2), CAST(N'2026-07-20T10:31:55.7742493' AS DateTime2), CAST(N'2026-07-13T10:32:54.4316488' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'297fc28b-470d-42e9-98be-7031df44f382', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMzIyNiwiZXhwIjoxNzg0MTA5NjI2fQ._KmVZXeFl5NauqAk0gZqSnUQJx9H602byhMTJ6gyfnQ', CAST(N'2026-07-14T17:00:26.2399318' AS DateTime2), CAST(N'2026-07-21T17:00:26.2399318' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'14534e3e-53e0-4add-a064-7595871ef19b', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzgyNjYxNywiZXhwIjoxNzgzOTEzMDE3fQ.sahzJt7ZmZELYmprrBuPQSXuXoQx6-D-Ik5qH5WGiAI', CAST(N'2026-07-12T03:23:37.4600322' AS DateTime2), CAST(N'2026-07-19T03:23:37.4603725' AS DateTime2), CAST(N'2026-07-12T04:09:38.9237287' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'64991cc6-aa2f-494b-b6ae-7946bf31e842', N'a0000000-0000-0000-0000-111111111111', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW50b3IxQHNlYWwuY29tIiwiaWF0IjoxNzgzNDkyMTYyLCJleHAiOjE3ODM1Nzg1NjJ9.sAdaLuKRbTsaKs8A7Pqm__PyFop_ZW1q3ne8cqnQCY8', CAST(N'2026-07-08T13:29:22.3874989' AS DateTime2), CAST(N'2026-07-15T13:29:22.3874989' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ec0aef1f-ec54-40bc-95ce-7cb135ea29d8', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5Njc4NCwiZXhwIjoxNzgzNTgzMTg0fQ.WLT5yOiHXAmT_wSsFWp9N6aZjD2Q6TRiVzQGYMlskjo', CAST(N'2026-07-08T14:46:24.2855851' AS DateTime2), CAST(N'2026-07-15T14:46:24.2855851' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'783da552-4b10-44fa-adcb-7d01378b8ee9', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5NDU5NCwiZXhwIjoxNzgzNTgwOTk0fQ.jq--D_bCCw0iI_sB1g8oqIqpqeL1aH56E447eI0fAxs', CAST(N'2026-07-08T14:09:54.0736241' AS DateTime2), CAST(N'2026-07-15T14:09:54.0736241' AS DateTime2), CAST(N'2026-07-08T14:16:09.2146766' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'1882693a-fc6d-4a8f-a332-7d01cbbcceca', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NjQxNSwiZXhwIjoxNzgzNjcyODE1fQ.Mb10f8phivXAOPdp05cAikknXhWfzO1ZbZyzujFbScc', CAST(N'2026-07-09T15:40:15.0204119' AS DateTime2), CAST(N'2026-07-16T15:40:15.0204119' AS DateTime2), CAST(N'2026-07-09T15:42:07.0210005' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'7872deab-434a-44be-9f62-82cc4a5e92d8', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5NDQ4OSwiZXhwIjoxNzgzNTgwODg5fQ.MUVbgGjVKo6r2THSoVLDiduHu0KP4iBLkwhiWEe0Fto', CAST(N'2026-07-08T14:08:09.6417303' AS DateTime2), CAST(N'2026-07-15T14:08:09.6417303' AS DateTime2), CAST(N'2026-07-08T14:09:52.2942885' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'89a1aaeb-f574-4c4a-86dd-83c4d64e7f3c', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTcxMDQ4LCJleHAiOjE3ODM2NTc0NDh9.HQIEiR_W8GrdOAk_LxAgXzuYHRaVBrDTpP1KyJTM9DM', CAST(N'2026-07-09T11:24:08.7778406' AS DateTime2), CAST(N'2026-07-16T11:24:08.7778406' AS DateTime2), CAST(N'2026-07-09T12:21:22.2594664' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'eb46bfc0-525d-48d5-b8b5-850df553d110', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4MzgzNjU5NSwiZXhwIjoxNzgzOTIyOTk1fQ.2QbnEpyZ4iSqb4InfjHR1afTaE6GxBtGKQn15q9H6KI', CAST(N'2026-07-12T13:09:55.4578531' AS DateTime2), CAST(N'2026-07-19T13:09:55.4578531' AS DateTime2), CAST(N'2026-07-12T13:42:53.8933165' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'42cd13a6-45c6-4c9f-8241-8b9a5fd657dc', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTc3OTgxLCJleHAiOjE3ODM2NjQzODF9.hMtUWyxZ0H1t-iRD_LJMbtrayAmRI23Y50_a0sCaW8o', CAST(N'2026-07-09T13:19:41.7595251' AS DateTime2), CAST(N'2026-07-16T13:19:41.7595251' AS DateTime2), CAST(N'2026-07-09T13:25:24.0314410' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f80b5ac7-ca14-48e6-a990-8e062a1a8e8a', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NDM3MywiZXhwIjoxNzgzNjcwNzczfQ.Fd7hwyrBcK-FAKF4Ir1yfUm8UGxJPrOYpXpE9HSrhtA', CAST(N'2026-07-09T15:06:13.3807147' AS DateTime2), CAST(N'2026-07-16T15:06:13.3807147' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'482f7673-fd3a-4ce1-8f6d-92788b1251a5', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1Nzg1NjMsImV4cCI6MTc4MzY2NDk2M30.cY0NViCK5gmFgFml-f_YngOPw5IK7aSqSJeg4xqI9PU', CAST(N'2026-07-09T13:29:23.0035203' AS DateTime2), CAST(N'2026-07-16T13:29:23.0035203' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'c6929852-9085-4eec-ad0f-9289fadeeccb', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzODI2MTUxLCJleHAiOjE3ODM5MTI1NTF9.mRCY5uIn3jKhxlYJxiqpxiqOr2sqQhk9mlrSA1bGzCI', CAST(N'2026-07-12T03:15:51.6164695' AS DateTime2), CAST(N'2026-07-19T03:15:51.6164728' AS DateTime2), CAST(N'2026-07-12T03:20:56.5739463' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'6aa12051-c46e-4ccb-8aa1-95d1983c8733', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDk2ODY2LCJleHAiOjE3ODM1ODMyNjZ9.M3JaB-4Y5kCG2H36txpVlMb20aZ_LIYul6NmB5kvqBI', CAST(N'2026-07-08T14:47:46.3704016' AS DateTime2), CAST(N'2026-07-15T14:47:46.3704016' AS DateTime2), CAST(N'2026-07-08T14:47:48.1064350' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'1f094b4a-a9dc-445c-b7a1-962e53bf07fa', N'a1000000-0000-0000-0000-000000000004', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkubWVudG9yQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjI4MSwiZXhwIjoxNzg0MTA4NjgxfQ.Xk4HnmUx2Ahh5zJVGjGyVevY-OBVQj8Dn2LLiW-YgS4', CAST(N'2026-07-14T16:44:41.8409667' AS DateTime2), CAST(N'2026-07-21T16:44:41.8409667' AS DateTime2), CAST(N'2026-07-14T16:45:31.6807376' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f9f16682-c66b-444b-9b39-97a03bd18ee3', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyMjc3NSwiZXhwIjoxNzg0MTA5MTc1fQ.EI3z7dj-Zf8ZILRFIaHKdoFTDN_T449_XCny1mW5-uY', CAST(N'2026-07-14T16:52:55.1886904' AS DateTime2), CAST(N'2026-07-21T16:52:55.1886904' AS DateTime2), CAST(N'2026-07-14T16:53:06.8137997' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'5b9e6cc9-a66f-40c9-a6a5-97c58acdd43a', N'a1000000-0000-0000-0000-000000000013', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuYmV0YS5tZW1iZXJAc2VhbC50ZXN0IiwiaWF0IjoxNzg0MDIyODA1LCJleHAiOjE3ODQxMDkyMDV9.GL_jcr1AVZtSuiWKCcw6VNqav_VACwL_hi4iK7tvT-M', CAST(N'2026-07-14T16:53:25.5840493' AS DateTime2), CAST(N'2026-07-21T16:53:25.5840493' AS DateTime2), CAST(N'2026-07-14T16:54:22.1991995' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'9e633d46-f088-4403-8f48-9820d485c4f5', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzY4MzQzNSwiZXhwIjoxNzgzNzY5ODM1fQ.TzVO_FwekOPKvDDHYskcx-JMvVD7s-A_xht_kR1-k68', CAST(N'2026-07-10T18:37:15.0486020' AS DateTime2), CAST(N'2026-07-17T18:37:15.0486020' AS DateTime2), CAST(N'2026-07-10T18:48:15.0889879' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'cdae0d62-2574-466f-b183-a0ce7f431e6a', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU3ODMzNCwiZXhwIjoxNzgzNjY0NzM0fQ.o4oxiA7gWrEiSuu02XnNe2GAw1CCHaXW7m8Ou9lHn4M', CAST(N'2026-07-09T13:25:34.5714710' AS DateTime2), CAST(N'2026-07-16T13:25:34.5714710' AS DateTime2), CAST(N'2026-07-09T14:40:07.1945656' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'2efc0655-987f-48d8-9d2f-a1569d1e6af1', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ3OTUxOSwiZXhwIjoxNzgzNTY1OTE5fQ.B_IY9137gOK8yu8vboDimhGuHls-Q68EFywoV8K3FCE', CAST(N'2026-07-08T09:58:39.5798500' AS DateTime2), CAST(N'2026-07-15T09:58:39.5798500' AS DateTime2), CAST(N'2026-07-08T09:58:42.9616783' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'56ee275d-140d-4f3f-9683-a426ddd15d3c', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMDI5NSwiZXhwIjoxNzg0MTA2Njk1fQ.b_r6V7zjiWb-BqT9mhjqFZXgtIJapqg90i3nwMaiGJw', CAST(N'2026-07-14T16:11:35.7023868' AS DateTime2), CAST(N'2026-07-21T16:11:35.7023868' AS DateTime2), CAST(N'2026-07-14T16:59:41.5309028' AS DateTime2), N'WEB')
GO
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b59b124c-3c1c-4a4e-8a25-a5421bba0750', N'ea969288-a77c-4683-b661-b10955f1df18', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cm9uZ2hpZXUxNzEwMjZAZ21haWwuY29tIiwiaWF0IjoxNzg0MDIzMDM1LCJleHAiOjE3ODQxMDk0MzV9.2KZy-BKX_a5lMn4uJm3P8lJZ5wpAEATpkoQSen6KEEo', CAST(N'2026-07-14T16:57:15.7832665' AS DateTime2), CAST(N'2026-07-21T16:57:15.7832665' AS DateTime2), CAST(N'2026-07-14T16:59:35.6987898' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'a783771b-2023-43b5-aed4-a636674ef12e', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NTExNSwiZXhwIjoxNzgzNjcxNTE1fQ.hLg9V7efA9Q28IQ-1roMxTKeAT1IonSMTBGn86uHRQ8', CAST(N'2026-07-09T15:18:35.6814495' AS DateTime2), CAST(N'2026-07-16T15:18:35.6814495' AS DateTime2), CAST(N'2026-07-09T15:20:55.4423065' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'063d6948-d71c-4003-960f-a6b4cb0929f2', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4MzgzNDk1NCwiZXhwIjoxNzgzOTIxMzU0fQ.zGBbMRTlwo_oRytxyOGPLYO48h60joDub2U9tqYdq4I', CAST(N'2026-07-12T12:42:34.1387047' AS DateTime2), CAST(N'2026-07-19T12:42:34.1387047' AS DateTime2), CAST(N'2026-07-12T12:46:14.6143924' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f071ab9c-dd96-4042-9f01-a7c98a3175e9', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg2NTMwLCJleHAiOjE3ODM2NzI5MzB9.v6JhIkMzwu5IIiH3znVyAWCeVUlG75polkYY3q2dMOc', CAST(N'2026-07-09T15:42:10.4524359' AS DateTime2), CAST(N'2026-07-16T15:42:10.4524359' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'6e8b7859-85dd-4511-bcfe-a7ffd3b4bf48', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzODI1Njc2LCJleHAiOjE3ODM5MTIwNzZ9.s-uu7IZtS5vSZmbUM3LCw9d1077xgGz31fJlyP_mcPs', CAST(N'2026-07-12T03:07:56.5682174' AS DateTime2), CAST(N'2026-07-19T03:07:56.5682214' AS DateTime2), CAST(N'2026-07-12T03:15:47.8840796' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'748073f9-237b-4a59-b582-ae356b73bab8', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDc2MDIzLCJleHAiOjE3ODM1NjI0MjN9.V5XZpxZ7N8bJO70bQE7sX-XpPcXHJxyWcowqrm9I4Io', CAST(N'2026-07-08T09:00:23.8070258' AS DateTime2), CAST(N'2026-07-15T09:00:23.8070258' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'cf5a110e-5388-4de8-ab89-b40e5f4b5174', N'a1000000-0000-0000-0000-000000000002', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuanVkZ2UxQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4MzM2OCwiZXhwIjoxNzgzNjY5NzY4fQ.RBwgYuNqfrwAzyffQKUShWq0gcFVOYyeQGRqxdW5BWg', CAST(N'2026-07-09T14:49:28.9884660' AS DateTime2), CAST(N'2026-07-16T14:49:28.9884660' AS DateTime2), CAST(N'2026-07-09T14:49:43.5720737' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd7436a22-7c4e-4c8f-9995-b788c3e7ef9e', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTYzMDIxLCJleHAiOjE3ODM2NDk0MjF9.MHtYEAiA7hF8xO0Soa-PyrJ_huWPpnFGuiDjAE0B6No', CAST(N'2026-07-09T09:10:21.2205061' AS DateTime2), CAST(N'2026-07-16T09:10:21.2205061' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd1c0c97f-314a-418d-874b-b7dddb4443ab', N'ea969288-a77c-4683-b661-b10955f1df18', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0cm9uZ2hpZXUxNzEwMjZAZ21haWwuY29tIiwiaWF0IjoxNzgzNDk4ODE4LCJleHAiOjE3ODM1ODUyMTh9.0_d3OKQ_I1gpXNGCo4qLSZZYYxbS8PD2W60EYLm0dg0', CAST(N'2026-07-08T15:20:18.2571221' AS DateTime2), CAST(N'2026-07-15T15:20:18.2571221' AS DateTime2), CAST(N'2026-07-08T15:22:14.5948558' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'a1b22f3f-5dba-423f-ba26-b7e707167cd8', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzODI2NDgyLCJleHAiOjE3ODM5MTI4ODJ9.IaNmNg5Gi6x-dy8p90W7tVjynn0r6FDjb5UgC2m8uvY', CAST(N'2026-07-12T03:21:22.3430351' AS DateTime2), CAST(N'2026-07-19T03:21:22.3430382' AS DateTime2), CAST(N'2026-07-12T03:23:32.3280091' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'48a614b2-30a7-4d46-b53b-b878df091059', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyMjc4OSwiZXhwIjoxNzg0MTA5MTg5fQ.A-97C_Lzd64B8u1o2HM9BHe_tZcFlqC7Q5F4pUcOI2w', CAST(N'2026-07-14T16:53:09.6385758' AS DateTime2), CAST(N'2026-07-21T16:53:09.6385758' AS DateTime2), CAST(N'2026-07-14T17:02:58.1708329' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'5c3c2de1-106b-44d0-b4f0-bcbfd048ddbc', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzMDc2MCwiZXhwIjoxNzg0MTE3MTYwfQ.4XA3MGVLAwusj6Zs_w8iAFrh_ipv-HfbFhinymgwXCM', CAST(N'2026-07-14T19:06:00.1343201' AS DateTime2), CAST(N'2026-07-21T19:06:00.1343201' AS DateTime2), CAST(N'2026-07-14T20:12:20.6669096' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'28e402d9-6308-4aec-8e00-bcc236dd5471', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTY3NDg2LCJleHAiOjE3ODM2NTM4ODZ9.yiHYbeMXjv7fz1KQYLJ6IwgSekGGAGPM6UDPNJeeS_A', CAST(N'2026-07-09T10:24:46.0007560' AS DateTime2), CAST(N'2026-07-16T10:24:46.0007560' AS DateTime2), CAST(N'2026-07-09T11:24:07.0368829' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'df8228c4-48cf-491d-99b7-bccbe8ded02c', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNjA2MCwiZXhwIjoxNzg0MTIyNDYwfQ.ZKFn3XK1JNOesx7xx16ucSJGP-YCPT6_8NiRXh_8gag', CAST(N'2026-07-14T20:34:20.4816623' AS DateTime2), CAST(N'2026-07-21T20:34:20.4816623' AS DateTime2), CAST(N'2026-07-14T20:34:39.8475827' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'952a34bf-9352-4c7b-8307-bcd6ae3ee8a6', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4MzY2OTgzOSwiZXhwIjoxNzgzNzU2MjM5fQ.kuxT-2CCoDGLyPDFaS_2MCBM2rtgKEc06RtSlF7lNYs', CAST(N'2026-07-10T14:50:39.9908957' AS DateTime2), CAST(N'2026-07-17T14:50:39.9908957' AS DateTime2), CAST(N'2026-07-12T12:40:39.0142117' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b41f36e9-f914-4f9c-95a8-bd52887563f5', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg4NDk0LCJleHAiOjE3ODM2NzQ4OTR9.etsrmR9rSt4aYx6kQkNtLjHnpzKucai2Jzlps4slaV0', CAST(N'2026-07-09T16:14:54.3885882' AS DateTime2), CAST(N'2026-07-16T16:14:54.3885882' AS DateTime2), CAST(N'2026-07-09T16:16:23.9990296' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'04feb1f3-eb39-4a05-8e5f-be103aadc16f', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzMDQ1MCwiZXhwIjoxNzg0MTE2ODUwfQ.CcmnQj5_Ymp00XnGTZH-Pko_vIoOO1xk8ez-ZDZj66o', CAST(N'2026-07-14T19:00:50.8922493' AS DateTime2), CAST(N'2026-07-21T19:00:50.8922493' AS DateTime2), CAST(N'2026-07-14T19:01:42.6290437' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ee5eb220-0a4b-4490-b707-be2662f933ce', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDk1MDMzLCJleHAiOjE3ODM1ODE0MzN9.k8N8HYKaJj75P8570qqCYeGt_EU7uZCm2T7-bUKKNoU', CAST(N'2026-07-08T14:17:13.3286976' AS DateTime2), CAST(N'2026-07-15T14:17:13.3286976' AS DateTime2), CAST(N'2026-07-08T14:17:25.7838561' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'c998a3fa-26e9-466e-8282-be5bfe967d59', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjIwNCwiZXhwIjoxNzg0MTA4NjA0fQ.hXvD-B0t3pUJMiXSupysYDSQrVD23dlQvBRwO4NuHfY', CAST(N'2026-07-14T16:43:24.2548722' AS DateTime2), CAST(N'2026-07-21T16:43:24.2548722' AS DateTime2), CAST(N'2026-07-14T16:44:31.3383428' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'927188e0-3204-4249-8b67-bf01c6d6a2cc', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzMDEwMSwiZXhwIjoxNzg0MTE2NTAxfQ.SYvJpvD2Ui5EUKf3ceCN0qg8Rc8UQN5TkXIVwPYnDOM', CAST(N'2026-07-14T18:55:01.4304633' AS DateTime2), CAST(N'2026-07-21T18:55:01.4304633' AS DateTime2), CAST(N'2026-07-14T19:00:46.7390594' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd648b90e-8b1e-40e1-bdab-c1364cd1e926', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg5MDIxLCJleHAiOjE3ODM2NzU0MjF9.EnUAnDNBJEc1phYH4iMZY1At6jO1DGqTarcJtHrlH-U', CAST(N'2026-07-09T16:23:41.7468711' AS DateTime2), CAST(N'2026-07-16T16:23:41.7468711' AS DateTime2), CAST(N'2026-07-09T16:25:03.7681340' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'a146872b-497c-4b9c-a8ec-c3f0b1540f1f', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxNTgxOCwiZXhwIjoxNzg0MTAyMjE4fQ.Q91wyJIWkumYP5W8wrEQ-dgILYmIOa6tFnjP0rh0VJE', CAST(N'2026-07-14T07:56:58.2501563' AS DateTime2), CAST(N'2026-07-21T07:56:58.2501607' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'4912385d-4f41-45c4-90ed-c6518e79134f', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzY2NTQxMSwiZXhwIjoxNzgzNzUxODExfQ.xa_-HbfNOZDugYLkMjfT4UZEDeE2e8CqdAa2OX7hwPo', CAST(N'2026-07-10T13:36:51.5598782' AS DateTime2), CAST(N'2026-07-17T13:36:51.5598782' AS DateTime2), CAST(N'2026-07-10T13:39:30.4840443' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'4a2f9780-4ec5-4e6e-8a1e-c662480e13c5', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg2MzIyLCJleHAiOjE3ODM2NzI3MjJ9.NEqfi7Tp9MAT5Tq_hNKV6dXhaSmTPMU1cthVUlAiOOo', CAST(N'2026-07-09T15:38:42.8898646' AS DateTime2), CAST(N'2026-07-16T15:38:42.8898646' AS DateTime2), CAST(N'2026-07-09T15:40:08.9610170' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'77cd04a7-fc54-4a3d-92c8-c7e67dfc3297', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ0MjU4NCwiZXhwIjoxNzgzNTI4OTg0fQ.FzE9_LRniOEe7avrjvR03YQcj6nxQtPL3m_fmsJJZEw', CAST(N'2026-07-07T23:43:04.6285654' AS DateTime2), CAST(N'2026-07-14T23:43:04.6285654' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'529beaff-583b-44e0-983f-c81dcdd8a7cf', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5Njg3NSwiZXhwIjoxNzgzNTgzMjc1fQ.F_1sB4JsJBTxHadFMo83oNS8ayYjToIRc0tXdUgtmv0', CAST(N'2026-07-08T14:47:55.3768552' AS DateTime2), CAST(N'2026-07-15T14:47:55.3768552' AS DateTime2), CAST(N'2026-07-08T15:49:58.2157308' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ab2dfec1-ca77-41ce-bf5a-c8996e8ef8f1', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg5MTgwLCJleHAiOjE3ODM2NzU1ODB9.RBSoqf1uO1hgya8mpkE-8jC7Qxy2XBMRN89tP944QB8', CAST(N'2026-07-09T16:26:20.5447444' AS DateTime2), CAST(N'2026-07-16T16:26:20.5447444' AS DateTime2), CAST(N'2026-07-09T16:35:17.3512447' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'e4acb942-da8b-4819-8a81-c8b9c0b2ef2b', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNDg3NiwiZXhwIjoxNzg0MTIxMjc2fQ.DBkrFJI34w_cLMrGENQ0ijb1iJD-1ATyIligO6XzOcE', CAST(N'2026-07-14T20:14:36.1766471' AS DateTime2), CAST(N'2026-07-21T20:14:36.1766471' AS DateTime2), CAST(N'2026-07-14T20:14:48.6408505' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b3ff43f5-4498-418f-82c4-c971764a4da1', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU2NzM5MiwiZXhwIjoxNzgzNjUzNzkyfQ.lvoJ7Mudzhkqsmjh3ydXznFkUscSJ8n7cXKp3fkP0ZA', CAST(N'2026-07-09T10:23:12.5770538' AS DateTime2), CAST(N'2026-07-16T10:23:12.5770538' AS DateTime2), CAST(N'2026-07-09T10:24:41.7235021' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'6707ad40-4539-4e13-9971-ca12bef65383', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg1NTIwLCJleHAiOjE3ODM2NzE5MjB9.eq7m5SoOpP--ZIloLm9iJuqnmkAkA-qGuOXba_qfXGM', CAST(N'2026-07-09T15:25:20.7530602' AS DateTime2), CAST(N'2026-07-16T15:25:20.7530602' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd305ee9e-8940-4517-8b00-cad755f43cd1', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1MDI3NTIsImV4cCI6MTc4MzU4OTE1Mn0.h--AX0A-1LXm8xrmwD4JvzWleviQPA7PGrAmSrVXrtU', CAST(N'2026-07-08T16:25:52.6226653' AS DateTime2), CAST(N'2026-07-15T16:25:52.6226653' AS DateTime2), CAST(N'2026-07-08T16:26:08.3034222' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'31cb5d32-4d06-4dbb-be6e-cae4214d2306', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5OTU3NSwiZXhwIjoxNzgzNTg1OTc1fQ.J7SekwlwDuCzebOexgInO3ucIu1wCQxv80npwoxpBzg', CAST(N'2026-07-08T15:32:55.5554054' AS DateTime2), CAST(N'2026-07-15T15:32:55.5554054' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b0c6c284-5d0a-4bae-b53a-cb219dd6f2c5', N'a1000000-0000-0000-0000-000000000015', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuZ3JlZW4ubWVtYmVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMzIzOSwiZXhwIjoxNzg0MTA5NjM5fQ.Dq9Z0ZMOyHd-j4nBXlQcHJC7wlUtfJEGm-W3PQMmDg8', CAST(N'2026-07-14T17:00:39.6477399' AS DateTime2), CAST(N'2026-07-21T17:00:39.6487402' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'2824eba1-155c-4eef-88e2-cbf098bdfcf4', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTY0NTMyLCJleHAiOjE3ODM2NTA5MzJ9.hYgAyJwbwfuzq_4Tt5r3D0gGBY7TXySfQsAuhVA7tFU', CAST(N'2026-07-09T09:35:32.1550134' AS DateTime2), CAST(N'2026-07-16T09:35:32.1550134' AS DateTime2), CAST(N'2026-07-09T09:35:43.9512045' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'da8b2a39-32cf-48d1-8db9-cc268ddc01e4', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg5NzE5LCJleHAiOjE3ODM2NzYxMTl9.UHlk7rn7tckkrvbAurpfYswRBrBmBne0igOJEwyh2uM', CAST(N'2026-07-09T16:35:19.0421883' AS DateTime2), CAST(N'2026-07-16T16:35:19.0421883' AS DateTime2), CAST(N'2026-07-10T10:48:28.9380707' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'686ca7a8-9438-4195-a75b-ccb6f2811e20', N'86eb15fb-4af1-4bc1-9bfe-15faa6a6c490', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWhhY2hlbmFsQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAxMDk4MSwiZXhwIjoxNzg0MDk3MzgxfQ.FFFTQ9JKjYEKTUhEUjq8758v5zx6vhLnXi-Oof0-I9w', CAST(N'2026-07-14T06:36:21.4788458' AS DateTime2), CAST(N'2026-07-21T06:36:21.4788489' AS DateTime2), CAST(N'2026-07-14T06:39:31.8216749' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'25aa88e4-9ee9-4c00-b6a4-ccf594dad5bd', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4Mzk1NzMzMSwiZXhwIjoxNzg0MDQzNzMxfQ.Ss1ToqRLl0rY1pbYOKEHhkadKTs6uu2tTlR2IMHOY7E', CAST(N'2026-07-13T22:42:11.8884510' AS DateTime2), CAST(N'2026-07-20T22:42:11.8884510' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'688980b1-cb76-4121-a1f1-cd25f44d3ca8', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzgzNTE4MCwiZXhwIjoxNzgzOTIxNTgwfQ.clJhrGIhIdcvpupWmemhhpnS53V7pUDNjv58YEEv5W4', CAST(N'2026-07-12T12:46:20.2300926' AS DateTime2), CAST(N'2026-07-19T12:46:20.2300926' AS DateTime2), CAST(N'2026-07-12T13:09:50.1656980' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8fdd0cba-5ecb-4e23-844d-cd868454a428', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTY2MTA3LCJleHAiOjE3ODM2NTI1MDd9.idKFjObPiR8KoFYSepfoUHCi-XVVl0M4hI1Bt8Dc6rg', CAST(N'2026-07-09T10:01:47.8990167' AS DateTime2), CAST(N'2026-07-16T10:01:47.8990167' AS DateTime2), CAST(N'2026-07-09T10:23:04.7834004' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bf645777-c3f1-4311-97a9-d050d0bfb9b4', N'a1000000-0000-0000-0000-000000000017', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuZ3Vlc3RqdWRnZUBzZWFsLnRlc3QiLCJpYXQiOjE3ODQwMjI3NzYsImV4cCI6MTc4NDEwOTE3Nn0.Wtknp1Jy3aT7ngx3AmFkjqoY-ASpgrgayhIXTINqMJM', CAST(N'2026-07-14T16:52:56.6118482' AS DateTime2), CAST(N'2026-07-21T16:52:56.6118482' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'1d09e842-9d6b-4e2a-9a1a-d0a7dd64361d', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyODY0NiwiZXhwIjoxNzg0MTE1MDQ2fQ.HeqaQzSCZdL6nxX2LLhbK_lXD5UXrCxDUw_X1rpOjJA', CAST(N'2026-07-14T18:30:46.3041544' AS DateTime2), CAST(N'2026-07-21T18:30:46.3041544' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'fe7111fd-dc27-4d42-a5bc-d14a98889bea', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1NzUwOTgsImV4cCI6MTc4MzY2MTQ5OH0.KWl4TfY-TRPPiR8QKC2zWIrYoggnZSvAq01rcAIwcVE', CAST(N'2026-07-09T12:31:38.1831692' AS DateTime2), CAST(N'2026-07-16T12:31:38.1831692' AS DateTime2), CAST(N'2026-07-09T12:38:38.8801856' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ff32a965-b1ab-409e-959f-d1c488ab0bfc', N'a1000000-0000-0000-0000-000000000010', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuYWxwaGEubGVhZGVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjMzOSwiZXhwIjoxNzg0MTA4NzM5fQ.zVokao__KlO7fB9B2aJhC0xKObTq3yYaRVZ9IBq9j-U', CAST(N'2026-07-14T16:45:39.3728243' AS DateTime2), CAST(N'2026-07-21T16:45:39.3728243' AS DateTime2), CAST(N'2026-07-14T16:49:49.9925206' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'eeb72aac-9275-4bfc-989b-d240e29d64e1', N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWhhY2hlbmFsQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAxMTAxNCwiZXhwIjoxNzg0MDk3NDE0fQ.dt6jBUYBgCZliIAbNj2UA7Y5rNE0UQZEW7N5UJ42bNo', CAST(N'2026-07-14T06:36:54.4349099' AS DateTime2), CAST(N'2026-07-21T06:36:54.4349138' AS DateTime2), CAST(N'2026-07-14T06:38:02.3379104' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'708bfa1d-544f-4656-883e-d2566e202504', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAxMjkxMCwiZXhwIjoxNzg0MDk5MzEwfQ.QhRr2AylGx6Ra5WbpSk8nqC2m-K0pTyOHb9uvxJQgxE', CAST(N'2026-07-14T07:08:30.9520958' AS DateTime2), CAST(N'2026-07-21T07:08:30.9520999' AS DateTime2), CAST(N'2026-07-14T07:08:50.0307720' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0796feff-5ab9-4a5b-8ff5-d2b2d50a865a', N'6006f192-3fd1-4647-b145-8ebccb5ebd61', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW50b3Jmb3J0ZXN0aW5nMTIzQGdtYWlsLmNvbSIsImlhdCI6MTc4MzUwMjc4NSwiZXhwIjoxNzgzNTg5MTg1fQ.SauTN3VS4oHbjjed5n6SyC9cKeDUFoV2V7ymCWr9DuE', CAST(N'2026-07-08T16:26:25.5105970' AS DateTime2), CAST(N'2026-07-15T16:26:25.5105970' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'84e8cbef-3f9b-4925-8fac-d3062ae6ee0c', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzg0MDIyMjc3LCJleHAiOjE3ODQxMDg2Nzd9.je4_oWhu2Zp46LxOZTP7QFDrQfM9MWGeVarulJwk9m0', CAST(N'2026-07-14T16:44:37.0801116' AS DateTime2), CAST(N'2026-07-21T16:44:37.0801116' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'c420571b-0aa6-4d88-947f-d4c2a5c6ffed', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAzNjEzMiwiZXhwIjoxNzg0MTIyNTMyfQ.p1dPSzuBaO6yAs9S1lexabw5CkqTOQGTjhGxuDzxJoc', CAST(N'2026-07-14T20:35:32.5952165' AS DateTime2), CAST(N'2026-07-21T20:35:32.5952165' AS DateTime2), CAST(N'2026-07-14T20:35:42.2710413' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'56ebc742-0464-42d4-95de-d5d73af2f0a4', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjA2NiwiZXhwIjoxNzg0MTA4NDY2fQ.Q1VRw2p_83cZ-GJ45msPzrQdBmpBwuFc0G3puLXcZgI', CAST(N'2026-07-14T16:41:06.2344750' AS DateTime2), CAST(N'2026-07-21T16:41:06.2344750' AS DateTime2), CAST(N'2026-07-14T16:43:20.5909789' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bc26a023-651d-4ba9-9f0d-d62216e69a2b', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1MDA3NDcsImV4cCI6MTc4MzU4NzE0N30.y3u7PEUULy3URmF9NRLXmtUFq8b6dl-7JVaERpB2mbU', CAST(N'2026-07-08T15:52:27.5569949' AS DateTime2), CAST(N'2026-07-15T15:52:27.5569949' AS DateTime2), CAST(N'2026-07-08T15:53:16.6953151' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bac96709-e4b4-4b84-80b5-d670d8aa847f', N'fa60133b-5874-4dbf-9299-a0069495b193', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4dWFudGFuMDcxMTA2QGdtYWlsLmNvbSIsImlhdCI6MTc4MzcwMjM0MCwiZXhwIjoxNzgzNzg4NzQwfQ.OUW0sqGKBmYTp0enaZueSHRhzkni5QHsrdgNm7oR370', CAST(N'2026-07-10T16:52:20.7142780' AS DateTime2), CAST(N'2026-07-17T16:52:20.7142873' AS DateTime2), CAST(N'2026-07-10T16:52:25.9175283' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'21bd12d2-c77e-4db6-aa47-d75dfeab9d83', N'a1000000-0000-0000-0000-000000000002', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuanVkZ2UxQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjIwMywiZXhwIjoxNzg0MTA4NjAzfQ.H4h-cBkbVAGvImlWpXq7hNbyRrS6zVhsEpkfmMXTlUA', CAST(N'2026-07-14T16:43:24.0000684' AS DateTime2), CAST(N'2026-07-21T16:43:24.0000684' AS DateTime2), CAST(N'2026-07-14T16:44:33.6999046' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'd02a99a9-67c1-4f29-aeb0-d826e074381c', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNjY0MjQyLCJleHAiOjE3ODM3NTA2NDJ9.LyU_3crkrhhpdDxZ5Gll5Z_2lQD41ztzxbcsq9Oy5yQ', CAST(N'2026-07-10T13:17:22.5889352' AS DateTime2), CAST(N'2026-07-17T13:17:22.5889352' AS DateTime2), CAST(N'2026-07-10T13:36:43.6172342' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'adb590c0-29c7-4597-8fa9-d8f76a740a15', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzODI5Mzg2LCJleHAiOjE3ODM5MTU3ODZ9.xYgXFOJq_3Y-F-uEUVOq1571GxAQofthjSTtXHour7k', CAST(N'2026-07-12T04:09:46.8245146' AS DateTime2), CAST(N'2026-07-19T04:09:46.8245175' AS DateTime2), CAST(N'2026-07-14T06:32:26.5149475' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'ab3e3842-b53b-47ab-a010-da84bc03c37b', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg5MTY5LCJleHAiOjE3ODM2NzU1Njl9.OYAlF0-2ai_5KkPZ7ELhmGT-kjOjjVQvTk4OqstQCs4', CAST(N'2026-07-09T16:26:09.0967379' AS DateTime2), CAST(N'2026-07-16T16:26:09.0967379' AS DateTime2), CAST(N'2026-07-09T16:26:14.9059657' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'0cd7befe-65aa-4ed7-8e32-dac6f0576eb0', N'a1000000-0000-0000-0000-000000000011', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuYWxwaGEubWVtYmVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAyMjU5OSwiZXhwIjoxNzg0MTA4OTk5fQ.Ew_05aoMbqrznXWJXNITpPK9hvAZsHLmKYlVCSbezlI', CAST(N'2026-07-14T16:49:59.9747509' AS DateTime2), CAST(N'2026-07-21T16:49:59.9747509' AS DateTime2), CAST(N'2026-07-14T16:51:55.3439370' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'36fe64cd-8d4c-4724-953a-dac9096c10c2', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ3OTYyMSwiZXhwIjoxNzgzNTY2MDIxfQ.Jb8mnIJSfobvminyj09EoIBW4jMVf3G2VuIabsTg0Po', CAST(N'2026-07-08T10:00:21.0685604' AS DateTime2), CAST(N'2026-07-15T10:00:21.0685604' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f1406c74-5045-4c08-8765-dd2a7fa23af6', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NTM4NSwiZXhwIjoxNzgzNjcxNzg1fQ.kEiUCFU5KfGfbdzhVr9203AQ0RtzkP5Bnab9-i5pNZo', CAST(N'2026-07-09T15:23:05.8512141' AS DateTime2), CAST(N'2026-07-16T15:23:05.8512141' AS DateTime2), CAST(N'2026-07-09T15:25:16.6999644' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'c15e51e5-48fd-447d-84ef-ddb3ebef4967', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyOTE4MSwiZXhwIjoxNzg0MTE1NTgxfQ.90MSy8H1kvyxiYDGVlH7FBwoqYy9bjuZZHsBQBbXsEk', CAST(N'2026-07-14T18:39:41.4414476' AS DateTime2), CAST(N'2026-07-21T18:39:41.4414476' AS DateTime2), CAST(N'2026-07-14T18:54:57.8178588' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'4d5e42e7-80fe-48e6-a8d8-de8287c79443', N'a1000000-0000-0000-0000-000000000002', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuanVkZ2UxQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4MzMxMywiZXhwIjoxNzgzNjY5NzEzfQ._J1e4t-w7sHvLFxEXeS-U5yUprHAidbG09vyVAE3HRQ', CAST(N'2026-07-09T14:48:33.3189919' AS DateTime2), CAST(N'2026-07-16T14:48:33.3189919' AS DateTime2), CAST(N'2026-07-09T14:49:12.7004469' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'45d438f8-a5f5-4305-85de-dea8ee3b11d3', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4NDAzNDc3NSwiZXhwIjoxNzg0MTIxMTc1fQ.BlkWgxuE2kGlDgzj1WW7xJf78NyLUS9OXN-yVD_im1U', CAST(N'2026-07-14T20:12:55.0749454' AS DateTime2), CAST(N'2026-07-21T20:12:55.0749454' AS DateTime2), CAST(N'2026-07-14T20:14:32.7263254' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'c0d9dc80-a230-4fea-a538-e2109ab7cb2a', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTAwNjA0LCJleHAiOjE3ODM1ODcwMDR9.glvIA39wULdTy1L9LmRA1XncFBCKboesp_E-GULpInU', CAST(N'2026-07-08T15:50:04.6285435' AS DateTime2), CAST(N'2026-07-15T15:50:04.6285435' AS DateTime2), CAST(N'2026-07-09T09:10:16.8593867' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'928ba830-d448-43ec-a4c4-e31aff6529c3', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNjEwNSwiZXhwIjoxNzg0MTIyNTA1fQ.NhrcGs_duey7sc06uDsO_K0O6nm5AvPaJGVGFzZDuSY', CAST(N'2026-07-14T20:35:05.3681934' AS DateTime2), CAST(N'2026-07-21T20:35:05.3681934' AS DateTime2), CAST(N'2026-07-14T20:35:29.6148792' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8ef0b681-4e29-4e12-8e7e-e43fde10baed', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzQ5NTA0OSwiZXhwIjoxNzgzNTgxNDQ5fQ.lwxi5IZezgzEPg2OJ7bi_uZdK3Z9Q08kKlZYM0nSoEk', CAST(N'2026-07-08T14:17:29.0465481' AS DateTime2), CAST(N'2026-07-15T14:17:29.0465481' AS DateTime2), CAST(N'2026-07-08T14:26:28.7543768' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8e086e84-50c7-4261-ba5b-e839ae8a031d', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzg0MDIyOTQwLCJleHAiOjE3ODQxMDkzNDB9.kActWhJMBvn_28-IsjwsNukw0kEzyOLnEEkU4migYuA', CAST(N'2026-07-14T16:55:40.8758155' AS DateTime2), CAST(N'2026-07-21T16:55:40.8758155' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'154db9b2-7001-4085-8d5e-ecd3cf4a355a', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmgzQGdtYWlsLmNvbSIsImlhdCI6MTc4NDAzNjE0NywiZXhwIjoxNzg0MTIyNTQ3fQ.geLHKpZXd-kL-Il3vlrrnp2O9xUJXuDRiEh1HPxMTns', CAST(N'2026-07-14T20:35:47.9697783' AS DateTime2), CAST(N'2026-07-21T20:35:47.9697783' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'2e6d64db-4a0d-4a65-a7c0-ed5d3513a348', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg2MDA3LCJleHAiOjE3ODM2NzI0MDd9.l6zRFU3N0aofTPBED6P_JcqXJQxrE1-SboY7m0f2JM0', CAST(N'2026-07-09T15:33:27.9319659' AS DateTime2), CAST(N'2026-07-16T15:33:27.9319659' AS DateTime2), CAST(N'2026-07-09T15:35:12.3958357' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bccdcc5f-7633-4410-b295-ee3222622154', N'a1000000-0000-0000-0000-000000000004', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkubWVudG9yQHNlYWwudGVzdCIsImlhdCI6MTc4MzU2NTM1NywiZXhwIjoxNzgzNjUxNzU3fQ.6nGX_-t6lAFOiEyjmfafNDHrnGToFoufxMgmgECUIIc', CAST(N'2026-07-09T09:49:17.5248168' AS DateTime2), CAST(N'2026-07-16T09:49:17.5248168' AS DateTime2), CAST(N'2026-07-09T09:49:29.2368293' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'8b394de4-351f-481c-b63e-eed2599e272d', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyMzM4MSwiZXhwIjoxNzg0MTA5NzgxfQ.uQm-6X4mmQ93eho3YeAOseFXJpDBWEAbAVcT6cXM1-Q', CAST(N'2026-07-14T17:03:01.3370576' AS DateTime2), CAST(N'2026-07-21T17:03:01.3370576' AS DateTime2), CAST(N'2026-07-14T17:03:03.5947183' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'7d0717ec-d1bb-4915-a4f7-ef2216c2cb1e', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU2NDU1NCwiZXhwIjoxNzgzNjUwOTU0fQ.fbrc03V4dhhkin9AC5sm6yvmVL3rBaf5YcH3I1VWEJg', CAST(N'2026-07-09T09:35:54.1286108' AS DateTime2), CAST(N'2026-07-16T09:35:54.1286108' AS DateTime2), CAST(N'2026-07-09T09:49:11.9044574' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'655c7fd6-7959-47bd-bfed-ef3a361a7075', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1Nzk1ODAsImV4cCI6MTc4MzY2NTk4MH0.xJR2lV4MOVYEWSYqTMj7Qny82YqABikMa7lnJm8L1Bo', CAST(N'2026-07-09T13:46:20.4208700' AS DateTime2), CAST(N'2026-07-16T13:46:20.4208700' AS DateTime2), CAST(N'2026-07-09T14:27:23.3963960' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'76e19cde-f159-4182-a570-f03af42f2400', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg4MDM0LCJleHAiOjE3ODM2NzQ0MzR9.LimyuCqhdZguAnNoc6Ecr7XYhLEgwTkG-uy5eC-xXpE', CAST(N'2026-07-09T16:07:14.4996695' AS DateTime2), CAST(N'2026-07-16T16:07:14.4996695' AS DateTime2), CAST(N'2026-07-09T16:13:14.0094246' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f131a1a2-6fd3-4498-b7b9-f0a8898472c9', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4NjI3OCwiZXhwIjoxNzgzNjcyNjc4fQ.uv94RQcQU3aW2WawvOQux7e_oc1SHygxHgvQYys9q_0', CAST(N'2026-07-09T15:37:58.2954744' AS DateTime2), CAST(N'2026-07-16T15:37:58.2954744' AS DateTime2), CAST(N'2026-07-09T15:38:30.3061516' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f9a096b4-69ad-42f1-b6e3-f0a8d3d408af', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjU1MzEyLCJleHAiOjE3ODM3NDE3MTJ9.hI5Y83nKIBW2yt31Jrg2LrXACq6gRt9o4-sCbNUwB3o', CAST(N'2026-07-10T10:48:32.1573842' AS DateTime2), CAST(N'2026-07-17T10:48:32.1573842' AS DateTime2), CAST(N'2026-07-10T10:49:34.6310533' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'aaa17afb-a9c6-4761-96b5-f38cd4b92b87', N'a0000000-0000-0000-0000-000000000000', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvcmdhbml6ZXJAc2VhbC5jb20iLCJpYXQiOjE3ODM1MDEzOTMsImV4cCI6MTc4MzU4Nzc5M30.3MM20QklDlEGvaiDBixji-Bc6S4HMR4aac3llYS09UQ', CAST(N'2026-07-08T16:03:13.0303581' AS DateTime2), CAST(N'2026-07-15T16:03:13.0303581' AS DateTime2), CAST(N'2026-07-08T16:25:36.1384063' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'a2562c27-58a1-46a7-92c2-f3a3853598ec', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjU1ODY1LCJleHAiOjE3ODM3NDIyNjV9._-5tVD6_c27JPUHtCx87K9XrN20oXbGbQGWjlWO2r_s', CAST(N'2026-07-10T10:57:45.7551907' AS DateTime2), CAST(N'2026-07-17T10:57:45.7551907' AS DateTime2), CAST(N'2026-07-10T13:17:17.4184229' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'163d9cbf-e5aa-48f3-818d-f4efc6d9d704', N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWFtMWFAZ21haWwuY29tIiwiaWF0IjoxNzgzNTg0NzU4LCJleHAiOjE3ODM2NzExNTh9.yYZgwgMZOuQfA1r0Ab9nGNkeL7xozF1TCS8KgkNpGHg', CAST(N'2026-07-09T15:12:38.0481000' AS DateTime2), CAST(N'2026-07-16T15:12:38.0481000' AS DateTime2), CAST(N'2026-07-09T15:15:49.2551600' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'de1c4024-5ec1-40b6-9361-f721fe1783bf', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4Mjg1NSwiZXhwIjoxNzgzNjY5MjU1fQ.d4MLiMqapl-kTy4qbp1c5pvtBJ0OLw7MB77Y52flS-M', CAST(N'2026-07-09T14:40:55.8325433' AS DateTime2), CAST(N'2026-07-16T14:40:55.8325433' AS DateTime2), CAST(N'2026-07-09T14:43:26.3220814' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'bda4b8ed-8401-411d-9ed0-f7413b04ac3f', N'a1000000-0000-0000-0000-000000000010', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkuYWxwaGEubGVhZGVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU4MjQ1OSwiZXhwIjoxNzgzNjY4ODU5fQ.kgKCUae5zwO1fcnNPjCbInKx_h1b1vEcnZPAEypoc5M', CAST(N'2026-07-09T14:34:19.9273677' AS DateTime2), CAST(N'2026-07-16T14:34:19.9273677' AS DateTime2), CAST(N'2026-07-09T14:45:23.9381732' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b915dc38-a610-4e97-91fd-f7d609cc7b0a', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNDg2NjI4LCJleHAiOjE3ODM1NzMwMjh9.tj2BCSsj7Ql5P2VowVc4vM3FjoecVg_CGufYbkN7SrQ', CAST(N'2026-07-08T11:57:08.4622175' AS DateTime2), CAST(N'2026-07-15T11:57:08.4622175' AS DateTime2), CAST(N'2026-07-08T14:03:06.6533719' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'168bdffc-30eb-4f22-8a82-f8ec901725bd', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzg0MDIzNDI0LCJleHAiOjE3ODQxMDk4MjR9.VFrkJXMO_aUgZd67ikqw1Gj5Mm3Jf8g6VL3TcF_vWvA', CAST(N'2026-07-14T10:03:44.6789446' AS DateTime2), CAST(N'2026-07-21T10:03:44.6789479' AS DateTime2), CAST(N'2026-07-14T12:59:21.4372152' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'6b43211d-8a87-417c-ba70-f905d0b8e50e', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzU2NjA3OCwiZXhwIjoxNzgzNjUyNDc4fQ.HOS6PVo6KTZ3n01J6s1LusEUFA1NUirz1TQfBcEBBww', CAST(N'2026-07-09T10:01:18.6485363' AS DateTime2), CAST(N'2026-07-16T10:01:18.6485363' AS DateTime2), CAST(N'2026-07-09T10:01:39.5350460' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'f212e6e5-f65f-4b2c-83e0-f92c9c54beb4', N'a0000000-0000-0000-0000-111111111111', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW50b3IxQHNlYWwuY29tIiwiaWF0IjoxNzgzNTAxMzc3LCJleHAiOjE3ODM1ODc3Nzd9.IJe4OjI3NyUEOu23SzsmZoitZC8kZQTDFbyzFySOK4s', CAST(N'2026-07-08T16:02:57.0693042' AS DateTime2), CAST(N'2026-07-15T16:02:57.0693042' AS DateTime2), CAST(N'2026-07-08T16:03:08.3860675' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'64e38fb5-8cfa-4c6e-afec-f99df6437dc4', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4NDAyOTA3MSwiZXhwIjoxNzg0MTE1NDcxfQ.R4kwsN1JFGE_K16rb2AmzF4-UBybIbUYjV4XGPKn9m4', CAST(N'2026-07-14T18:37:51.6709757' AS DateTime2), CAST(N'2026-07-21T18:37:51.6709757' AS DateTime2), NULL, N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'49a4f1ea-875a-4495-9a08-fb3e0db73343', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNTY2MDQ3LCJleHAiOjE3ODM2NTI0NDd9.tZqptsNCDybQfa4xuh7-irwmsKkdYWx5PLWLTz-h6xw', CAST(N'2026-07-09T10:00:47.3070544' AS DateTime2), CAST(N'2026-07-16T10:00:47.3070544' AS DateTime2), CAST(N'2026-07-09T10:01:10.3281799' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'90261652-5e0d-41fe-bc22-fbc4105ce148', N'5d565896-2307-4911-939d-94e11e4b1c44', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZ3V5ZW5kdWNuYW1hbmg0QGdtYWlsLmNvbSIsImlhdCI6MTc4Mzk0MjYxNCwiZXhwIjoxNzg0MDI5MDE0fQ.TZtuuRAE70YDQm912_wTe6VfYvj196mfyemT2JKoulw', CAST(N'2026-07-13T18:36:54.4836964' AS DateTime2), CAST(N'2026-07-20T18:36:54.4836964' AS DateTime2), CAST(N'2026-07-13T22:42:07.9126757' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'efc2b782-5cb3-47d2-b763-ff4e5087008d', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcGkub3JnYW5pemVyQHNlYWwudGVzdCIsImlhdCI6MTc4MzgzODU3NywiZXhwIjoxNzgzOTI0OTc3fQ.pyv53XulcSFOstFBLR8qmjv8pUls3wKbM4Rf2K2eM-E', CAST(N'2026-07-12T13:42:57.0632572' AS DateTime2), CAST(N'2026-07-19T13:42:57.0632572' AS DateTime2), CAST(N'2026-07-13T18:36:48.9715307' AS DateTime2), N'WEB')
INSERT [dbo].[RefreshTokens] ([TokenID], [UserID], [TokenHash], [IssuedAt], [ExpiresAt], [RevokedAt], [DeviceInfo]) VALUES (N'b4ed6e11-dbee-4e66-ae8f-ff78cff357f1', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaGloaW5lMDNAZ21haWwuY29tIiwiaWF0IjoxNzgzNjkxMDU2LCJleHAiOjE3ODM3Nzc0NTZ9.mOU7Z4F2qFDzYoXXpCKyKKjCJ1tnJNwaBNoqB3Jsu5k', CAST(N'2026-07-10T13:44:16.8133176' AS DateTime2), CAST(N'2026-07-17T13:44:16.8133646' AS DateTime2), CAST(N'2026-07-10T13:47:40.3979787' AS DateTime2), N'WEB')
GO
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000001', N'd1000000-0000-0000-0000-000000000001', N'c3000000-0000-0000-0000-000000000001', CAST(0.40 AS Numeric(38, 2)), N'Innovation', N'Originality and user value.', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000002', N'd1000000-0000-0000-0000-000000000001', N'c3000000-0000-0000-0000-000000000002', CAST(0.35 AS Numeric(38, 2)), N'Technical Quality', N'Implementation quality.', CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000003', N'd1000000-0000-0000-0000-000000000001', N'c3000000-0000-0000-0000-000000000003', CAST(0.25 AS Numeric(38, 2)), N'Presentation', N'Demo and pitch quality.', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000011', N'd1000000-0000-0000-0000-000000000002', N'c3000000-0000-0000-0000-000000000001', CAST(0.40 AS Numeric(38, 2)), N'Innovation', N'Originality and user value.', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000012', N'd1000000-0000-0000-0000-000000000002', N'c3000000-0000-0000-0000-000000000002', CAST(0.35 AS Numeric(38, 2)), N'Technical Quality', N'Implementation quality.', CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000013', N'd1000000-0000-0000-0000-000000000002', N'c3000000-0000-0000-0000-000000000003', CAST(0.25 AS Numeric(38, 2)), N'Presentation', N'Demo and pitch quality.', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000021', N'd1000000-0000-0000-0000-000000000003', N'c3000000-0000-0000-0000-000000000001', CAST(0.40 AS Numeric(38, 2)), N'Innovation', N'Originality and user value.', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000022', N'd1000000-0000-0000-0000-000000000003', N'c3000000-0000-0000-0000-000000000002', CAST(0.35 AS Numeric(38, 2)), N'Technical Quality', N'Implementation quality.', CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000023', N'd1000000-0000-0000-0000-000000000003', N'c3000000-0000-0000-0000-000000000003', CAST(0.25 AS Numeric(38, 2)), N'Presentation', N'Demo and pitch quality.', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000031', N'd1000000-0000-0000-0000-000000000004', N'c3000000-0000-0000-0000-000000000011', CAST(0.20 AS Numeric(38, 2)), N'Innovation', N'Originality and user value.', CAST(10.00 AS Numeric(38, 2)), NULL)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000032', N'd1000000-0000-0000-0000-000000000004', N'c3000000-0000-0000-0000-000000000012', CAST(0.35 AS Numeric(38, 2)), N'Technical Quality', N'Implementation quality.', CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'd2000000-0000-0000-0000-000000000033', N'd1000000-0000-0000-0000-000000000004', N'c3000000-0000-0000-0000-000000000013', CAST(0.25 AS Numeric(38, 2)), N'Presentation', N'Demo and pitch quality.', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'97b5dd57-32cc-41b8-be14-018414111c86', N'951e45f4-fe2e-4f59-aacd-8bff725b1a32', N'c3000000-0000-0000-0000-000000000011', CAST(0.40 AS Numeric(38, 2)), N'Innovation', N'Originality and user value.', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'db67566c-bb6c-4332-9f40-062094966670', N'ee34e149-474d-4e1e-bff6-098dff4edf2d', N'c3000000-0000-0000-0000-000000000011', CAST(0.40 AS Numeric(38, 2)), N'Innovation', N'Originality and user value.', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'5de51f3b-76be-4fa3-8ebe-07ddceae8cfb', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'd20de1d1-1545-4c93-a1f8-52f8c2944a05', CAST(2.00 AS Numeric(38, 2)), N'Technical Complexity', N'Depth and complexity of technical implementation', CAST(10.00 AS Numeric(38, 2)), 5)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'daab8226-f742-4648-bc1e-0d25f691a956', N'dcf2cbd3-4a73-4395-bd4e-558d1aac4bf0', N'02791336-467f-4400-89ef-3986ad66338c', CAST(1.50 AS Numeric(38, 2)), N'Feasibility', N'Real-world applicability and viability', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'ef627da5-63bc-49ed-b76f-1e13a6d0c653', N'dcf2cbd3-4a73-4395-bd4e-558d1aac4bf0', N'e230d584-7926-4e6c-a24d-49b6e9cc2f8f', CAST(1.50 AS Numeric(38, 2)), N'Presentation Quality', N'Clarity and effectiveness of the demo/slide', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'15b66367-2e3c-4d3b-8573-2203a3ccc0ad', N'ee34e149-474d-4e1e-bff6-098dff4edf2d', N'c3000000-0000-0000-0000-000000000013', CAST(0.25 AS Numeric(38, 2)), N'Presentation', N'Demo and pitch quality.', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'0285b0be-586b-4e40-824b-41c773e06c67', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'f1771b9c-2f10-467a-99b6-716873c7cb55', CAST(1.50 AS Numeric(38, 2)), N'Feasibility', N'Real-world applicability and viability', CAST(10.00 AS Numeric(38, 2)), 3)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'8664194f-021f-421f-834a-550f93ef5b20', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'4e9514ef-a5d4-4530-9ff8-ee9ce786d17f', CAST(1.00 AS Numeric(38, 2)), N'Code Quality', N'Readability, structure, and maintainability', CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'ad1a5dd8-f592-4507-9b46-572e9571d986', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'9a46b529-0ed0-4de6-91c7-1b996fb9f96a', CAST(1.00 AS Numeric(38, 2)), N'Idea', NULL, CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'2e87fac8-9f82-4150-88e7-6c8ec6783b8d', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'9a46b529-0ed0-4de6-91c7-1b996fb9f96a', CAST(1.00 AS Numeric(38, 2)), N'Idea', NULL, CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'2b053b7e-cbc9-4c0d-9b7c-75f8161e81b3', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'7961cef8-d8b9-403e-b25c-15e8ef495b52', CAST(2.00 AS Numeric(38, 2)), N'Tech', NULL, CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'964a0a74-9323-4209-8437-9125678304c1', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'9a46b529-0ed0-4de6-91c7-1b996fb9f96a', CAST(1.00 AS Numeric(38, 2)), N'Idea', NULL, CAST(10.00 AS Numeric(38, 2)), 1)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'2120c861-fe68-461a-a023-9290e1aaf8b7', N'ee34e149-474d-4e1e-bff6-098dff4edf2d', N'123f399b-2d8b-47e3-9766-0000ecbabef3', CAST(2.00 AS Numeric(38, 2)), N'Technical Complexity', N'Depth and complexity of technical implementation', CAST(10.00 AS Numeric(38, 2)), 4)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'87b741bc-659a-4ed7-ab4e-9be7f2bbb645', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'52c004e2-bee1-4809-9376-a4abe265fd10', CAST(2.00 AS Numeric(38, 2)), N'Innovation', N'Originality and creativity of the solution', CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'bdcf83d8-3ca7-4d81-8fe0-bcb800d602b0', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'7961cef8-d8b9-403e-b25c-15e8ef495b52', CAST(2.00 AS Numeric(38, 2)), N'Tech', NULL, CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'8e0af91a-62e8-4b36-97c9-caa0eeee6cb6', N'02a4a30d-1474-480b-aaf3-65cadfc90b96', N'e869aa38-9be3-400e-b24f-ade00070d667', CAST(0.30 AS Numeric(38, 2)), N'Innovation & Creativity', N'Evaluates creativity, originality, feasibility, and differentiation from existing solutions.', CAST(10.00 AS Numeric(38, 2)), 2)
INSERT [dbo].[RoundCriteria] ([RoundCriterionID], [RoundID], [EventCriterionID], [Weight], [criterionName], [description], [MaxScore], [SortOrder]) VALUES (N'9c004b17-118f-45e9-8feb-d251091d193d', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'7961cef8-d8b9-403e-b25c-15e8ef495b52', CAST(2.00 AS Numeric(38, 2)), N'Tech', NULL, CAST(10.00 AS Numeric(38, 2)), 2)
GO
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'd3000000-0000-0000-0000-000000000001', N'd1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000002', CAST(N'2026-06-09T08:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'd3000000-0000-0000-0000-000000000002', N'd1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000003', CAST(N'2026-06-09T08:05:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'd3000000-0000-0000-0000-000000000003', N'd1000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000002', CAST(N'2026-06-12T08:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'd3000000-0000-0000-0000-000000000004', N'd1000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000003', CAST(N'2026-06-12T08:05:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'd3000000-0000-0000-0000-000000000005', N'd1000000-0000-0000-0000-000000000003', N'a1000000-0000-0000-0000-000000000002', CAST(N'2026-06-09T08:10:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'd3000000-0000-0000-0000-000000000006', N'd1000000-0000-0000-0000-000000000004', N'a1000000-0000-0000-0000-000000000017', CAST(N'2025-10-09T08:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'9cdfc061-a2d7-418e-a5c2-1c836e297a25', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'a1000000-0000-0000-0000-000000000002', CAST(N'2026-07-09T15:20:29.1141644' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'41facb78-16e4-4c97-b2cd-2e4c18934fb4', N'951e45f4-fe2e-4f59-aacd-8bff725b1a32', N'8c5c3287-cd14-4186-9180-340d0387fc3a', CAST(N'2026-07-09T01:49:37.5626037' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'e19291f2-2e33-4f4e-bef5-31b9cec51abd', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'a0000000-0000-0000-0000-222222222222', CAST(N'2026-07-09T13:39:00.8431524' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'bec09942-7d67-4663-a36f-32f91c8ac3c1', N'd1000000-0000-0000-0000-000000000003', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-09T15:12:11.2561915' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'f798d0e7-db77-4e79-8356-3a980381f8a1', N'ee34e149-474d-4e1e-bff6-098dff4edf2d', N'a1000000-0000-0000-0000-000000000003', CAST(N'2026-07-13T19:41:37.9430220' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'7d5f9a38-497d-418a-ab4e-3e15dfa4c7a7', N'02a4a30d-1474-480b-aaf3-65cadfc90b96', N'8c5c3287-cd14-4186-9180-340d0387fc3a', CAST(N'2026-07-14T00:33:10.6915387' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'be1e9046-f3b0-451b-ad18-46a05a2e0d91', N'd1000000-0000-0000-0000-000000000001', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-08T23:47:55.4904993' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'dad1ef6a-8ef8-495f-925c-59df49fd7790', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'9a9ef0d0-2bb3-4ed2-8e61-5dd814f4b20d', CAST(N'2026-07-09T16:16:36.9992747' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'e47e067c-3dbf-4405-9416-5c8e51e2e0d5', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-01T09:56:41.5833333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'fac2e75c-713a-46ee-b5bf-5e4e89c812b8', N'41e246b2-534c-42ee-a884-9ef7df2913ab', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-14T04:45:52.2980378' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'754fe6c3-b039-4bf6-8178-6f5f1af17024', N'd1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000017', CAST(N'2026-07-08T23:46:58.3959895' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'681ead49-9f53-4716-8e2f-87da7d1ed0be', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-06T10:16:40.7933333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'e0ed1d17-ddca-4fae-adb0-8884347d654a', N'd1000000-0000-0000-0000-000000000001', N'89abab57-98ab-4927-9a7f-9cdae0d8434f', CAST(N'2026-07-08T23:48:11.1120715' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'24d2b358-7a13-4db0-bdb3-aca5a15cab8c', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-09T14:06:23.6864045' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'4abf3236-5fb1-45bf-8534-b825b4e0f1b3', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'a30e3b73-7e3d-4453-84d7-baf8d98b198a', N'd1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000020', CAST(N'2026-07-08T23:47:45.1789432' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'02ca1ceb-4f0f-4eeb-9966-d3e5ad3c4e12', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'a0000000-0000-0000-0000-222222222222', CAST(N'2026-07-09T14:06:23.6864045' AS DateTime2), N'a0000000-0000-0000-0000-000000000000', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'e9bbc728-507a-4a0e-85ab-da2fac9e5fa3', N'd1000000-0000-0000-0000-000000000003', N'a1000000-0000-0000-0000-000000000003', CAST(N'2026-07-09T00:01:38.0169624' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'04c7c468-2dae-4095-8f87-db21df83f87c', N'02a4a30d-1474-480b-aaf3-65cadfc90b96', N'a1000000-0000-0000-0000-000000000003', CAST(N'2026-07-14T00:13:16.7530601' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 0)
INSERT [dbo].[RoundJudges] ([RoundJudgeID], [RoundID], [UserID], [AssignedAt], [AssignedByID], [IsActive]) VALUES (N'923ebc26-832f-40d8-b23b-ff3b8fc25990', N'd1000000-0000-0000-0000-000000000004', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', CAST(N'2026-07-13T19:30:38.3348512' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', 1)
GO
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'f3000000-0000-0000-0000-000000000001', N'd1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000001', N'f1000000-0000-0000-0000-000000000001', CAST(51.5000 AS Decimal(10, 4)), CAST(8.5833 AS Decimal(10, 4)), 1, 1, CAST(N'2026-06-12T12:00:00.0000000' AS DateTime2), 1)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'f3000000-0000-0000-0000-000000000002', N'd1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000002', N'f1000000-0000-0000-0000-000000000002', CAST(44.5000 AS Decimal(10, 4)), CAST(7.4167 AS Decimal(10, 4)), 2, 0, CAST(N'2026-06-12T12:00:00.0000000' AS DateTime2), 1)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'f3000000-0000-0000-0000-000000000003', N'd1000000-0000-0000-0000-000000000004', N'c1000000-0000-0000-0000-000000000003', N'e1000000-0000-0000-0000-000000000004', N'f1000000-0000-0000-0000-000000000005', CAST(9.3700 AS Decimal(10, 4)), CAST(3.1233 AS Decimal(10, 4)), 1, 1, CAST(N'2025-10-12T12:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'9bf31ab0-81e9-411e-86d6-2d27abec8da9', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'97b3e382-2725-4202-b41e-ec637292a634', N'bab0ac21-0e0e-496a-af83-576b30867164', N'391cb64b-6ff7-4471-b76a-973ade51874f', CAST(65.0000 AS Decimal(10, 4)), CAST(16.2500 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-10T18:46:57.7780263' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'c15c8e33-34a5-4322-9f6f-2f45e718bdc8', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'24b1a01c-1b20-4c15-ba8c-fbc4ad9272b1', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 3, 1, CAST(N'2026-07-04T01:23:02.1307819' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'03c29aee-db8c-411c-afaa-3d008982b199', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'4fbbe238-1f5c-4579-8be8-cdc6ffe052e2', CAST(29.0000 AS Decimal(10, 4)), CAST(14.5000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:23:02.0438249' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'b5a915ae-2064-43b0-84c1-4f8711581ce2', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', CAST(30.0000 AS Decimal(10, 4)), CAST(15.0000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:57:54.5483064' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'5f163a97-0cc5-474a-92ca-6f7c8326f163', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', CAST(30.0000 AS Decimal(10, 4)), CAST(15.0000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:57:48.3386485' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'44ef0efd-237e-46ce-bbb7-974b9090c029', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'5d22eea8-c3dc-49b0-adda-b42b348d8f3e', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:22:53.8577132' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'fe2f8d5f-6574-4513-bd82-a8dc9fdbfa60', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'433899b8-250d-4ef6-884a-deef5415679d', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:57:48.4142883' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'3be86ffb-03b6-4673-9ad3-ae7b91d8ab81', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'8f58b7c2-aeff-4364-8bbc-9424049b5bc1', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:22:45.4661581' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'875fa8d7-5c56-4a60-81bb-b1d60bb12c80', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'433899b8-250d-4ef6-884a-deef5415679d', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:57:54.6155031' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'30e77109-beb1-4007-a528-b286e889e10e', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'8f58b7c2-aeff-4364-8bbc-9424049b5bc1', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:57:48.3770659' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'0d8d8a4c-5fc1-4a58-ad6d-b5afb1b25a06', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'2c4c90b3-6715-454f-aace-081e47883c73', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:22:53.7081739' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'882987a1-9359-4c35-a3c7-c19556c2dbef', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'2cb52977-79a8-4692-8b41-71e0a8fcd17a', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:22:53.8021153' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'90fdf2cb-c0a9-46a6-a07f-d3d8c7dc7bd1', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'433899b8-250d-4ef6-884a-deef5415679d', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:22:45.6096977' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'6175b45f-26cc-4376-b444-ed158b433662', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'8f58b7c2-aeff-4364-8bbc-9424049b5bc1', CAST(0.0000 AS Decimal(10, 4)), CAST(0.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:57:54.5818230' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'fb5fc6ac-01ea-4404-93f2-f319d60df96b', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', CAST(30.0000 AS Decimal(10, 4)), CAST(15.0000 AS Decimal(10, 4)), 1, 1, CAST(N'2026-07-04T01:22:45.3502234' AS DateTime2), 0)
INSERT [dbo].[RoundRankings] ([RankingID], [RoundID], [CategoryID], [TeamID], [SubmissionID], [TotalScore], [AverageScore], [RankPosition], [IsAdvanced], [ComputedAt], [IsPublished]) VALUES (N'a9a8826e-1b54-4b63-977f-f69047601dca', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'732cb879-0c63-4a36-864e-96a833c243b8', CAST(28.0000 AS Decimal(10, 4)), CAST(14.0000 AS Decimal(10, 4)), 2, 1, CAST(N'2026-07-04T01:23:02.0962972' AS DateTime2), 0)
GO
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'd1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'AI Qualifier', 1, N'40000000-0000-0000-0000-000000000004', CAST(N'2026-06-11T23:59:59.0000000' AS DateTime2), CAST(N'2026-06-12T17:00:00.0000000' AS DateTime2), CAST(N'2026-06-10T08:00:00.0000000' AS DateTime2), CAST(N'2026-06-12T18:00:00.0000000' AS DateTime2), 1, 0, N'Completed qualifier with scores and ranking.')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'd1000000-0000-0000-0000-000000000002', N'c1000000-0000-0000-0000-000000000001', N'AI Final', 2, N'40000000-0000-0000-0000-000000000003', CAST(N'2026-06-18T23:59:59.0000000' AS DateTime2), CAST(N'2026-06-20T17:00:00.0000000' AS DateTime2), CAST(N'2026-06-13T08:00:00.0000000' AS DateTime2), CAST(N'2026-06-20T18:00:00.0000000' AS DateTime2), 1, 0, N'Final round ready for judging API tests.')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'd1000000-0000-0000-0000-000000000003', N'c1000000-0000-0000-0000-000000000002', N'Green Qualifier', 1, N'40000000-0000-0000-0000-000000000002', CAST(N'2026-06-17T23:59:59.0000000' AS DateTime2), CAST(N'2026-06-18T17:00:00.0000000' AS DateTime2), CAST(N'2026-06-10T08:00:00.0000000' AS DateTime2), CAST(N'2026-06-18T18:00:00.0000000' AS DateTime2), 1, 0, N'Submission and disqualification test round.')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'd1000000-0000-0000-0000-000000000004', N'c1000000-0000-0000-0000-000000000003', N'Showcase Final', 1, N'40000000-0000-0000-0000-000000000004', CAST(N'2025-10-11T23:59:59.0000000' AS DateTime2), CAST(N'2025-10-12T17:00:00.0000000' AS DateTime2), CAST(N'2025-10-10T08:00:00.0000000' AS DateTime2), CAST(N'2025-10-12T18:00:00.0000000' AS DateTime2), 1, 0, N'Completed final for hall-of-fame tests.')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'97b3e382-2725-4202-b41e-ec637292a634', N'Calibrate Round', 1, N'40000000-0000-0000-0000-000000000003', CAST(N'2026-07-09T15:00:00.0000000' AS DateTime2), CAST(N'2026-07-09T18:00:00.0000000' AS DateTime2), CAST(N'2026-07-09T17:15:00.0000000' AS DateTime2), CAST(N'2026-07-06T20:15:00.0000000' AS DateTime2), 1, 0, N'For Judger to manage their judge status compare to the other')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'ee34e149-474d-4e1e-bff6-098dff4edf2d', N'c1000000-0000-0000-0000-000000000003', N'Round 1', 2, N'40000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, 0, N'')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Final Round', 3, N'40000000-0000-0000-0000-000000000004', NULL, NULL, NULL, NULL, 3, 0, NULL)
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'3e0198c9-abc8-483f-adf8-1e8b9c523dc5', N'6b8cd8f6-d267-49a5-936d-5ac6411d6c56', N'Semi-Final', 1, N'40000000-0000-0000-0000-000000000001', CAST(N'2026-07-10T21:36:00.0000000' AS DateTime2), CAST(N'2026-07-13T21:36:00.0000000' AS DateTime2), CAST(N'2026-07-09T21:36:00.0000000' AS DateTime2), CAST(N'2026-07-09T21:36:00.0000000' AS DateTime2), NULL, 0, N'Solution Presentation')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'cba49c54-ca15-47c9-aa74-3b2154056e6a', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'Testing', 1, N'40000000-0000-0000-0000-000000000002', CAST(N'2026-07-04T17:55:00.0000000' AS DateTime2), CAST(N'2026-07-05T12:55:00.0000000' AS DateTime2), CAST(N'2026-07-04T15:54:00.0000000' AS DateTime2), CAST(N'2026-07-04T15:54:00.0000000' AS DateTime2), 10, 0, N'')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'dcf2cbd3-4a73-4395-bd4e-558d1aac4bf0', N'74d82808-67b9-4a10-b5d1-14222bf8cb1e', N'Calibration Round', 1, N'40000000-0000-0000-0000-000000000001', CAST(N'2026-07-07T08:24:00.0000000' AS DateTime2), CAST(N'2026-07-10T08:25:00.0000000' AS DateTime2), CAST(N'2026-07-02T08:24:00.0000000' AS DateTime2), CAST(N'2026-07-02T08:24:00.0000000' AS DateTime2), 10, 1, N'')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'02a4a30d-1474-480b-aaf3-65cadfc90b96', N'86a35a9c-cd51-4819-8569-e3868f4e3353', N'Round 1', 1, N'40000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, 0, N'')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Preliminary Round', 1, N'40000000-0000-0000-0000-000000000004', NULL, NULL, NULL, NULL, 10, 0, NULL)
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'7a2de745-0f84-407c-9541-835aaf2936ee', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Semi-Final Round', 2, N'40000000-0000-0000-0000-000000000004', NULL, NULL, NULL, NULL, 5, 0, NULL)
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'951e45f4-fe2e-4f59-aacd-8bff725b1a32', N'95e6a07c-4524-4f48-98b8-0bb6f7d26546', N'Round 1', 1, N'40000000-0000-0000-0000-000000000001', CAST(N'2026-07-12T01:49:00.0000000' AS DateTime2), CAST(N'2026-07-15T01:49:00.0000000' AS DateTime2), CAST(N'2026-07-09T01:48:00.0000000' AS DateTime2), CAST(N'2026-07-09T01:48:00.0000000' AS DateTime2), NULL, 0, N'Idea Presentation')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'41e246b2-534c-42ee-a884-9ef7df2913ab', N'93084f05-fbf5-4e87-83c9-995cc3e8d57a', N'Round 1', 1, N'40000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, 5, 0, N'')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'0a985a32-7f8d-44e4-9f3d-da5d8bfeff41', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'Calibration Round', 3, N'40000000-0000-0000-0000-000000000001', CAST(N'2026-07-13T13:55:00.0000000' AS DateTime2), CAST(N'2026-07-14T12:00:00.0000000' AS DateTime2), CAST(N'2026-07-12T13:55:00.0000000' AS DateTime2), CAST(N'2026-07-12T13:55:00.0000000' AS DateTime2), NULL, 1, N'Round hi?u chu?n')
INSERT [dbo].[Rounds] ([RoundID], [CategoryID], [RoundName], [RoundOrder], [RoundStatusID], [SubmissionDeadline], [JudgingDeadline], [StartDate], [EndDate], [AdvancementTopN], [IsCalibrationRound], [Description]) VALUES (N'13dbacec-771a-4e2e-97d4-e8073a360fe6', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'Round 1', 2, N'40000000-0000-0000-0000-000000000001', CAST(N'2026-07-10T21:30:00.0000000' AS DateTime2), CAST(N'2026-07-11T21:30:00.0000000' AS DateTime2), CAST(N'2026-07-08T21:29:00.0000000' AS DateTime2), CAST(N'2026-07-08T21:29:00.0000000' AS DateTime2), 5, 0, N'')
GO
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000004', N'Completed')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000003', N'Judging')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000002', N'Submission Open')
INSERT [dbo].[RoundStatus] ([StatusID], [StatusName]) VALUES (N'40000000-0000-0000-0000-000000000001', N'Upcoming')
GO
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'f1000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000001', N'd1000000-0000-0000-0000-000000000001', N'50000000-0000-0000-0000-000000000002', N'https://github.com/example/api-alpha', N'https://alpha.example.test', N'https://example.test/reports/alpha.pdf', N'https://example.test/slides/alpha', N'{"language":"Java","branch":"main"}', CAST(N'2026-06-11T21:00:00.0000000' AS DateTime2), 12, 3, CAST(N'2026-06-11T21:30:00.0000000' AS DateTime2), CAST(N'2026-06-12T08:00:00.0000000' AS DateTime2), N'a1000000-0000-0000-0000-000000000010', N'Ready for judging.', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'f1000000-0000-0000-0000-000000000002', N'e1000000-0000-0000-0000-000000000002', N'd1000000-0000-0000-0000-000000000001', N'50000000-0000-0000-0000-000000000002', N'https://github.com/example/api-beta', N'https://beta.example.test', N'https://example.test/reports/beta.pdf', N'https://example.test/slides/beta', N'{"language":"TypeScript","branch":"main"}', CAST(N'2026-06-11T20:00:00.0000000' AS DateTime2), 8, 2, CAST(N'2026-06-11T20:30:00.0000000' AS DateTime2), CAST(N'2026-06-12T08:00:00.0000000' AS DateTime2), N'a1000000-0000-0000-0000-000000000012', N'Ready for judging.', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'f1000000-0000-0000-0000-000000000003', N'e1000000-0000-0000-0000-000000000001', N'd1000000-0000-0000-0000-000000000002', N'50000000-0000-0000-0000-000000000002', N'https://github.com/example/api-alpha', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-06-12T10:00:00.0000000' AS DateTime2), N'a1000000-0000-0000-0000-000000000010', N'Draft final submission.', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'f1000000-0000-0000-0000-000000000004', N'e1000000-0000-0000-0000-000000000003', N'd1000000-0000-0000-0000-000000000003', N'50000000-0000-0000-0000-000000000004', N'https://github.com/example/api-green', NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-06-11T19:00:00.0000000' AS DateTime2), CAST(N'2026-06-12T09:00:00.0000000' AS DateTime2), N'a1000000-0000-0000-0000-000000000014', N'Disqualified sample submission.', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'f1000000-0000-0000-0000-000000000005', N'e1000000-0000-0000-0000-000000000004', N'd1000000-0000-0000-0000-000000000004', N'50000000-0000-0000-0000-000000000002', N'https://github.com/example/api-legacy-winners', N'https://legacy.example.test', N'https://example.test/reports/legacy.pdf', N'https://example.test/slides/legacy', NULL, NULL, NULL, NULL, CAST(N'2025-10-11T20:00:00.0000000' AS DateTime2), CAST(N'2025-10-12T08:00:00.0000000' AS DateTime2), N'a1000000-0000-0000-0000-000000000010', N'Winning submission.', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'2c4c90b3-6715-454f-aace-081e47883c73', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'50000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-01T09:56:41.5833333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Gamma Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'1cbe312c-2ecb-474a-b251-2f8b470b1ccc', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'50000000-0000-0000-0000-000000000005', N'https://github.com/FPT-SEAL-Hackathon', N'jolly-tree-05b5e1d00.7.azurestaticapps.net', N'https://docs.google.com/document/d/1xVTPl1sHdRlL0wakq-ygA8t0my7LgxFpwppYZg6QFAo/edit?usp=sharing', NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-02T08:10:00.2600000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Beta Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'2cb52977-79a8-4692-8b41-71e0a8fcd17a', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'50000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-01T09:56:41.5833333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Beta Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'2b708319-e446-44be-8831-75be637591a0', N'31f337e9-dc45-46bf-b6c5-280f93b482ca', N'cba49c54-ca15-47c9-aa74-3b2154056e6a', N'50000000-0000-0000-0000-000000000002', N'https://github.com/FPT-SEAL-Hackathon/SEAL-Hackathon-BE', N'http://localhost:5173/student/submissions', N'https://canva.link/0ereyalwx3n3pxx', N'https://canva.link/0ereyalwx3n3pxx', NULL, NULL, NULL, NULL, CAST(N'2026-07-04T10:02:37.4100000' AS DateTime2), CAST(N'2026-07-04T10:02:37.4100000' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', N'Nộp bài', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'8f58b7c2-aeff-4364-8bbc-9424049b5bc1', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'50000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-01T09:56:41.5833333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Alpha Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'732cb879-0c63-4a36-864e-96a833c243b8', N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'50000000-0000-0000-0000-000000000005', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-02T07:47:06.4566667' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Beta Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'391cb64b-6ff7-4471-b76a-973ade51874f', N'bab0ac21-0e0e-496a-af83-576b30867164', N'bef0cb72-a18b-4b1e-aeec-08cd09a6da77', N'50000000-0000-0000-0000-000000000005', N'https://github.com/FPT-SEAL-Hackathon', N'https://jolly-tree-05b5e1d00.7.azurestaticapps.net', N'https://docs.google.com/document/d/1xVTPl1sHdRlL0wakq-ygA8t0my7LgxFpwppYZg6QFAo/edit?tab=t.0', N'https://www.canva.com/design/DAHMXq4DPv0/bub78e5PybbpJ9zAYgkHYQ/edit', NULL, NULL, NULL, NULL, CAST(N'2026-07-09T08:36:26.3800000' AS DateTime2), CAST(N'2026-07-09T08:36:26.3800000' AS DateTime2), N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'Seal Hackathon', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'5d22eea8-c3dc-49b0-adda-b42b348d8f3e', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'7a2de745-0f84-407c-9541-835aaf2936ee', N'50000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-01T09:56:41.5833333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Alpha Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'4fbbe238-1f5c-4579-8be8-cdc6ffe052e2', N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'50000000-0000-0000-0000-000000000005', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-02T08:16:21.6933333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Alpha Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'433899b8-250d-4ef6-884a-deef5415679d', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'b5f4b69a-8c67-4783-b4ff-6ec02e4f2b9d', N'50000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-01T09:56:41.5833333' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Gamma Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'24b1a01c-1b20-4c15-ba8c-fbc4ad9272b1', N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'fc103b70-c4df-41a1-9ef1-18f83876b6c9', N'50000000-0000-0000-0000-000000000001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Gamma Submission Notes', 0, 0)
INSERT [dbo].[Submissions] ([SubmissionID], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL], [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt], [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission]) VALUES (N'9584fd1f-de26-4e53-8689-ffed1b25c8ec', NULL, N'0a985a32-7f8d-44e4-9f3d-da5d8bfeff41', N'50000000-0000-0000-0000-000000000002', N'https://github.com/FPT-SEAL-Hackathon/SEAL-Hackathon-BE', N'http://localhost:5173/organizer/event-detail', N'https://canva.link/0ereyalwx3n3pxx', N'https://canva.link/0ereyalwx3n3pxx', NULL, NULL, NULL, NULL, CAST(N'2026-07-12T14:35:04.9235687' AS DateTime2), CAST(N'2026-07-12T14:35:04.9235687' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'Test', 0, 1)
GO
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000004', N'Disqualified')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000001', N'Draft')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000006', N'In Progress')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000005', N'Scored')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000002', N'Submitted')
INSERT [dbo].[SubmissionStatus] ([StatusID], [StatusName]) VALUES (N'50000000-0000-0000-0000-000000000003', N'Under Review')
GO
INSERT INTO [dbo].[SubmissionHistory]
    ([SubmissionID], [VersionNumber], [TeamID], [RoundID], [SubmissionStatusID], [RepositoryURL], [DemoURL], [ReportURL], [SlideURL],
     [RepoMetadataJSON], [RepoLastCommitAt], [RepoStarCount], [RepoForkCount], [SubmittedAt], [LastUpdatedAt],
     [SubmittedByUserID], [Notes], [IsScoreApproved], [IsSampleSubmission], [SnapshotCreatedAt])
SELECT
    s.[SubmissionID], 1, s.[TeamID], s.[RoundID], s.[SubmissionStatusID], s.[RepositoryURL], s.[DemoURL], s.[ReportURL], s.[SlideURL],
    s.[RepoMetadataJSON], s.[RepoLastCommitAt], s.[RepoStarCount], s.[RepoForkCount], s.[SubmittedAt], s.[LastUpdatedAt],
    s.[SubmittedByUserID], s.[Notes], s.[IsScoreApproved], s.[IsSampleSubmission], GETUTCDATE()
FROM [dbo].[Submissions] s
GO
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'allowLateSubmissions', N'true', N'BOOLEAN', N'Allow submissions after deadline', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'contactEmail', N'seal@fpt.edu.vn', N'STRING', N'Platform support contact email', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'enablePublicLeaderboard', N'true', N'BOOLEAN', N'Show leaderboard to public', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'maxTeamSize', N'5', N'INTEGER', N'Maximum allowed team members', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'minTeamSize', N'2', N'INTEGER', N'Minimum required team members', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'platformName', N'SEAL FPT Hackathon Platform', N'STRING', N'Display name of the platform', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'requireEmailVerification', N'true', N'BOOLEAN', N'Require email verification on register', NULL)
INSERT [dbo].[SystemSettings] ([SettingKey], [SettingValue], [SettingType], [Description], [UpdatedAt]) VALUES (N'submissionGracePeriod', N'30', N'INTEGER', N'Grace period in minutes after deadline', NULL)
GO
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'e3000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000016', N'PENDING', CAST(N'2026-06-11T10:00:00.0000000' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'7fe9effb-3719-4525-a9c0-001935ad3ee7', N'fecd3712-159c-4073-9c34-e06603b10382', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'APPROVED', CAST(N'2026-07-14T20:34:35.4087855' AS DateTime2), CAST(N'2026-07-14T20:34:53.5700799' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'25c6e2aa-d527-4687-b6a0-021df26a8fda', N'fecd3712-159c-4073-9c34-e06603b10382', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'APPROVED', CAST(N'2026-07-14T19:00:42.8984401' AS DateTime2), CAST(N'2026-07-14T19:00:59.2727280' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'660a14b1-3504-4c65-a143-111185d9c8a6', N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'REJECTED', CAST(N'2026-07-04T15:37:19.0603363' AS DateTime2), CAST(N'2026-07-04T15:46:12.9743850' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'2dfb2b00-c925-482c-95bd-16b36f04813d', N'9cd13168-5e37-4819-aefb-1e8fa6be80ba', N'a1000000-0000-0000-0000-000000000015', N'PENDING', CAST(N'2026-07-14T17:01:08.1832217' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'bcf45840-6f03-455e-bc3f-2acd704f1f03', N'bab0ac21-0e0e-496a-af83-576b30867164', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'APPROVED', CAST(N'2026-07-04T02:37:47.4432560' AS DateTime2), CAST(N'2026-07-04T02:38:30.3448226' AS DateTime2), N'5181ec59-afee-46ee-95d1-16cde3a8819c', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'2cedc8d1-16e2-4cd1-86ba-2c2919527b39', N'9c48c09c-5339-4bfd-a0bf-32e934acf8f2', N'5d565896-2307-4911-939d-94e11e4b1c44', N'PENDING', CAST(N'2026-06-27T12:49:27.0730014' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'204143db-c297-489f-b7be-415f5fd9f78a', N'bab0ac21-0e0e-496a-af83-576b30867164', N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', N'APPROVED', CAST(N'2026-07-04T02:38:11.0978145' AS DateTime2), CAST(N'2026-07-04T02:38:31.7959401' AS DateTime2), N'5181ec59-afee-46ee-95d1-16cde3a8819c', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'19b47fd6-703e-4712-8b98-4954cd182cdd', N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'APPROVED', CAST(N'2026-07-04T15:24:17.6645849' AS DateTime2), CAST(N'2026-07-04T15:24:36.2333346' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'55942498-de0b-4fa2-a9c5-98faaf068993', N'9c48c09c-5339-4bfd-a0bf-32e934acf8f2', N'9e789580-0b06-4ae3-8862-1b0694c05ff6', N'PENDING', CAST(N'2026-06-26T09:48:24.1733333' AS DateTime2), NULL, NULL, NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'ca357992-9bf1-46e3-beac-a588c0ef9cd5', N'31f337e9-dc45-46bf-b6c5-280f93b482ca', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'APPROVED', CAST(N'2026-07-04T15:57:15.5331771' AS DateTime2), CAST(N'2026-07-04T15:57:40.4209954' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'0b6a1ef3-ee05-48ac-a786-b3a9883bf988', N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'APPROVED', CAST(N'2026-07-04T11:35:23.4675602' AS DateTime2), CAST(N'2026-07-04T11:35:34.8019518' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'4d3e8286-de35-4982-9e74-b7e6bbba062e', N'9ae433ec-b102-4a88-985a-11251de7b591', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'REJECTED', CAST(N'2026-06-27T13:37:17.6517974' AS DateTime2), CAST(N'2026-07-04T11:11:13.0744434' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
INSERT [dbo].[TeamJoinRequests] ([RequestID], [TeamID], [UserID], [RequestStatus], [RequestedAt], [RespondedAt], [RespondedByID], [ResponseNote]) VALUES (N'6283f0a8-7a3b-4fbf-a7b6-eea942b5ab38', N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'APPROVED', CAST(N'2026-07-04T15:06:39.3048078' AS DateTime2), CAST(N'2026-07-04T15:23:52.7173154' AS DateTime2), N'5d565896-2307-4911-939d-94e11e4b1c44', NULL)
GO
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000001', N'e1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000010', CAST(N'2026-06-01T08:00:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000002', N'e1000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000011', CAST(N'2026-06-01T09:00:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000003', N'e1000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000012', CAST(N'2026-06-01T08:10:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000004', N'e1000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000013', CAST(N'2026-06-01T09:10:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000005', N'e1000000-0000-0000-0000-000000000003', N'a1000000-0000-0000-0000-000000000014', CAST(N'2026-06-01T08:20:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000006', N'e1000000-0000-0000-0000-000000000003', N'a1000000-0000-0000-0000-000000000015', CAST(N'2026-06-01T09:20:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000011', N'e1000000-0000-0000-0000-000000000004', N'a1000000-0000-0000-0000-000000000010', CAST(N'2025-09-15T08:00:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e2000000-0000-0000-0000-000000000012', N'e1000000-0000-0000-0000-000000000004', N'a1000000-0000-0000-0000-000000000012', CAST(N'2025-09-15T09:00:00.0000000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e7d6fd20-df1d-4f51-b88a-0374358dc39a', N'bab0ac21-0e0e-496a-af83-576b30867164', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', CAST(N'2026-07-04T02:38:30.1725069' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'35a25a82-b7e3-44fd-ac81-0fd536d74942', N'fecd3712-159c-4073-9c34-e06603b10382', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-07-10T13:59:20.8081165' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'8d70b123-9077-4a32-898f-25d8d1ab23c4', N'9c48c09c-5339-4bfd-a0bf-32e934acf8f2', N'0e9aeffe-0d45-4b52-b38f-4d6d125a81c8', CAST(N'2026-06-26T09:48:24.1700000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'86ced066-89e5-4af8-bd01-4f1fafb9c5c2', N'bab0ac21-0e0e-496a-af83-576b30867164', N'5181ec59-afee-46ee-95d1-16cde3a8819c', CAST(N'2026-07-04T02:37:17.0307334' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'eb65c7af-2ce9-4232-b052-6205d7a83e6f', N'9c48c09c-5339-4bfd-a0bf-32e934acf8f2', N'8d288ab9-b557-4d9c-aeea-4477ed168395', CAST(N'2026-06-26T09:48:24.1700000' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'92cacb80-f035-4c2b-8a83-691f19dc52da', N'fecd3712-159c-4073-9c34-e06603b10382', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', CAST(N'2026-07-14T20:34:53.4675880' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'a3d8969d-8d55-4c00-8456-6b4b95362a6c', N'31f337e9-dc45-46bf-b6c5-280f93b482ca', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-07-04T15:53:58.9036257' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'b4d79990-c289-49b5-8f1a-6c47526175ee', N'bab0ac21-0e0e-496a-af83-576b30867164', N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', CAST(N'2026-07-04T02:38:31.6174333' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'8f188505-9407-4228-8ff9-822315e32a5b', N'3dfb2ba7-77a3-4608-bd4d-cc49b619bf54', N'a6fec830-bca4-4cab-a3d6-de094ae67034', CAST(N'2026-06-26T09:48:24.1866667' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'2bd203e6-3c7c-4677-9692-834d725206cf', N'31f337e9-dc45-46bf-b6c5-280f93b482ca', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', CAST(N'2026-07-04T15:57:40.1328730' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'e7ba1921-9680-4817-a96a-9fe0358e7fd5', N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', CAST(N'2026-07-04T15:24:36.1380393' AS DateTime2), CAST(N'2026-07-04T15:26:51.6557016' AS DateTime2), 0)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'9b4db67d-32af-497c-a1a9-c4847d59af26', N'e559ea1a-5b37-4710-a091-f49f4d4876a0', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', CAST(N'2026-07-14T16:47:21.1173127' AS DateTime2), CAST(N'2026-07-14T18:55:08.4523759' AS DateTime2), 0)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'b3582571-43d6-4b1e-a588-c99c89c75348', N'9cd13168-5e37-4819-aefb-1e8fa6be80ba', N'a1000000-0000-0000-0000-000000000013', CAST(N'2026-07-14T16:53:46.7947598' AS DateTime2), NULL, 1)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'ef40d618-d511-4648-afcc-cd23a7888a3f', N'9ae433ec-b102-4a88-985a-11251de7b591', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-06-27T12:50:10.3007400' AS DateTime2), CAST(N'2026-07-04T11:13:21.5848272' AS DateTime2), 0)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'456cf4df-04c8-44cd-925b-efda6e263e95', N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-07-04T11:32:25.8079108' AS DateTime2), CAST(N'2026-07-04T15:46:14.6111780' AS DateTime2), 0)
INSERT [dbo].[TeamMembers] ([TeamMemberID], [TeamID], [UserID], [JoinedAt], [LeftAt], [IsActive]) VALUES (N'36b2e8be-0759-494b-8713-f557a391f8e4', N'9c48c09c-5339-4bfd-a0bf-32e934acf8f2', N'0cd69fe9-1d0e-4094-b607-50d5f122f0f1', CAST(N'2026-06-26T09:48:24.1700000' AS DateTime2), NULL, 1)
GO
INSERT [dbo].[TeamMilestones] ([MilestoneID], [TeamID], [MentorUserID], [Label], [IsDone], [SortOrder], [CreatedAt], [UpdatedAt]) VALUES (N'7c0020bc-9ebf-4cd3-9964-6365c55db095', N'98c856ef-f5d0-41ce-bab8-6e3747c58da4', N'a0000000-0000-0000-0000-111111111111', N'Demo milestone', 0, 0, CAST(N'2026-07-08T13:35:32.2522212' AS DateTime2), CAST(N'2026-07-08T13:35:38.4130229' AS DateTime2))
GO
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'e1000000-0000-0000-0000-000000000001', N'b1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'API Alpha', N'60000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000010', CAST(N'2026-06-01T08:00:00.0000000' AS DateTime2), CAST(N'2026-06-10T08:00:00.0000000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'e1000000-0000-0000-0000-000000000002', N'b1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000001', N'API Beta', N'60000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000012', CAST(N'2026-06-01T08:10:00.0000000' AS DateTime2), CAST(N'2026-06-10T08:10:00.0000000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'e1000000-0000-0000-0000-000000000003', N'b1000000-0000-0000-0000-000000000001', N'c1000000-0000-0000-0000-000000000002', N'API Green', N'60000000-0000-0000-0000-000000000003', N'a1000000-0000-0000-0000-000000000014', CAST(N'2026-06-01T08:20:00.0000000' AS DateTime2), CAST(N'2026-06-12T08:20:00.0000000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'e1000000-0000-0000-0000-000000000004', N'b1000000-0000-0000-0000-000000000002', N'c1000000-0000-0000-0000-000000000003', N'API Legacy Winners', N'60000000-0000-0000-0000-000000000002', N'a1000000-0000-0000-0000-000000000010', CAST(N'2025-09-15T08:00:00.0000000' AS DateTime2), CAST(N'2025-10-13T08:00:00.0000000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'd0000000-0000-0000-0000-111111111111', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ca000000-0000-0000-0000-111111111111', N'Data Ninjas', N'60000000-0000-0000-0000-000000000002', N'a0000000-0000-0000-0000-333333333333', CAST(N'2026-07-01T21:40:12.3133333' AS DateTime2), CAST(N'2026-07-01T21:40:12.3133333' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'9ae433ec-b102-4a88-985a-11251de7b591', N'f1230799-7a9e-4325-a999-f49719c5752b', N'91c1abd4-836f-4930-87f4-1d00c9cd74b4', N'TOP1VN', N'60000000-0000-0000-0000-000000000004', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-06-27T12:50:10.3007400' AS DateTime2), CAST(N'2026-07-04T11:13:21.5848272' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'9cd13168-5e37-4819-aefb-1e8fa6be80ba', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Simple', N'60000000-0000-0000-0000-000000000001', N'a1000000-0000-0000-0000-000000000013', CAST(N'2026-07-14T16:53:46.7947598' AS DateTime2), CAST(N'2026-07-14T16:53:46.7947598' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'd0000000-0000-0000-0000-222222222222', N'f1230799-7a9e-4325-a999-f49719c5752b', N'ca000000-0000-0000-0000-222222222222', N'Web Wizards', N'60000000-0000-0000-0000-000000000002', N'a0000000-0000-0000-0000-444444444444', CAST(N'2026-07-01T21:40:12.3133333' AS DateTime2), CAST(N'2026-07-01T21:40:12.3133333' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'bac927f2-ce8a-4722-ac23-2797afb8754b', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'TeamVN', N'60000000-0000-0000-0000-000000000004', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-07-04T11:32:25.8079108' AS DateTime2), CAST(N'2026-07-04T15:46:14.6111780' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'31f337e9-dc45-46bf-b6c5-280f93b482ca', N'8fb898cb-dc68-4a6f-b88f-1b3f7305fe32', N'99f9d3d6-72e2-4cf6-a547-2c14229d2aa0', N'dfsdfdsf', N'60000000-0000-0000-0000-000000000002', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-07-04T15:53:58.9036257' AS DateTime2), CAST(N'2026-07-04T16:16:04.1810746' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'9c48c09c-5339-4bfd-a0bf-32e934acf8f2', N'f1230799-7a9e-4325-a999-f49719c5752b', N'91c1abd4-836f-4930-87f4-1d00c9cd74b4', N'SEAL Builders', N'60000000-0000-0000-0000-000000000002', N'8d288ab9-b557-4d9c-aeea-4477ed168395', CAST(N'2026-06-26T09:48:24.1633333' AS DateTime2), CAST(N'2026-06-26T09:48:24.1633333' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'bab0ac21-0e0e-496a-af83-576b30867164', N'1eb4241e-e471-4395-a61f-43b40fe75af6', N'97b3e382-2725-4202-b41e-ec637292a634', N'UIT1', N'60000000-0000-0000-0000-000000000002', N'5181ec59-afee-46ee-95d1-16cde3a8819c', CAST(N'2026-07-04T02:37:17.0307334' AS DateTime2), CAST(N'2026-07-08T14:11:01.5489964' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'98c856ef-f5d0-41ce-bab8-6e3747c58da4', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Team Vô Địch', N'60000000-0000-0000-0000-000000000002', N'a5ae98c3-201c-4772-967c-145af22af723', CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'6692795c-fe5f-46c2-b938-9b74a2c80668', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'5ca407bb-fb8a-4ef8-8719-ecc79f178306', N'Team Flutter', N'60000000-0000-0000-0000-000000000002', N'a5ae98c3-201c-4772-967c-145af22af723', CAST(N'2026-07-01T08:39:17.6000000' AS DateTime2), CAST(N'2026-07-01T08:39:17.6000000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'3f0a7058-35e3-4d4c-b0b8-9bedd031d52f', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Demo Team Beta', N'60000000-0000-0000-0000-000000000002', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2), CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'9db12c83-94fb-487f-8bcb-aee523a60d75', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Demo Team Gamma', N'60000000-0000-0000-0000-000000000002', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2), CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'df3a4514-398e-479c-8ddb-b32b0cded991', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'cf4bcc5d-aedd-4397-8387-0f62df1a2ae7', N'Team ReactJS', N'60000000-0000-0000-0000-000000000002', N'20fad3e3-87f2-48b3-91b8-d93f24d9302c', CAST(N'2026-07-01T08:39:17.5533333' AS DateTime2), CAST(N'2026-07-01T08:39:17.5533333' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'3dfb2ba7-77a3-4608-bd4d-cc49b619bf54', N'f1230799-7a9e-4325-a999-f49719c5752b', N'91c1abd4-836f-4930-87f4-1d00c9cd74b4', N'Disqualified Demo Team', N'60000000-0000-0000-0000-000000000003', N'a6fec830-bca4-4cab-a3d6-de094ae67034', CAST(N'2026-06-26T09:48:24.1800000' AS DateTime2), CAST(N'2026-06-26T09:48:24.1800000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'fecd3712-159c-4073-9c34-e06603b10382', N'b1000000-0000-0000-0000-000000000002', N'95e6a07c-4524-4f48-98b8-0bb6f7d26546', N'Top1VN', N'60000000-0000-0000-0000-000000000001', N'5d565896-2307-4911-939d-94e11e4b1c44', CAST(N'2026-07-10T13:59:20.8081165' AS DateTime2), CAST(N'2026-07-10T13:59:20.8081165' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'b2946362-9d7a-422b-ad34-e6f5ef22de7a', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Demo Team Alpha', N'60000000-0000-0000-0000-000000000002', N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2), CAST(N'2026-07-01T09:56:41.5800000' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'f8b4d5f2-55b0-4e1c-87f9-ec17d6a619c6', N'3fbbd982-a9f8-4dba-a577-1f77c368d213', N'02a6f740-a675-4b60-bdb1-0ba5a1b87fb0', N'Team Siêu Nhân', N'60000000-0000-0000-0000-000000000003', N'20fad3e3-87f2-48b3-91b8-d93f24d9302c', CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), CAST(N'2026-07-09T13:55:21.8575675' AS DateTime2))
INSERT [dbo].[Teams] ([TeamID], [EventID], [CategoryID], [TeamName], [TeamStatusID], [LeaderUserID], [CreatedAt], [UpdatedAt]) VALUES (N'e559ea1a-5b37-4710-a091-f49f4d4876a0', N'b1000000-0000-0000-0000-000000000002', N'95e6a07c-4524-4f48-98b8-0bb6f7d26546', N'Test', N'60000000-0000-0000-0000-000000000004', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', CAST(N'2026-07-14T16:47:21.1173127' AS DateTime2), CAST(N'2026-07-14T18:55:08.4523759' AS DateTime2))
GO
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000002', N'Active')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000003', N'Disqualified')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000001', N'Forming')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000004', N'Withdrawn')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000005', N'Pending')
INSERT [dbo].[TeamStatus] ([StatusID], [StatusName]) VALUES (N'60000000-0000-0000-0000-000000000006', N'Rejected')
GO
INSERT [dbo].[UserOAuthAccounts] ([OAuthAccountID], [UserID], [Provider], [ProviderUserID], [Email], [EmailVerified], [DisplayName], [AvatarUrl], [CreatedAt], [UpdatedAt]) VALUES (N'fa4520b9-0839-4eef-b11b-38435779c012', N'fa60133b-5874-4dbf-9299-a0069495b193', N'GOOGLE', N'103136036491599236908', N'xuantan071106@gmail.com', 1, N'Nguyễn Xuân Tân', N'https://lh3.googleusercontent.com/a/ACg8ocLfmXDM7fM706ZaJbUPwk5NqmbD3EoVld8aUZiWQlXOcRMP3g=s96-c', CAST(N'2026-07-08T14:28:44.0257229' AS DateTime2), CAST(N'2026-07-10T16:52:17.4490359' AS DateTime2))
INSERT [dbo].[UserOAuthAccounts] ([OAuthAccountID], [UserID], [Provider], [ProviderUserID], [Email], [EmailVerified], [DisplayName], [AvatarUrl], [CreatedAt], [UpdatedAt]) VALUES (N'808c3754-d025-494c-b566-64b1b04eee0e', N'ea969288-a77c-4683-b661-b10955f1df18', N'GOOGLE', N'102910810692374987520', N'tronghieu171026@gmail.com', 1, N'Hiếu Nguyễn Trọng', N'https://lh3.googleusercontent.com/a/ACg8ocLELUfG2rewWyzn-wkTNq2OBap8GKksY8ulRPwRFJPdwz0muOwU=s96-c', CAST(N'2026-07-04T16:09:39.9781434' AS DateTime2), CAST(N'2026-07-10T16:57:26.5696032' AS DateTime2))
INSERT [dbo].[UserOAuthAccounts] ([OAuthAccountID], [UserID], [Provider], [ProviderUserID], [Email], [EmailVerified], [DisplayName], [AvatarUrl], [CreatedAt], [UpdatedAt]) VALUES (N'38fb5a0d-fc4e-4a48-b095-7f201bf1f68a', N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'GOOGLE', N'117234139186319116919', N'ahihine03@gmail.com', 1, N'Xuantan Nguyen', N'https://lh3.googleusercontent.com/a/ACg8ocJs_TfgAHNU7pJQtxIJ9WkpZTiuZBnlH49_h54JBAVUXVhLOA=s96-c', CAST(N'2026-07-13T07:57:27.6299655' AS DateTime2), CAST(N'2026-07-14T10:03:43.6796162' AS DateTime2))
INSERT [dbo].[UserOAuthAccounts] ([OAuthAccountID], [UserID], [Provider], [ProviderUserID], [Email], [EmailVerified], [DisplayName], [AvatarUrl], [CreatedAt], [UpdatedAt]) VALUES (N'dde2c833-b601-4dc7-b765-a94d530e9d2b', N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', N'GOOGLE', N'112039555192203091077', N'hahachenal@gmail.com', 1, N'Nguyễn Xuân Tân', N'https://lh3.googleusercontent.com/a/ACg8ocKZGxUA9uYFQ7H9u0to9XhwKNGQUIHo9ir7p8RtOuJoxmiUg4UO=s96-c', CAST(N'2026-07-14T06:36:20.6515979' AS DateTime2), CAST(N'2026-07-14T12:59:38.7543504' AS DateTime2))
GO
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a0000000-0000-0000-0000-000000000000', N'organizer@seal.com', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Alice Organizer', NULL, N'10000000-0000-0000-0000-000000000003', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-01T21:40:12.2666667' AS DateTime2), CAST(N'2026-07-01T21:46:35.6766667' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000002', N'api.judge1@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Test Judge One', N'0900000002', N'10000000-0000-0000-0000-000000000004', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-05-02T08:00:00.0000000' AS DateTime2), CAST(N'2026-05-02T08:00:00.0000000' AS DateTime2), CAST(N'2026-05-02T09:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000003', N'api.judge2@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Test Judge Two', N'0900000003', N'10000000-0000-0000-0000-000000000004', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-05-02T08:10:00.0000000' AS DateTime2), CAST(N'2026-05-02T08:10:00.0000000' AS DateTime2), CAST(N'2026-05-02T09:10:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000004', N'api.mentor@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Test Mentor', N'0900000004', N'10000000-0000-0000-0000-000000000007', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-05-02T08:20:00.0000000' AS DateTime2), CAST(N'2026-05-02T08:20:00.0000000' AS DateTime2), CAST(N'2026-05-02T09:20:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000010', N'api.alpha.leader@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Alpha Leader', N'0910000010', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200010', NULL, N'FPT University', CAST(N'2026-05-03T08:00:00.0000000' AS DateTime2), CAST(N'2026-05-03T08:00:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000011', N'api.alpha.member@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Alpha Member', N'0910000011', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200011', NULL, N'FPT University', CAST(N'2026-05-03T08:10:00.0000000' AS DateTime2), CAST(N'2026-05-03T08:10:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:10:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000012', N'api.beta.leader@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Beta Leader', N'0910000012', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200012', NULL, N'FPT University', CAST(N'2026-05-03T08:20:00.0000000' AS DateTime2), CAST(N'2026-05-03T08:20:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:20:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000013', N'api.beta.member@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Beta Member', N'0910000013', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200013', NULL, N'FPT University', CAST(N'2026-05-03T08:30:00.0000000' AS DateTime2), CAST(N'2026-05-03T08:30:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:30:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000014', N'api.green.leader@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Green Leader', N'0910000014', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200014', NULL, N'FPT University', CAST(N'2026-05-03T08:40:00.0000000' AS DateTime2), CAST(N'2026-05-03T08:40:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:40:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000015', N'api.green.member@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Green External Member', N'0910000015', N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000002', NULL, N'EXT-200015', N'Ho Chi Minh City University of Technology', CAST(N'2026-05-03T08:50:00.0000000' AS DateTime2), CAST(N'2026-05-03T08:50:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:50:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000016', N'api.applicant@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Pending Team Applicant', N'0910000016', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200016', NULL, N'FPT University', CAST(N'2026-05-03T09:00:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:00:00.0000000' AS DateTime2), CAST(N'2026-05-03T10:00:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000017', N'api.guestjudge@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Test Guest Judge', N'0900000017', N'10000000-0000-0000-0000-000000000005', N'20000000-0000-0000-0000-000000000005', NULL, NULL, NULL, CAST(N'2026-05-02T08:30:00.0000000' AS DateTime2), CAST(N'2026-05-02T08:30:00.0000000' AS DateTime2), CAST(N'2026-05-02T09:30:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-12-31T23:59:59.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000018', N'api.join.member@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Join Test Member', N'0910000018', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200018', NULL, N'FPT University', CAST(N'2026-05-03T09:10:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:10:00.0000000' AS DateTime2), CAST(N'2026-05-03T10:10:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000019', N'api.join.leader@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Join Test Leader', N'0910000019', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE200019', NULL, N'FPT University', CAST(N'2026-05-03T09:20:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:20:00.0000000' AS DateTime2), CAST(N'2026-05-03T10:20:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a1000000-0000-0000-0000-000000000020', N'api.join.guest@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'API Join Test Guest', N'0900000020', N'10000000-0000-0000-0000-000000000005', N'20000000-0000-0000-0000-000000000005', NULL, NULL, NULL, CAST(N'2026-05-03T09:30:00.0000000' AS DateTime2), CAST(N'2026-05-03T09:30:00.0000000' AS DateTime2), CAST(N'2026-05-03T10:30:00.0000000' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', CAST(N'2026-12-31T23:59:59.0000000' AS DateTime2), 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'3644121c-cb57-4576-ad3e-03e9315f5896', N'ahihine03@gmail.com', N'$2a$12$68PRJpUqmxn364UI0Gewn.DXxBzGxoF/ucrrhNv2nOTEvGAuPdMbC', N'Xuantan Nguyen', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000004', NULL, NULL, NULL, CAST(N'2026-07-13T07:57:27.9095207' AS DateTime2), CAST(N'2026-07-13T10:32:54.8281545' AS DateTime2), NULL, NULL, NULL, 1, 0)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a0000000-0000-0000-0000-111111111111', N'mentor1@seal.com', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Simple Mentor (AI)', NULL, N'33333333-3333-3333-3333-333333333333', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-01T21:40:12.2666667' AS DateTime2), CAST(N'2026-07-04T00:57:54.6897793' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', N'hahachenal@gmail.com', N'$2a$12$scjsIMB7ceT/IYJXIC8X4eqj/0OvYeIvdJR2DB.OiGEtxb0.1E9aK', N'Tân', N'0962647139', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000004', N'SE201466', NULL, N'FPT University', CAST(N'2026-07-14T06:33:29.4482841' AS DateTime2), CAST(N'2026-07-14T06:43:18.4851621' AS DateTime2), NULL, NULL, NULL, 1, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a5ae98c3-201c-4772-967c-145af22af723', N'stu2@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Sinh Viên Test 2', NULL, N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE123457', NULL, NULL, CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'86eb15fb-4af1-4bc1-9bfe-15faa6a6c490', N'hahachenal@gmail.com', N'$2a$12$kdgruXHRtV3vHAkCG4KqwO06.wvbTkSfG4wqq0lp2P4fx28q1A.Li', N'Nguyễn Xuân Tân', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000004', NULL, NULL, NULL, CAST(N'2026-07-14T06:36:20.6799633' AS DateTime2), CAST(N'2026-07-14T06:39:32.0217523' AS DateTime2), NULL, NULL, NULL, 1, 0)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'5181ec59-afee-46ee-95d1-16cde3a8819c', N'team1a@gmail.com', N'$2a$12$ksYVzxlTG9ln/diXwK0oX.2ritjD6PnuosAKJtDIkc9w/9pT97Q96', N'TeamA1', N'1234567890', N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000002', NULL, N'UIT0001', N'UIT', CAST(N'2026-07-04T02:10:06.9210370' AS DateTime2), CAST(N'2026-07-04T02:10:51.5714054' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'9e789580-0b06-4ae3-8862-1b0694c05ff6', N'applicant1@fpt.edu.vn', N'$2a$12$PLACEHOLDER_HASH_REPLACE_IN_APP', N'Pham Thi Applicant', N'0900000004', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE170004', NULL, NULL, CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'teama2@gmail.com', N'$2a$12$JcuQxwwNtEnT/5fS2oISge8f.wLl5/UnEUIzCzZHjXZ9pJnNsegTe', N'TeamA2', N'1234567890', N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000002', NULL, N'UIT0002', N'UIT', CAST(N'2026-07-04T02:11:30.3080258' AS DateTime2), CAST(N'2026-07-04T02:11:50.4428932' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a0000000-0000-0000-0000-222222222222', N'mentor2@seal.com', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Charlie Mentor (Web)', NULL, N'33333333-3333-3333-3333-333333333333', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-01T21:40:12.2666667' AS DateTime2), CAST(N'2026-07-01T21:46:35.6766667' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'41f482e3-e453-44f5-940d-2480ae7a3110', N'vanchien0307206@gmail.com', N'$2a$12$W81ZVUCFFIRlZzB0loePn.YZBbTG9joHqtRR3zXKXBzEkgNX/RsSW', N'Vũ Văn Chiến', N'0967466658', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE204003', NULL, N'FPT University', CAST(N'2026-06-27T13:34:58.2343181' AS DateTime2), CAST(N'2026-06-27T13:35:31.7789260' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a0000000-0000-0000-0000-333333333333', N'leader1@seal.com', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'David Leader', NULL, N'55555555-5555-5555-5555-555555555555', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-01T21:40:12.2666667' AS DateTime2), CAST(N'2026-07-01T21:46:35.6766667' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'8c5c3287-cd14-4186-9180-340d0387fc3a', N'judefortesting123@gmail.com', N'$2a$12$xervY/TQiN5kTgXBvhctZuUQD7KatuDMvEojje1f0j6JVTdHAYNya', N'Judge For Testing', N'0912312312', N'10000000-0000-0000-0000-000000000004', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-08T15:53:11.4232853' AS DateTime2), CAST(N'2026-07-08T15:53:11.4232853' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a0000000-0000-0000-0000-444444444444', N'leader2@seal.com', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Eve Leader', NULL, N'55555555-5555-5555-5555-555555555555', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-01T21:40:12.2666667' AS DateTime2), CAST(N'2026-07-01T21:46:35.6766667' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'8d288ab9-b557-4d9c-aeea-4477ed168395', N'leader1@fpt.edu.vn', N'$2a$12$PLACEHOLDER_HASH_REPLACE_IN_APP', N'Nguyen Van Leader', N'0900000001', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE170001', NULL, NULL, CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'0e9aeffe-0d45-4b52-b38f-4d6d125a81c8', N'member1@fpt.edu.vn', N'$2a$12$PLACEHOLDER_HASH_REPLACE_IN_APP', N'Tran Thi Member', N'0900000002', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE170002', NULL, NULL, CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'97027c55-304d-44cd-b99d-4eb09607dc87', N'admin@seal.fpt.edu.vn', N'$2a$12$PLACEHOLDER_HASH_REPLACE_IN_APP', N'SEAL Administrator', NULL, N'10000000-0000-0000-0000-000000000003', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-06-26T09:48:24.1300000' AS DateTime2), CAST(N'2026-06-26T09:48:24.1300000' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'0cd69fe9-1d0e-4094-b607-50d5f122f0f1', N'member2@fpt.edu.vn', N'$2a$12$PLACEHOLDER_HASH_REPLACE_IN_APP', N'Le Van Member', N'0900000003', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE170003', NULL, NULL, CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'nguyenducnamanh3@gmail.com', N'$2a$12$W5JuDT8hChWmOQE8SYFv/eeWFgCmxwtTwMWTjxcbzE54vSqumL.Iy', N'Anh Nam', N'0814113135', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE201169', NULL, N'FPT University', CAST(N'2026-06-27T13:36:24.7433093' AS DateTime2), CAST(N'2026-06-27T13:36:40.5976296' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'e3f9b065-3380-47ff-9705-5d8106188e4e', N'teamc1@gmail.com', N'$2a$12$uHBdxBaTr0faaXnR5g1oQOfwwm/Q9yrjQ61/OqiIiyuS./8Yr0SDO', N'TeamC1', NULL, N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE654321', NULL, NULL, CAST(N'2026-07-04T02:13:44.9272347' AS DateTime2), CAST(N'2026-07-04T02:13:44.9272347' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'9a9ef0d0-2bb3-4ed2-8e61-5dd814f4b20d', N'dellcomatkhau088888@gmail.com', N'$2a$12$S.UxbynaiwQXLjaqHljmAOC88bKM2Q3D9I0huG6h3GsaoRe/3o4pu', N'Vu Van Simple', N'0934523854', N'10000000-0000-0000-0000-000000000004', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-09T14:46:29.6403868' AS DateTime2), CAST(N'2026-07-09T14:46:29.6403868' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'960a79c4-da41-478b-85bd-61f579e43f5b', N'teamc2@gmail.com', N'$2a$12$gyAXKZYi4n4Hv.LiTSnP6OMVufjp9guGW/JZ66je57/B9Tq2Dvfr.', N'TeamC2', NULL, N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE135790', NULL, NULL, CAST(N'2026-07-04T02:14:30.3944006' AS DateTime2), CAST(N'2026-07-04T02:14:30.3944006' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'7711bc51-b424-4ea4-8e62-629a6e92c43f', N'touman876@gmail.com', N'$2a$12$I1hFBZGPGB.RqbNLlugH4OlBfXHmdjA5RLRm8kpEv.vAkP.2cwGUy', N'Foung dep zai', N'0345678998', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'hello world', NULL, N'FPT University', CAST(N'2026-07-01T06:10:02.1255527' AS DateTime2), CAST(N'2026-07-01T06:11:26.2515279' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'd359de35-5ccb-4e8c-9476-732b6046914f', N'xuantan071106@gmail.com', N'$2a$12$cS53.fL3EKRVSPk.J16bFuQqQwXgmfR9P6Iugm2JRwgSZn7rqhNcC', N'Nguyễn Xuân Tân', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000004', NULL, NULL, NULL, CAST(N'2026-07-08T14:28:44.0277310' AS DateTime2), CAST(N'2026-07-08T14:29:31.8017051' AS DateTime2), NULL, NULL, NULL, 1, 0)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'95f3305a-a36e-46f0-abb3-756cbf1e6484', N'api.organizer@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Alice Organizer', NULL, N'10000000-0000-0000-0000-000000000003', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-06-26T09:49:45.4266667' AS DateTime2), CAST(N'2026-07-09T00:21:25.8147985' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'c42a5abb-502e-43a4-bbe3-837034a36a71', N'admin@fpt.edu.vn', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'System Organizer', NULL, N'10000000-0000-0000-0000-000000000003', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-06-26T09:49:32.3266667' AS DateTime2), CAST(N'2026-06-26T09:49:32.3266667' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'6bb7e7b8-f7a9-4d11-ba12-8df4c1c7577b', N'teama3@gmail.com', N'$2a$12$/vNWzttVAlYpaTSmE8wqde4HJ8r8WKXlHLt6yOXoh7iKaGRT/NpN6', N'TeamA3', N'1234567890', N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000002', NULL, N'UIT0003', N'UIT', CAST(N'2026-07-04T02:12:24.0305840' AS DateTime2), CAST(N'2026-07-04T02:12:24.0305840' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'6006f192-3fd1-4647-b145-8ebccb5ebd61', N'mentorfortesting123@gmail.com', N'$2a$12$EQPagbMLafHFRKI1SJleCObeFIp7Pea17Xe0ycwI0t31lJa8rznt2', N'Mentor For Testing', N'0912354323', N'10000000-0000-0000-0000-000000000007', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-08T16:26:04.3369595' AS DateTime2), CAST(N'2026-07-08T16:26:04.3369595' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'46f63efa-185a-4a0a-9e9f-934f01c6513c', N'teamd1@gmail.com', N'$2a$12$s/gup4wnF9BKUkM9Y8IQt.vH8fwl8XbN0jcmZRc5Dz5YSwFIek6b6', N'TeamD1', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000002', NULL, N'US0001', N'HCMUS', CAST(N'2026-07-04T02:15:14.6296205' AS DateTime2), CAST(N'2026-07-04T02:15:14.6296205' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'5d565896-2307-4911-939d-94e11e4b1c44', N'nguyenducnamanh4@gmail.com', N'$2a$12$LKw0ZIgfXOVRRf8ki2wH8.TlgdGTJNcLFJzRlpp7IIiw2RoIXluz.', N'Nam Anh', N'0814113135', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE201169', NULL, N'FPT University', CAST(N'2026-06-27T12:48:20.1224110' AS DateTime2), CAST(N'2026-06-27T12:48:40.6868782' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'89abab57-98ab-4927-9a7f-9cdae0d8434f', N'thaovann1405@gmail.com', N'$2a$12$lkGK4g77babTRmlMCBcJBOKiJOOAUOxtfL3wOZg7Nz2ERuSzlD9JG', N'hello bbi', N'1234567890', N'10000000-0000-0000-0000-000000000004', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-07-01T13:45:20.2729024' AS DateTime2), CAST(N'2026-07-01T13:48:14.2112092' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'fa60133b-5874-4dbf-9299-a0069495b193', N'xuantan071106@gmail.com', N'$2a$12$6GnOeOHEwpmuvzkwOD4bqeN.NvwmjsELT3quEFAgpJTyKhY1N/tCy', N'Nguyễn Xuân Tân', N'123456789', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE201466', NULL, N'FPT University', CAST(N'2026-06-26T16:57:43.8997100' AS DateTime2), CAST(N'2026-06-26T16:58:21.8181769' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'4d2f7a5b-a634-46a9-92ee-ac28fa245a1a', N'teame1@gmail.com', N'$2a$12$2Q9pX/odhza0bbxwir3B0eFwPby/H6jllv1nX8VhwthM1X14TORNK', N'TeamE1', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000002', NULL, N'PT0001', N'PTIT', CAST(N'2026-07-04T02:16:01.5298797' AS DateTime2), CAST(N'2026-07-04T02:16:01.5298797' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'ea969288-a77c-4683-b661-b10955f1df18', N'tronghieu171026@gmail.com', N'$2a$12$MzYZnKC1rty7VgbEBFRWQOtM3uSKT7vsIG1MgGij.eWF3GSO8LH5K', N'Nguyễn Trọng Hiếu', N'0388009404', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE201430', NULL, N'FPT University', CAST(N'2026-06-28T20:52:03.8766997' AS DateTime2), CAST(N'2026-07-04T16:58:02.6780509' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'1cefeef1-28e1-4897-b4ff-b85942aaad9f', N'teamb1@gmail.com', N'$2a$12$/dd92.kyXT/1OGvRKkFbGumu9tB27CVNuayHaLqq/6B.ItE3i1zJm', N'TeamB1', N'1234567890', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE123456', NULL, NULL, CAST(N'2026-07-04T02:13:06.3994808' AS DateTime2), CAST(N'2026-07-04T02:13:06.3994808' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'b82f80fd-a301-43a3-9d88-cadf9f67d1cc', N'tronghieu171026@gmail.com', N'$2a$12$F8NwvyLGY7YwZxM/8zDcm.LDfvvtcJcvkPS3r0eHqJCPwV/JbC7X.', N'Hiếu Nguyễn Trọng', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000005', NULL, NULL, NULL, CAST(N'2026-07-03T23:06:04.0563331' AS DateTime2), CAST(N'2026-07-03T23:06:04.0563331' AS DateTime2), NULL, NULL, NULL, 0, 0)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'20fad3e3-87f2-48b3-91b8-d93f24d9302c', N'stu1@seal.test', N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm', N'Sinh Viên Test 1', NULL, N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE123456', NULL, NULL, CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), CAST(N'2026-07-01T08:39:16.9666667' AS DateTime2), N'95f3305a-a36e-46f0-abb3-756cbf1e6484', NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'a6fec830-bca4-4cab-a3d6-de094ae67034', N'leader2@fpt.edu.vn', N'$2a$12$PLACEHOLDER_HASH_REPLACE_IN_APP', N'Hoang Van Leader', N'0900000005', N'10000000-0000-0000-0000-000000000001', N'20000000-0000-0000-0000-000000000002', N'SE170005', NULL, NULL, CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), CAST(N'2026-06-26T09:48:24.1433333' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'52e4735a-b7a7-4500-ad29-eb8621aadab7', N'ahihine03@gmail.com', N'$2a$12$gIlv47AacwfDYdDoNcExXOX52tN1gPVfUVkB.gSNiDfJSIHhIE59O', N'Nguyễn Xuân Bel', N'0123456789', N'33333333-3333-3333-3333-333333333333', N'20000000-0000-0000-0000-000000000002', NULL, NULL, NULL, CAST(N'2026-06-30T21:24:25.7157267' AS DateTime2), CAST(N'2026-07-09T10:24:38.0995429' AS DateTime2), NULL, NULL, NULL, 0, 1)
INSERT [dbo].[Users] ([UserID], [Email], [PasswordHash], [FullName], [Phone], [UserTypeID], [AccountStatusID], [FPTStudentCode], [ExternalStudentCode], [UniversityName], [CreatedAt], [UpdatedAt], [ApprovedAt], [ApprovedByUserID], [AccountExpiresAt], [IsDeleted], [LocalLoginEnabled]) VALUES (N'578815a3-fd27-40fa-86f1-f011b76c163e', N'tronghieu171026@gmail.com', N'$2a$12$ObqL7HaKMthcR.Db58AZM.rNX7rz.YtqGImiZ6s6jAkVcyoaAyGH.', N'Hiếu Nguyễn Trọng', NULL, N'10000000-0000-0000-0000-000000000002', N'20000000-0000-0000-0000-000000000004', NULL, NULL, NULL, CAST(N'2026-07-04T16:09:39.9874790' AS DateTime2), CAST(N'2026-07-04T16:10:23.5052238' AS DateTime2), NULL, NULL, NULL, 1, 0)
GO
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
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'bca2a8f6-eb9d-400f-bf5f-4893909e7bd6', N'41f482e3-e453-44f5-940d-2480ae7a3110', N'5bc087aa60885afac34c5713c99407e046b7cd437a1f2a9f10da1829af27fbf4', CAST(N'2026-06-27T13:34:58.1300707' AS DateTime2), CAST(N'2026-06-28T13:34:58.1300707' AS DateTime2), CAST(N'2026-06-27T13:35:31.6885994' AS DateTime2))
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'23c3fa2b-e0f6-474e-9516-6e9dd389721b', N'7711bc51-b424-4ea4-8e62-629a6e92c43f', N'd71e7f1ffc2ce6be79d369ddf3c365e237555e49f09e23b967613c0b0d56d382', CAST(N'2026-07-01T06:10:01.6087806' AS DateTime2), CAST(N'2026-07-02T06:10:01.6087806' AS DateTime2), CAST(N'2026-07-01T06:11:26.2038925' AS DateTime2))
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'78773589-c9c1-441b-a449-8825e67ba073', N'6eda58a7-1866-4f0b-98c8-213c1e07e016', N'6e919cb910665ad717a7735a2895cf09a87cb69c785b43969b02bfcdac1f9e1d', CAST(N'2026-07-04T02:11:30.1707182' AS DateTime2), CAST(N'2026-07-05T02:11:30.1707182' AS DateTime2), NULL)
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'1de3528a-5f42-4deb-9fbc-a45cc0b3269d', N'58f44b80-6e5d-4db5-aa0d-5c92af46668b', N'55014848-0f09-4856-89b4-07b7545add08', CAST(N'2026-06-27T13:36:24.7360399' AS DateTime2), CAST(N'2026-06-28T13:36:24.7360399' AS DateTime2), CAST(N'2026-06-27T13:36:40.5485588' AS DateTime2))
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'308ab8a4-09e5-4bdd-933e-cfd51bafc98c', N'ea969288-a77c-4683-b661-b10955f1df18', N'9d12c28a7b747fa10e4e973adb47c042d4489b1335ef4adb5239eb12e6485961', CAST(N'2026-06-28T20:52:03.8092700' AS DateTime2), CAST(N'2026-06-29T20:52:03.8092700' AS DateTime2), CAST(N'2026-06-28T20:52:30.8658025' AS DateTime2))
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'ed1a9600-2105-434b-9f3d-da5eb2196c50', N'5d565896-2307-4911-939d-94e11e4b1c44', N'db03c573-948c-4018-9146-6ddedb40cc2e', CAST(N'2026-06-27T12:48:20.1150352' AS DateTime2), CAST(N'2026-06-28T12:48:20.1150352' AS DateTime2), CAST(N'2026-06-27T12:48:40.6161107' AS DateTime2))
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'900ab17f-0d79-4d02-a96a-dc5c33141d1e', N'fa60133b-5874-4dbf-9299-a0069495b193', N'5b53cac7-a0af-4a1d-98bf-fcd6c9677af2', CAST(N'2026-06-26T16:57:43.8851699' AS DateTime2), CAST(N'2026-06-27T16:57:43.8851699' AS DateTime2), CAST(N'2026-06-26T16:58:21.7635327' AS DateTime2))
INSERT [dbo].[VerificationTokens] ([TokenID], [UserID], [TokenHash], [CreatedAt], [ExpiresAt], [UsedAt]) VALUES (N'4b04cc4b-998c-454c-bdff-ed58f9bb0780', N'9203aec2-e2c5-47a8-9d24-12c2e9f7da6d', N'c4a20d56fcb1d58f18fe9f240e5c83e4a88c373b220ad5e98dd2674188452c56', CAST(N'2026-07-14T06:33:28.8076071' AS DateTime2), CAST(N'2026-07-15T06:33:28.8076071' AS DateTime2), CAST(N'2026-07-14T06:35:34.0876834' AS DateTime2))
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UKon5lvd4jlhyt5pyp5npbj6cv9]    Script Date: 7/14/2026 9:01:57 PM ******/
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
/****** Object:  Index [UX_TeamWithdrawalRequests_OnePendingPerTeam]    Script Date: 7/28/2026 ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_TeamWithdrawalRequests_OnePendingPerTeam] ON [dbo].[TeamWithdrawalRequests]
(
	[TeamID] ASC
)
WHERE [RequestStatus] = N'PENDING'
WITH (STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_TeamWithdrawalRequests_EventStatus]    Script Date: 7/28/2026 ******/
CREATE NONCLUSTERED INDEX [IX_TeamWithdrawalRequests_EventStatus] ON [dbo].[TeamWithdrawalRequests]
(
	[RequestStatus] ASC,
	[TeamID] ASC
)
WITH (STATISTICS_NORECOMPUTE = OFF, ONLINE = OFF, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
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
ALTER TABLE [dbo].[TeamWithdrawalRequests] ADD  DEFAULT (newid()) FOR [RequestID]
GO
ALTER TABLE [dbo].[TeamWithdrawalRequests] ADD  DEFAULT (N'PENDING') FOR [RequestStatus]
GO
ALTER TABLE [dbo].[TeamWithdrawalRequests] ADD  DEFAULT (getutcdate()) FOR [RequestedAt]
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
ALTER TABLE [dbo].[TeamWithdrawalRequests]  WITH CHECK ADD FOREIGN KEY([RequestedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[TeamWithdrawalRequests]  WITH CHECK ADD FOREIGN KEY([RespondedByID])
REFERENCES [dbo].[Users] ([UserID])
GO
ALTER TABLE [dbo].[TeamWithdrawalRequests]  WITH CHECK ADD FOREIGN KEY([TeamID])
REFERENCES [dbo].[Teams] ([TeamID])
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
ALTER TABLE [dbo].[TeamWithdrawalRequests]  WITH CHECK ADD  CONSTRAINT [CK_TeamWithdrawalRequests_Status] CHECK  (([RequestStatus]=N'REJECTED' OR [RequestStatus]=N'APPROVED' OR [RequestStatus]=N'PENDING'))
GO
ALTER TABLE [dbo].[TeamWithdrawalRequests] CHECK CONSTRAINT [CK_TeamWithdrawalRequests_Status]
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
