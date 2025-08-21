package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.WorkloadExecutionTable
import com.smartcampus.domain.models.WorkloadExecutionCreateRequest
import com.smartcampus.domain.models.WorkloadExecutionDto
import com.smartcampus.domain.models.WorkloadExecutionUpdateRequest
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate

class WorkloadExecutionDao(private val db: SmartCampusDb) {

    suspend fun listExecutions(workloadId: Int? = null): List<WorkloadExecutionDto> = db.query {
        val q = if (workloadId != null) {
            WorkloadExecutionTable.selectAll().where { WorkloadExecutionTable.workloadId eq workloadId }
        } else {
            WorkloadExecutionTable.selectAll()
        }
        q.map {
            WorkloadExecutionDto(
                id = it[WorkloadExecutionTable.id].value,
                workloadId = it[WorkloadExecutionTable.workloadId].value,
                executionDate = it[WorkloadExecutionTable.executionDate].toString(),
                hours = it[WorkloadExecutionTable.hours].toDouble(),
                executedBy = it[WorkloadExecutionTable.executedBy]?.value,
                notes = it[WorkloadExecutionTable.notes],
                status = it[WorkloadExecutionTable.status]
            )
        }
    }

    suspend fun getExecutionById(id: Int): WorkloadExecutionDto? = db.query {
        WorkloadExecutionTable.selectAll().where { WorkloadExecutionTable.id eq id }.singleOrNull()?.let {
            WorkloadExecutionDto(
                id = it[WorkloadExecutionTable.id].value,
                workloadId = it[WorkloadExecutionTable.workloadId].value,
                executionDate = it[WorkloadExecutionTable.executionDate].toString(),
                hours = it[WorkloadExecutionTable.hours].toDouble(),
                executedBy = it[WorkloadExecutionTable.executedBy]?.value,
                notes = it[WorkloadExecutionTable.notes],
                status = it[WorkloadExecutionTable.status]
            )
        }
    }

    suspend fun createExecution(req: WorkloadExecutionCreateRequest): WorkloadExecutionDto = db.query {
        val newId = WorkloadExecutionTable.insertAndGetId {
            it[workloadId] = req.workloadId
            it[executionDate] = LocalDate.parse(req.executionDate)
            it[hours] = req.hours.toBigDecimal()
            it[executedBy] = req.executedBy
            it[notes] = req.notes
            it[status] = req.status
        }
        WorkloadExecutionDto(
            id = newId.value,
            workloadId = req.workloadId,
            executionDate = req.executionDate,
            hours = req.hours,
            executedBy = req.executedBy,
            notes = req.notes,
            status = req.status
        )
    }

    suspend fun updateExecution(id: Int, req: WorkloadExecutionUpdateRequest): WorkloadExecutionDto? = db.query {
        val existing = WorkloadExecutionTable.selectAll().where { WorkloadExecutionTable.id eq id }.singleOrNull() ?: return@query null
        WorkloadExecutionTable.update({ WorkloadExecutionTable.id eq id }) {
            if (req.executionDate != null) it[executionDate] = LocalDate.parse(req.executionDate)
            if (req.hours != null) it[hours] = req.hours.toBigDecimal()
            if (req.executedBy != null) it[executedBy] = req.executedBy
            if (req.notes != null) it[notes] = req.notes
            if (req.status != null) it[status] = req.status
        }
        getExecutionById(id)
    }

    suspend fun deleteExecution(id: Int): Boolean = db.query {
        WorkloadExecutionTable.deleteWhere { WorkloadExecutionTable.id eq id } > 0
    }
}
