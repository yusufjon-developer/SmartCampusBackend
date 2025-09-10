package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.AttendanceTable
import com.smartcampus.domain.models.AttendanceCreateRequest
import com.smartcampus.domain.models.AttendanceRecordDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil

class AttendanceDao(private val db: SmartCampusDb) {

    private object Sortable {
        val A: Map<String, Column<*>> = mapOf("id" to AttendanceTable.id, "day" to AttendanceTable.day)
    }

    private fun Query.applyPaginationAndSorting(params: PageRequestParams, sortable: Map<String, Column<*>>, default: Column<*>): Query {
        val s = params.sortBy?.lowercase()
        val c = sortable[s] ?: default
        this.orderBy(c to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    suspend fun markAttendance(req: AttendanceCreateRequest): AttendanceRecordDto = db.query {
        val id = AttendanceTable.insertAndGetId {
            it[AttendanceTable.day] = LocalDate.parse(req.day)
            if (req.time != null) it[AttendanceTable.time] = LocalTime.parse(req.time)
            it[AttendanceTable.studentId] = req.studentId
            it[AttendanceTable.disciplineId] = req.disciplineId
            it[AttendanceTable.mark] = req.mark
        }
        getAttendanceById(id.value)!!
    }

    suspend fun getAttendanceById(id: Int): AttendanceRecordDto? = db.query {
        AttendanceTable.selectAll().where { AttendanceTable.id eq id }.singleOrNull()?.toDto()
    }

    suspend fun updateAttendance(id: Int, req: AttendanceCreateRequest): AttendanceRecordDto? = db.query {
        AttendanceTable.update({ AttendanceTable.id eq id }) {
            it[AttendanceTable.day] = LocalDate.parse(req.day)
            if (req.time != null) it[AttendanceTable.time] = LocalTime.parse(req.time)
            it[AttendanceTable.studentId] = req.studentId
            it[AttendanceTable.disciplineId] = req.disciplineId
            it[AttendanceTable.mark] = req.mark
        }
        getAttendanceById(id)
    }

    suspend fun deleteAttendance(id: Int): Boolean = db.query {
        AttendanceTable.deleteWhere { AttendanceTable.id eq id } > 0
    }

    suspend fun getAttendanceForStudent(studentId: Int, params: PageRequestParams): PaginatedResult<AttendanceRecordDto> = db.query {
        val q = AttendanceTable.selectAll().where { AttendanceTable.studentId eq studentId }
        val total = q.count()
        val rows = q.applyPaginationAndSorting(params, Sortable.A, AttendanceTable.day).map { it.toDto() }
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getAttendanceForDiscipline(disciplineId: Int, params: PageRequestParams): PaginatedResult<AttendanceRecordDto> = db.query {
        val q = AttendanceTable.selectAll().where { AttendanceTable.disciplineId eq disciplineId }
        val total = q.count()
        val rows = q.applyPaginationAndSorting(params, Sortable.A, AttendanceTable.day).map { it.toDto() }
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    private fun ResultRow.toDto(): AttendanceRecordDto =
        AttendanceRecordDto(
            id = this[AttendanceTable.id].value,
            day = this[AttendanceTable.day].toString(),
            time = this[AttendanceTable.time]?.toString(),
            studentId = this[AttendanceTable.studentId]!!.value,
            disciplineId = this[AttendanceTable.disciplineId]!!.value,
            mark = this[AttendanceTable.mark]
        )
}
