package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object AcademicWeeksTable : IntIdTable(name = "SmartCampus.dbo.Academic_Weeks") {
    val academicYear = varchar("academic_year", 20)
    val weekNumber = integer("week_number")
    val startDate = date("start_date")
    val endDate = date("end_date")
    val description = varchar("description", 255).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}
