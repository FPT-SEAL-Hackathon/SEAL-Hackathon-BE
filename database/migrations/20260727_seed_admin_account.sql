-- Migration: Bootstrap tai khoan ADMIN dau tien
-- Date: 2026-07-27
--
-- Boi canh: role "Admin" (UserType) da co san trong schema nhung CHUA co user nao mang role
-- nay. Sau khi quan ly nguoi dung / cau hinh he thong chuyen tu ORGANIZER sang ADMIN, phai co
-- it nhat mot tai khoan Admin de dang nhap, neu khong se khong ai vao duoc User Management.
--
-- !!! DEV ONLY !!!
-- Mat khau khoi tao la mat khau dung chung cua bo seed dev (xem database/seed_api_test_data.sql).
-- PHAI doi mat khau ngay sau lan dang nhap dau tien, va KHONG dung file nay tren moi truong that.
--
-- Idempotent: chay lai nhieu lan khong tao trung.

SET NOCOUNT ON;
GO

DECLARE @AdminUserTypeID  UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
DECLARE @ActiveStatusID   UNIQUEIDENTIFIER = '20000000-0000-0000-0000-000000000002';
DECLARE @AdminUserID      UNIQUEIDENTIFIER = 'A0000000-0000-0000-0000-00000000AD01';
DECLARE @AdminEmail       NVARCHAR(255)    = N'admin@seal.local';

-- BCrypt(10) cua mat khau dev dung chung: Test@123
DECLARE @PasswordHash     NVARCHAR(512)    =
    N'$2a$10$r2IN3b9UZqJTpgDEhaorz.NrIFFe31HahrsCDfnmDUwO58BrE1lPm';

DECLARE @Now DATETIME2(7) = SYSUTCDATETIME();

-- 1) Dam bao UserType 'Admin' ton tai (DB dung tu snapshot cu co the con thieu row nay).
IF NOT EXISTS (SELECT 1 FROM dbo.UserType WHERE TypeName = N'Admin')
BEGIN
    INSERT INTO dbo.UserType (UserTypeID, TypeName) VALUES (@AdminUserTypeID, N'Admin');
END
ELSE
BEGIN
    -- Lay dung ID thuc te neu DB da co san row 'Admin' voi ID khac.
    SELECT @AdminUserTypeID = UserTypeID FROM dbo.UserType WHERE TypeName = N'Admin';
END;

-- 2) Tao tai khoan Admin neu chua co.
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Email = @AdminEmail OR UserID = @AdminUserID)
BEGIN
    INSERT INTO dbo.Users
        (UserID, Email, PasswordHash, FullName, Phone, UserTypeID, AccountStatusID,
         CreatedAt, UpdatedAt, ApprovedAt, IsDeleted)
    VALUES
        (@AdminUserID, @AdminEmail, @PasswordHash, N'System Administrator', N'0900000099',
         @AdminUserTypeID, @ActiveStatusID,
         @Now, @Now, @Now, 0);
END;

-- 3) Bat dang nhap local neu cot ton tai (them boi 20260718_unique_local_email.sql).
--    Tai khoan da o trang thai Active nen khong can buoc xac minh email.
IF COL_LENGTH(N'dbo.Users', N'LocalLoginEnabled') IS NOT NULL
BEGIN
    EXEC sp_executesql
        N'UPDATE dbo.Users SET LocalLoginEnabled = 1 WHERE UserID = @id AND LocalLoginEnabled <> 1;',
        N'@id UNIQUEIDENTIFIER', @id = @AdminUserID;
END;
GO
