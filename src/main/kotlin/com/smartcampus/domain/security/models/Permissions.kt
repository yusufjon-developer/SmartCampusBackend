package com.smartcampus.domain.security.models

object Permissions {

    // --- Specialities (Специальности) ---
    const val SPECIALITIES_CREATE = "specialities:create"
    const val SPECIALITIES_READ = "specialities:read"
    const val SPECIALITIES_UPDATE = "specialities:update"
    const val SPECIALITIES_DELETE = "specialities:delete"

    // --- Groups (Группы) ---
    const val GROUPS_CREATE = "groups:create"
    const val GROUPS_READ = "groups:read"
    const val GROUPS_UPDATE = "groups:update"
    const val GROUPS_DELETE = "groups:delete"

    // --- Subjects (Предметы) ---
    const val SUBJECTS_CREATE = "subjects:create"
    const val SUBJECTS_READ = "subjects:read"
    const val SUBJECTS_UPDATE = "subjects:update"
    const val SUBJECTS_DELETE = "subjects:delete"

    // --- Teachers (Преподаватели) ---
    const val TEACHERS_CREATE = "teachers:create"
    const val TEACHERS_READ_ALL = "teachers:read_all"
    const val TEACHERS_READ_OWN = "teachers:read_own"
    const val TEACHERS_UPDATE_ALL = "teachers:update_all"
    const val TEACHERS_UPDATE_OWN = "teachers:update_own"
    const val TEACHERS_DELETE = "teachers:delete"
    const val TEACHERS_INFO_UPDATE_ALL = "teachers_info:update_all"
    const val TEACHERS_INFO_UPDATE_OWN = "teachers_info:update_own"

    // --- Students (Студенты) ---
    const val STUDENTS_CREATE = "students:create"
    const val STUDENTS_READ_ALL = "students:read_all"
    const val STUDENTS_READ_OWN = "students:read_own"
    const val STUDENTS_UPDATE_ALL = "students:update_all"
    const val STUDENTS_UPDATE_OWN = "students:update_own"
    const val STUDENTS_DELETE = "students:delete"
    const val STUDENTS_INFO_UPDATE_ALL = "students_info:update_all"
    const val STUDENTS_INFO_UPDATE_OWN = "students_info:update_own"

    // --- Auditoriums (Аудитории) ---
    const val AUDITORIUMS_CREATE = "auditoriums:create"
    const val AUDITORIUMS_READ = "auditoriums:read"
    const val AUDITORIUMS_UPDATE = "auditoriums:update"
    const val AUDITORIUMS_DELETE = "auditoriums:delete"

    // --- Disciplines (Дисциплины курса/специальности) ---
    const val DISCIPLINES_CREATE = "disciplines:create"
    const val DISCIPLINES_READ = "disciplines:read"
    const val DISCIPLINES_UPDATE = "disciplines:update"
    const val DISCIPLINES_DELETE = "disciplines:delete"

    // --- Curriculums (Учебные планы) ---
    const val CURRICULUMS_CREATE = "curriculums:create"
    const val CURRICULUMS_READ = "curriculums:read"
    const val CURRICULUMS_UPDATE = "curriculums:update"
    const val CURRICULUMS_DELETE = "curriculums:delete"
    const val CURRICULUMS_APPROVE = "curriculums:approve"

    // --- Curriculum_Disciplines (Дисциплины в учебных планах) ---
    const val CURRICULUM_DISCIPLINES_MANAGE = "curriculum_disciplines:manage"
    const val CURRICULUM_DISCIPLINES_READ = "curriculum_disciplines:read"

    // --- Teachers_Workload (Нагрузка преподавателей) ---
    const val TEACHERS_WORKLOAD_CREATE = "teachers_workload:create"
    const val TEACHERS_WORKLOAD_READ_ALL = "teachers_workload:read_all"
    const val TEACHERS_WORKLOAD_READ_OWN = "teachers_workload:read_own"
    const val TEACHERS_WORKLOAD_UPDATE = "teachers_workload:update"
    const val TEACHERS_WORKLOAD_DELETE = "teachers_workload:delete"

