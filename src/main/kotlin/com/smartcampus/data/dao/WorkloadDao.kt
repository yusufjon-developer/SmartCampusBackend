package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.TeachersWorkloadTable
import com.smartcampus.domain.models.TeacherWorkloadCreateRequest
import com.smartcampus.domain.models.TeacherWorkloadDto
import com.smartcampus.domain.models.TeacherWorkloadUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.math.ceil

class WorkloadDao(private val db: SmartCampusDb) {

    private object Sortable {
        val WL: Map<String, Column<*>> = mapOf(
            "id" to TeachersWorkloadTable.id,
            "hours" to TeachersWorkloadTable.hours,
            "teacher_id" to TeachersWorkloadTable.teacherId
        )
    }

    private fun Query.applyPaginationAndSorting(params: PageRequestParams, sortable: Map<String, Column<*>>, default: Column<*>): Query {
        val f = params.sortBy?.lowercase()
        val c = sortable[f] ?: default
        this.orderBy(c to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    suspend fun getWorkloads(params: PageRequestParams): PaginatedResult<TeacherWorkloadDto> = db.query {
        val rows = TeachersWorkloadTable.selectAll().applyPaginationAndSorting(params, Sortable.WL, TeachersWorkloadTable.id).map { it.toDto() }
        val total = TeachersWorkloadTable.selectAll().count()
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getWorkloadById(id: Int): TeacherWorkloadDto? = db.query {
        TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.id eq id }.singleOrNull()?.toDto()
    }

    suspend fun createWorkload(req: TeacherWorkloadCreateRequest): TeacherWorkloadDto = db.query {
        val id = TeachersWorkloadTable.insertAndGetId {
            it[TeachersWorkloadTable.teacherId] = req.teacherId
            it[TeachersWorkloadTable.disciplineId] = req.disciplineId
            it[TeachersWorkloadTable.type] = req.type
            if (req.hours != null) it[TeachersWorkloadTable.hours] = req.hours
            it[TeachersWorkloadTable.academicYear] = req.academicYear
            it[TeachersWorkloadTable.controlType] = req.controlType
            it[TeachersWorkloadTable.groupId] = req.groupId
        }
        getWorkloadById(id.value)!!
    }

    suspend fun updateWorkload(id: Int, req: TeacherWorkloadUpdateRequest): TeacherWorkloadDto? = db.query {
        TeachersWorkloadTable.update({ TeachersWorkloadTable.id eq id }) {
            if (req.type != null) it[TeachersWorkloadTable.type] = req.type
            if (req.hours != null) it[TeachersWorkloadTable.hours] = req.hours
            if (req.academicYear != null) it[TeachersWorkloadTable.academicYear] = req.academicYear
            if (req.controlType != null) it[TeachersWorkloadTable.controlType] = req.controlType
            if (req.groupId != null) it[TeachersWorkloadTable.groupId] = req.groupId
        }
        getWorkloadById(id)
    }

    suspend fun deleteWorkload(id: Int): Boolean = db.query {
        TeachersWorkloadTable.deleteWhere { TeachersWorkloadTable.id eq id } > 0
    }

    suspend fun getWorkloadsByTeacher(teacherId: Int, params: PageRequestParams): PaginatedResult<TeacherWorkloadDto> = db.query {
        val rows = TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.teacherId eq teacherId }.applyPaginationAndSorting(params, Sortable.WL, TeachersWorkloadTable.id).map { it.toDto() }
        val total = TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.teacherId eq teacherId }.count()
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getWorkloadsByGroup(groupId: Int, params: PageRequestParams): PaginatedResult<TeacherWorkloadDto> = db.query {
        val rows = TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.groupId eq groupId }.applyPaginationAndSorting(params, Sortable.WL, TeachersWorkloadTable.id).map { it.toDto() }
        val total = TeachersWorkloadTable.selectAll().where { TeachersWorkloadTable.groupId eq groupId }.count()
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    private fun ResultRow.toDto(): TeacherWorkloadDto =
        TeacherWorkloadDto(
            id = this[TeachersWorkloadTable.id].value,
            teacherId = this[TeachersWorkloadTable.teacherId]!!.value,
            disciplineId = this[TeachersWorkloadTable.disciplineId]!!.value,
            type = this[TeachersWorkloadTable.type],
            hours = this[TeachersWorkloadTable.hours],
            academicYear = this[TeachersWorkloadTable.academicYear],
            controlType = this[TeachersWorkloadTable.controlType],
            groupId = this[TeachersWorkloadTable.groupId]?.value
        )
}
