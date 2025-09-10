package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime

object ScheduleTable : IntIdTable("SmartCampus.dbo.Schedule") {
    val workloadId = reference("workload_id", TeachersWorkloadTable).nullable()
    val day = date("day").nullable()
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val groupId = reference("group_id", GroupsTable).nullable()
    val disciplineId = reference("discipline_id", DisciplinesTable).nullable()
    val teacherId = reference("teacher_id", TeachersTable).nullable()
    val auditoriumId = reference("auditorium_id", AuditoriumsTable).nullable()
    val type = varchar("type", 100).nullable()
}