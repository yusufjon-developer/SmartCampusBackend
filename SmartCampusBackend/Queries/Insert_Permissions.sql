USE SmartCampusAuth;
GO

PRINT 'Starting cleanup and recreation of permissions...';

-- 1. Удаление всех существующих индивидуальных прав доступа
PRINT 'Deleting data from Access_Grants...';
DELETE FROM Access_Grants;
-- Сброс IDENTITY, если нужно начать ID с 1 (опционально, зависит от политики)
-- DBCC CHECKIDENT ('Access_Grants', RESEED, 0);
GO

-- 2. Удаление всех существующих связей ролей и разрешений
PRINT 'Deleting data from Role_Permissions...';
DELETE FROM Role_Permissions;
GO

-- 3. Удаление всех существующих разрешений
PRINT 'Deleting data from Permissions...';
DELETE FROM Permissions;
-- Сброс IDENTITY для таблицы Permissions, чтобы ID начинались с 1
DBCC CHECKIDENT ('Permissions', RESEED, 0);
GO

PRINT 'Cleanup complete. Inserting new CRUD+Own Permissions for SmartCampus tables...';

-- Таблица: Specialities (Специальности)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:create')
    INSERT INTO Permissions (name, description) VALUES ('specialities:create', N'Создание специальностей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:read')
    INSERT INTO Permissions (name, description) VALUES ('specialities:read', N'Просмотр списка специальностей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:update')
    INSERT INTO Permissions (name, description) VALUES ('specialities:update', N'Редактирование специальностей');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'specialities:delete')
    INSERT INTO Permissions (name, description) VALUES ('specialities:delete', N'Удаление специальностей');
GO

-- Таблица: Groups (Группы)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:create')
    INSERT INTO Permissions (name, description) VALUES ('groups:create', N'Создание групп');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:read')
    INSERT INTO Permissions (name, description) VALUES ('groups:read', N'Просмотр списка групп');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:update')
    INSERT INTO Permissions (name, description) VALUES ('groups:update', N'Редактирование групп');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'groups:delete')
    INSERT INTO Permissions (name, description) VALUES ('groups:delete', N'Удаление групп');
GO

-- Таблица: Subjects (Предметы/Дисциплины - используем "subjects" как в таблице)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:create')
    INSERT INTO Permissions (name, description) VALUES ('subjects:create', N'Создание учебных предметов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:read')
    INSERT INTO Permissions (name, description) VALUES ('subjects:read', N'Просмотр списка учебных предметов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:update')
    INSERT INTO Permissions (name, description) VALUES ('subjects:update', N'Редактирование учебных предметов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'subjects:delete')
    INSERT INTO Permissions (name, description) VALUES ('subjects:delete', N'Удаление учебных предметов');
GO

-- Таблица: Teachers (Преподаватели)
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
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_info:update_all')
    INSERT INTO Permissions (name, description) VALUES ('teachers_info:update_all', N'Редактирование доп. информации о любом преподавателе');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'teachers_info:update_own')
    INSERT INTO Permissions (name, description) VALUES ('teachers_info:update_own', N'Редактирование своей доп. информации преподавателя');
GO

-- Таблица: Students (Студенты)
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
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students_info:update_all')
    INSERT INTO Permissions (name, description) VALUES ('students_info:update_all', N'Редактирование доп. информации о любом студенте');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'students_info:update_own')
    INSERT INTO Permissions (name, description) VALUES ('students_info:update_own', N'Редактирование своей доп. информации студента');
GO

-- Таблица: Auditoriums (Аудитории)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:create')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:create', N'Добавление аудиторий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:read')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:read', N'Просмотр списка аудиторий');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:update')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:update', N'Редактирование информации об аудиториях');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'auditoriums:delete')
    INSERT INTO Permissions (name, description) VALUES ('auditoriums:delete', N'Удаление аудиторий');
GO

-- Таблица: Disciplines (Дисциплины курса/специальности)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:create')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:create', N'Добавление дисциплин к специальностям/курсам');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:read')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:read', N'Просмотр списка дисциплин специальностей/курсов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:update')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:update', N'Редактирование дисциплин специальностей/курсов');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'disciplines:delete')
    INSERT INTO Permissions (name, description) VALUES ('disciplines:delete', N'Удаление дисциплин из специальностей/курсов');
GO

-- Таблица: Curriculums (Учебные планы)
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
GO

-- Таблица: Curriculum_Disciplines (Дисциплины в учебных планах)
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculum_disciplines:manage')
    INSERT INTO Permissions (name, description) VALUES ('curriculum_disciplines:manage', N'Управление дисциплинами в учебных планах (добавление, изменение, удаление)');
IF NOT EXISTS (SELECT 1 FROM Permissions WHERE name = 'curriculum_disciplines:read')
    INSERT INTO Permissions (name, description) VALUES ('curriculum_disciplines:read', N'Просмотр дисциплин в учебных планах');
GO

-- Таблица: Teachers_Workload (Нагрузка преподавателей)
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
GO

-- Таблица: Schedule (Расписание)
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
GO

-- Таблица: Attendance (Посещаемость)
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
GO

-- Таблица: Grades (Оценки)
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

PRINT 'All permissions have been recreated with Unicode descriptions.';
GO

