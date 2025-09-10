USE SmartCampusAuth;
GO

PRINT 'Starting full setup for permissions, roles, and users...';

-- 1) Убедимся, что таблицы и FK есть.
--    This part is idempotent and will not fail if tables already exist.
PRINT 'Ensuring all necessary tables and foreign keys exist...';

IF OBJECT_ID('dbo.Roles','U') IS NULL
CREATE TABLE dbo.Roles (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(50) NOT NULL UNIQUE,
                           description NVARCHAR(255) NULL
);
GO
IF OBJECT_ID('dbo.Permissions','U') IS NULL
CREATE TABLE dbo.Permissions (
                                 id INT IDENTITY(1,1) PRIMARY KEY,
                                 name NVARCHAR(100) NOT NULL UNIQUE,
                                 description NVARCHAR(255) NULL
);
GO
IF OBJECT_ID('dbo.Users','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Users (
                                   id INT IDENTITY(1,1) PRIMARY KEY,
                                   username NVARCHAR(100) NOT NULL UNIQUE,
                                   password_hash NVARCHAR(255) NOT NULL,
                                   email NVARCHAR(255) NULL,
                                   full_name NVARCHAR(255) NULL,
                                   role_id INT NULL,
                                   is_active BIT DEFAULT 1,
                                   created_at DATETIME DEFAULT GETDATE(),
                                   student_profile_id INT NULL,
                                   teacher_profile_id INT NULL
        );
    END
ELSE
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'student_profile_id' AND Object_ID = Object_ID(N'dbo.Users'))
        ALTER TABLE dbo.Users ADD student_profile_id INT NULL;
        IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE Name = N'teacher_profile_id' AND Object_ID = Object_ID(N'dbo.Users'))
        ALTER TABLE dbo.Users ADD teacher_profile_id INT NULL;
    END
GO
IF OBJECT_ID('dbo.Superusers','U') IS NULL
CREATE TABLE dbo.Superusers (user_id INT PRIMARY KEY);
GO
IF OBJECT_ID('dbo.Role_Permissions','U') IS NULL
CREATE TABLE dbo.Role_Permissions (
                                      role_id INT NOT NULL,
                                      permission_id INT NOT NULL,
                                      CONSTRAINT PK_RolePermissions PRIMARY KEY (role_id, permission_id)
);
GO
IF OBJECT_ID('FK_RP_Roles','F') IS NULL
    BEGIN
        ALTER TABLE dbo.Role_Permissions
            ADD CONSTRAINT FK_RP_Roles FOREIGN KEY (role_id) REFERENCES dbo.Roles(id) ON DELETE NO ACTION;
    END
GO
IF OBJECT_ID('FK_RP_Permissions','F') IS NULL
    BEGIN
        ALTER TABLE dbo.Role_Permissions
            ADD CONSTRAINT FK_RP_Permissions FOREIGN KEY (permission_id) REFERENCES dbo.Permissions(id) ON DELETE NO ACTION;
    END
GO

-- 2) Очистка и сброс ID
PRINT 'Cleanup complete. Deleting all existing permissions, roles, and users...';

DELETE FROM Access_Grants;
DELETE FROM Role_Permissions;
DELETE FROM Permissions;
DBCC CHECKIDENT ('Permissions', RESEED, 0);
DELETE FROM Superusers;
DELETE FROM Users WHERE username IN ('sudo', 'root');
DELETE FROM Roles WHERE name IN ('Student', 'Teacher', 'SystemAdmin');

PRINT 'Cleanup complete. Inserting new CRUD+Own Permissions for SmartCampus tables...';

