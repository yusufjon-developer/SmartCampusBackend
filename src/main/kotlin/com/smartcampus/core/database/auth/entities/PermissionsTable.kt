package com.smartcampus.core.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object PermissionsTable : IntIdTable("Permissions") {
    val name = varchar("name", 100).uniqueIndex()
    val description = varchar("description", 255)
}