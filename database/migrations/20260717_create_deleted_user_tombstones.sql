-- Tombstone cho tài khoản bị organizer xóa cứng (hard delete).
-- Chỉ dùng để hiển thị thông báo "tài khoản đã bị gỡ, hãy tạo tài khoản mới"
-- khi user cũ đăng nhập trong vòng 7 ngày; KHÔNG chặn đăng ký lại bằng email này.
-- Bản ghi hết hạn được scheduled job xóa định kỳ.
-- Không FK tới Users: organizer thực hiện xóa sau này cũng có thể bị xóa.

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'DeletedUserTombstones')
BEGIN
    CREATE TABLE [dbo].[DeletedUserTombstones] (
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
        ON [dbo].[DeletedUserTombstones] ([Email]) INCLUDE ([ExpiresAt]);
END
GO
