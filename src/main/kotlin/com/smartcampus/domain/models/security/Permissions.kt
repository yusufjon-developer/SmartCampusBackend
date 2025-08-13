package com.smartcampus.domain.models.security

object Permissions {

    // --- Общие и административные разрешения ---
    const val VIEW_PROFILE = "ViewProfile"
    const val EDIT_PROFILE = "EditProfile"
    // ... (старые общие права, если они остаются актуальными) ...
    const val MANAGE_ALL_USERS = "ManageAllUsers"
    const val GRANT_ANY_PERMISSION = "GrantAnyPermission"
    const val MANAGE_ROLES = "ManageRoles"
    const val APPROVE_DEVICES = "ApproveDevices"
    const val VIEW_SYSTEM_LOGS = "ViewSystemLogs"
    const val STUDENT_REGISTER = "student:register" // Если регистрация студентов требует спец. права
    const val EMPLOYEE_REGISTER = "employee:register"

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
}