package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object SubjectsTable : IntIdTable("SmartCampus.dbo.Subjects") {
    val name = varchar("name", 255)
}