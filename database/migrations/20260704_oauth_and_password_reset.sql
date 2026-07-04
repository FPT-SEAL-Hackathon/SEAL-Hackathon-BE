-- ==========================================================
-- OAuth (Google) + Password Reset foundation
-- Ngày: 2026-07-04
-- Mục đích:
--   1. Cho phép tài khoản LOCAL và tài khoản GOOGLE tồn tại song song
--      với cùng một email (bỏ UNIQUE toàn cục trên Users.Email).
--   2. Thêm cờ LocalLoginEnabled để phân biệt tài khoản có mật khẩu local.
--   3. Tạo bảng UserOAuthAccounts: định danh OAuth bằng (Provider, ProviderUserID),
--      KHÔNG dùng email làm định danh OAuth.
--   4. Tạo bảng PasswordResetTokens: lưu HASH của token reset, không lưu token thô.
--   5. Bỏ 2 CHECK constraint bắt buộc student code theo UserType vì user OAuth
--      TEMPORARY được tạo trước khi hoàn thiện hồ sơ; validation chuyển về
--      tầng service (applyRoleSpecificFields / complete-profile).
-- ==========================================================

-- 1. Bỏ UNIQUE toàn cục trên Users.Email (tên constraint được sinh tự động nên phải tra cứu động)
DECLARE @uqEmail NVARCHAR(256);
SELECT @uqEmail = kc.name
FROM sys.key_constraints kc
         JOIN sys.index_columns ic
              ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
         JOIN sys.columns c
              ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID(N'Users')
  AND kc.type = 'UQ'
  AND c.name = N'Email';

IF @uqEmail IS NOT NULL
    EXEC(N'ALTER TABLE Users DROP CONSTRAINT [' + @uqEmail + N']');
GO

-- Giữ tra cứu email nhanh bằng index KHÔNG unique (IX_Users_Email đã tồn tại thì thôi)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Users_Email' AND object_id = OBJECT_ID(N'Users'))
    CREATE NONCLUSTERED INDEX IX_Users_Email ON Users(Email) WHERE IsDeleted = 0;
GO

-- 2. Thêm cờ LocalLoginEnabled (mặc định 1: mọi tài khoản hiện có đều là local)
IF COL_LENGTH(N'Users', N'LocalLoginEnabled') IS NULL
    ALTER TABLE Users ADD LocalLoginEnabled BIT NOT NULL CONSTRAINT DF_Users_LocalLoginEnabled DEFAULT 1;
GO

-- 3. Bỏ CHECK constraint student-code cứng theo UserType.
--    User OAuth TEMPORARY được gán role tạm EXTERNAL_STUDENT nhưng chưa có
--    student code; ràng buộc nghiệp vụ này được service tầng trên đảm bảo
--    khi accountStatus chuyển sang ACTIVE (complete-profile).
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Users_FPTCode')
    ALTER TABLE Users DROP CONSTRAINT CK_Users_FPTCode;
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Users_ExternalCode')
    ALTER TABLE Users DROP CONSTRAINT CK_Users_ExternalCode;
GO

-- 4. Bảng định danh OAuth: một user có thể có nhiều provider,
--    nhưng một (Provider, ProviderUserID) chỉ thuộc về đúng một user.
IF OBJECT_ID(N'UserOAuthAccounts', N'U') IS NULL
BEGIN
    CREATE TABLE UserOAuthAccounts (
        OAuthAccountID UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID),
        Provider NVARCHAR(30) NOT NULL,
        ProviderUserID NVARCHAR(255) NOT NULL,
        Email NVARCHAR(255) NULL,
        EmailVerified BIT NULL,
        DisplayName NVARCHAR(255) NULL,
        AvatarUrl NVARCHAR(1000) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        UpdatedAt DATETIME2 NULL,
        CONSTRAINT UQ_UserOAuthAccounts_Provider_ProviderUserID UNIQUE (Provider, ProviderUserID),
        CONSTRAINT UQ_UserOAuthAccounts_User_Provider UNIQUE (UserID, Provider)
    );

    CREATE NONCLUSTERED INDEX IX_UserOAuthAccounts_UserID ON UserOAuthAccounts(UserID);
END
GO

-- 5. Bảng token reset mật khẩu: chỉ lưu hash SHA-256, dùng một lần, hết hạn ngắn.
IF OBJECT_ID(N'PasswordResetTokens', N'U') IS NULL
BEGIN
    CREATE TABLE PasswordResetTokens (
        TokenID UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID),
        TokenHash NVARCHAR(128) NOT NULL UNIQUE,
        CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        ExpiresAt DATETIME2 NOT NULL,
        UsedAt DATETIME2 NULL
    );

    CREATE NONCLUSTERED INDEX IX_PasswordResetTokens_UserID ON PasswordResetTokens(UserID);
END
GO
