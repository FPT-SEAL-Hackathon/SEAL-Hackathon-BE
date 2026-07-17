-- ==========================================================
-- Account Link Tickets (Google <-> Local, một user một người)
-- Ngày: 2026-07-16
-- Mục đích:
--   1. Khi đăng nhập Google với sub MỚI nhưng email ĐÃ tồn tại:
--      KHÔNG tạo user mới — phát linkingToken ngắn hạn (ACCOUNT_LINK_REQUIRED)
--      để user xác minh quyền sở hữu (mật khẩu local hoặc OTP email)
--      rồi mới ghi UserOAuthAccounts trỏ vào user hiện có.
--   2. Khi đăng ký local với email thuộc user Google-only (chưa có mật khẩu):
--      KHÔNG tạo user mới — phát linkingToken, xác minh OTP email,
--      rồi thiết lập PasswordHash cho user hiện có (local/setup-password).
--   3. Chỉ lưu HASH (SHA-256) của linkingToken và OTP; token dùng một lần,
--      hết hạn 15 phút; OTP hết hạn 10 phút, tối đa 5 lần nhập sai.
-- ==========================================================

IF OBJECT_ID(N'AccountLinkTickets', N'U') IS NULL
BEGIN
    CREATE TABLE AccountLinkTickets (
        TicketID UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
        -- User đích sẽ được liên kết / thiết lập mật khẩu. Backend tự resolve,
        -- frontend không bao giờ được gửi UserID tùy ý.
        UserID UNIQUEIDENTIFIER NOT NULL REFERENCES Users(UserID),
        -- GOOGLE_LINK: gắn định danh Google vào user local hiện có.
        -- LOCAL_SETUP: thiết lập mật khẩu local cho user Google-only hiện có.
        Purpose NVARCHAR(30) NOT NULL,
        -- Snapshot định danh Google đã xác thực (chỉ dùng cho GOOGLE_LINK).
        Provider NVARCHAR(30) NULL,
        ProviderUserID NVARCHAR(255) NULL,
        ProviderEmail NVARCHAR(255) NULL,
        ProviderEmailVerified BIT NULL,
        ProviderDisplayName NVARCHAR(255) NULL,
        ProviderAvatarUrl NVARCHAR(1000) NULL,
        -- Hash SHA-256 của linkingToken thô (token thô chỉ nằm ở client).
        TokenHash NVARCHAR(128) NOT NULL UNIQUE,
        -- OTP email (hash SHA-256), phát khi user chọn xác minh bằng OTP.
        OtpHash NVARCHAR(128) NULL,
        OtpExpiresAt DATETIME2 NULL,
        OtpAttempts INT NOT NULL DEFAULT 0,
        OtpLastSentAt DATETIME2 NULL,
        OtpVerifiedAt DATETIME2 NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        ExpiresAt DATETIME2 NOT NULL,
        ConsumedAt DATETIME2 NULL
    );

    CREATE NONCLUSTERED INDEX IX_AccountLinkTickets_UserID ON AccountLinkTickets(UserID);
END
GO
