-- Migration: Remove obsolete account status "Pending Approval"
-- Date: 2026-07-25
-- Luồng duyệt trước đây là duyệt TỪNG học sinh (account status Pending Approval);
-- nay đã đổi sang duyệt TEAM (team có status PENDING riêng) nên account status này thừa.
-- Users.AccountStatusID là FK NOT NULL → phải reassign user còn ở Pending Approval
-- trước khi xóa row (nếu không sẽ vi phạm khóa ngoại).
--
-- StatusID:
--   20000000-...0001 = Pending Approval  (xóa)
--   20000000-...0006 = Unverified        (đích reassign — vẫn CHƯA login được cho tới khi verify,
--                                          giữ đúng ngữ nghĩa "chưa được phép vào" của các account cũ)

IF EXISTS (SELECT 1 FROM dbo.AccountStatus WHERE StatusID = '20000000-0000-0000-0000-000000000001')
BEGIN
    -- 1) Chuyển mọi user còn ở Pending Approval sang Unverified.
    UPDATE dbo.Users
        SET AccountStatusID = '20000000-0000-0000-0000-000000000006'
        WHERE AccountStatusID = '20000000-0000-0000-0000-000000000001';

    -- 2) Xóa row status đã lỗi thời.
    DELETE FROM dbo.AccountStatus
        WHERE StatusID = '20000000-0000-0000-0000-000000000001';
END;
GO
