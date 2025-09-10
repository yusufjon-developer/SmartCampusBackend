package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object SuperusersTable : IntIdTable("SmartCampusAuth.dbo.Superusers") {
    val userId = reference("user_id", UsersTable).uniqueIndex()
}