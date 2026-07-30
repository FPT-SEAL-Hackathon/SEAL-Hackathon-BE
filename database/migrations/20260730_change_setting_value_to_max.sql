-- Alter SystemSettings SettingValue column to support longer strings (e.g., JSON settings for landing page)
ALTER TABLE [dbo].[SystemSettings] ALTER COLUMN [SettingValue] NVARCHAR(MAX) NOT NULL;
