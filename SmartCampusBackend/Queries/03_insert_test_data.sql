/***********************************************
  03_insert_test_data.sql (CORRECTED & OPTIMIZED v2)
  Генерация большого объема тестовых данных для SmartCampus и SmartCampusAuth
  Идемпотентно, быстро и надежно.
  Предназначено для MS SQL Server.
***********************************************/

---------------------------------------
-- PART A: SmartCampus (main DB)
---------------------------------------
USE [SmartCampus];
GO

SET NOCOUNT ON;

-- ========== Config: настраиваемый объём данных ==========
DECLARE
    @specialitiesCount INT = 10,
    @subjectsCount INT = 60,
    @teachersCount INT = 80,
    @auditoriumsCount INT = 20,
    @groupsPerSpec INT = 6,
    @studentsPerGroup INT = 25,
    @disciplinesCount INT = 220,
    @curriculumsPerSpec INT = 2,
    @disciplinesPerCurriculum INT = 12,
    @workloadsPerTeacher INT = 6,
    @executionsPerWorkload INT = 3, -- [FIX] Возвращаем недостающую переменную
    @scheduleEntries INT = 1500,
    @attendanceRecords INT = 5000,
    @gradeRecords INT = 4000;
-- ======================================================

-- Увеличиваем размер таблицы #nums, чтобы хватило для всех операций
IF OBJECT_ID('tempdb..#nums') IS NOT NULL DROP TABLE #nums;
CREATE TABLE #nums(n INT PRIMARY KEY);
INSERT INTO #nums(n)
SELECT TOP (50000) ROW_NUMBER() OVER (ORDER BY (SELECT NULL))
FROM sys.all_columns a CROSS JOIN sys.all_columns b;

PRINT 'Starting test data generation for SmartCampus...';

