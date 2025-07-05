-- Создание базы данных (если еще не создана)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'SmartCampus')
BEGIN
    CREATE DATABASE SmartCampus;
END
GO

USE SmartCampus;
GO

-- Таблица: Specialities
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Specialities' and xtype='U')
CREATE TABLE Specialities (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL
);
GO

-- Таблица: Groups
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Groups' and xtype='U')
CREATE TABLE Groups (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    spec_id INT, -- Оставляем NULLABLE, как в оригинале
    course INT,
    CONSTRAINT FK_Groups_Specialities FOREIGN KEY (spec_id) REFERENCES Specialities(id) ON DELETE CASCADE
);
GO

-- Таблица: Subjects
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Subjects' and xtype='U')
CREATE TABLE Subjects (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL
);
GO

-- Таблица: Teachers
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Teachers' and xtype='U')
CREATE TABLE Teachers (
    id INT PRIMARY KEY IDENTITY(1,1),
    surname NVARCHAR(255),
    name NVARCHAR(255),
    lastname NVARCHAR(255),
    birthday DATE,
    email NVARCHAR(255),
    phone_number NVARCHAR(50),
    photo VARBINARY(MAX)
);
GO

-- Таблица: Teachers_Info
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Teachers_Info' and xtype='U')
CREATE TABLE Teachers_Info (
    teacher_id INT PRIMARY KEY,
    address NTEXT,
    passport_number NVARCHAR(100),
    high_school NVARCHAR(255),
    document_number NVARCHAR(100),
    military NVARCHAR(100),
    degree NVARCHAR(100),
    title NVARCHAR(100),
    position NVARCHAR(100),
    CONSTRAINT FK_TeachersInfo_Teachers FOREIGN KEY (teacher_id) REFERENCES Teachers(id) ON DELETE CASCADE
);
GO

-- Таблица: Students
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Students' and xtype='U')
CREATE TABLE Students (
    id INT PRIMARY KEY IDENTITY(1,1),
    surname NVARCHAR(255),
    name NVARCHAR(255),
    lastname NVARCHAR(255),
    birthday DATE,
    group_id INT,
    email NVARCHAR(255),
    phone_number NVARCHAR(50),
    photo VARBINARY(MAX),
    CONSTRAINT FK_Students_Groups FOREIGN KEY (group_id) REFERENCES Groups(id) ON DELETE CASCADE
);
GO

-- Таблица: Students_Info
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Students_Info' and xtype='U')
CREATE TABLE Students_Info (
    student_id INT PRIMARY KEY,
    address NTEXT,
    passport_number NVARCHAR(100),
    school NVARCHAR(255),
    document_number NVARCHAR(100),
    military NVARCHAR(100),
    student_card_number NVARCHAR(100),
    study_type NVARCHAR(100),
    study_form NVARCHAR(100),
    status NVARCHAR(100),
    father_fio NVARCHAR(255),
    father_phone NVARCHAR(50),
    father_address NTEXT,
    mother_fio NVARCHAR(255),
    mother_phone NVARCHAR(50),
    mother_address NTEXT,
    CONSTRAINT FK_StudentsInfo_Students FOREIGN KEY (student_id) REFERENCES Students(id) ON DELETE CASCADE
);
GO

-- Таблица: Auditoriums
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Auditoriums' and xtype='U')
CREATE TABLE Auditoriums (
    id INT PRIMARY KEY IDENTITY(1,1),
    number NVARCHAR(100),
    type NVARCHAR(100)
);
GO

-- Таблица: Disciplines
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Disciplines' and xtype='U')
CREATE TABLE Disciplines (
    id INT PRIMARY KEY IDENTITY(1,1),
    subject_id INT,
    semester INT,
    speciality_id INT,
    course INT,
    lecture INT,
    practice INT,
    lab INT,
    seminar INT,
    control NVARCHAR(100),
    CONSTRAINT FK_Disciplines_Subjects FOREIGN KEY (subject_id) REFERENCES Subjects(id) ON DELETE CASCADE,
    CONSTRAINT FK_Disciplines_Specialities FOREIGN KEY (speciality_id) REFERENCES Specialities(id) ON DELETE CASCADE
);
GO

