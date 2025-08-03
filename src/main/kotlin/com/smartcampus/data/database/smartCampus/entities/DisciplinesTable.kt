package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object DisciplinesTable : IntIdTable("Disciplines") {
    val subjectId = reference("subject_id", SubjectsTable).nullable()
    val semester = integer("semester").nullable()
    val specialityId = reference("speciality_id", SpecialitiesTable).nullable()
    val course = integer("course").nullable()
    val lecture = integer("lecture").nullable()
    val practice = integer("practice").nullable()
    val lab = integer("lab").nullable()
    val seminar = integer("seminar").nullable()
    val control = varchar("control", 100).nullable()
}