SET NOCOUNT ON;

PRINT N'Bắt đầu quá trình dọn dẹp Database (Bỏ qua các System Objects của Azure)...';

DECLARE @Sql NVARCHAR(MAX) = N'';

-- 1. XÓA TOÀN BỘ KHÓA NGOẠI (FOREIGN KEYS)
PRINT N'1. Đang xóa các Khóa ngoại (Foreign Keys)...';
SELECT @Sql += 'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.foreign_keys
WHERE is_ms_shipped = 0; -- Bỏ qua đồ của hệ thống

IF LEN(@Sql) > 0 EXEC sp_executesql @Sql;

-- 2. XÓA TOÀN BỘ BẢNG (TABLES)
PRINT N'2. Đang xóa các Bảng (Tables)...';
SET @Sql = N'';
SELECT @Sql += 'DROP TABLE ' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.tables
WHERE is_ms_shipped = 0;

IF LEN(@Sql) > 0 EXEC sp_executesql @Sql;

-- 3. XÓA TOÀN BỘ VIEWS
PRINT N'3. Đang xóa các Views...';
SET @Sql = N'';
SELECT @Sql += 'DROP VIEW ' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.views
WHERE is_ms_shipped = 0;

IF LEN(@Sql) > 0 EXEC sp_executesql @Sql;

-- 4. XÓA TOÀN BỘ STORED PROCEDURES
PRINT N'4. Đang xóa các Stored Procedures...';
SET @Sql = N'';
SELECT @Sql += 'DROP PROCEDURE ' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.procedures
WHERE is_ms_shipped = 0;

IF LEN(@Sql) > 0 EXEC sp_executesql @Sql;

-- 5. XÓA TOÀN BỘ TRIGGERS (Bổ sung thêm để sạch hoàn toàn)
PRINT N'5. Đang xóa các Triggers...';
SET @Sql = N'';
SELECT @Sql += 'DROP TRIGGER ' + QUOTENAME(SCHEMA_NAME(o.schema_id)) + '.' + QUOTENAME(t.name) + ';' + CHAR(13)
FROM sys.triggers t
INNER JOIN sys.objects o ON t.parent_id = o.object_id
WHERE t.is_ms_shipped = 0;

IF LEN(@Sql) > 0 EXEC sp_executesql @Sql;

PRINT N'==================================================';
PRINT N'DỌN DẸP HOÀN TẤT! DATABASE CỦA BẠN ĐÃ SẠCH SẼ TRÊN AZURE.';
PRINT N'==================================================';