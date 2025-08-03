package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object AuditoriumsTable : IntIdTable("Auditoriums") {
    val number = varchar("number", 100).nullable()
    val type = varchar("type", 100).nullable()
}