package com.smartcampus.data.database.smartCampus.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

object WorkloadExecutionTable : IntIdTable(name = "SmartCampus.dbo.Workload_Execution") {
    val workloadId = reference("workload_id", TeachersWorkloadTable)
    val executionDate = date("execution_date")
    val hours = decimal("hours", precision = 5, scale = 2)
    val executedBy = reference("executed_by", TeachersTable).nullable()
    val notes = varchar("notes", 1000).nullable()
    val status = varchar("status", 50).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}
