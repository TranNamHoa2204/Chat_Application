-- Tạo Database (SQL Server không dùng IF NOT EXISTS trực tiếp kiểu MySQL)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'JavaChatApp')
BEGIN
    CREATE DATABASE JavaChatApp;
END
GO

USE JavaChatApp;
GO

-- Tạo bảng Users
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);

-- Tạo bảng Messages
CREATE TABLE messages (
    id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT DEFAULT NULL, -- NULL nghĩa là chat chung (Public), có ID cụ thể là chat riêng (Private)
    content NVARCHAR(MAX) NOT NULL, -- Dùng NVARCHAR để gõ được tiếng Việt có dấu khi chat
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Thêm thử 2 tài khoản mẫu
INSERT INTO users (username, password) VALUES ('admin', '123456');
INSERT INTO users (username, password) VALUES ('guest', '123456');