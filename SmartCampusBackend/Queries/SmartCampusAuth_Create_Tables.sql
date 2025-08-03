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

-- Таблица: Roles (НОВАЯ)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Roles' and xtype='U')
CREATE TABLE Roles (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL
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
    role_id INT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Users_Roles FOREIGN KEY (role_id) REFERENCES Roles(id) ON DELETE SET NULL
);
GO

-- Таблица: User_Departments
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='User_Departments' and xtype='U')
CREATE TABLE User_Departments (
    user_id INT NOT NULL,
    department_id INT NOT NULL,
    is_manager BIT DEFAULT 0,
    CONSTRAINT PK_UserDepartments PRIMARY KEY (user_id, department_id),
    CONSTRAINT FK_UserDepartments_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT FK_UserDepartments_Departments FOREIGN KEY (department_id) REFERENCES Departments(id) ON DELETE CASCADE
);
GO

-- Таблица: Superusers
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Superusers' and xtype='U')
CREATE TABLE Superusers (
    user_id INT PRIMARY KEY,
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

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Role_Permissions' and xtype='U')
CREATE TABLE Role_Permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT PK_RolePermissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_RolePermissions_Roles FOREIGN KEY (role_id) REFERENCES Roles(id) ON DELETE CASCADE,
    CONSTRAINT FK_RolePermissions_Permissions FOREIGN KEY (permission_id) REFERENCES Permissions(id) ON DELETE CASCADE
);
GO

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Access_Grants' and xtype='U')
CREATE TABLE Access_Grants (
    id INT PRIMARY KEY IDENTITY(1,1),
    granted_by INT NOT NULL,
    granted_to INT NOT NULL,
    permission_id INT NOT NULL,
    grant_date DATETIME DEFAULT GETDATE(),
    expires_at DATETIME NULL,
    comment NVARCHAR(255),
    CONSTRAINT FK_AccessGrants_GrantedBy FOREIGN KEY (granted_by) REFERENCES Users(id),
    CONSTRAINT FK_AccessGrants_GrantedTo FOREIGN KEY (granted_to) REFERENCES Users(id),
    CONSTRAINT FK_AccessGrants_Permissions FOREIGN KEY (permission_id) REFERENCES Permissions(id)
);
GO

-- Уникальность выдачи одного и того же индивидуального права одному пользователю
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='UX_GrantedIndividualPermission' AND object_id = OBJECT_ID('Access_Grants'))
CREATE UNIQUE INDEX UX_GrantedIndividualPermission
ON Access_Grants(granted_to, permission_id);
GO


-- Таблица: UserDevices
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='UserDevices' and xtype='U')
CREATE TABLE UserDevices (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    device_uuid NVARCHAR(255) NOT NULL,
    is_approved BIT DEFAULT 0,
    description NVARCHAR(255) NULL,
    last_login_at DATETIME NULL,
    registered_at DATETIME DEFAULT GETDATE(),
    approved_at DATETIME NULL,
    approved_by INT NULL,
    CONSTRAINT FK_UserDevices_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT FK_UserDevices_ApprovedBy FOREIGN KEY (approved_by) REFERENCES Users(id) ON DELETE NO ACTION -- Пользователь, одобривший устройство, не должен удаляться каскадно с устройством
);
GO

-- Индексы для UserDevices
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='UX_UserDeviceUUID' AND object_id = OBJECT_ID('UserDevices'))
CREATE UNIQUE INDEX UX_UserDeviceUUID
ON UserDevices(user_id, device_uuid); -- Одно устройство на пользователя
GO


-- 1. Создание базовых Ролей
PRINT 'Inserting Roles...';
IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'Student')
    INSERT INTO Roles (name, description) VALUES ('Student', 'Default role for students');
IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'Teacher')
    INSERT INTO Roles (name, description) VALUES ('Teacher', 'Role for teaching staff');
IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'DepartmentAdmin')
    INSERT INTO Roles (name, description) VALUES ('DepartmentAdmin', 'Administrator for a specific department');
IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'SystemAdmin')
    INSERT INTO Roles (name, description) VALUES ('SystemAdmin', 'Superuser with broad system access, typically IB');
IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'Employee')
    INSERT INTO Roles (name, description) VALUES ('Employee', 'General employee role');
GO

-- 2. Создание базовых Разрешений (Permissions)
PRINT 'Inserting Permissions...';
-- Общие разрешения
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ViewProfile')
    INSERT INTO Permissions (name, description) VALUES ('ViewProfile', 'View own user profile');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'EditProfile')
    INSERT INTO Permissions (name, description) VALUES ('EditProfile', 'Edit own user profile');

