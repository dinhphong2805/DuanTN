-- Tạo bảng vouchers
-- Chạy trong SQL Server, database KesnStore

USE KesnStore;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vouchers')
BEGIN
    CREATE TABLE vouchers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code NVARCHAR(50) NOT NULL UNIQUE,
        discount DECIMAL(19,0) NOT NULL DEFAULT 0,
        type NVARCHAR(20) NOT NULL DEFAULT 'percent',
        min_order DECIMAL(19,0) DEFAULT 0
    );
END
GO
