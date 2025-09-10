package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.*
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

class WorkloadDao(private val db: SmartCampusDb) {

    private object SortableFields {
        val WORKLOADS: Map<String, Column<*>> = mapOf(
            "id" to TeachersWorkloadTable.id,
            "teacherId" to TeachersWorkloadTable.teacherId,
            "academicYear" to TeachersWorkloadTable.academicYear
        )
    }

    private fun Query.applyPaginationAndSorting(
        params: PageRequestParams,
        sortableFields: Map<String, Column<*>>,
        defaultSortColumn: Column<*>
    ): Query {
        val sortField = params.sortBy?.lowercase()
        val columnToSort = sortableFields[sortField] ?: defaultSortColumn
        this.orderBy(columnToSort to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    // list workloads (paginated)
    suspend fun listWorkloads(params: PageRequestParams): PaginatedResult<TeacherWorkloadDto> = db.query {
        val w = TeachersWorkloadTable
        val t = TeachersTable
        val d = DisciplinesTable
        val g = GroupsTable

        val joined = w
            .leftJoin(t, { w.teacherId }, { t.id })
            .leftJoin(d, { w.disciplineId }, { d.id })
            .leftJoin(g, { w.groupId }, { g.id })
            .select(w.columns + t.columns + d.columns + g.columns)
            .applyPaginationAndSorting(params, SortableFields.WORKLOADS, w.id)

        val rows = joined.map { row ->
            TeacherWorkloadDto(
                id = row[w.id].value,
                teacherId = row[w.teacherId]?.value ?: 0,
                teacherName = row[t.surname]?.let { s -> listOfNotNull(s, row[t.name]).joinToString(" ") },
                disciplineId = row[w.disciplineId]?.value,
                disciplineName = row[d.control]?.let { _ -> row[d.id].value.toString() }, // optional - adjust
                type = row[w.type],
                hours = row[w.hours],
                academicYear = row[w.academicYear],
                controlType = row[w.controlType],
                groupId = row[w.groupId]?.value,
                groupName = row[g.name]
            )
        }

        val totalItems = TeachersWorkloadTable.selectAll().count()
        val totalPages =
            if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()

        PaginatedResult(
            items = rows,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = params.page,
            pageSize = params.limit,
            sortBy = params.sortBy
        )
    }

    // get by id
    suspend fun getWorkloadById(id: Int): TeacherWorkloadDto? = db.query {
        val w = TeachersWorkloadTable
        val t = TeachersTable
        val d = DisciplinesTable
        val g = GroupsTable

        w.leftJoin(t, { w.teacherId }, { t.id })
            .leftJoin(d, { w.disciplineId }, { d.id })
            .leftJoin(g, { w.groupId }, { g.id })
            .select(w.columns + t.columns + d.columns + g.columns)
            .where { w.id eq id }
            .singleOrNull()
            ?.let { row ->
                TeacherWorkloadDto(
                    id = row[w.id].value,
                    teacherId = row[w.teacherId]?.value ?: 0,
                    teacherName = row[t.surname]?.let { s -> listOfNotNull(s, row[t.name]).joinToString(" ") },
                    disciplineId = row[w.disciplineId]?.value,
                    disciplineName = row[d.control]?.let { _ -> row[d.id].value.toString() },
                    type = row[w.type],
                    hours = row[w.hours],
                    academicYear = row[w.academicYear],
                    controlType = row[w.controlType],
                    groupId = row[w.groupId]?.value,
                    groupName = row[g.name]
                )
            }
    }

    // create workload
    suspend fun createWorkload(req: TeacherWorkloadCreateRequest): TeacherWorkloadDto {
        val newId = db.query {
            TeachersWorkloadTable.insertAndGetId {
                it[teacherId] = EntityID(req.teacherId, TeachersTable)
                it[disciplineId] = req.disciplineId?.let { EntityID(it, DisciplinesTable) }
                it[type] = req.type
                it[hours] = req.hours
                it[academicYear] = req.academicYear
                it[controlType] = req.controlType
                it[groupId] = req.groupId?.let { EntityID(it, GroupsTable) }
            }
        }
        return getWorkloadById(newId.value)!!
    }

    // update workload
    suspend fun updateWorkload(id: Int, req: TeacherWorkloadUpdateRequest): TeacherWorkloadDto? {
        db.query {
            TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.id eq id }.singleOrNull()
                ?: return@query null
            TeachersWorkloadTable.update({ TeachersWorkloadTable.id eq id }) {
                req.teacherId?.let { v -> it[teacherId] = EntityID(v, TeachersTable) }
                req.disciplineId?.let { v -> it[disciplineId] = EntityID(v, DisciplinesTable) }
                req.type?.let { v -> it[type] = v }
                req.hours?.let { v -> it[hours] = v }
                req.academicYear?.let { v -> it[academicYear] = v }
                req.controlType?.let { v -> it[controlType] = v }
                req.groupId?.let { v -> it[groupId] = EntityID(v, GroupsTable) }
            }
        }
        return getWorkloadById(id)
    }

    // delete
    suspend fun deleteWorkload(id: Int): Boolean = db.query {
        TeachersWorkloadTable.deleteWhere { TeachersWorkloadTable.id eq id } > 0
    }

    // get workloads for teacher
    suspend fun getWorkloadsForTeacher(teacherId: Int, academicYear: String?): List<TeacherWorkloadDto> = db.query {
        val q = TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.teacherId eq teacherId }
        if (academicYear != null) q.andWhere { TeachersWorkloadTable.academicYear eq academicYear }
        // join for extra info
        q.map { row ->
            TeacherWorkloadDto(
                id = row[TeachersWorkloadTable.id].value,
                teacherId = row[TeachersWorkloadTable.teacherId]?.value ?: teacherId,
                type = row[TeachersWorkloadTable.type],
                hours = row[TeachersWorkloadTable.hours],
                academicYear = row[TeachersWorkloadTable.academicYear],
                controlType = row[TeachersWorkloadTable.controlType],
                groupId = row[TeachersWorkloadTable.groupId]?.value,
                disciplineId = row[TeachersWorkloadTable.disciplineId]?.value
            )
        }
    }

