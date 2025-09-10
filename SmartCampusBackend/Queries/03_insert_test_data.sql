/***********************************************
  03_insert_test_data.sql (BIG CONFIG v10)
  Генерация большого объема тестовых данных для SmartCampus.
  - v10: Восстановлен большой конфиг как в оригинале.
         С сохраненными фиксами от v9 (overflow, divide by zero).
         Исправлена логика генерации расписания: чередование 3/4 пар в день,
         с большим перерывом между 3 и 4 парой (уже было в TimeSlots).
  - Идемпотентно, быстро и надежно.
  - Предназначено для MS SQL Server.
***********************************************/
---------------------------------------
-- PART A: SmartCampus (main DB)
---------------------------------------
USE [SmartCampus];
GO
SET NOCOUNT ON;
-- ========== Config: большой объём как в оригинале ==========
DECLARE
    @specialitiesCount INT = 5,
    @subjectsCount INT = 100,
    @teachersCount INT = 40,
    @auditoriumsCount INT = 20,
    @groupsPerSpec INT = 2,
    @studentsPerGroup INT = 20,
    @disciplinesCount INT = 200,
    @curriculumsPerSpec INT = 2,
    @disciplinesPerCurriculum INT = 15,
    @workloadsPerTeacher INT = 8,
    @executionsPerWorkload INT = 4,
    @scheduleEntries INT = 1000,
    @attendanceRecords INT = 4000,
    @gradeRecords INT = 3000;
-- ======================================================
-- Увеличиваем #nums для большого объема
IF OBJECT_ID('tempdb..#nums') IS NOT NULL DROP TABLE #nums;
CREATE TABLE #nums (n INT PRIMARY KEY);
INSERT INTO #nums(n)
SELECT TOP (100000) ROW_NUMBER() OVER (ORDER BY (SELECT NULL))
FROM sys.all_columns a CROSS JOIN sys.all_columns b;
-- Tables for names (unchanged)
IF OBJECT_ID('tempdb..#maleNames') IS NOT NULL DROP TABLE #maleNames;
CREATE TABLE #maleNames (name NVARCHAR(50));
INSERT INTO #maleNames VALUES (N'Александр'),(N'Михаил'),(N'Дмитрий'),(N'Андрей'),(N'Сергей'),(N'Алексей'),(N'Владимир'),(N'Иван'),(N'Павел'),(N'Константин'),(N'Виктор'),(N'Евгений'),(N'Игорь'),(N'Олег'),(N'Николай'),(N'Василий'),(N'Юрий'),(N'Анатолий'),(N'Валерий'),(N'Геннадий');
IF OBJECT_ID('tempdb..#femaleNames') IS NOT NULL DROP TABLE #femaleNames;
CREATE TABLE #femaleNames (name NVARCHAR(50));
INSERT INTO #femaleNames VALUES (N'Мария'),(N'Елена'),(N'Анна'),(N'Ольга'),(N'Татьяна'),(N'Ирина'),(N'Наталья'),(N'Светлана'),(N'Юлия'),(N'Екатерина'),(N'Галина'),(N'Валентина'),(N'Людмила'),(N'Анастасия'),(N'Дарья'),(N'Ксения'),(N'Виктория'),(N'Марина'),(N'Алёна'),(N'Полина');
IF OBJECT_ID('tempdb..#surnames') IS NOT NULL DROP TABLE #surnames;
CREATE TABLE #surnames (surname NVARCHAR(50));
INSERT INTO #surnames VALUES (N'Иванов'),(N'Смирнов'),(N'Кузнецов'),(N'Попов'),(N'Васильев'),(N'Петров'),(N'Соколов'),(N'Михайлов'),(N'Новиков'),(N'Фёдоров'),(N'Морозов'),(N'Волков'),(N'Алексеев'),(N'Лебедев'),(N'Семёнов'),(N'Егоров'),(N'Павлов'),(N'Козлов'),(N'Степанов'),(N'Николаев'),(N'Орлов'),(N'Андреев'),(N'Макаров'),(N'Никитин'),(N'Захаров');
IF OBJECT_ID('tempdb..#lastnames') IS NOT NULL DROP TABLE #lastnames;
CREATE TABLE #lastnames (lastname NVARCHAR(50));
INSERT INTO #lastnames VALUES (N'Александрович'),(N'Михайлович'),(N'Дмитриевич'),(N'Андреевич'),(N'Сергеевич'),(N'Алексеевич'),(N'Владимирович'),(N'Иванович'),(N'Павлович'),(N'Константинович'),(N'Викторович'),(N'Евгеньевич'),(N'Игоревич'),(N'Олегович'),(N'Николаевич'),(N'Васильевич'),(N'Юрьевич'),(N'Анатольевич'),(N'Валерьевич'),(N'Геннадьевич'),(N'Борисович'),(N'Станиславович'),(N'Львович'),(N'Ярославович'),(N'Ильич');
IF OBJECT_ID('tempdb..#femaleLastnames') IS NOT NULL DROP TABLE #femaleLastnames;
CREATE TABLE #femaleLastnames (lastname NVARCHAR(50));
INSERT INTO #femaleLastnames VALUES (N'Александровна'),(N'Михайловна'),(N'Дмитриевна'),(N'Андреевна'),(N'Сергеевна'),(N'Алексеевна'),(N'Владимировна'),(N'Ивановна'),(N'Павловна'),(N'Константиновна'),(N'Викторовна'),(N'Евгеньевна'),(N'Игоревна'),(N'Олеговна'),(N'Николаевна'),(N'Васильевна'),(N'Юрьевна'),(N'Анатольевна'),(N'Валерьевна'),(N'Геннадьевна'),(N'Борисовна'),(N'Станиславовна'),(N'Львовна'),(N'Ярославовна'),(N'Ильинична');
PRINT N'Starting test data generation for SmartCampus...';
-- 1) Specialities
IF NOT EXISTS (SELECT 1 FROM Specialities)
    BEGIN
        PRINT N'-> Inserting Specialities...';
        INSERT INTO Specialities (name)
        SELECT TOP (@specialitiesCount) CHOOSE(n % 5 + 1, CONCAT(N'Информационные технологии и программирование (', n, N')'), CONCAT(N'Электроника и автоматика (', n, N')'), CONCAT(N'Механика и металлообработка (', n, N')'), CONCAT(N'Экономика и бухгалтерский учет (', n, N')'), CONCAT(N'Дизайн и полиграфия (', n, N')'))
        FROM #nums ORDER BY n;
    END
