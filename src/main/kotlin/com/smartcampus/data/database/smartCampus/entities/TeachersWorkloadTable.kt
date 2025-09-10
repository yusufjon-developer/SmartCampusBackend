package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object TeachersWorkloadTable : IntIdTable("SmartCampus.dbo.Teachers_Workload") {
    val teacherId = reference("teacher_id", TeachersTable).nullable()
    val disciplineId = reference("discipline_id", DisciplinesTable).nullable()
    val type = varchar("type", 100).nullable()
    val hours = integer("hours").nullable()
    val academicYear = varchar("academic_year", 20).nullable()
    val controlType = varchar("control_type", 100).nullable()
    val groupId = reference("group_id", GroupsTable).nullable()
}