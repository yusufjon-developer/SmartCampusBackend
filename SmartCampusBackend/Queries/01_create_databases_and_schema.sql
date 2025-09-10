/***********************************************
  01_create_databases_and_schema.sql
  Создаёт базы SmartCampus и SmartCampusAuth и все таблицы,
  если их ещё нет. Идемпотентно — можно запускать повторно.
  Предназначено для MS SQL Server (SSMS).
***********************************************/

-- --------------------------
--  Create SmartCampus DB
-- --------------------------
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = N'SmartCampus')
    BEGIN
        PRINT 'Creating database SmartCampus...';
        CREATE DATABASE [SmartCampus];
    END
GO

USE [SmartCampus];
GO

SET NOCOUNT ON;
-- create tables in correct order (referenced tables first)

-- Specialities
IF OBJECT_ID('dbo.Specialities','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Specialities (
                                          id INT IDENTITY(1,1) PRIMARY KEY,
                                          name NVARCHAR(255) NOT NULL
        );
    END
GO

-- Subjects
IF OBJECT_ID('dbo.Subjects','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Subjects (
                                      id INT IDENTITY(1,1) PRIMARY KEY,
                                      name NVARCHAR(255) NOT NULL
        );
    END
GO

-- Auditoriums
IF OBJECT_ID('dbo.Auditoriums','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Auditoriums (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         number NVARCHAR(100) NULL,
                                         type NVARCHAR(100) NULL
        );
    END
GO

-- Teachers
IF OBJECT_ID('dbo.Teachers','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Teachers (
                                      id INT IDENTITY(1,1) PRIMARY KEY,
                                      surname NVARCHAR(255) NULL,
                                      name NVARCHAR(255) NULL,
                                      lastname NVARCHAR(255) NULL,
                                      birthday DATE NULL,
                                      phone_number NVARCHAR(50) NULL,
                                      photo VARBINARY(MAX) NULL
        );
    END
GO

-- Groups (depends on Specialities)
IF OBJECT_ID('dbo.Groups','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Groups (
                                    id INT IDENTITY(1,1) PRIMARY KEY,
                                    name NVARCHAR(255) NOT NULL,
                                    spec_id INT NULL,
                                    course INT NULL,
                                    CONSTRAINT FK_Groups_Specialities FOREIGN KEY (spec_id) REFERENCES dbo.Specialities(id) ON DELETE CASCADE
        );
    END
GO

-- Students (depends on Groups)
IF OBJECT_ID('dbo.Students','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Students (
                                      id INT IDENTITY(1,1) PRIMARY KEY,
                                      surname NVARCHAR(255) NULL,
                                      name NVARCHAR(255) NULL,
                                      lastname NVARCHAR(255) NULL,
                                      birthday DATE NULL,
                                      group_id INT NULL,
                                      phone_number NVARCHAR(50) NULL,
                                      photo VARBINARY(MAX) NULL,
                                      CONSTRAINT FK_Students_Groups FOREIGN KEY (group_id) REFERENCES dbo.Groups(id) ON DELETE CASCADE
        );
    END
GO

-- Teachers_Info
IF OBJECT_ID('dbo.Teachers_Info','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Teachers_Info (
                                           teacher_id INT PRIMARY KEY,
                                           address NTEXT NULL,
                                           passport_number NVARCHAR(100) NULL,
                                           high_school NVARCHAR(255) NULL,
                                           document_number NVARCHAR(100) NULL,
                                           military NVARCHAR(100) NULL,
                                           degree NVARCHAR(100) NULL,
                                           title NVARCHAR(100) NULL,
                                           position NVARCHAR(100) NULL,
                                           CONSTRAINT FK_TeachersInfo_Teachers FOREIGN KEY (teacher_id) REFERENCES dbo.Teachers(id) ON DELETE CASCADE
        );
    END
GO

