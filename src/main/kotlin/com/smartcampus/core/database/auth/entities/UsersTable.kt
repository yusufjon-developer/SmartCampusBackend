package com.smartcampus.core.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

object UsersTable : IntIdTable("Users") {
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val email = varchar("email", 255).uniqueIndex()
    val fullName = varchar("full_name", 255)
    val role = varchar("role", 50).default("Student")
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}