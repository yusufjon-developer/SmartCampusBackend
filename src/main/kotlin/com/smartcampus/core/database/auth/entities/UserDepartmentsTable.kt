package com.smartcampus.core.database.auth.entities

import org.jetbrains.exposed.v1.core.Table


object UserDepartmentsTable : Table("User_Departments") {
    val userId = integer("user_id").references(UsersTable.id)
    val departmentId = integer("department_id").references(DepartmentsTable.id)
    val isManager = bool("is_manager").default(false)

    override val primaryKey = PrimaryKey(userId, departmentId)
}