-- Students_Info
IF OBJECT_ID('dbo.Students_Info','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Students_Info (
                                           student_id INT PRIMARY KEY,
                                           address NTEXT NULL,
                                           passport_number NVARCHAR(100) NULL,
                                           school NVARCHAR(255) NULL,
                                           document_number NVARCHAR(100) NULL,
                                           military NVARCHAR(100) NULL,
                                           student_card_number NVARCHAR(100) NULL,
                                           study_type NVARCHAR(100) NULL,
                                           study_form NVARCHAR(100) NULL,
                                           status NVARCHAR(100) NULL,
                                           father_fio NVARCHAR(255) NULL,
                                           father_phone NVARCHAR(50) NULL,
                                           father_address NTEXT NULL,
                                           mother_fio NVARCHAR(255) NULL,
                                           mother_phone NVARCHAR(50) NULL,
                                           mother_address NTEXT NULL,
                                           CONSTRAINT FK_StudentsInfo_Students FOREIGN KEY (student_id) REFERENCES dbo.Students(id) ON DELETE CASCADE
        );
    END
GO

-- Disciplines (depends on Subjects and Specialities)
IF OBJECT_ID('dbo.Disciplines','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Disciplines (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         subject_id INT NULL,
                                         semester INT NULL,
                                         speciality_id INT NULL,
                                         course INT NULL,
                                         lecture INT NULL,
                                         practice INT NULL,
                                         lab INT NULL,
                                         seminar INT NULL,
                                         control NVARCHAR(100) NULL,
                                         CONSTRAINT FK_Disciplines_Subjects FOREIGN KEY (subject_id) REFERENCES dbo.Subjects(id) ON DELETE CASCADE,
                                         CONSTRAINT FK_Disciplines_Specialities FOREIGN KEY (speciality_id) REFERENCES dbo.Specialities(id) ON DELETE CASCADE
        );
    END
GO

-- Curriculums
IF OBJECT_ID('dbo.Curriculums','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Curriculums (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         speciality_id INT NULL,
                                         year INT NULL,
                                         profile NVARCHAR(255) NULL,
                                         education_form NVARCHAR(100) NULL,
                                         degree NVARCHAR(100) NULL,
                                         duration INT NULL,
                                         approved_date DATE NULL,
                                         CONSTRAINT FK_Curriculums_Specialities FOREIGN KEY (speciality_id) REFERENCES dbo.Specialities(id) ON DELETE CASCADE
        );
    END
GO

-- Curriculum_Disciplines
IF OBJECT_ID('dbo.Curriculum_Disciplines','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Curriculum_Disciplines (
                                                    id INT IDENTITY(1,1) PRIMARY KEY,
                                                    curriculum_id INT NULL,
                                                    discipline_id INT NULL,
                                                    semester INT NULL,
                                                    course INT NULL,
                                                    exam BIT NULL,
                                                    credit BIT NULL,
                                                    coursework BIT NULL,
                                                    control_type NVARCHAR(100) NULL,
                                                    credits INT NULL,
                                                    CONSTRAINT FK_CurriculumDisciplines_Curriculums FOREIGN KEY (curriculum_id) REFERENCES dbo.Curriculums(id) ON DELETE CASCADE,
                                                    CONSTRAINT FK_CurriculumDisciplines_Disciplines FOREIGN KEY (discipline_id) REFERENCES dbo.Disciplines(id) ON DELETE NO ACTION
        );
    END
GO

-- Teachers_Workload
IF OBJECT_ID('dbo.Teachers_Workload','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Teachers_Workload (
                                               id INT IDENTITY(1,1) PRIMARY KEY,
                                               teacher_id INT NULL,
                                               discipline_id INT NULL,
                                               type NVARCHAR(100) NULL,
                                               hours INT NULL,
                                               academic_year NVARCHAR(20) NULL,
                                               control_type NVARCHAR(100) NULL,
                                               group_id INT NULL,
                                               CONSTRAINT FK_TeachersWorkload_Teachers FOREIGN KEY (teacher_id) REFERENCES dbo.Teachers(id) ON DELETE CASCADE,
                                               CONSTRAINT FK_TeachersWorkload_Disciplines FOREIGN KEY (discipline_id) REFERENCES dbo.Disciplines(id) ON DELETE NO ACTION,
                                               CONSTRAINT FK_TeachersWorkload_Groups FOREIGN KEY (group_id) REFERENCES dbo.Groups(id) ON DELETE NO ACTION
        );
    END
