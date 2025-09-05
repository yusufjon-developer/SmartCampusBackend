package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date

object CurriculumsTable : IntIdTable("SmartCampus.dbo.Curriculums") {
    val specialityId = reference("speciality_id", SpecialitiesTable).nullable()
    val year = integer("year").nullable()
    val profile = varchar("profile", 255).nullable()
    val educationForm = varchar("education_form", 100).nullable()
    val degree = varchar("degree", 100).nullable()
    val duration = integer("duration").nullable()
    val approvedDate = date("approved_date").nullable()
}