    // --- Schedule (Расписание) ---
    const val SCHEDULE_CREATE = "schedule:create"
    const val SCHEDULE_READ_ALL = "schedule:read_all"
    const val SCHEDULE_READ_GROUP = "schedule:read_group"
    const val SCHEDULE_READ_TEACHER = "schedule:read_teacher"
    const val SCHEDULE_UPDATE = "schedule:update"
    const val SCHEDULE_DELETE = "schedule:delete"

    // --- Attendance (Посещаемость) ---
    const val ATTENDANCE_CREATE = "attendance:create"
    const val ATTENDANCE_READ_ALL = "attendance:read_all"
    const val ATTENDANCE_READ_OWN_STUDENT = "attendance:read_own_student"
    const val ATTENDANCE_READ_GROUP = "attendance:read_group"
    const val ATTENDANCE_UPDATE = "attendance:update"

    // --- Grades (Оценки) ---
    const val GRADES_CREATE = "grades:create"
    const val GRADES_READ_ALL = "grades:read_all"
    const val GRADES_READ_OWN_STUDENT = "grades:read_own_student"
    const val GRADES_READ_GROUP = "grades:read_group"
    const val GRADES_UPDATE = "grades:update"

    fun getAllPermissionNames(): List<String> {
        return listOf(
            SPECIALITIES_CREATE, SPECIALITIES_READ, SPECIALITIES_UPDATE, SPECIALITIES_DELETE,
            GROUPS_CREATE, GROUPS_READ, GROUPS_UPDATE, GROUPS_DELETE,
            SUBJECTS_CREATE, SUBJECTS_READ, SUBJECTS_UPDATE, SUBJECTS_DELETE,
            TEACHERS_CREATE, TEACHERS_READ_ALL, TEACHERS_READ_OWN, TEACHERS_UPDATE_ALL, TEACHERS_UPDATE_OWN, TEACHERS_DELETE, TEACHERS_INFO_UPDATE_ALL, TEACHERS_INFO_UPDATE_OWN,
            STUDENTS_CREATE, STUDENTS_READ_ALL, STUDENTS_READ_OWN, STUDENTS_UPDATE_ALL, STUDENTS_UPDATE_OWN, STUDENTS_DELETE, STUDENTS_INFO_UPDATE_ALL, STUDENTS_INFO_UPDATE_OWN,
            AUDITORIUMS_CREATE, AUDITORIUMS_READ, AUDITORIUMS_UPDATE, AUDITORIUMS_DELETE,
            DISCIPLINES_CREATE, DISCIPLINES_READ, DISCIPLINES_UPDATE, DISCIPLINES_DELETE,
            CURRICULUMS_CREATE, CURRICULUMS_READ, CURRICULUMS_UPDATE, CURRICULUMS_DELETE, CURRICULUMS_APPROVE,
            CURRICULUM_DISCIPLINES_MANAGE, CURRICULUM_DISCIPLINES_READ,
            TEACHERS_WORKLOAD_CREATE, TEACHERS_WORKLOAD_READ_ALL, TEACHERS_WORKLOAD_READ_OWN, TEACHERS_WORKLOAD_UPDATE, TEACHERS_WORKLOAD_DELETE,
            SCHEDULE_CREATE, SCHEDULE_READ_ALL, SCHEDULE_READ_GROUP, SCHEDULE_READ_TEACHER, SCHEDULE_UPDATE, SCHEDULE_DELETE,
            ATTENDANCE_CREATE, ATTENDANCE_READ_ALL, ATTENDANCE_READ_OWN_STUDENT, ATTENDANCE_READ_GROUP, ATTENDANCE_UPDATE,
            GRADES_CREATE, GRADES_READ_ALL, GRADES_READ_OWN_STUDENT, GRADES_READ_GROUP, GRADES_UPDATE
        )
    }
}