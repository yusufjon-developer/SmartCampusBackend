-- Индексы для внешних ключей и частых выборок

-- Groups
CREATE INDEX IDX_Groups_spec_id ON Groups(spec_id);

-- Students
CREATE INDEX IDX_Students_group_id ON Students(group_id);

-- Students_Info
CREATE INDEX IDX_Students_Info_student_id ON Students_Info(student_id);

-- Teachers_Info
CREATE INDEX IDX_Teachers_Info_teacher_id ON Teachers_Info(teacher_id);

-- Disciplines
CREATE INDEX IDX_Disciplines_subject_id ON Disciplines(subject_id);
CREATE INDEX IDX_Disciplines_speciality_id ON Disciplines(speciality_id);

-- Curriculum_Disciplines
CREATE INDEX IDX_CurriculumDisciplines_curriculum_id ON Curriculum_Disciplines(curriculum_id);
CREATE INDEX IDX_CurriculumDisciplines_discipline_id ON Curriculum_Disciplines(discipline_id);

-- Teachers_Workload
CREATE INDEX IDX_TeachersWorkload_teacher_id ON Teachers_Workload(teacher_id);
CREATE INDEX IDX_TeachersWorkload_discipline_id ON Teachers_Workload(discipline_id);
CREATE INDEX IDX_TeachersWorkload_group_id ON Teachers_Workload(group_id);

-- Schedule
CREATE INDEX IDX_Schedule_group_id ON Schedule(group_id);
CREATE INDEX IDX_Schedule_teacher_id ON Schedule(teacher_id);
CREATE INDEX IDX_Schedule_discipline_id ON Schedule(discipline_id);

-- Attendance
CREATE INDEX IDX_Attendance_student_id ON Attendance(student_id);
CREATE INDEX IDX_Attendance_discipline_id ON Attendance(discipline_id);

-- Grades
CREATE INDEX IDX_Grades_student_id ON Grades(student_id);
CREATE INDEX IDX_Grades_teacher_id ON Grades(teacher_id);
CREATE INDEX IDX_Grades_discipline_id ON Grades(discipline_id);