    // Workload executions
    private val dateFormatter = DateTimeFormatter.ISO_DATE

    suspend fun listExecutionsForWorkload(workloadId: Int): List<WorkloadExecutionDto> = db.query {
        WorkloadExecutionTable.selectAll()
            .where { WorkloadExecutionTable.workloadId eq workloadId }
            .orderBy(WorkloadExecutionTable.executionDate, SortOrder.ASC)
            .map { row ->
                WorkloadExecutionDto(
                    id = row[WorkloadExecutionTable.id].value,
                    workloadId = row[WorkloadExecutionTable.workloadId].value,
                    executionDate = row[WorkloadExecutionTable.executionDate].toString(),
                    hours = row[WorkloadExecutionTable.hours].toDouble(),
                    executedBy = row[WorkloadExecutionTable.executedBy]?.value,
                    notes = row[WorkloadExecutionTable.notes],
                    status = row[WorkloadExecutionTable.status],
                    createdAt = row[WorkloadExecutionTable.createdAt].toString()
                )
            }
    }

    suspend fun createExecution(workloadId: Int, req: WorkloadExecutionCreateRequest): WorkloadExecutionDto? {
        val id = db.query {
            // ensure workload exists
            TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.id eq workloadId }.singleOrNull()
                ?: return@query null
            WorkloadExecutionTable.insertAndGetId {
                it[WorkloadExecutionTable.workloadId] = EntityID(workloadId, TeachersWorkloadTable)
                it[WorkloadExecutionTable.executionDate] = LocalDate.parse(req.executionDate, dateFormatter)
                it[WorkloadExecutionTable.hours] = req.hours.toBigDecimal()
                it[WorkloadExecutionTable.executedBy] = req.executedBy?.let { EntityID(it, TeachersTable) }
                it[WorkloadExecutionTable.notes] = req.notes
                it[WorkloadExecutionTable.status] = req.status
            }
        }
        return listExecutionsForWorkload(workloadId).find { it.id == id?.value }
    }
    // optional: sum executed hours for workload
    suspend fun sumExecutedHours(workloadId: Int): Double = db.query {
        WorkloadExecutionTable.select(WorkloadExecutionTable.hours.sum())
            .where { WorkloadExecutionTable.workloadId eq workloadId }
            .map { it[WorkloadExecutionTable.hours.sum()]?.toDouble() ?: 0.0 }
            .firstOrNull() ?: 0.0
    }
}
