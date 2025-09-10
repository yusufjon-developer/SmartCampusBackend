package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object SpecialitiesTable : IntIdTable("SmartCampus.dbo.Specialities") {
    val name = varchar("name", 255)
}