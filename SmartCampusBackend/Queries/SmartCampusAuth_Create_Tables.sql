-- Создание базы данных (если еще не создана)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'SmartCampusAuth')
BEGIN
    CREATE DATABASE SmartCampusAuth;
END
GO

USE SmartCampusAuth;
GO

-- Таблица: Departments
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Departments' and xtype='U')
CREATE TABLE Departments (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- Таблица: Users
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Users' and xtype='U')
CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    email NVARCHAR(255),
    full_name NVARCHAR(255),
    role NVARCHAR(50) NOT NULL DEFAULT 'Student',
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- Таблица: User_Departments
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='User_Departments' and xtype='U')
CREATE TABLE User_Departments (
    user_id INT NOT NULL, -- PK добавлен ниже
    department_id INT NOT NULL, -- PK добавлен ниже
    is_manager BIT DEFAULT 0,
    CONSTRAINT PK_UserDepartments PRIMARY KEY (user_id, department_id), -- Явно указываем PK как в вашем оригинальном скрипте
    CONSTRAINT FK_UserDepartments_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT FK_UserDepartments_Departments FOREIGN KEY (department_id) REFERENCES Departments(id) ON DELETE CASCADE
);
GO

-- Таблица: Superusers
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Superusers' and xtype='U')
CREATE TABLE Superusers (
    user_id INT PRIMARY KEY, -- user_id является и PK и FK
    CONSTRAINT FK_Superusers_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);
GO

-- Таблица: Permissions
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Permissions' and xtype='U')
CREATE TABLE Permissions (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255)
);
GO

-- Таблица: Access_Grants
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Access_Grants' and xtype='U')
CREATE TABLE Access_Grants (
    id INT PRIMARY KEY IDENTITY(1,1),
    granted_by INT NOT NULL,
    granted_to INT NOT NULL,
    permission_id INT NOT NULL,
    grant_date DATETIME DEFAULT GETDATE(),
    expires_at DATETIME NULL,
    comment NVARCHAR(255),
    CONSTRAINT FK_AccessGrants_GrantedBy FOREIGN KEY (granted_by) REFERENCES Users(id), -- ON DELETE NO ACTION по умолчанию для FK без указания
    CONSTRAINT FK_AccessGrants_GrantedTo FOREIGN KEY (granted_to) REFERENCES Users(id), -- ON DELETE NO ACTION по умолчанию для FK без указания (если не Users(id) ON DELETE CASCADE)
                                                                                          -- В вашем оригинальном скрипте не было ON DELETE CASCADE для granted_to и granted_by в Access_Grants, я это сохраняю.
                                                                                          -- Если Users(id) имеет ON DELETE CASCADE, это поведение может быть переопределено.
                                                                                          -- Для безопасности и сохранения истории, часто оставляют NO ACTION или SET NULL.
    CONSTRAINT FK_AccessGrants_Permissions FOREIGN KEY (permission_id) REFERENCES Permissions(id) -- ON DELETE NO ACTION по умолчанию
);
GO

-- Таблица: UserDevices
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='UserDevices' and xtype='U')
CREATE TABLE UserDevices (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    device_uuid NVARCHAR(255) NOT NULL,
    device_type NVARCHAR(50) NOT NULL,
    is_approved BIT DEFAULT 0,
    description NVARCHAR(255) NULL,
    last_login_at DATETIME NULL,
    registered_at DATETIME DEFAULT GETDATE(),
    approved_at DATETIME NULL,
    approved_by INT NULL,
    CONSTRAINT FK_UserDevices_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT FK_UserDevices_ApprovedBy FOREIGN KEY (approved_by) REFERENCES Users(id) ON DELETE NO ACTION
);
GO

-- Уникальность выдачи одного и того же права одному пользователю
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='UX_GrantedPermission' AND object_id = OBJECT_ID('Access_Grants'))
CREATE UNIQUE INDEX UX_GrantedPermission
ON Access_Grants(granted_to, permission_id);
GO

-- Индексы для UserDevices (оставляем их, так как они относятся к новой таблице и полезны)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='UX_UserDeviceUUID' AND object_id = OBJECT_ID('UserDevices'))
CREATE UNIQUE INDEX UX_UserDeviceUUID
ON UserDevices(user_id, device_uuid);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='IX_UserDeviceType' AND object_id = OBJECT_ID('UserDevices'))
CREATE INDEX IX_UserDeviceType
ON UserDevices(user_id, device_type);
GO