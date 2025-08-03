package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.time

object ScheduleTable : IntIdTable("Schedule") {
    val day = date("day").nullable()
    val time = time("time").nullable()
    val groupId = reference("group_id", GroupsTable).nullable()
    val disciplineId = reference("discipline_id", DisciplinesTable).nullable()
    val teacherId = reference("teacher_id", TeachersTable).nullable()
    val auditoriumId = reference("auditorium_id", AuditoriumsTable).nullable()
    val type = varchar("type", 100).nullable()
}