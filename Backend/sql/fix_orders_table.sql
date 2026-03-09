-- Sửa bảng orders và order_items cho đúng schema backend
-- Chạy trong SQL Server, database KesnStore

USE KesnStore;
GO

-- Xóa tất cả FK tham chiếu tới orders
DECLARE @sql NVARCHAR(MAX) = '';
SELECT @sql = @sql + 'ALTER TABLE [' + OBJECT_SCHEMA_NAME(parent_object_id) + '].[' + OBJECT_NAME(parent_object_id) + '] DROP CONSTRAINT [' + name + '];'
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('orders');
IF LEN(@sql) > 0 EXEC sp_executesql @sql;
GO

-- Xóa bảng order_items
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'order_items')
    DROP TABLE order_items;
GO

-- Xóa bảng orders
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'orders')
    DROP TABLE orders;
GO

-- Tạo lại bảng orders đúng schema
CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,
    customer_name NVARCHAR(255),
    customer_email NVARCHAR(255),
    customer_phone NVARCHAR(100),
    address NVARCHAR(500),
    status NVARCHAR(20) NOT NULL DEFAULT 'pending',
    total DECIMAL(19,0) NOT NULL DEFAULT 0,
    created_at DATETIMEOFFSET
);
GO

-- Tạo lại bảng order_items
CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name NVARCHAR(255),
    quantity INT DEFAULT 1,
    unit_price DECIMAL(19,0) DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
GO
