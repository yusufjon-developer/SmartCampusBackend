package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.Table

object TeachersInfoTable : Table("SmartCampus.dbo.Teachers_Info") {
    val teacherId = reference("teacher_id", TeachersTable)
    override val primaryKey = PrimaryKey(teacherId)

    val address = text("address").nullable()
    val passportNumber = varchar("passport_number", 100).nullable()
    val highSchool = varchar("high_school", 255).nullable()
    val documentNumber = varchar("document_number", 100).nullable()
    val military = varchar("military", 100).nullable()
    val degree = varchar("degree", 100).nullable()
    val title = varchar("title", 100).nullable()
    val position = varchar("position", 100).nullable()
}