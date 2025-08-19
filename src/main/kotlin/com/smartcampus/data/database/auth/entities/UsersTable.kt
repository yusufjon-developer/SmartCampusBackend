package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object UsersTable : IntIdTable("Users") {
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val email = varchar("email", 255).nullable()
    val fullName = varchar("full_name", 255).nullable()
    val roleId = reference("role_id", RolesTable).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val studentProfileId = integer("student_profile_id").nullable().uniqueIndex()
    val teacherProfileId = integer("teacher_profile_id").nullable().uniqueIndex()
}