GO

-- Workload_Execution
IF OBJECT_ID('dbo.Workload_Execution','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Workload_Execution (
                                                id INT IDENTITY(1,1) PRIMARY KEY,
                                                workload_id INT NOT NULL,
                                                execution_date DATE NOT NULL,
                                                hours DECIMAL(5,2) NOT NULL CHECK (hours >= 0),
                                                executed_by INT NULL,
                                                notes NVARCHAR(1000) NULL,
                                                status NVARCHAR(50) NULL,
                                                created_at DATETIME NOT NULL DEFAULT GETDATE(),
                                                CONSTRAINT FK_WorkloadExecution_Workload FOREIGN KEY (workload_id) REFERENCES dbo.Teachers_Workload(id) ON DELETE CASCADE,
                                                CONSTRAINT FK_WorkloadExecution_ExecutedBy FOREIGN KEY (executed_by) REFERENCES dbo.Teachers(id) ON DELETE NO ACTION
        );

        IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_WorkloadExecution_WorkloadId' AND object_id = OBJECT_ID('dbo.Workload_Execution'))
            BEGIN
                CREATE INDEX IX_WorkloadExecution_WorkloadId ON dbo.Workload_Execution(workload_id);
            END
    END
GO

-- Schedule
IF OBJECT_ID('dbo.Schedule','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Schedule (
                                      id INT IDENTITY(1,1) PRIMARY KEY,
                                      workload_id INT NULL,
                                      day DATE NULL,
                                      start_time DATETIME2 NULL,
                                      end_time DATETIME2 NULL,
                                      group_id INT NULL,
                                      discipline_id INT NULL,
                                      teacher_id INT NULL,
                                      auditorium_id INT NULL,
                                      type NVARCHAR(100) NULL,
                                      CONSTRAINT FK_Schedule_Groups FOREIGN KEY (group_id) REFERENCES dbo.Groups(id) ON DELETE CASCADE,
                                      CONSTRAINT FK_Schedule_Disciplines FOREIGN KEY (discipline_id) REFERENCES dbo.Disciplines(id) ON DELETE NO ACTION,
                                      CONSTRAINT FK_Schedule_Teachers FOREIGN KEY (teacher_id) REFERENCES dbo.Teachers(id) ON DELETE NO ACTION,
                                      CONSTRAINT FK_Schedule_Auditoriums FOREIGN KEY (auditorium_id) REFERENCES dbo.Auditoriums(id) ON DELETE NO ACTION,
                                      CONSTRAINT FK_Schedule_Teachers_Workload FOREIGN KEY (workload_id) REFERENCES dbo.Teachers_Workload(id) ON DELETE NO ACTION
        );
    END
GO

-- Attendance
IF OBJECT_ID('dbo.Attendance','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Attendance (
                                        id INT IDENTITY(1,1) PRIMARY KEY,
                                        day DATE NULL,
                                        time TIME NULL,
                                        student_id INT NULL,
                                        discipline_id INT NULL,
                                        mark NVARCHAR(100) NULL,
                                        CONSTRAINT FK_Attendance_Students FOREIGN KEY (student_id) REFERENCES dbo.Students(id) ON DELETE CASCADE,
                                        CONSTRAINT FK_Attendance_Disciplines FOREIGN KEY (discipline_id) REFERENCES dbo.Disciplines(id) ON DELETE NO ACTION
        );
    END
GO

-- Grades
IF OBJECT_ID('dbo.Grades','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Grades (
                                    id INT IDENTITY(1,1) PRIMARY KEY,
                                    day DATE NULL,
                                    time TIME NULL,
                                    student_id INT NULL,
                                    discipline_id INT NULL,
                                    teacher_id INT NULL,
                                    round NVARCHAR(50) NULL,
                                    mark NVARCHAR(100) NULL,
                                    CONSTRAINT FK_Grades_Students FOREIGN KEY (student_id) REFERENCES dbo.Students(id) ON DELETE CASCADE,
                                    CONSTRAINT FK_Grades_Disciplines FOREIGN KEY (discipline_id) REFERENCES dbo.Disciplines(id) ON DELETE NO ACTION,
                                    CONSTRAINT FK_Grades_Teachers FOREIGN KEY (teacher_id) REFERENCES dbo.Teachers(id) ON DELETE NO ACTION
        );
    END
