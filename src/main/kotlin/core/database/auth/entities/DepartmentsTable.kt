package com.smartcampus.core.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object DepartmentsTable : IntIdTable("Departments") {
    val name = varchar("name", 100).uniqueIndex()
}