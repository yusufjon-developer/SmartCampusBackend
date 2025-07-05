package com.smartcampus.core.database.auth.entities

import org.jetbrains.exposed.v1.core.Table

object SuperusersTable : Table("Superusers") {
    val userId = integer("user_id").references(UsersTable.id)

    override val primaryKey = PrimaryKey(userId)
}