-- 2) Subjects
IF NOT EXISTS (SELECT 1 FROM Subjects)
    BEGIN
        PRINT N'-> Inserting Subjects...';
        WITH SubjectCategories AS (SELECT N'Математика и информатика' as category UNION ALL SELECT N'Физика и химия' UNION ALL SELECT N'Иностранные языки' UNION ALL SELECT N'История и обществознание' UNION ALL SELECT N'Физическая культура' UNION ALL SELECT N'Экономика и менеджмент' UNION ALL SELECT N'Программирование' UNION ALL SELECT N'Электротехника' UNION ALL SELECT N'Механика' UNION ALL SELECT N'Дизайн')
        INSERT INTO Subjects (name)
        SELECT TOP (@subjectsCount) CONCAT(sc.category, N' - ', RIGHT('000' + CAST(n.n AS VARCHAR(6)), 3))
        FROM #nums n CROSS JOIN SubjectCategories sc ORDER BY n.n;
    END
-- 3) Auditoriums (FIXED: NVARCHAR(10))
IF NOT EXISTS (SELECT 1 FROM Auditoriums)
    BEGIN
        PRINT N'-> Inserting Auditoriums...';
        WITH AuditoriumTypes AS (SELECT N'Лекционный зал' as type, 1 as type_id UNION ALL SELECT N'Лаборатория', 2 UNION ALL SELECT N'Практический кабинет', 3 UNION ALL SELECT N'Компьютерный класс', 4 UNION ALL SELECT N'Спортивный зал', 5)
        INSERT INTO Auditoriums (number, type)
        SELECT TOP (@auditoriumsCount) CONCAT(CHOOSE(at.type_id, N'ЛЗ-', N'ЛБ-', N'ПК-', N'КК-', N'СЗ-'), RIGHT(N'00' + CAST(((n.n - 1) / 5) + 1 AS NVARCHAR(10)), 2)), at.type
        FROM #nums n CROSS JOIN AuditoriumTypes at
        WHERE (n.n - 1) % 5 + 1 = at.type_id ORDER BY n.n;
    END
