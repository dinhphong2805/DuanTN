# Kesn Store Backend

Spring Boot + SQL Server (sa / 123)

## Chạy app

1. Đảm bảo SQL Server đang chạy (localhost:1433)
2. Tạo database: `CREATE DATABASE KesnStore;`
3. `mvn spring-boot:run` hoặc chạy từ IDE

App tự tạo bảng `users` và **tự insert admin** khi khởi động:
- **Email:** admin@kesn.com
- **Password:** 123

## Insert admin thủ công (SQL)

Nếu cần chạy SQL trực tiếp:

```sql
USE KesnStore;

INSERT INTO users (email, password, full_name, phone, role) 
VALUES (
  'admin@kesn.com', 
  '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', 
  N'Admin Kesn', 
  '0901234567', 
  'admin'
);
```

(Password `123` đã được hash BCrypt)
