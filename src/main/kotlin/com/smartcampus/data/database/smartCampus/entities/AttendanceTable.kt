package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.time

object AttendanceTable : IntIdTable("SmartCampus.dbo.Attendance") {
    val day = date("day").nullable()
    val time = time("time").nullable()
    val studentId = reference("student_id", StudentsTable).nullable()
    val disciplineId = reference("discipline_id", DisciplinesTable).nullable()
    val mark = varchar("mark", 100).nullable()
}