-- MVP GitHub Repository Integrations Migration

-- Drop tables if they exist (for MVP re-runs during development)
-- IF OBJECT_ID('RepositorySyncLogs', 'U') IS NOT NULL DROP TABLE RepositorySyncLogs;
-- IF OBJECT_ID('RepositoryIssues', 'U') IS NOT NULL DROP TABLE RepositoryIssues;
-- IF OBJECT_ID('Repositories', 'U') IS NOT NULL DROP TABLE Repositories;
-- IF OBJECT_ID('RepositoryIntegrations', 'U') IS NOT NULL DROP TABLE RepositoryIntegrations;

-- Create RepositoryIntegrations
CREATE TABLE RepositoryIntegrations (
    IntegrationID UNIQUEIDENTIFIER PRIMARY KEY,
    EventID UNIQUEIDENTIFIER NOT NULL,
    Provider VARCHAR(50) NOT NULL, -- 'GITHUB'
    EncryptedToken VARBINARY(MAX),
    EncryptionFormatVersion INT,
    EncryptionIV VARBINARY(12),
    ConnectionStatus VARCHAR(50) NOT NULL DEFAULT 'CONNECTED', -- 'CONNECTED', 'DISCONNECTED', 'TOKEN_INVALID'
    CreatedByID UNIQUEIDENTIFIER NOT NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME,
    
    CONSTRAINT FK_Integration_Events FOREIGN KEY (EventID) REFERENCES Events(EventID),
    CONSTRAINT FK_Integration_Users FOREIGN KEY (CreatedByID) REFERENCES Users(UserID)
);

CREATE INDEX IX_Integration_Event ON RepositoryIntegrations(EventID);

-- Create Repositories
CREATE TABLE Repositories (
    RepositoryID UNIQUEIDENTIFIER PRIMARY KEY,
    IntegrationID UNIQUEIDENTIFIER NOT NULL,
    ExternalRepositoryID VARCHAR(100) NOT NULL,
    RepositoryName NVARCHAR(255) NOT NULL,
    RepositoryFullName NVARCHAR(255) NOT NULL,
    RepositoryUrl NVARCHAR(MAX),
    Description NVARCHAR(MAX),
    SyncStatus VARCHAR(50) NOT NULL DEFAULT 'IDLE', -- 'IDLE', 'RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'
    ConnectedAt DATETIME NOT NULL DEFAULT GETDATE(),
    LastSyncAt DATETIME,
    
    CONSTRAINT FK_Repo_Integration FOREIGN KEY (IntegrationID) REFERENCES RepositoryIntegrations(IntegrationID),
    CONSTRAINT UQ_Integration_ExternalRepository UNIQUE (IntegrationID, ExternalRepositoryID)
);

-- Create RepositoryIssues
CREATE TABLE RepositoryIssues (
    IssueID UNIQUEIDENTIFIER PRIMARY KEY,
    RepositoryID UNIQUEIDENTIFIER NOT NULL,
    ExternalIssueID VARCHAR(100) NOT NULL,
    IssueNumber INT NOT NULL,
    Title NVARCHAR(500) NOT NULL,
    Body NVARCHAR(MAX),
    State VARCHAR(50) NOT NULL, -- 'open', 'closed'
    Url NVARCHAR(MAX),
    AuthorUsername NVARCHAR(255),
    AssigneeUsername NVARCHAR(255),
    Labels NVARCHAR(MAX),
    Milestone NVARCHAR(255),
    CommentCount INT NOT NULL DEFAULT 0,
    ExternalCreatedAt DATETIME NOT NULL,
    ExternalUpdatedAt DATETIME NOT NULL,
    ExternalClosedAt DATETIME,
    LastSynchronizedAt DATETIME NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT FK_Issue_Repo FOREIGN KEY (RepositoryID) REFERENCES Repositories(RepositoryID),
    CONSTRAINT UQ_Repository_ExternalIssue UNIQUE (RepositoryID, ExternalIssueID)
);

-- Create RepositorySyncLogs
CREATE TABLE RepositorySyncLogs (
    SyncLogID UNIQUEIDENTIFIER PRIMARY KEY,
    RepositoryID UNIQUEIDENTIFIER NOT NULL,
    TriggeredByUserID UNIQUEIDENTIFIER NOT NULL,
    SyncType VARCHAR(50) NOT NULL, -- 'INITIAL', 'MANUAL'
    Status VARCHAR(50) NOT NULL, -- 'RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'
    ItemsFetched INT NOT NULL DEFAULT 0,
    ItemsCreated INT NOT NULL DEFAULT 0,
    ItemsUpdated INT NOT NULL DEFAULT 0,
    ItemsFailed INT NOT NULL DEFAULT 0,
    HasMore BIT NOT NULL DEFAULT 0,
    ErrorCode VARCHAR(100),
    ErrorMessage NVARCHAR(MAX),
    StartedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CompletedAt DATETIME,
    
    CONSTRAINT FK_SyncLog_Repo FOREIGN KEY (RepositoryID) REFERENCES Repositories(RepositoryID),
    CONSTRAINT FK_SyncLog_User FOREIGN KEY (TriggeredByUserID) REFERENCES Users(UserID)
);