GO

-- Academic_Weeks
IF OBJECT_ID('dbo.Academic_Weeks','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Academic_Weeks (
                                            id INT IDENTITY(1,1) PRIMARY KEY,
                                            academic_year NVARCHAR(20) NOT NULL,
                                            week_number INT NOT NULL,
                                            start_date DATE NOT NULL,
                                            end_date DATE NOT NULL,
                                            description NVARCHAR(255) NULL,
                                            is_active BIT NOT NULL DEFAULT 1,
                                            created_at DATETIME NOT NULL DEFAULT GETDATE()
        );
        IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'UX_AcademicWeeks_Year_Week' AND object_id = OBJECT_ID('dbo.Academic_Weeks'))
            BEGIN
                CREATE UNIQUE INDEX UX_AcademicWeeks_Year_Week ON dbo.Academic_Weeks(academic_year, week_number);
            END
    END
GO

PRINT 'SmartCampus schema ensured.';
GO

-- --------------------------
--  Create SmartCampusAuth DB
-- --------------------------
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = N'SmartCampusAuth')
    BEGIN
        PRINT 'Creating database SmartCampusAuth...';
        CREATE DATABASE [SmartCampusAuth];
    END
GO

USE [SmartCampusAuth];
GO

SET NOCOUNT ON;

-- Departments
IF OBJECT_ID('dbo.Departments','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Departments (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         name NVARCHAR(100) NOT NULL UNIQUE
        );
    END
GO

-- Roles
IF OBJECT_ID('dbo.Roles','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Roles (
                                   id INT IDENTITY(1,1) PRIMARY KEY,
                                   name NVARCHAR(50) NOT NULL UNIQUE,
                                   description NVARCHAR(255) NULL
        );
    END
GO

-- Users (single definition)
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
                                   teacher_profile_id INT NULL,
                                   CONSTRAINT FK_Users_Roles FOREIGN KEY (role_id) REFERENCES dbo.Roles(id) ON DELETE SET NULL,
                                   CONSTRAINT CK_Users_ProfileXor CHECK (
                                       (CASE WHEN student_profile_id IS NULL THEN 0 ELSE 1 END) +
                                       (CASE WHEN teacher_profile_id IS NULL THEN 0 ELSE 1 END) <= 1
                                       ),
                                   CONSTRAINT UQ_Users_StudentProfileId UNIQUE (student_profile_id),
                                   CONSTRAINT UQ_Users_TeacherProfileId UNIQUE (teacher_profile_id)
        );
    END
GO

-- Superusers
IF OBJECT_ID('dbo.Superusers','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Superusers (
                                        user_id INT PRIMARY KEY,
                                        CONSTRAINT FK_Superusers_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE
        );
    END
GO

-- Permissions
IF OBJECT_ID('dbo.Permissions','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Permissions (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         name NVARCHAR(100) NOT NULL UNIQUE,
                                         description NVARCHAR(255) NULL
        );
    END
GO

-- Role_Permissions
IF OBJECT_ID('dbo.Role_Permissions','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Role_Permissions (
                                              role_id INT NOT NULL,
                                              permission_id INT NOT NULL,
                                              CONSTRAINT PK_RolePermissions PRIMARY KEY (role_id, permission_id),
                                              CONSTRAINT FK_RolePermissions_Roles FOREIGN KEY (role_id) REFERENCES dbo.Roles(id) ON DELETE CASCADE,
                                              CONSTRAINT FK_RolePermissions_Permissions FOREIGN KEY (permission_id) REFERENCES dbo.Permissions(id) ON DELETE CASCADE
        );
    END
GO