-- Разрешения для Студентов
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ViewOwnGrades')
    INSERT INTO Permissions (name, description) VALUES ('ViewOwnGrades', 'View own academic grades');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ViewOwnSchedule')
    INSERT INTO Permissions (name, description) VALUES ('ViewOwnSchedule', 'View own class schedule');

-- Разрешения для Преподавателей
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ViewCourseStudents')
    INSERT INTO Permissions (name, description) VALUES ('ViewCourseStudents', 'View students enrolled in their courses');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ManageGradesForCourse')
    INSERT INTO Permissions (name, description) VALUES ('ManageGradesForCourse', 'Input and modify grades for students in their courses');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ViewDepartmentSchedule')
    INSERT INTO Permissions (name, description) VALUES ('ViewDepartmentSchedule', 'View schedule for their department');


-- Разрешения для Администраторов Отдела
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ManageDepartmentUsers')
    INSERT INTO Permissions (name, description) VALUES ('ManageDepartmentUsers', 'Add, remove, or modify users within their department');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ManageDepartmentSchedule')
    INSERT INTO Permissions (name, description) VALUES ('ManageDepartmentSchedule', 'Manage the schedule for their department');

-- Разрешения для Системных Администраторов (ИБ)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ManageAllUsers')
    INSERT INTO Permissions (name, description) VALUES ('ManageAllUsers', 'Manage any user in the system');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'GrantAnyPermission')
    INSERT INTO Permissions (name, description) VALUES ('GrantAnyPermission', 'Grant any permission to any user or role');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ManageRoles')
    INSERT INTO Permissions (name, description) VALUES ('ManageRoles', 'Create, edit, or delete roles and their permissions');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ApproveDevices')
    INSERT INTO Permissions (name, description) VALUES ('ApproveDevices', 'Approve or reject user devices for system access');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'ViewSystemLogs')
    INSERT INTO Permissions (name, description) VALUES ('ViewSystemLogs', 'View system audit and security logs');
GO


-- 3. Назначение Разрешений Ролям (Role_Permissions)
PRINT 'Assigning Permissions to Roles...';
DECLARE @StudentRoleID INT, @TeacherRoleID INT, @DeptAdminRoleID INT, @SysAdminRoleID INT, @EmployeeRoleID INT;
SELECT @StudentRoleID = id FROM Roles WHERE name = 'Student';
SELECT @TeacherRoleID = id FROM Roles WHERE name = 'Teacher';
SELECT @DeptAdminRoleID = id FROM Roles WHERE name = 'DepartmentAdmin';
SELECT @SysAdminRoleID = id FROM Roles WHERE name = 'SystemAdmin';
SELECT @EmployeeRoleID = id FROM Roles WHERE name = 'Employee';

DECLARE @ViewProfilePermID INT, @EditProfilePermID INT;
DECLARE @ViewOwnGradesPermID INT, @ViewOwnSchedulePermID INT;
DECLARE @ViewCourseStudentsPermID INT, @ManageGradesForCoursePermID INT, @ViewDeptSchedulePermID INT;
DECLARE @ManageDeptUsersPermID INT, @ManageDeptSchedulePermID INT;
DECLARE @ManageAllUsersPermID INT, @GrantAnyPermID INT, @ManageRolesPermID INT, @ApproveDevicesPermID INT, @ViewSystemLogsPermID INT;

SELECT @ViewProfilePermID = id FROM Permissions WHERE name = 'ViewProfile';
SELECT @EditProfilePermID = id FROM Permissions WHERE name = 'EditProfile';
SELECT @ViewOwnGradesPermID = id FROM Permissions WHERE name = 'ViewOwnGrades';
SELECT @ViewOwnSchedulePermID = id FROM Permissions WHERE name = 'ViewOwnSchedule';
SELECT @ViewCourseStudentsPermID = id FROM Permissions WHERE name = 'ViewCourseStudents';
SELECT @ManageGradesForCoursePermID = id FROM Permissions WHERE name = 'ManageGradesForCourse';
SELECT @ViewDeptSchedulePermID = id FROM Permissions WHERE name = 'ViewDepartmentSchedule';
SELECT @ManageDeptUsersPermID = id FROM Permissions WHERE name = 'ManageDepartmentUsers';
SELECT @ManageDeptSchedulePermID = id FROM Permissions WHERE name = 'ManageDepartmentSchedule';
SELECT @ManageAllUsersPermID = id FROM Permissions WHERE name = 'ManageAllUsers';
SELECT @GrantAnyPermID = id FROM Permissions WHERE name = 'GrantAnyPermission';
SELECT @ManageRolesPermID = id FROM Permissions WHERE name = 'ManageRoles';
SELECT @ApproveDevicesPermID = id FROM Permissions WHERE name = 'ApproveDevices';
SELECT @ViewSystemLogsPermID = id FROM Permissions WHERE name = 'ViewSystemLogs';