-- 3) Вставка всех разрешений
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:create')
    INSERT INTO Permissions (name, description) VALUES ('specialities:create', N'Создание специальностей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:read')
    INSERT INTO Permissions (name, description) VALUES ('specialities:read', N'Просмотр списка специальностей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:update')
    INSERT INTO Permissions (name, description) VALUES ('specialities:update', N'Редактирование специальностей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:delete')
    INSERT INTO Permissions (name, description) VALUES ('specialities:delete', N'Удаление специальностей');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:create')
    INSERT INTO Permissions (name, description) VALUES ('groups:create', N'Создание групп');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:read')
    INSERT INTO Permissions (name, description) VALUES ('groups:read', N'Просмотр списка групп');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:update')
    INSERT INTO Permissions (name, description) VALUES ('groups:update', N'Редактирование групп');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:delete')
    INSERT INTO Permissions (name, description) VALUES ('groups:delete', N'Удаление групп');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:create')
    INSERT INTO Permissions (name, description) VALUES ('subjects:create', N'Создание учебных предметов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:read')
    INSERT INTO Permissions (name, description) VALUES ('subjects:read', N'Просмотр списка учебных предметов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:update')
    INSERT INTO Permissions (name, description) VALUES ('subjects:update', N'Редактирование учебных предметов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:delete')
    INSERT INTO Permissions (name, description) VALUES ('subjects:delete', N'Удаление учебных предметов');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:create')
    INSERT INTO Permissions (name, description) VALUES ('teachers:create', N'Регистрация новых преподавателей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:read_all')
    INSERT INTO Permissions (name, description) VALUES ('teachers:read_all', N'Просмотр информации обо всех преподавателях');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:read_own')
    INSERT INTO Permissions (name, description) VALUES ('teachers:read_own', N'Просмотр своего профиля преподавателя');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:update_all')
    INSERT INTO Permissions (name, description) VALUES ('teachers:update_all', N'Редактирование информации о любом преподавателе');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:update_own')
    INSERT INTO Permissions (name, description) VALUES ('teachers:update_own', N'Редактирование своего профиля преподавателя');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:delete')
    INSERT INTO Permissions (name, description) VALUES ('teachers:delete', N'Удаление профилей преподавателей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers:read_info')
    INSERT INTO Permissions (name, description) VALUES ('teachers:read_info', N'Просмотр доп. информации о любом преподавателе');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_info:update_all')
    INSERT INTO Permissions (name, description) VALUES ('teachers_info:update_all', N'Редактирование доп. информации о любом преподавателе');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_info:update_own')
    INSERT INTO Permissions (name, description) VALUES ('teachers_info:update_own', N'Редактирование своей доп. информации преподавателя');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:create')
    INSERT INTO Permissions (name, description) VALUES ('students:create', N'Регистрация новых студентов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:read_all')
    INSERT INTO Permissions (name, description) VALUES ('students:read_all', N'Просмотр информации обо всех студентах');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:read_own')
    INSERT INTO Permissions (name, description) VALUES ('students:read_own', N'Просмотр своего профиля студента');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:update_all')
    INSERT INTO Permissions (name, description) VALUES ('students:update_all', N'Редактирование информации о любом студенте');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:update_own')
    INSERT INTO Permissions (name, description) VALUES ('students:update_own', N'Редактирование своего профиля студента');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:delete')
    INSERT INTO Permissions (name, description) VALUES ('students:delete', N'Удаление профилей студентов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students:read_info')
    INSERT INTO Permissions (name, description) VALUES ('students:read_info', N'Просмотр доп. информации о любом студенте');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students_info:update_all')
    INSERT INTO Permissions (name, description) VALUES ('students_info:update_all', N'Редактирование доп. информации о любом студенте');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students_info:update_own')
    INSERT INTO Permissions (name, description) VALUES ('students_info:update_own', N'Редактирование своей доп. информации студента');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:create')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:create', N'Добавление аудиторий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:read')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:read', N'Просмотр списка аудиторий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:update')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:update', N'Редактирование информации об аудиториях');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:delete')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:delete', N'Удаление аудиторий');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:create')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:create', N'Добавление дисциплин к специальностям/курсам');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:read')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:read', N'Просмотр списка дисциплин специальностей/курсов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:update')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:update', N'Редактирование дисциплин специальностей/курсов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:delete')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:delete', N'Удаление дисциплин из специальностей/курсов');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculums:create')
    INSERT INTO Permissions (name, description) VALUES ('curriculums:create', N'Создание учебных планов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculums:read')
    INSERT INTO Permissions (name, description) VALUES ('curriculums:read', N'Просмотр учебных планов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculums:update')
    INSERT INTO Permissions (name, description) VALUES ('curriculums:update', N'Редактирование учебных планов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculums:delete')
    INSERT INTO Permissions (name, description) VALUES ('curriculums:delete', N'Удаление учебных планов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculums:approve')
    INSERT INTO Permissions (name, description) VALUES ('curriculums:approve', N'Утверждение учебных планов');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculum_disciplines:manage')
    INSERT INTO Permissions (name, description) VALUES ('curriculum_disciplines:manage', N'Управление дисциплинами в учебных планах (добавление, изменение, удаление)');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculum_disciplines:read')
    INSERT INTO Permissions (name, description) VALUES ('curriculum_disciplines:read', N'Просмотр дисциплин в учебных планах');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_workload:create')
    INSERT INTO Permissions (name, description) VALUES ('teachers_workload:create', N'Формирование нагрузки преподавателей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_workload:read_all')
    INSERT INTO Permissions (name, description) VALUES ('teachers_workload:read_all', N'Просмотр нагрузки всех преподавателей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_workload:read_own')
    INSERT INTO Permissions (name, description) VALUES ('teachers_workload:read_own', N'Просмотр своей учебной нагрузки');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_workload:update')
    INSERT INTO Permissions (name, description) VALUES ('teachers_workload:update', N'Редактирование нагрузки преподавателей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_workload:delete')
    INSERT INTO Permissions (name, description) VALUES ('teachers_workload:delete', N'Удаление записей о нагрузке преподавателей');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'schedule:create')
    INSERT INTO Permissions (name, description) VALUES ('schedule:create', N'Составление расписания занятий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'schedule:read_all')
    INSERT INTO Permissions (name, description) VALUES ('schedule:read_all', N'Просмотр полного расписания занятий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'schedule:read_group')
    INSERT INTO Permissions (name, description) VALUES ('schedule:read_group', N'Просмотр расписания своей учебной группы');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'schedule:read_teacher')
    INSERT INTO Permissions (name, description) VALUES ('schedule:read_teacher', N'Просмотр своего расписания занятий (преподаватель)');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'schedule:update')
    INSERT INTO Permissions (name, description) VALUES ('schedule:update', N'Редактирование расписания занятий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'schedule:delete')
    INSERT INTO Permissions (name, description) VALUES ('schedule:delete', N'Удаление записей из расписания');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'attendance:create')
    INSERT INTO Permissions (name, description) VALUES ('attendance:create', N'Отметка посещаемости студентов на занятиях');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'attendance:read_all')
    INSERT INTO Permissions (name, description) VALUES ('attendance:read_all', N'Просмотр данных о посещаемости всех студентов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'attendance:read_own_student')
    INSERT INTO Permissions (name, description) VALUES ('attendance:read_own_student', N'Просмотр своей посещаемости (студент)');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'attendance:read_group')
    INSERT INTO Permissions (name, description) VALUES ('attendance:read_group', N'Просмотр посещаемости учебной группы');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'attendance:update')
    INSERT INTO Permissions (name, description) VALUES ('attendance:update', N'Редактирование данных о посещаемости');

IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'grades:create')
    INSERT INTO Permissions (name, description) VALUES ('grades:create', N'Выставление оценок студентам');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'grades:read_all')
    INSERT INTO Permissions (name, description) VALUES ('grades:read_all', N'Просмотр всех оценок студентов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'grades:read_own_student')
    INSERT INTO Permissions (name, description) VALUES ('grades:read_own_student', N'Просмотр своих оценок (студент)');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'grades:read_group')
    INSERT INTO Permissions (name, description) VALUES ('grades:read_group', N'Просмотр оценок учебной группы');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'grades:update')
    INSERT INTO Permissions (name, description) VALUES ('grades:update', N'Редактирование оценок студентов');
GO

PRINT 'All permissions have been inserted.';
PRINT 'Ensuring base roles exist...';

-- 4) Вставляем базовые роли (idempotent)
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE name = 'Student') INSERT INTO dbo.Roles (name, description) VALUES ('Student', N'Default role for students');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE name = 'Teacher') INSERT INTO dbo.Roles (name, description) VALUES ('Teacher', N'Role for teaching staff');
IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE name = 'SystemAdmin') INSERT INTO dbo.Roles (name, description) VALUES ('SystemAdmin', N'Superuser with broad system access');
GO

-- 5) Создаём/обновляем базовые аккаунты (idempotent)
PRINT 'Ensuring base users exist...';

