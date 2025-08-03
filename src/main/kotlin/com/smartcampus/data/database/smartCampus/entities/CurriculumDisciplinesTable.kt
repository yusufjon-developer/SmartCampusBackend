package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object CurriculumDisciplinesTable : IntIdTable("Curriculum_Disciplines") {
    val curriculumId = reference("curriculum_id", CurriculumsTable).nullable()
    val disciplineId = reference("discipline_id", DisciplinesTable).nullable()
    val semester = integer("semester").nullable()
    val course = integer("course").nullable()
    val exam = bool("exam").nullable()
    val credit = bool("credit").nullable()
    val coursework = bool("coursework").nullable()
    val controlType = varchar("control_type", 100).nullable()
    val credits = integer("credits").nullable()
}