-- Разрешения для Студента
IF @StudentRoleID IS NOT NULL
BEGIN
    IF @ViewProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @StudentRoleID AND permission_id = @ViewProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@StudentRoleID, @ViewProfilePermID);
    IF @EditProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @StudentRoleID AND permission_id = @EditProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@StudentRoleID, @EditProfilePermID);
    IF @ViewOwnGradesPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @StudentRoleID AND permission_id = @ViewOwnGradesPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@StudentRoleID, @ViewOwnGradesPermID);
    IF @ViewOwnSchedulePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @StudentRoleID AND permission_id = @ViewOwnSchedulePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@StudentRoleID, @ViewOwnSchedulePermID);
END

-- Разрешения для Сотрудника (базовые)
IF @EmployeeRoleID IS NOT NULL
BEGIN
    IF @ViewProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @EmployeeRoleID AND permission_id = @ViewProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@EmployeeRoleID, @ViewProfilePermID);
    IF @EditProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @EmployeeRoleID AND permission_id = @EditProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@EmployeeRoleID, @EditProfilePermID);
    IF @ViewDeptSchedulePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @EmployeeRoleID AND permission_id = @ViewDeptSchedulePermID) -- Например, все сотрудники могут видеть расписание своего отдела
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@EmployeeRoleID, @ViewDeptSchedulePermID);
END

-- Разрешения для Преподавателя (наследуются от Сотрудника + свои)
IF @TeacherRoleID IS NOT NULL
BEGIN
    IF @ViewProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @TeacherRoleID AND permission_id = @ViewProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@TeacherRoleID, @ViewProfilePermID);
    IF @EditProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @TeacherRoleID AND permission_id = @EditProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@TeacherRoleID, @EditProfilePermID);
    IF @ViewDeptSchedulePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @TeacherRoleID AND permission_id = @ViewDeptSchedulePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@TeacherRoleID, @ViewDeptSchedulePermID);
    -- Специфичные для преподавателя
    IF @ViewCourseStudentsPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @TeacherRoleID AND permission_id = @ViewCourseStudentsPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@TeacherRoleID, @ViewCourseStudentsPermID);
    IF @ManageGradesForCoursePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @TeacherRoleID AND permission_id = @ManageGradesForCoursePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@TeacherRoleID, @ManageGradesForCoursePermID);
END

-- Разрешения для Администратора Отдела
IF @DeptAdminRoleID IS NOT NULL
BEGIN
    -- Можно добавить наследование от Employee/Teacher или определить явно
    IF @ViewProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @DeptAdminRoleID AND permission_id = @ViewProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@DeptAdminRoleID, @ViewProfilePermID);
    IF @ManageDeptUsersPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @DeptAdminRoleID AND permission_id = @ManageDeptUsersPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@DeptAdminRoleID, @ManageDeptUsersPermID);
    IF @ManageDeptSchedulePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @DeptAdminRoleID AND permission_id = @ManageDeptSchedulePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@DeptAdminRoleID, @ManageDeptSchedulePermID);
    IF @ApproveDevicesPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @DeptAdminRoleID AND permission_id = @ApproveDevicesPermID) -- Админ отдела может одобрять устройства для своего отдела
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@DeptAdminRoleID, @ApproveDevicesPermID);
END

-- Разрешения для Системного Администратора (ИБ) - обычно имеют все или почти все права
IF @SysAdminRoleID IS NOT NULL
BEGIN
    IF @ManageAllUsersPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @SysAdminRoleID AND permission_id = @ManageAllUsersPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@SysAdminRoleID, @ManageAllUsersPermID);
    IF @GrantAnyPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @SysAdminRoleID AND permission_id = @GrantAnyPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@SysAdminRoleID, @GrantAnyPermID);
    IF @ManageRolesPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @SysAdminRoleID AND permission_id = @ManageRolesPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@SysAdminRoleID, @ManageRolesPermID);
    IF @ApproveDevicesPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @SysAdminRoleID AND permission_id = @ApproveDevicesPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@SysAdminRoleID, @ApproveDevicesPermID);
    IF @ViewSystemLogsPermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @SysAdminRoleID AND permission_id = @ViewSystemLogsPermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@SysAdminRoleID, @ViewSystemLogsPermID);
    -- Можно добавить и остальные разрешения для полноты прав
    IF @ViewProfilePermID IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Role_Permissions WHERE role_id = @SysAdminRoleID AND permission_id = @ViewProfilePermID)
        INSERT INTO Role_Permissions (role_id, permission_id) VALUES (@SysAdminRoleID, @ViewProfilePermID);
    -- ... и так далее для других ключевых разрешений
END
GO

PRINT 'Database schema and initial data setup complete.';
GO