DECLARE @bcrypt_default NVARCHAR(255) = '$2a$10$63PCuwOhLeJxliJTAjbBr.0HYxdjFig2C55ChHZmDsqHM0PAeE99K';
IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE username = 'sudo')
    INSERT INTO dbo.Users (username, password_hash, email, full_name, role_id) VALUES ('sudo', @bcrypt_default, 'sudo@admin', N'System Administrator', (SELECT id FROM dbo.Roles WHERE name = 'SystemAdmin'));
ELSE
    UPDATE dbo.Users SET role_id = (SELECT id FROM dbo.Roles WHERE name = 'SystemAdmin') WHERE username = 'sudo';

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE username = 'root')
    INSERT INTO dbo.Users (username, password_hash, email, full_name, role_id) VALUES ('root', @bcrypt_default, 'root@sudo', N'Root Superadmin', (SELECT id FROM dbo.Roles WHERE name = 'SystemAdmin'));
ELSE
    UPDATE dbo.Users SET role_id = (SELECT id FROM dbo.Roles WHERE name = 'SystemAdmin') WHERE username = 'root';
GO

-- 6) Помещаем 'sudo' и 'root' в Superusers (idempotent)
PRINT 'Ensuring superusers entries...';

INSERT INTO dbo.Superusers (user_id)
SELECT u.id FROM dbo.Users u
WHERE u.username IN ('sudo','root')
  AND NOT EXISTS (SELECT 1 FROM dbo.Superusers s WHERE s.user_id = u.id);
GO

-- 7) Триггеры для защиты от удаления (остаются без изменений)
PRINT 'Ensuring protection triggers exist...';

IF OBJECT_ID('dbo.trg_PreventDelete_ProtectedRoles','TR') IS NOT NULL DROP TRIGGER dbo.trg_PreventDelete_ProtectedRoles;
GO
CREATE TRIGGER dbo.trg_PreventDelete_ProtectedRoles ON dbo.Roles INSTEAD OF DELETE AS
BEGIN
    IF EXISTS (SELECT 1 FROM deleted WHERE name IN ('SystemAdmin','Student','Teacher'))
        BEGIN
            RAISERROR('Cannot delete protected role(s): SystemAdmin / Student / Teacher',16,1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
    DELETE FROM dbo.Roles WHERE id IN (SELECT id FROM deleted);
END;
GO

IF OBJECT_ID('dbo.trg_PreventDelete_Superusers','TR') IS NOT NULL DROP TRIGGER dbo.trg_PreventDelete_Superusers;
GO
CREATE TRIGGER dbo.trg_PreventDelete_Superusers ON dbo.Users INSTEAD OF DELETE AS
BEGIN
    IF EXISTS (SELECT 1 FROM deleted d JOIN dbo.Superusers s ON d.id = s.user_id)
        BEGIN
            RAISERROR('Cannot delete protected superuser account(s).',16,1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
    DELETE FROM dbo.Users WHERE id IN (SELECT id FROM deleted);
END;
GO

-- 8) Назначим SystemAdmin ВСЕМ permissions (idempotent)
PRINT 'Granting all permissions to SystemAdmin role (idempotent)...';

DECLARE @sysRoleId INT = (SELECT id FROM dbo.Roles WHERE name = 'SystemAdmin');
IF @sysRoleId IS NOT NULL
    BEGIN
        INSERT INTO dbo.Role_Permissions (role_id, permission_id)
        SELECT @sysRoleId, p.id
        FROM dbo.Permissions p
        WHERE NOT EXISTS (
            SELECT 1 FROM dbo.Role_Permissions rp WHERE rp.role_id = @sysRoleId AND rp.permission_id = p.id
        );
    END
GO

PRINT 'Full setup complete.';
GO
