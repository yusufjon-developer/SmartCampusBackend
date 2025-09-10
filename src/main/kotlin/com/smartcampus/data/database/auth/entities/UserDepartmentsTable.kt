package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable


object UserDepartmentsTable : IntIdTable("SmartCampusAuth.dbo.User_Departments") {
    val userId = reference("user_id", UsersTable)
    val departmentId = reference("department_id", DepartmentsTable)
    val isManager = bool("is_manager").default(false)

    init {
        uniqueIndex("PK_UserDepartments", userId, departmentId)
    }
}