-- 4) Teachers
IF NOT EXISTS (SELECT 1 FROM Teachers)
    BEGIN
        PRINT N'-> Inserting Teachers...';
        WITH TeacherData AS (SELECT TOP (@teachersCount) n.n, CASE WHEN (n.n % 2) = 0 THEN mn.name ELSE fn.name END AS first_name, s.surname, CASE WHEN (n.n % 2) = 0 THEN ml.lastname ELSE fl.lastname END AS patronymic, DATEADD(DAY, -(n.n % (20 * 365)), '2000-01-01') AS birth_date, CONCAT('+7(9', FORMAT((CAST(n.n AS BIGINT) * 1234567) % 100000000, '00'), ')', FORMAT((CAST(n.n AS BIGINT) * 7654321) % 10000000, '000-00-00')) AS phone
                             FROM #nums n CROSS APPLY (SELECT TOP 1 name FROM #maleNames ORDER BY n.n % (SELECT COUNT(*) FROM #maleNames) + 1) mn CROSS APPLY (SELECT TOP 1 name FROM #femaleNames ORDER BY n.n % (SELECT COUNT(*) FROM #femaleNames) + 1) fn CROSS APPLY (SELECT TOP 1 surname FROM #surnames ORDER BY n.n % (SELECT COUNT(*) FROM #surnames) + 1) s CROSS APPLY (SELECT TOP 1 lastname FROM #lastnames ORDER BY n.n % (SELECT COUNT(*) FROM #lastnames) + 1) ml CROSS APPLY (SELECT TOP 1 lastname FROM #femaleLastnames ORDER BY n.n % (SELECT COUNT(*) FROM #femaleLastnames) + 1) fl)
        INSERT INTO Teachers (surname, name, lastname, birthday, phone_number) SELECT surname, first_name, patronymic, birth_date, phone FROM TeacherData ORDER BY n;
    END
-- Teachers_Info
IF NOT EXISTS (SELECT 1 FROM dbo.Teachers_Info)
    BEGIN
        PRINT N'-> Inserting Teachers_Info...';
        INSERT INTO dbo.Teachers_Info (teacher_id, address, passport_number, high_school, degree, position)
        SELECT T.id, CONCAT(N'г. Москва, ул. Преподавательская, д. ', (T.id % 100) + 1, N', кв. ', (T.id % 50) + 1), CONCAT(N'ПАС-', FORMAT(T.id, '0000'), '-', FORMAT(CHECKSUM(NEWID()) % 1000000, '000000')), CHOOSE((T.id % 4) + 1, N'МГУ', N'МФТИ', N'МАИ', N'НИУ ВШЭ'), CHOOSE((T.id % 5) + 1, N'Кандидат наук', N'Доктор наук', N'Магистр', N'Специалист', N'Бакалавр'), CHOOSE((T.id % 6) + 1, N'Профессор', N'Доцент', N'Старший преподаватель', N'Преподаватель', N'Ассистент', N'Заведующий кафедрой')
        FROM dbo.Teachers AS T;
    END
-- 5) Groups
IF NOT EXISTS (SELECT 1 FROM Groups)
    BEGIN
        PRINT N'-> Creating Groups (set-based)...';
        INSERT INTO Groups (name, spec_id, course)
        SELECT CONCAT(LEFT(s.name, CHARINDEX('(', s.name) - 2), '-', (s.id % 10) + 1, '-', g.n), s.id, g.n
        FROM Specialities s CROSS JOIN (SELECT TOP (@groupsPerSpec) n FROM #nums ORDER BY n) g WHERE g.n <= 4;
    END
-- 6) Students
IF NOT EXISTS (SELECT 1 FROM Students)
    BEGIN
        PRINT N'-> Inserting Students (set-based)...';
        DECLARE @totalGroups INT = (SELECT COUNT(*) FROM Groups);
        IF @totalGroups > 0
            BEGIN
                WITH NumberedGroups AS (SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn FROM Groups),
                     StudentData AS (SELECT TOP (@totalGroups * @studentsPerGroup) n.n, CASE WHEN (n.n % 2) = 0 THEN mn.name ELSE fn.name END AS first_name, s.surname, CASE WHEN (n.n % 2) = 0 THEN ml.lastname ELSE fl.lastname END AS patronymic, DATEADD(DAY, -(n.n % (5 * 365)), '2008-01-01') AS birth_date, g.id AS group_id, CONCAT('+7(9', FORMAT((CAST(n.n AS BIGINT) * 7890123) % 100000000, '00'), ')', FORMAT((CAST(n.n AS BIGINT) * 3210987) % 10000000, '000-00-00')) AS phone
                                     FROM #nums n CROSS APPLY (SELECT TOP 1 name FROM #maleNames ORDER BY n.n % (SELECT COUNT(*) FROM #maleNames) + 1) mn CROSS APPLY (SELECT TOP 1 name FROM #femaleNames ORDER BY n.n % (SELECT COUNT(*) FROM #femaleNames) + 1) fn CROSS APPLY (SELECT TOP 1 surname FROM #surnames ORDER BY n.n % (SELECT COUNT(*) FROM #surnames) + 1) s CROSS APPLY (SELECT TOP 1 lastname FROM #lastnames ORDER BY n.n % (SELECT COUNT(*) FROM #lastnames) + 1) ml CROSS APPLY (SELECT TOP 1 lastname FROM #femaleLastnames ORDER BY n.n % (SELECT COUNT(*) FROM #femaleLastnames) + 1) fl JOIN NumberedGroups g ON (n.n - 1) % @totalGroups + 1 = g.rn)
                INSERT INTO Students (surname, name, lastname, birthday, group_id, phone_number) SELECT surname, first_name, patronymic, birth_date, group_id, phone FROM StudentData ORDER BY n;
            END
    END
-- Students_Info
IF NOT EXISTS (SELECT 1 FROM dbo.Students_Info)
    BEGIN
        PRINT N'-> Inserting Students_Info...';
        INSERT INTO dbo.Students_Info (student_id, address, passport_number, school, student_card_number, study_type, study_form, status)
        SELECT S.id, CONCAT(N'г. Москва, ул. Студенческая, д. ', (S.id % 200) + 1, N', кв. ', (S.id % 100) + 1), CONCAT(N'ПАС-', FORMAT(S.id, '00000'), '-', FORMAT(CHECKSUM(NEWID()) % 1000000, '000000')), CONCAT(N'Школа №', ABS(CHECKSUM(NEWID())) % 200 + 1), CONCAT(N'СТ-', FORMAT(S.id, '000000'), '-', FORMAT(CHECKSUM(NEWID()) % 1000, '000')), CHOOSE((S.id % 3) + 1, N'Бюджет', N'Контракт', N'Целевой'), CHOOSE((S.id % 2) + 1, N'Очная', N'Заочная'), CHOOSE((S.id % 5) + 1, N'Обучается', N'Академический отпуск', N'Отчислен', N'Переведен', N'Выпускник')
        FROM dbo.Students AS S;
    END
-- Prepare #Rnd tables (sequential rn)
DROP TABLE IF EXISTS #RndSubjects, #RndSpecs, #RndTeachers, #RndGroups, #RndDisciplines, #RndAuditoriums, #RndWorkloads, #RndStudents;
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndSubjects FROM Subjects;
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndSpecs FROM Specialities;
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndTeachers FROM Teachers;
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndGroups FROM Groups;
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndAuditoriums FROM Auditoriums;
-- 7) Disciplines (add check for counts >0)
IF NOT EXISTS (SELECT 1 FROM Disciplines)
    BEGIN
        PRINT N'-> Inserting Disciplines (set-based)...';
        DECLARE @subjCount INT = (SELECT COUNT(*) FROM #RndSubjects), @specCount INT = (SELECT COUNT(*) FROM #RndSpecs);
        IF @subjCount > 0 AND @specCount > 0
            BEGIN
                INSERT INTO Disciplines (subject_id, semester, speciality_id, course, lecture, practice, lab, seminar, control)
                SELECT s.id, CASE WHEN (n.n % 8) + 1 <= 2 THEN (n.n % 2) + 1 WHEN (n.n % 8) + 1 <= 4 THEN ((n.n % 2) + 1) + 2 WHEN (n.n % 8) + 1 <= 6 THEN ((n.n % 2) + 1) + 4 ELSE ((n.n % 2) + 1) + 6 END, sp.id, CASE WHEN (n.n % 8) + 1 <= 2 THEN 1 WHEN (n.n % 8) + 1 <= 4 THEN 2 WHEN (n.n % 8) + 1 <= 6 THEN 3 ELSE 4 END, ABS(CHECKSUM(s.id, sp.id)) % 50 + 10, ABS(CHECKSUM(s.id, sp.id, 1)) % 50 + 10, ABS(CHECKSUM(s.id, sp.id, 2)) % 30 + 5, ABS(CHECKSUM(s.id, sp.id, 3)) % 20 + 0, CHOOSE((n.n % 4) + 1, N'Экзамен', N'Зачет', N'Дифференцированный зачет', N'Курсовая работа')
                FROM (SELECT TOP (@disciplinesCount) n FROM #nums ORDER BY n) n LEFT JOIN #RndSubjects s ON n.n % @subjCount + 1 = s.rn LEFT JOIN #RndSpecs sp ON n.n % @specCount + 1 = sp.rn;
            END
    END
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndDisciplines FROM Disciplines;
-- 8) Curriculums and Curriculum_Disciplines
IF NOT EXISTS (SELECT 1 FROM Curriculums)
    BEGIN
        PRINT N'-> Inserting Curriculums & Disciplines (set-based with OUTPUT)...';
        DECLARE @discCount INT = (SELECT COUNT(*) FROM #RndDisciplines);
        IF @discCount > 0
            BEGIN
                DECLARE @InsertedCurriculums TABLE (id INT, spec_id INT);
                INSERT INTO Curriculums (speciality_id, year, profile, education_form, degree, duration, approved_date)
                OUTPUT inserted.id, inserted.speciality_id INTO @InsertedCurriculums
                SELECT s.id, 2022 + (c.n - 1) * 2, CONCAT(N'Профиль: ', CHOOSE((s.id + c.n) % 3 + 1, N'Теоретический', N'Практический', N'Исследовательский'), N' (', s.name, N')'), N'Очная', N'Бакалавриат', 4, DATEADD(YEAR, -1, DATEFROMPARTS(2022 + (c.n - 1) * 2, 9, 1))
                FROM Specialities s CROSS JOIN (SELECT TOP (@curriculumsPerSpec) n FROM #nums ORDER BY n) c;
                INSERT INTO Curriculum_Disciplines (curriculum_id, discipline_id, semester, course, exam, credit, coursework, control_type, credits)
                SELECT ic.id, d.id, cd.semester, cd.course, CASE WHEN cd.control IN (N'Экзамен', N'Дифференцированный зачет') THEN 1 ELSE 0 END, CASE WHEN cd.control IN (N'Зачет', N'Дифференцированный зачет') THEN 1 ELSE 0 END, CASE WHEN cd.control = N'Курсовая работа' THEN 1 ELSE 0 END, cd.control, (cd.lecture + cd.practice + cd.lab + cd.seminar) / 36 + 1
                FROM @InsertedCurriculums ic CROSS JOIN (SELECT TOP (@disciplinesPerCurriculum) n FROM #nums ORDER BY n) n JOIN #RndDisciplines d ON n.n % @discCount + 1 = d.rn JOIN Disciplines cd ON d.id = cd.id WHERE cd.speciality_id = ic.spec_id;
            END
    END
-- 9) Teachers_Workload
IF NOT EXISTS (SELECT 1 FROM Teachers_Workload)
    BEGIN
        PRINT N'-> Inserting Teachers_Workload (set-based)...';
        DECLARE @teachCount INT = (SELECT COUNT(*) FROM #RndTeachers), @discCountW INT = (SELECT COUNT(*) FROM #RndDisciplines), @groupCountW INT = (SELECT COUNT(*) FROM #RndGroups);
        IF @teachCount > 0 AND @discCountW > 0 AND @groupCountW > 0
            BEGIN
                INSERT INTO Teachers_Workload (teacher_id, discipline_id, type, hours, academic_year, control_type, group_id)
                SELECT t.id, d.id, CHOOSE((n.n % 4) + 1, N'Лекция', N'Практика', N'Лабораторная', N'Семинар'), ABS(CHECKSUM(t.id, d.id)) % 120 + 10, '2025/2026', CHOOSE((n.n % 4) + 1, N'Экзамен', N'Зачет', N'Дифференцированный зачет', N'Курсовая работа'), g.id
                FROM (SELECT TOP (@teachersCount * @workloadsPerTeacher) n FROM #nums ORDER BY n) n LEFT JOIN #RndTeachers t ON n.n % @teachCount + 1 = t.rn LEFT JOIN #RndDisciplines d ON (n.n + 10) % @discCountW + 1 = d.rn LEFT JOIN #RndGroups g ON (n.n + 20) % @groupCountW + 1 = g.rn;
            END
    END
SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndWorkloads FROM Teachers_Workload;
-- 10) Workload_Execution
IF NOT EXISTS (SELECT 1 FROM Workload_Execution)
    BEGIN
        PRINT N'-> Inserting Workload_Execution (set-based)...';
        DECLARE @workloadCount INT = (SELECT COUNT(*) FROM #RndWorkloads), @teachCountE INT = (SELECT COUNT(*) FROM #RndTeachers);
        IF @workloadCount > 0 AND @teachCountE > 0
            BEGIN
                WITH DateRange AS (SELECT DATEADD(DAY, n.n, '2025-09-01') as exec_date FROM (SELECT TOP 270 n FROM #nums) n WHERE DATEPART(WEEKDAY, DATEADD(DAY, n.n, '2025-09-01')) BETWEEN 2 AND 7)
                INSERT INTO Workload_Execution (workload_id, execution_date, hours, executed_by, notes, status)
                SELECT TOP (@workloadCount * @executionsPerWorkload) w.id, dr.exec_date, CAST((ABS(CHECKSUM(w.id, dr.exec_date)) % 3) + 1.0 AS DECIMAL(5, 2)), t.id, CONCAT(CHOOSE(ABS(CHECKSUM(w.id, dr.exec_date)) % 5 + 1, N'Проведено занятие по теме: ', N'Рассмотрены вопросы: ', N'Выполнены задания: ', N'Проведена лабораторная работа: ', N'Обсуждены проблемы: '), N'Тема ', ABS(CHECKSUM(w.id, dr.exec_date)) % 100 + 1), CHOOSE(ABS(CHECKSUM(w.id, dr.exec_date)) % 4 + 1, N'Запланировано', N'Выполнено', N'Частично выполнено', N'Отменено')
                FROM (SELECT TOP (@workloadCount * @executionsPerWorkload * 2) n FROM #nums ORDER BY n) n JOIN #RndWorkloads w ON n.n % @workloadCount + 1 = w.rn CROSS JOIN DateRange dr JOIN #RndTeachers t ON (n.n + 1) % @teachCountE + 1 = t.rn WHERE ABS(CHECKSUM(w.id, dr.exec_date)) % 3 <> 0 ORDER BY n.n;
            END
    END

SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn INTO #RndStudents FROM Students;
-- 12) Attendance & Grades
IF NOT EXISTS (SELECT 1 FROM Attendance)
    BEGIN
        PRINT N'-> Inserting Attendance (set-based)...';
        DECLARE @studCountA INT = (SELECT COUNT(*) FROM #RndStudents), @discCountA INT = (SELECT COUNT(*) FROM #RndDisciplines);
        IF @studCountA > 0 AND @discCountA > 0
            BEGIN
                WITH ScheduleDates AS (SELECT DISTINCT day as class_date FROM Schedule WHERE day BETWEEN '2025-09-01' AND '2025-12-31')
                INSERT INTO Attendance (day, time, student_id, discipline_id, mark)
                SELECT TOP (@attendanceRecords) sd.class_date, CAST(DATEADD(MINUTE, (ABS(CHECKSUM(s.id, d.id, sd.class_date)) % 300), '08:30') AS TIME), s.id, d.id, CHOOSE(ABS(CHECKSUM(s.id, d.id, sd.class_date)) % 4 + 1, N'Присутствовал', N'Отсутствовал', N'Опоздал', N'Уважительная причина')
                FROM ScheduleDates sd CROSS JOIN (SELECT TOP (@attendanceRecords * 2) n FROM #nums ORDER BY n) n LEFT JOIN #RndStudents s ON n.n % @studCountA + 1 = s.rn LEFT JOIN #RndDisciplines d ON (n.n + 1) % @discCountA + 1 = d.rn ORDER BY n.n;
            END
    END
IF NOT EXISTS (SELECT 1 FROM Grades)
    BEGIN
        PRINT N'-> Inserting Grades (set-based)...';
        DECLARE @studCountG INT = (SELECT COUNT(*) FROM #RndStudents), @discCountG INT = (SELECT COUNT(*) FROM #RndDisciplines), @teachCountG INT = (SELECT COUNT(*) FROM #RndTeachers);
        IF @studCountG > 0 AND @discCountG > 0 AND @teachCountG > 0
            BEGIN
                WITH GradeTypes AS (SELECT N'Промежуточная аттестация' as round_type UNION ALL SELECT N'Экзамен' UNION ALL SELECT N'Зачет' UNION ALL SELECT N'Курсовая работа')
                INSERT INTO Grades (day, time, student_id, discipline_id, teacher_id, round, mark)
                SELECT TOP (@gradeRecords) DATEADD(DAY, ABS(CHECKSUM(s.id, d.id, t.id)) % 120, '2025-09-01'), CAST(DATEADD(MINUTE, (ABS(CHECKSUM(s.id, d.id, t.id, 1)) % 300), '08:30') AS TIME), s.id, d.id, t.id, gt.round_type, CASE WHEN gt.round_type = N'Экзамен' THEN CHOOSE(ABS(CHECKSUM(s.id, d.id, t.id, 2)) % 5 + 1, N'Отлично', N'Хорошо', N'Удовлетворительно', N'Неудовлетворительно', N'Не явился') WHEN gt.round_type = N'Зачет' THEN CHOOSE(ABS(CHECKSUM(s.id, d.id, t.id, 3)) % 3 + 1, N'Зачтено', N'Не зачтено', N'Не явился') WHEN gt.round_type = N'Курсовая работа' THEN CHOOSE(ABS(CHECKSUM(s.id, d.id, t.id, 4)) % 5 + 1, N'Отлично', N'Хорошо', N'Удовлетворительно', N'Неудовлетворительно', N'Не защита') ELSE CAST(ABS(CHECKSUM(s.id, d.id, t.id, 5)) % 101 AS NVARCHAR(10)) END
                FROM (SELECT TOP (@gradeRecords * 2) n FROM #nums ORDER BY n) n CROSS JOIN GradeTypes gt LEFT JOIN #RndStudents s ON n.n % @studCountG + 1 = s.rn LEFT JOIN #RndDisciplines d ON (n.n + 1) % @discCountG + 1 = d.rn LEFT JOIN #RndTeachers t ON (n.n + 2) % @teachCountG + 1 = t.rn ORDER BY n.n;
            END
    END
PRINT N'Test data generation for SmartCampus completed.';
GO

/***********************************************
  05_insert_manual_schedule.sql
  Вставка тестового расписания вручную для 2 недель.
  Неделя 1: 01.09.2025 (пн) - 07.09.2025 (вс)
  Неделя 2: 08.09.2025 (пн) - 14.09.2025 (вс)
  Предназначено для MS SQL Server.
***********************************************/

USE [SmartCampus];
GO
SET NOCOUNT ON;

-- Проверим, есть ли данные, которые нам нужны
DECLARE @WorkloadCount INT = (SELECT COUNT(*) FROM Teachers_Workload);
DECLARE @GroupCount INT = (SELECT COUNT(*) FROM Groups);
DECLARE @DisciplineCount INT = (SELECT COUNT(*) FROM Disciplines);
DECLARE @TeacherCount INT = (SELECT COUNT(*) FROM Teachers);
DECLARE @AuditoriumCount INT = (SELECT COUNT(*) FROM Auditoriums);

IF @WorkloadCount = 0 OR @GroupCount = 0 OR @DisciplineCount = 0 OR @TeacherCount = 0 OR @AuditoriumCount = 0
    BEGIN
        PRINT N'Ошибка: Необходимые справочные данные (Workloads, Groups, Disciplines, Teachers, Auditoriums) отсутствуют.';
        PRINT N'Пожалуйста, сначала выполните скрипт 03_insert_test_data.sql.';
        RETURN;
    END

PRINT N'Найдено данных для генерации расписания:';
PRINT CONCAT(N' - Нагрузок (Workloads): ', @WorkloadCount);
PRINT CONCAT(N' - Групп: ', @GroupCount);
PRINT CONCAT(N' - Дисциплин: ', @DisciplineCount);
PRINT CONCAT(N' - Преподавателей: ', @TeacherCount);
PRINT CONCAT(N' - Аудиторий: ', @AuditoriumCount);

-- Определим даты
DECLARE @StartDate DATE = '2025-09-01'; -- Понедельник
DECLARE @EndDate DATE = '2025-09-14';   -- Воскресенье той же недели + 7 дней = Вторая неделя

-- Определим стандартные временные слоты
DECLARE @TimeSlots TABLE (slot_num INT, start_time TIME, end_time TIME);
INSERT INTO @TimeSlots (slot_num, start_time, end_time)
VALUES
    (1, '08:00', '09:30'),
    (2, '09:45', '11:15'),
    (3, '11:30', '13:00'),
    (4, '14:00', '15:30'), -- 1 час перерыв после 3-й пары
    (5, '15:45', '17:15');

-- Создадим CTE с датами и слотами для 2 недель
-- Для четных дней (0,2,4,6,8,10,12) - 3 пары
-- Для нечетных дней (1,3,5,7,9,11,13) - 4 пары
;WITH DateRange AS (
    SELECT @StartDate AS schedule_date
    UNION ALL
    SELECT DATEADD(DAY, 1, schedule_date)
    FROM DateRange
    WHERE schedule_date < @EndDate
),
      DaysWithIndex AS (
          SELECT schedule_date, DATEDIFF(DAY, @StartDate, schedule_date) AS day_index
          FROM DateRange
          WHERE DATEPART(WEEKDAY, schedule_date) BETWEEN 2 AND 7 -- Пн=2, Вс=7 (в зависимости от настроек DATEFIRST, обычно Пн=1, но в SQL Server по умолчанию Вс=1)
      ),
-- Корректируем для стандартного DATEFIRST (Вс=1), Пн=2, Вт=3 ... Сб=7
      DaysAdjusted AS (
          SELECT schedule_date, DATEDIFF(DAY, @StartDate, schedule_date) AS day_index
          FROM DateRange
          WHERE DATEPART(WEEKDAY, schedule_date) BETWEEN 2 AND 7 -- Пн-Сб
      ),
      DateSlotCombinations AS (
          SELECT
              d.schedule_date,
              ts.slot_num,
              ts.start_time,
              ts.end_time
          FROM DaysAdjusted d
                   CROSS JOIN @TimeSlots ts
          WHERE ts.slot_num <= CASE WHEN d.day_index % 2 = 0 THEN 3 ELSE 4 END -- Чередование 3/4 пар
      ),
-- Получим ID сущностей для случайного сопоставления
      WorkloadIDs AS (
          SELECT id, ROW_NUMBER() OVER(ORDER BY id) AS rn, COUNT(*) OVER() AS cnt FROM Teachers_Workload
      ),
      GroupIDs AS (
          SELECT id, ROW_NUMBER() OVER(ORDER BY id) AS rn, COUNT(*) OVER() AS cnt FROM Groups
      ),
      DisciplineIDs AS (
          SELECT id, ROW_NUMBER() OVER(ORDER BY id) AS rn, COUNT(*) OVER() AS cnt FROM Disciplines
      ),
      TeacherIDs AS (
          SELECT id, ROW_NUMBER() OVER(ORDER BY id) AS rn, COUNT(*) OVER() AS cnt FROM Teachers
      ),
      AuditoriumIDs AS (
          SELECT id, ROW_NUMBER() OVER(ORDER BY id) AS rn, COUNT(*) OVER() AS cnt FROM Auditoriums
      ),
-- Создадим комбинации для вставки
      ScheduleCombinations AS (
          SELECT
              dsc.schedule_date,
              dsc.start_time,
              dsc.end_time,
              w.id AS workload_id,
              g.id AS group_id,
              d.id AS discipline_id,
              t.id AS teacher_id,
              a.id AS auditorium_id,
              -- Определим тип занятия на основе нагрузки или случайно
              CHOOSE(ABS(CHECKSUM(dsc.schedule_date, w.id)) % 4 + 1, N'Лекция', N'Практика', N'Лабораторная', N'Семинар') AS lesson_type
          FROM DateSlotCombinations dsc
                   -- Сопоставим с нагрузками, группами и т.д. используя CHECKSUM для псевдослучайности
                   JOIN WorkloadIDs w ON ABS(CHECKSUM(dsc.schedule_date, dsc.slot_num)) % w.cnt + 1 = w.rn
                   JOIN GroupIDs g ON ABS(CHECKSUM(dsc.schedule_date, dsc.slot_num, 1)) % g.cnt + 1 = g.rn
                   JOIN DisciplineIDs d ON ABS(CHECKSUM(dsc.schedule_date, dsc.slot_num, 2)) % d.cnt + 1 = d.rn
                   JOIN TeacherIDs t ON ABS(CHECKSUM(dsc.schedule_date, dsc.slot_num, 3)) % t.cnt + 1 = t.rn
                   JOIN AuditoriumIDs a ON ABS(CHECKSUM(dsc.schedule_date, dsc.slot_num, 4)) % a.cnt + 1 = a.rn
      )
-- Вставляем данные в таблицу Schedule
 INSERT INTO Schedule (workload_id, day, start_time, end_time, group_id, discipline_id, teacher_id, auditorium_id, type)
 SELECT
     workload_id,
     schedule_date,
     -- Преобразуем TIME в DATETIME для вставки
     CAST(schedule_date AS DATETIME) + CAST(start_time AS DATETIME),
     CAST(schedule_date AS DATETIME) + CAST(end_time AS DATETIME),
     group_id,
     discipline_id,
     teacher_id,
     auditorium_id,
     lesson_type
 FROM ScheduleCombinations
-- Добавим проверку, чтобы не вставлять дубликаты, если скрипт запущен повторно
 WHERE NOT EXISTS (
     SELECT 1 FROM Schedule s2
     WHERE s2.day = ScheduleCombinations.schedule_date
       AND s2.start_time = CAST(ScheduleCombinations.schedule_date AS DATETIME) + CAST(ScheduleCombinations.start_time AS DATETIME)
       AND s2.group_id = ScheduleCombinations.group_id
 );

DECLARE @InsertedCount INT = @@ROWCOUNT;
PRINT CONCAT(N'-> Вставлено ', @InsertedCount, N' записей в таблицу Schedule.');

-- Проверим итог
DECLARE @FinalCount INT = (SELECT COUNT(*) FROM Schedule);
PRINT CONCAT(N'-> Всего записей в таблице Schedule: ', @FinalCount);

PRINT N'Вставка тестового расписания на 2 недели завершена.';
GO

/***********************************************
  04_insert_test_data_auth.sql (BIG CONFIG v2, no staff)
  Генерация тестовых данных для SmartCampusAuth.
  - v2: Большой конфиг (увеличены объемы).
        Закомментирована генерация staff пользователей и User_Departments (как не нужны).
  - Идемпотентно: вставляет только если данных нет.
  - Связывает с профилями из SmartCampus.
  - Dummy password_hash.
***********************************************/
USE [SmartCampusAuth];
GO
SET NOCOUNT ON;

PRINT N'Starting test data generation for SmartCampusAuth...';

-- Config: большой объем
DECLARE
    @departmentsCount INT = 10,  -- Увеличено
    @additionalUsersCount INT = 50,  -- Увеличено, но закомментировано
    @devicesPerUser INT = 3,  -- Увеличено
    @accessGrantsCount INT = 100;  -- Увеличено

-- 1) Departments (увеличено)
/*
IF NOT EXISTS (SELECT 1 FROM Departments)
    BEGIN
        PRINT N'-> Inserting Departments...';
        INSERT INTO Departments (name)
        SELECT TOP (@departmentsCount) CONCAT(N'Department ', n)
        FROM (SELECT ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n FROM sys.all_columns) AS nums
        WHERE n <= @departmentsCount;
    END
*/

-- 2) Users for Teachers
DECLARE @teacherRoleId INT = (SELECT id FROM Roles WHERE name = 'Teacher');
IF @teacherRoleId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Users WHERE teacher_profile_id IS NOT NULL)
    BEGIN
        PRINT N'-> Inserting Users for Teachers...';
        INSERT INTO Users (username, password_hash, email, full_name, role_id, teacher_profile_id)
        SELECT TOP 100 PERCENT
            CONCAT(N'teacher_', t.id),
            '$2a$10$63PCuwOhLeJxliJTAjbBr.0HYxdjFig2C55ChHZmDsqHM0PAeE99K',
            CONCAT(N'teacher_', t.id, '@example.com'),
            CONCAT(t.surname, N' ', t.name, N' ', t.lastname),
            @teacherRoleId,
            t.id
        FROM SmartCampus.dbo.Teachers t
        WHERE NOT EXISTS (SELECT 1 FROM Users u WHERE u.teacher_profile_id = t.id);
    END

-- 3) Users for Students
DECLARE @studentRoleId INT = (SELECT id FROM Roles WHERE name = 'Student');
IF @studentRoleId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Users WHERE student_profile_id IS NOT NULL)
    BEGIN
        PRINT N'-> Inserting Users for Students...';
        INSERT INTO Users (username, password_hash, email, full_name, role_id, student_profile_id)
        SELECT TOP 100 PERCENT
            CONCAT(N'student_', s.id),
            '$2a$10$63PCuwOhLeJxliJTAjbBr.0HYxdjFig2C55ChHZmDsqHM0PAeE99K',
            CONCAT(N'student_', s.id, '@example.com'),
            CONCAT(s.surname, N' ', s.name, N' ', s.lastname),
            @studentRoleId,
            s.id
        FROM SmartCampus.dbo.Students s
        WHERE NOT EXISTS (SELECT 1 FROM Users u WHERE u.student_profile_id = s.id);
    END

-- 4) Additional Users (staff) - ЗАКОММЕНТИРОВАНО
/*
IF (SELECT COUNT(*) FROM Users WHERE student_profile_id IS NULL AND teacher_profile_id IS NULL AND username NOT IN ('sudo', 'root')) < @additionalUsersCount
BEGIN
    PRINT N'-> Inserting Additional Users...';
    DECLARE @sysAdminRoleId INT = (SELECT id FROM Roles WHERE name = 'SystemAdmin');
    INSERT INTO Users (username, password_hash, email, full_name, role_id)
    SELECT CONCAT(N'staff_', n),
           '$2a$10$63PCuwOhLeJxliJTAjbBr.0HYxdjFig2C55ChHZmDsqHM0PAeE99K',
           CONCAT(N'staff_', n, '@example.com'),
           CONCAT(N'Staff User ', n),
           @sysAdminRoleId
    FROM (SELECT TOP (@additionalUsersCount) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n FROM sys.all_columns) AS nums
    WHERE NOT EXISTS (SELECT 1 FROM Users WHERE username = CONCAT(N'staff_', n));
END
*/

-- 5) Superusers (additional if needed, but sudo/root already there)

-- 6) User_Departments - ЗАКОММЕНТИРОВАНО
/*
IF NOT EXISTS (SELECT 1 FROM User_Departments)
BEGIN
    PRINT N'-> Assigning Users to Departments...';
    INSERT INTO User_Departments (user_id, department_id, is_manager)
    SELECT u.id, d.id, CASE WHEN u.id % 5 = 0 THEN 1 ELSE 0 END
    FROM Users u
    CROSS JOIN Departments d
    WHERE u.id % (SELECT COUNT(*) FROM Departments) + 1 = d.id;
END
*/

-- 7) UserDevices (увеличено @devicesPerUser)
IF NOT EXISTS (SELECT 1 FROM UserDevices)
    BEGIN
        PRINT N'-> Inserting User Devices...';
        INSERT INTO UserDevices (user_id, device_uuid, is_approved, description, last_login_at)
        SELECT u.id,
               NEWID(),
               1,
               CONCAT(N'Device ', n.n, N' for user ', u.username),
               GETDATE()
        FROM Users u
                 CROSS JOIN (SELECT TOP (@devicesPerUser) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n FROM sys.all_columns) n;
    END

-- 8) Access_Grants (увеличено)
IF NOT EXISTS (SELECT 1 FROM Access_Grants)
    BEGIN
        PRINT N'-> Inserting Access Grants...';
        DECLARE @adminId INT = (SELECT TOP 1 id FROM Users WHERE username = 'sudo');
        INSERT INTO Access_Grants (granted_by, granted_to, permission_id, expires_at, comment)
        SELECT @adminId,
               u.id,
               p.id,
               DATEADD(MONTH, 12, GETDATE()),
               N'Test grant'
        FROM (SELECT TOP (@accessGrantsCount) id FROM Users ORDER BY NEWID()) u
                 CROSS JOIN (SELECT TOP 5 id FROM Permissions ORDER BY NEWID()) p;
    END

PRINT N'Test data generation for SmartCampusAuth completed.';
GO