-- 1) Specialities
IF OBJECT_ID('Specialities','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Specialities)
    BEGIN
        PRINT '-> Inserting Specialities...';
        INSERT INTO Specialities (name)
        SELECT TOP (@specialitiesCount) CONCAT('Speciality ', n) FROM #nums ORDER BY n;
    END

-- 2) Subjects
IF OBJECT_ID('Subjects','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Subjects)
    BEGIN
        PRINT '-> Inserting Subjects...';
        INSERT INTO Subjects (name)
        SELECT TOP (@subjectsCount) CONCAT('Subject_', RIGHT('000' + CAST(n AS VARCHAR(6)),6)) FROM #nums ORDER BY n;
    END

-- 3) Auditoriums
IF OBJECT_ID('Auditoriums','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Auditoriums)
    BEGIN
        PRINT '-> Inserting Auditoriums...';
        INSERT INTO Auditoriums (number, type)
        SELECT TOP (@auditoriumsCount)
            CONCAT('A-', RIGHT('00' + CAST(n AS VARCHAR(3)), 3)),
            CHOOSE((n % 4) + 1, 'Lecture Hall', 'Lab', 'Seminar Room', 'Computer Lab')
        FROM #nums;
    END

-- 4) Teachers
IF OBJECT_ID('Teachers','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Teachers)
    BEGIN
        PRINT '-> Inserting Teachers...';
        INSERT INTO Teachers (surname, name, lastname, birthday, phone_number)
        SELECT TOP (@teachersCount)
            CONCAT('TeacherSurname', n), CONCAT('TeacherName', n), CONCAT('TLast', n),
            DATEADD(DAY, - (9000 + (n % 4000)), GETDATE()),
            CONCAT('+7-9', FORMAT(n % 10000000, '0000000'))
        FROM #nums;
    END

-- 5) Groups
IF OBJECT_ID('Groups','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Groups)
    BEGIN
        PRINT '-> Creating Groups (set-based)...';
        INSERT INTO Groups (name, spec_id, course)
        SELECT
            CONCAT('G_', s.id, '_', g.n),
            s.id,
            (ABS(CHECKSUM(NEWID())) % 4) + 1
        FROM Specialities s
                 CROSS JOIN (SELECT TOP (@groupsPerSpec) n FROM #nums) g;
    END

-- 6) Students
IF OBJECT_ID('Students','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Students)
    BEGIN
        PRINT '-> Inserting Students (set-based)...';
        DECLARE @totalGroups INT = (SELECT COUNT(*) FROM Groups);
        IF @totalGroups > 0
            BEGIN
                ;WITH NumberedGroups AS (
                    SELECT id, ROW_NUMBER() OVER(ORDER BY id) as rn
                    FROM Groups
                )
                 INSERT INTO Students (surname, name, lastname, birthday, group_id, phone_number)
                 SELECT
                     CONCAT('Surn', n.n), CONCAT('Name', n.n), CONCAT('Last', n.n),
                     DATEADD(DAY, - (7000 + (n.n % 4000)), GETDATE()),
                     g.id,
                     CONCAT('+7-8', FORMAT(n.n % 10000000, '0000000'))
                 FROM (SELECT TOP (@totalGroups * @studentsPerGroup) n FROM #nums) n
                          JOIN NumberedGroups g ON (n.n - 1) % @totalGroups + 1 = g.rn;
            END
    END

-- Для всех последующих вставок подготовим временные таблицы с нумерованными ID.
DROP TABLE IF EXISTS #RndSubjects, #RndSpecs, #RndTeachers, #RndGroups, #RndDisciplines, #RndAuditoriums, #RndWorkloads, #RndStudents;
SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndSubjects FROM Subjects;
SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndSpecs FROM Specialities;
SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndTeachers FROM Teachers;
SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndGroups FROM Groups;
SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndAuditoriums FROM Auditoriums;

-- 7) Disciplines
IF OBJECT_ID('Disciplines','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Disciplines)
    BEGIN
        PRINT '-> Inserting Disciplines (set-based)...';
        DECLARE @subjCount INT = (SELECT COUNT(*) FROM #RndSubjects), @specCount INT = (SELECT COUNT(*) FROM #RndSpecs);
        INSERT INTO Disciplines (subject_id, semester, speciality_id, course, lecture, practice, lab, seminar, control)
        SELECT
            s.id, (n.n % 8) + 1, sp.id, (n.n % 4) + 1,
            (n.n % 50), ( (n.n+10) % 50), ( (n.n+20) % 30), ( (n.n+30) % 20),
            CHOOSE((n.n % 3) + 1, 'Exam', 'Credit', 'Differentiated Credit')
        FROM (SELECT TOP (@disciplinesCount) n FROM #nums) n
                 LEFT JOIN #RndSubjects s ON n.n % @subjCount + 1 = s.rn
                 LEFT JOIN #RndSpecs sp ON n.n % @specCount + 1 = sp.rn;
    END

SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndDisciplines FROM Disciplines;

-- 8) Curriculums and Curriculum_Disciplines
IF OBJECT_ID('Curriculums','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Curriculums)
    BEGIN
        PRINT '-> Inserting Curriculums & Disciplines (set-based with OUTPUT)...';
        DECLARE @discCount INT = (SELECT COUNT(*) FROM #RndDisciplines);
        DECLARE @InsertedCurriculums TABLE (id INT, spec_id INT);

        INSERT INTO Curriculums (speciality_id, year, profile, education_form, degree, duration, approved_date)
        OUTPUT inserted.id, inserted.speciality_id INTO @InsertedCurriculums
        SELECT
            s.id, 2024 + (c.n - 1), CONCAT('Profile_', s.id, '_', c.n),
            'Full-time', 'Bachelor', 4, GETDATE()
        FROM Specialities s
                 CROSS JOIN (SELECT TOP (@curriculumsPerSpec) n FROM #nums) c;

        INSERT INTO Curriculum_Disciplines (curriculum_id, discipline_id, semester, course, exam, credit, coursework, control_type, credits)
        SELECT
            ic.id, d.id, (n.n % 8)+1, (n.n % 4)+1, n.n % 2, (n.n+1) % 2, (n.n+2) % 2,
            CHOOSE((n.n % 2) + 1, 'Exam', 'Credit'), (n.n % 5) + 2
        FROM @InsertedCurriculums ic
                 CROSS JOIN (SELECT TOP (@disciplinesPerCurriculum) n FROM #nums) n
                 JOIN #RndDisciplines d ON n.n % @discCount + 1 = d.rn;
    END

-- 9) Teachers_Workload
IF OBJECT_ID('Teachers_Workload','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Teachers_Workload)
    BEGIN
        PRINT '-> Inserting Teachers_Workload (set-based)...';
        DECLARE @teachCount INT = (SELECT COUNT(*) FROM #RndTeachers), @discCountW INT = (SELECT COUNT(*) FROM #RndDisciplines), @groupCountW INT = (SELECT COUNT(*) FROM #RndGroups);
        INSERT INTO Teachers_Workload (teacher_id, discipline_id, type, hours, academic_year, control_type, group_id)
        SELECT
            t.id, d.id, CHOOSE((n.n % 4) + 1, 'lecture', 'practice', 'lab', 'seminar'),
            (n.n % 120) + 10, '2024/2025', CHOOSE((n.n % 3) + 1, 'exam', 'credit', 'pass/fail'), g.id
        FROM (SELECT TOP (@teachersCount * @workloadsPerTeacher) n FROM #nums) n
                 LEFT JOIN #RndTeachers t ON n.n % @teachCount + 1 = t.rn
                 LEFT JOIN #RndDisciplines d ON (n.n + 10) % @discCountW + 1 = d.rn
                 LEFT JOIN #RndGroups g ON (n.n + 20) % @groupCountW + 1 = g.rn;
    END

SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndWorkloads FROM Teachers_Workload;

-- 10) Workload_Execution
IF OBJECT_ID('Workload_Execution','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Workload_Execution)
    BEGIN
        PRINT '-> Inserting Workload_Execution (set-based)...';
        DECLARE @workloadCount INT = (SELECT COUNT(*) FROM #RndWorkloads), @teachCountE INT = (SELECT COUNT(*) FROM #RndTeachers);
        INSERT INTO Workload_Execution (workload_id, execution_date, hours, executed_by, notes, status)
        SELECT
            w.id, DATEADD(DAY, (n.n % 120), '2024-09-01'), CAST((n.n % 4) + 1.5 AS DECIMAL(5,2)),
            t.id, CONCAT('Auto note ', n.n), CHOOSE((n.n % 3) + 1, 'planned', 'done', 'partial')
        FROM (SELECT TOP (@workloadCount * @executionsPerWorkload) n FROM #nums) n
                 JOIN #RndWorkloads w ON n.n % @workloadCount + 1 = w.rn
                 JOIN #RndTeachers t ON (n.n+1) % @teachCountE + 1 = t.rn;
    END

-- 11) Schedule
IF OBJECT_ID('Schedule','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Schedule)
    BEGIN
        PRINT '-> Inserting Schedule entries (set-based)...';
        DECLARE @workloadCountS INT = (SELECT COUNT(*) FROM #RndWorkloads), @groupCountS INT = (SELECT COUNT(*) FROM #RndGroups), @discCountS INT = (SELECT COUNT(*) FROM #RndDisciplines), @teachCountS INT = (SELECT COUNT(*) FROM #RndTeachers), @audCountS INT = (SELECT COUNT(*) FROM #RndAuditoriums);
        INSERT INTO Schedule (workload_id, day, start_time, end_time, group_id, discipline_id, teacher_id, auditorium_id, type)
        SELECT
            w.id, DATEADD(DAY, n.n % 120, '2024-09-01'),
            CAST(DATEADD(MINUTE, (n.n % 5)*90, '08:30') AS DATETIME2),
            CAST(DATEADD(MINUTE, 80, DATEADD(MINUTE, (n.n % 5)*90, '08:30')) AS DATETIME2),
            g.id, d.id, t.id, a.id,
            CHOOSE((n.n % 4) + 1, 'Lecture', 'Lab', 'Practice', 'Seminar')
        FROM (SELECT TOP (@scheduleEntries) n FROM #nums) n
                 LEFT JOIN #RndWorkloads w ON n.n % @workloadCountS + 1 = w.rn
                 LEFT JOIN #RndGroups g ON (n.n+1) % @groupCountS + 1 = g.rn
                 LEFT JOIN #RndDisciplines d ON (n.n+2) % @discCountS + 1 = d.rn
                 LEFT JOIN #RndTeachers t ON (n.n+3) % @teachCountS + 1 = t.rn
                 LEFT JOIN #RndAuditoriums a ON (n.n+4) % @audCountS + 1 = a.rn;
    END

SELECT id, ROW_NUMBER() OVER(ORDER BY NEWID()) as rn INTO #RndStudents FROM Students;

-- 12) Attendance & Grades
IF OBJECT_ID('Attendance','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Attendance)
    BEGIN
        PRINT '-> Inserting Attendance (set-based)...';
        DECLARE @studCountA INT = (SELECT COUNT(*) FROM #RndStudents), @discCountA INT = (SELECT COUNT(*) FROM #RndDisciplines);
        INSERT INTO Attendance (day, time, student_id, discipline_id, mark)
        SELECT
            DATEADD(DAY, n.n % 120, '2024-09-01'), CAST(DATEADD(MINUTE, (n.n % 300), '08:30') AS TIME),
            s.id, d.id, CHOOSE((n.n % 6)+1, 'A', 'B', 'C', 'D', 'E', 'F')
        FROM (SELECT TOP (@attendanceRecords) n FROM #nums) n
                 LEFT JOIN #RndStudents s ON n.n % @studCountA + 1 = s.rn
                 LEFT JOIN #RndDisciplines d ON (n.n+1) % @discCountA + 1 = d.rn;
    END

IF OBJECT_ID('Grades','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Grades)
    BEGIN
        PRINT '-> Inserting Grades (set-based)...';
        DECLARE @studCountG INT = (SELECT COUNT(*) FROM #RndStudents), @discCountG INT = (SELECT COUNT(*) FROM #RndDisciplines), @teachCountG INT = (SELECT COUNT(*) FROM #RndTeachers);
        INSERT INTO Grades (day, time, student_id, discipline_id, teacher_id, round, mark)
        SELECT
            DATEADD(DAY, n.n % 120, '2024-09-01'), CAST(DATEADD(MINUTE, (n.n % 300), '08:30') AS TIME),
            s.id, d.id, t.id, CHOOSE((n.n % 3) + 1, 'midterm', 'final', 'quiz'), CAST(n.n % 101 AS NVARCHAR(10))
        FROM (SELECT TOP (@gradeRecords) n FROM #nums) n
                 LEFT JOIN #RndStudents s ON n.n % @studCountG + 1 = s.rn
                 LEFT JOIN #RndDisciplines d ON (n.n+1) % @discCountG + 1 = d.rn
                 LEFT JOIN #RndTeachers t ON (n.n+2) % @teachCountG + 1 = t.rn;
    END

-- 13) Academic weeks
IF OBJECT_ID('Academic_Weeks','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Academic_Weeks)
    BEGIN
        PRINT '-> Inserting Academic weeks...';
        INSERT INTO Academic_Weeks (academic_year, week_number, start_date, end_date, description)
        SELECT '2024/2025', n, DATEADD(WEEK, n - 1, '2024-09-02'), DATEADD(DAY, 6, DATEADD(WEEK, n - 1, '2024-09-02')), CONCAT('Week ', n)
        FROM (SELECT TOP 30 n FROM #nums) weeks;
    END

-- cleanup
DROP TABLE IF EXISTS #nums, #RndSubjects, #RndSpecs, #RndTeachers, #RndGroups, #RndDisciplines, #RndAuditoriums, #RndWorkloads, #RndStudents;
PRINT 'SmartCampus: test data generation complete.';
GO

---------------------------------------
-- PART B: SmartCampusAuth (auth DB)
---------------------------------------
USE [SmartCampusAuth];
GO
SET NOCOUNT ON;

PRINT 'Starting test data generation for SmartCampusAuth...';

-- bcrypt-hash to use for generated test users
DECLARE @bcrypt_test NVARCHAR(255) = '$2a$10$63PCuwOhLeJxliJTAjbBr.0HYxdjFig2C55ChHZmDsqHM0PAeE99K'; -- 'sudo' example

-- 1) Create batches of auth test users (idempotent)
DECLARE @authTeachers INT = 30, @authStudents INT = 150;

PRINT '-> Inserting additional auth users (set-based)...';
-- Teacher users
INSERT INTO dbo.Users (username, password_hash, email, full_name, role_id)
SELECT
    CONCAT('authtest_teacher_', n), @bcrypt_test, CONCAT('authtest_teacher_', n, '@example.local'),
    CONCAT(N'Auth Teacher ', n), (SELECT id FROM dbo.Roles WHERE name = 'Teacher')
FROM (SELECT TOP (@authTeachers) n FROM (SELECT ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) as n FROM sys.all_columns) t) nums
WHERE NOT EXISTS (SELECT 1 FROM dbo.Users WHERE username = CONCAT('authtest_teacher_', n));

-- Student users
INSERT INTO dbo.Users (username, password_hash, email, full_name, role_id)
SELECT
    CONCAT('authtest_student_', n), @bcrypt_test, CONCAT('authtest_student_', n, '@example.local'),
    CONCAT(N'Auth Student ', n), (SELECT id FROM dbo.Roles WHERE name = 'Student')
FROM (SELECT TOP (@authStudents) n FROM (SELECT ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) as n FROM sys.all_columns) t) nums
WHERE NOT EXISTS (SELECT 1 FROM dbo.Users WHERE username = CONCAT('authtest_student_', n));

-- 2) Give some individual permissions (Access_Grants)
PRINT '-> Creating Access_Grants...';
IF NOT EXISTS (SELECT 1 FROM dbo.Permissions WHERE name = 'ApproveDevices')
    INSERT INTO dbo.Permissions(name, description) VALUES ('ApproveDevices', 'Allows user to approve or reject new devices for login.');

DECLARE @sudoId INT = (SELECT id FROM dbo.Users WHERE username = 'sudo');
DECLARE @permId INT = (SELECT id FROM dbo.Permissions WHERE name = 'ApproveDevices');

IF @sudoId IS NOT NULL AND @permId IS NOT NULL
    BEGIN
        INSERT INTO dbo.Access_Grants (granted_by, granted_to, permission_id, comment)
        SELECT @sudoId, u.id, @permId, N'Auto-granted for testing'
        FROM (
                 SELECT TOP 10 id FROM dbo.Users
                 WHERE username LIKE 'authtest_teacher_%' ORDER BY id
             ) u
        WHERE NOT EXISTS (SELECT 1 FROM dbo.Access_Grants WHERE granted_to = u.id AND permission_id = @permId);
    END

-- 3) Add a few UserDevices (idempotent)
IF OBJECT_ID('dbo.UserDevices','U') IS NOT NULL
    BEGIN
        PRINT '-> Inserting a few UserDevices...';
        ;WITH SampleUsers AS (
            SELECT id, username FROM dbo.Users WHERE username = 'authtest_teacher_1'
            UNION ALL
            SELECT id, username FROM dbo.Users WHERE username = 'authtest_student_1'
        )
         INSERT INTO dbo.UserDevices (user_id, device_uuid, is_approved, description)
         SELECT
             su.id,
             CASE WHEN su.username LIKE 'authtest_teacher%' THEN 'dev-tt-1' ELSE 'dev-ss-1' END,
             CASE WHEN su.username LIKE 'authtest_teacher%' THEN 1 ELSE 0 END,
             CASE WHEN su.username LIKE 'authtest_teacher%' THEN 'Sample teacher device' ELSE 'Sample student device' END
         FROM SampleUsers su
         WHERE NOT EXISTS (
             SELECT 1 FROM dbo.UserDevices
             WHERE user_id = su.id AND device_uuid = CASE WHEN su.username LIKE 'authtest_teacher%' THEN 'dev-tt-1' ELSE 'dev-ss-1' END
         );
    END

PRINT 'SmartCampusAuth: test users and grants inserted.';
GO

PRINT '03_insert_test_data.sql complete.';
GO