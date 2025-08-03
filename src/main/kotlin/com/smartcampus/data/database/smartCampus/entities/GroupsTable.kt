package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object GroupsTable : IntIdTable("Groups") {
    val name = varchar("name", 255)
    val specId = reference("spec_id", SpecialitiesTable).nullable()
    val course = integer("course").nullable()
}
