package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RolesTable : IntIdTable("Roles") {
    val name = varchar("name", 50).uniqueIndex()
    val description = varchar("description", 255).nullable()
}