-- Access_Grants
IF OBJECT_ID('dbo.Access_Grants','U') IS NULL
    BEGIN
        CREATE TABLE dbo.Access_Grants (
                                           id INT IDENTITY(1,1) PRIMARY KEY,
                                           granted_by INT NOT NULL,
                                           granted_to INT NOT NULL,
                                           permission_id INT NOT NULL,
                                           grant_date DATETIME DEFAULT GETDATE(),
                                           expires_at DATETIME NULL,
                                           comment NVARCHAR(255) NULL,
                                           CONSTRAINT FK_AccessGrants_GrantedBy FOREIGN KEY (granted_by) REFERENCES dbo.Users(id),
                                           CONSTRAINT FK_AccessGrants_GrantedTo FOREIGN KEY (granted_to) REFERENCES dbo.Users(id),
                                           CONSTRAINT FK_AccessGrants_Permissions FOREIGN KEY (permission_id) REFERENCES dbo.Permissions(id)
        );
        IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'UX_GrantedIndividualPermission' AND object_id = OBJECT_ID('dbo.Access_Grants'))
            BEGIN
                CREATE UNIQUE INDEX UX_GrantedIndividualPermission ON dbo.Access_Grants(granted_to, permission_id);
            END
    END
GO

-- User_Departments
IF OBJECT_ID('dbo.User_Departments','U') IS NULL
    BEGIN
        CREATE TABLE dbo.User_Departments (
                                              user_id INT NOT NULL,
                                              department_id INT NOT NULL,
                                              is_manager BIT DEFAULT 0,
                                              CONSTRAINT PK_UserDepartments PRIMARY KEY (user_id, department_id),
                                              CONSTRAINT FK_UserDepartments_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE,
                                              CONSTRAINT FK_UserDepartments_Departments FOREIGN KEY (department_id) REFERENCES dbo.Departments(id) ON DELETE CASCADE
        );
    END
GO

-- UserDevices
IF OBJECT_ID('dbo.UserDevices','U') IS NULL
    BEGIN
        CREATE TABLE dbo.UserDevices (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         user_id INT NOT NULL,
                                         device_uuid NVARCHAR(255) NOT NULL,
                                         is_approved BIT DEFAULT 0,
                                         description NVARCHAR(255) NULL,
                                         last_login_at DATETIME NULL,
                                         registered_at DATETIME DEFAULT GETDATE(),
                                         approved_at DATETIME NULL,
                                         approved_by INT NULL,
                                         CONSTRAINT FK_UserDevices_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(id) ON DELETE CASCADE,
                                         CONSTRAINT FK_UserDevices_ApprovedBy FOREIGN KEY (approved_by) REFERENCES dbo.Users(id) ON DELETE NO ACTION
        );
        IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'UX_UserDeviceUUID' AND object_id = OBJECT_ID('dbo.UserDevices'))
            BEGIN
                CREATE UNIQUE INDEX UX_UserDeviceUUID ON dbo.UserDevices(user_id, device_uuid);
            END
    END
GO

PRINT 'Dropping old UNIQUE constraints on Users table if they exist...';
-- Удаляем старые UNIQUE constraints
IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_Users_StudentProfileId' AND type = 'UQ')
    BEGIN
        ALTER TABLE dbo.Users DROP CONSTRAINT UQ_Users_StudentProfileId;
    END
GO

IF EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'UQ_Users_TeacherProfileId' AND type = 'UQ')
    BEGIN
        ALTER TABLE dbo.Users DROP CONSTRAINT UQ_Users_TeacherProfileId;
    END
GO

PRINT 'Creating new FILTERED UNIQUE indexes on Users table...';
-- Создаём правильные ФИЛЬТРОВАННЫЕ уникальные индексы
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Users_StudentProfileId_UniqueNotNull')
    BEGIN
        CREATE UNIQUE INDEX IX_Users_StudentProfileId_UniqueNotNull
            ON dbo.Users(student_profile_id)
            WHERE student_profile_id IS NOT NULL;
    END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Users_TeacherProfileId_UniqueNotNull')
    BEGIN
        CREATE UNIQUE INDEX IX_Users_TeacherProfileId_UniqueNotNull
            ON dbo.Users(teacher_profile_id)
            WHERE teacher_profile_id IS NOT NULL;
    END
GO

PRINT 'Schema for Users table has been corrected successfully.';

PRINT 'SmartCampusAuth schema ensured.';
GO

-- End of script
