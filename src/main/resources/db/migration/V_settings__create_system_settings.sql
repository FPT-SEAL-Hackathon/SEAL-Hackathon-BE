-- ============================================================
-- Migration: Create SystemSettings table
-- Run this script manually on your SQL Server database
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME = 'SystemSettings'
)
BEGIN
    CREATE TABLE SystemSettings (
        SettingKey   NVARCHAR(100)  NOT NULL PRIMARY KEY,
        SettingValue NVARCHAR(1000) NOT NULL,
        SettingType  NVARCHAR(20)   NULL,   -- STRING | INTEGER | BOOLEAN
        Description  NVARCHAR(500)  NULL,
        UpdatedAt    DATETIME2      NULL
    );

    -- Seed default values
    INSERT INTO SystemSettings (SettingKey, SettingValue, SettingType, Description) VALUES
    ('platformName',             'SEAL FPT Hackathon Platform', 'STRING',  'Display name of the platform'),
    ('maxTeamSize',              '5',                           'INTEGER', 'Maximum allowed team members'),
    ('minTeamSize',              '2',                           'INTEGER', 'Minimum required team members'),
    ('submissionGracePeriod',    '30',                          'INTEGER', 'Grace period in minutes after deadline'),
    ('contactEmail',             'seal@fpt.edu.vn',             'STRING',  'Platform support contact email'),
    ('allowLateSubmissions',     'true',                        'BOOLEAN', 'Allow submissions after deadline'),
    ('enablePublicLeaderboard',  'true',                        'BOOLEAN', 'Show leaderboard to public'),
    ('requireEmailVerification', 'true',                        'BOOLEAN', 'Require email verification on register');

    PRINT 'SystemSettings table created and seeded.';
END
ELSE
BEGIN
    PRINT 'SystemSettings table already exists. Skipping.';
END