-- Таблица: Curriculums
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Curriculums' and xtype='U')
CREATE TABLE Curriculums (
    id INT PRIMARY KEY IDENTITY(1,1),
    speciality_id INT,
    year INT,
    profile NVARCHAR(255),
    education_form NVARCHAR(100),
    degree NVARCHAR(100),
    duration INT,
    approved_date DATE,
    CONSTRAINT FK_Curriculums_Specialities FOREIGN KEY (speciality_id) REFERENCES Specialities(id) ON DELETE CASCADE
);
GO

-- Таблица: Curriculum_Disciplines
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Curriculum_Disciplines' and xtype='U')
CREATE TABLE Curriculum_Disciplines (
    id INT PRIMARY KEY IDENTITY(1,1),
    curriculum_id INT,
    discipline_id INT,
    semester INT,
    course INT,
    exam BIT,
    credit BIT,
    coursework BIT,
    control_type NVARCHAR(100),
    credits INT,
    CONSTRAINT FK_CurriculumDisciplines_Curriculums FOREIGN KEY (curriculum_id) REFERENCES Curriculums(id) ON DELETE CASCADE,
    CONSTRAINT FK_CurriculumDisciplines_Disciplines FOREIGN KEY (discipline_id) REFERENCES Disciplines(id) ON DELETE NO ACTION
);
GO

-- Таблица: Teachers_Workload
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Teachers_Workload' and xtype='U')
CREATE TABLE Teachers_Workload (
    id INT PRIMARY KEY IDENTITY(1,1),
    teacher_id INT,
    discipline_id INT,
    type NVARCHAR(100),
    hours INT,
    academic_year NVARCHAR(20),
    control_type NVARCHAR(100),
    group_id INT,
    CONSTRAINT FK_TeachersWorkload_Teachers FOREIGN KEY (teacher_id) REFERENCES Teachers(id) ON DELETE CASCADE,
    CONSTRAINT FK_TeachersWorkload_Disciplines FOREIGN KEY (discipline_id) REFERENCES Disciplines(id) ON DELETE NO ACTION,
    CONSTRAINT FK_TeachersWorkload_Groups FOREIGN KEY (group_id) REFERENCES Groups(id) ON DELETE NO ACTION
);
GO

-- Таблица: Schedule
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Schedule' and xtype='U')
CREATE TABLE Schedule (
    id INT PRIMARY KEY IDENTITY(1,1),
    day DATE,
    time TIME,
    group_id INT,
    discipline_id INT,
    teacher_id INT,
    auditorium_id INT,
    type NVARCHAR(100),
    CONSTRAINT FK_Schedule_Groups FOREIGN KEY (group_id) REFERENCES Groups(id) ON DELETE CASCADE,
    CONSTRAINT FK_Schedule_Disciplines FOREIGN KEY (discipline_id) REFERENCES Disciplines(id) ON DELETE NO ACTION,
    CONSTRAINT FK_Schedule_Teachers FOREIGN KEY (teacher_id) REFERENCES Teachers(id) ON DELETE NO ACTION,
    CONSTRAINT FK_Schedule_Auditoriums FOREIGN KEY (auditorium_id) REFERENCES Auditoriums(id) ON DELETE NO ACTION
);
GO

-- Таблица: Attendance
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Attendance' and xtype='U')
CREATE TABLE Attendance (
    id INT PRIMARY KEY IDENTITY(1,1),
    day DATE,
    time TIME,
    student_id INT,
    discipline_id INT,
    mark NVARCHAR(100),
    CONSTRAINT FK_Attendance_Students FOREIGN KEY (student_id) REFERENCES Students(id) ON DELETE CASCADE,
    CONSTRAINT FK_Attendance_Disciplines FOREIGN KEY (discipline_id) REFERENCES Disciplines(id) ON DELETE NO ACTION
);
GO

-- Таблица: Grades
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Grades' and xtype='U')
CREATE TABLE Grades (
    id INT PRIMARY KEY IDENTITY(1,1),
    day DATE,
    time TIME,
    student_id INT,
    discipline_id INT,
    teacher_id INT,
    round NVARCHAR(50),
    mark NVARCHAR(100),
    CONSTRAINT FK_Grades_Students FOREIGN KEY (student_id) REFERENCES Students(id) ON DELETE CASCADE,
    CONSTRAINT FK_Grades_Disciplines FOREIGN KEY (discipline_id) REFERENCES Disciplines(id) ON DELETE NO ACTION,
    CONSTRAINT FK_Grades_Teachers FOREIGN KEY (teacher_id) REFERENCES Teachers(id) ON DELETE NO ACTION
);
GO