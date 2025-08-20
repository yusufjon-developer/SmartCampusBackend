package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.GradesTable
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.GradeCreateRequest
import com.smartcampus.domain.models.GradeRecordDto
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.math.ceil
import java.time.LocalDate
import java.time.LocalTime

class GradesDao(private val db: SmartCampusDb) {

    private object Sortable {
        val G: Map<String, Column<*>> = mapOf("id" to GradesTable.id, "day" to GradesTable.day)
    }

    private fun Query.applyPaginationAndSorting(params: PageRequestParams, sortable: Map<String, Column<*>>, default: Column<*>): Query {
        val f = params.sortBy?.lowercase()
        val c = sortable[f] ?: default
        this.orderBy(c to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    suspend fun addGrade(req: GradeCreateRequest): GradeRecordDto = db.query {
        val id = GradesTable.insertAndGetId {
            it[GradesTable.day] = LocalDate.parse(req.day)
            if (req.time != null) it[GradesTable.time] = LocalTime.parse(req.time)
            it[GradesTable.studentId] = req.studentId
            it[GradesTable.disciplineId] = req.disciplineId
            it[GradesTable.teacherId] = req.teacherId
            it[GradesTable.round] = req.round
            it[GradesTable.mark] = req.mark
        }
        getGradeById(id.value)!!
    }

    suspend fun getGradeById(id: Int): GradeRecordDto? = db.query {
        GradesTable.selectAll().where { GradesTable.id eq id }.singleOrNull()?.toDto()
    }

    suspend fun updateGrade(id: Int, req: GradeCreateRequest): GradeRecordDto? = db.query {
        GradesTable.update({ GradesTable.id eq id }) {
            it[GradesTable.day] = LocalDate.parse(req.day)
            if (req.time != null) it[GradesTable.time] = LocalTime.parse(req.time)
            it[GradesTable.studentId] = req.studentId
            it[GradesTable.disciplineId] = req.disciplineId
            it[GradesTable.teacherId] = req.teacherId
            it[GradesTable.round] = req.round
            it[GradesTable.mark] = req.mark
        }
        getGradeById(id)
    }

    suspend fun deleteGrade(id: Int): Boolean = db.query {
        GradesTable.deleteWhere { GradesTable.id eq id } > 0
    }

    suspend fun getGradesForStudent(studentId: Int, params: PageRequestParams): PaginatedResult<GradeRecordDto> = db.query {
        val q = GradesTable.selectAll().where { GradesTable.studentId eq studentId }
        val total = q.count()
        val rows = q.applyPaginationAndSorting(params, Sortable.G, GradesTable.day).map { it.toDto() }
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getGradesForDiscipline(disciplineId: Int, params: PageRequestParams): PaginatedResult<GradeRecordDto> = db.query {
        val q = GradesTable.selectAll().where { GradesTable.disciplineId eq disciplineId }
        val total = q.count()
        val rows = q.applyPaginationAndSorting(params, Sortable.G, GradesTable.day).map { it.toDto() }
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    private fun ResultRow.toDto(): GradeRecordDto =
        GradeRecordDto(
            id = this[GradesTable.id].value,
            day = this[GradesTable.day].toString(),
            time = this[GradesTable.time]?.toString(),
            studentId = this[GradesTable.studentId]!!.value,
            disciplineId = this[GradesTable.disciplineId]!!.value,
            teacherId = this[GradesTable.teacherId]?.value,
            round = this[GradesTable.round],
            mark = this[GradesTable.mark]
        )
}
