IF OBJECT_ID(N'dbo.FptStudentCodePrefixes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.FptStudentCodePrefixes (
        Prefix NVARCHAR(2) NOT NULL,
        EnglishName NVARCHAR(100) NOT NULL,
        VietnameseName NVARCHAR(200) NOT NULL,
        MajorGroup NVARCHAR(100) NOT NULL,
        MajorCode NVARCHAR(20) NULL,
        Note NVARCHAR(500) NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_FptStudentCodePrefixes_IsActive DEFAULT (1),
        CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_FptStudentCodePrefixes_CreatedAt DEFAULT (SYSUTCDATETIME()),
        UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_FptStudentCodePrefixes_UpdatedAt DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT PK_FptStudentCodePrefixes PRIMARY KEY (Prefix),
        CONSTRAINT CK_FptStudentCodePrefixes_Prefix CHECK (Prefix NOT LIKE '%[^A-Z]%' AND LEN(Prefix) = 2)
    );
END
GO

MERGE dbo.FptStudentCodePrefixes AS target
USING (VALUES
    (N'SE', N'Software Engineering', N'Ky thuat phan mem', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - SE (Software Engineering)'),
    (N'IA', N'Information Assurance', N'An toan thong tin', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - IA (Information Assurance)'),
    (N'AI', N'Artificial Intelligence', N'Tri tue nhan tao', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - AI (Artificial Intelligence)'),
    (N'DS', N'Data Science', N'Khoa hoc du lieu ung dung', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - DS (Data Science)'),
    (N'IC', N'Integrated Circuits', N'Thiet ke vi mach ban dan', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - IC (Integrated Circuits)'),
    (N'AM', N'Automotive', N'Cong nghe o to so', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - AM (Automotive)'),
    (N'IS', N'Information Systems', N'He thong thong tin', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - IS (Information Systems)'),
    (N'GD', N'Graphic Design', N'Thiet ke do hoa va my thuat so', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - GD (Graphic Design)'),
    (N'RA', N'Robotics and AI', N'Robot va Tri tue nhan tao', N'Cong nghe thong tin', N'7480201', N'Cong nghe thong tin (Ma nganh: 7480201) - RA (Robotics and AI)'),
    (N'SB', N'Business Administration', N'Quan tri kinh doanh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - SB (Business Administration)'),
    (N'BA', N'Business Administration', N'Quan tri kinh doanh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - BA (Business Administration)'),
    (N'DM', N'Digital Marketing', N'Quan tri truyen thong va Marketing so', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - DM (Digital Marketing)'),
    (N'IB', N'International Business', N'Kinh doanh quoc te', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - IB (International Business)'),
    (N'HM', N'Hotel Management', N'Quan tri khach san', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - HM (Hotel Management)'),
    (N'TM', N'Tourism Management', N'Quan tri dich vu du lich va lu hanh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - TM (Tourism Management)'),
    (N'FI', N'Finance', N'Tai chinh', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - FI (Finance)'),
    (N'LS', N'Logistics', N'Logistics va quan ly chuoi cung ung', N'Kinh te va Quan tri kinh doanh', N'7340101', N'Kinh te va Quan tri kinh doanh (Ma nganh: 7340101) - LS (Logistics)'),
    (N'QA', N'English', N'Ngon ngu Anh', N'Ngon ngu va Xa hoi', N'7220201', N'Ngon ngu va Xa hoi - QA (English), Ma nganh: 7220201'),
    (N'EN', N'English', N'Ngon ngu Anh', N'Ngon ngu va Xa hoi', N'7220201', N'Ngon ngu va Xa hoi - EN (English), Ma nganh: 7220201'),
    (N'JA', N'Japanese', N'Ngon ngu Nhat', N'Ngon ngu va Xa hoi', N'7220209', N'Ngon ngu va Xa hoi - JA (Japanese), Ma nganh: 7220209'),
    (N'KR', N'Korean', N'Ngon ngu Han Quoc', N'Ngon ngu va Xa hoi', N'7220210', N'Ngon ngu va Xa hoi - KR (Korean), Ma nganh: 7220210'),
    (N'CH', N'Chinese', N'Ngon ngu Trung Quoc', N'Ngon ngu va Xa hoi', N'7220204', N'Ngon ngu va Xa hoi - CH (Chinese), Ma nganh: 7220204'),
    (N'MC', N'Multimedia Communication', N'Truyen thong da phuong tien', N'Ngon ngu va Xa hoi', N'7320106', N'Ngon ngu va Xa hoi - MC (Multimedia Communication), Ma nganh: 7320106'),
    (N'LE', N'Law', N'Luat / Luat kinh te', N'Ngon ngu va Xa hoi', N'7380101', N'Ngon ngu va Xa hoi - LE (Law), Ma nganh: 7380101')
) AS source (Prefix, EnglishName, VietnameseName, MajorGroup, MajorCode, Note)
ON target.Prefix = source.Prefix
WHEN MATCHED THEN
    UPDATE SET
        EnglishName = source.EnglishName,
        VietnameseName = source.VietnameseName,
        MajorGroup = source.MajorGroup,
        MajorCode = source.MajorCode,
        Note = source.Note,
        IsActive = 1,
        UpdatedAt = SYSUTCDATETIME()
WHEN NOT MATCHED THEN
    INSERT (Prefix, EnglishName, VietnameseName, MajorGroup, MajorCode, Note, IsActive)
    VALUES (source.Prefix, source.EnglishName, source.VietnameseName, source.MajorGroup, source.MajorCode, source.Note, 1);
GO
