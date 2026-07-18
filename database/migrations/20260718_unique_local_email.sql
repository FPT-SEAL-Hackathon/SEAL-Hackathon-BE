-- Chống tạo trùng tài khoản LOCAL cùng email (double-submit / 2 request đồng thời).
-- Chỉ áp cho account đăng nhập local, chưa xóa; KHÔNG đụng account OAuth-only
-- (LocalLoginEnabled=0) hay account đã soft-delete (IsDeleted=1) — vẫn cho phép
-- local + Google cùng email song song theo thiết kế hiện tại.
--
-- LƯU Ý: nếu DB đang có sẵn 2+ account local cùng email (IsDeleted=0), lệnh tạo
-- index sẽ FAIL. Chạy truy vấn kiểm tra trùng trước và xử lý thủ công:
--
--   SELECT Email, COUNT(*) FROM dbo.Users
--   WHERE LocalLoginEnabled = 1 AND IsDeleted = 0
--   GROUP BY Email HAVING COUNT(*) > 1;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes WHERE name = 'UQ_Users_Email_LocalActive'
)
BEGIN
    CREATE UNIQUE INDEX [UQ_Users_Email_LocalActive]
        ON [dbo].[Users] ([Email])
        WHERE [LocalLoginEnabled] = 1 AND [IsDeleted] = 0;
END
GO
