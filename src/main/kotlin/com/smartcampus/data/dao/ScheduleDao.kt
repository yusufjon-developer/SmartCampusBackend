package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.ScheduleTable
import com.smartcampus.domain.models.ScheduleCreateRequest
import com.smartcampus.domain.models.ScheduleEntryDto
import com.smartcampus.domain.models.ScheduleSearchFilter
import com.smartcampus.domain.models.ScheduleSearchResult
import com.smartcampus.domain.models.ScheduleUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil

class ScheduleDao(private val db: SmartCampusDb) {

    private object Sortable {
        val S: Map<String, Column<*>> = mapOf(
            "id" to ScheduleTable.id,
            "day" to ScheduleTable.day,
            "time" to ScheduleTable.time
        )
    }

    private fun Query.applyPaginationAndSorting(params: PageRequestParams, sortable: Map<String, Column<*>>, default: Column<*>): Query {
        val sf = params.sortBy?.lowercase()
        val col = sortable[sf] ?: default
        this.orderBy(col to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    suspend fun searchSchedule(filter: ScheduleSearchFilter): ScheduleSearchResult = db.query {
        var q = ScheduleTable.selectAll()
        if (filter.dayFrom != null) q = q.andWhere { ScheduleTable.day greaterEq LocalDate.parse(filter.dayFrom) }
        if (filter.dayTo != null) q = q.andWhere { ScheduleTable.day lessEq LocalDate.parse(filter.dayTo) }
        if (filter.teacherId != null) q = q.andWhere { ScheduleTable.teacherId eq filter.teacherId }
        if (filter.groupId != null) q = q.andWhere { ScheduleTable.groupId eq filter.groupId }
        if (filter.disciplineId != null) q = q.andWhere { ScheduleTable.disciplineId eq filter.disciplineId }
        if (filter.auditoriumId != null) q = q.andWhere { ScheduleTable.auditoriumId eq filter.auditoriumId }
        if (filter.type != null) q = q.andWhere { ScheduleTable.type eq filter.type }

        val total = q.count()
        val page = filter.page
        val size = filter.size
        val offset = ((if (page > 0) page - 1 else 0) * size).toLong()
        val rows = q.limit(size).offset(offset).map { it.toDto() }
        ScheduleSearchResult(results = rows, total = total, page = page, size = size)
    }

    suspend fun getScheduleEntries(params: PageRequestParams): PaginatedResult<ScheduleEntryDto> = db.query {
        val rows = ScheduleTable.selectAll().applyPaginationAndSorting(params, Sortable.S, ScheduleTable.day).map { it.toDto() }
        val total = ScheduleTable.selectAll().count()
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getScheduleEntryById(id: Int): ScheduleEntryDto? = db.query {
        ScheduleTable.selectAll().where { ScheduleTable.id eq id }.singleOrNull()?.toDto()
    }

    suspend fun createScheduleEntry(req: ScheduleCreateRequest): ScheduleEntryDto = db.query {
        val newId = ScheduleTable.insertAndGetId {
            it[ScheduleTable.day] = LocalDate.parse(req.day)
            it[ScheduleTable.time] = LocalTime.parse(req.time)
            it[ScheduleTable.groupId] = req.groupId
            it[ScheduleTable.disciplineId] = req.disciplineId
            it[ScheduleTable.teacherId] = req.teacherId
            it[ScheduleTable.auditoriumId] = req.auditoriumId
            it[ScheduleTable.type] = req.type
        }
        getScheduleEntryById(newId.value)!!
    }

    suspend fun updateScheduleEntry(id: Int, req: ScheduleUpdateRequest): ScheduleEntryDto? = db.query {
        ScheduleTable.update({ ScheduleTable.id eq id }) {
            if (req.day != null) it[ScheduleTable.day] = LocalDate.parse(req.day)
            if (req.time != null) it[ScheduleTable.time] = LocalTime.parse(req.time)
            if (req.groupId != null) it[ScheduleTable.groupId] = req.groupId
            it[ScheduleTable.disciplineId] = req.disciplineId
            it[ScheduleTable.teacherId] = req.teacherId
            it[ScheduleTable.auditoriumId] = req.auditoriumId
            it[ScheduleTable.type] = req.type
        }
        getScheduleEntryById(id)
    }

    suspend fun deleteScheduleEntry(id: Int): Boolean = db.query {
        ScheduleTable.deleteWhere { ScheduleTable.id eq id } > 0
    }

    suspend fun getScheduleForTeacher(teacherId: Int, filter: ScheduleSearchFilter): ScheduleSearchResult = db.query {
        var q = ScheduleTable.selectAll().where { ScheduleTable.teacherId eq teacherId }
        if (filter.dayFrom != null) q = q.andWhere { ScheduleTable.day greaterEq LocalDate.parse(filter.dayFrom) }
        if (filter.dayTo != null) q = q.andWhere { ScheduleTable.day lessEq LocalDate.parse(filter.dayTo) }
        val total = q.count()
        val offset = ((if (filter.page > 0) filter.page - 1 else 0) * filter.size).toLong()
        val rows = q.limit(filter.size).offset(offset).map { it.toDto() }
        ScheduleSearchResult(rows, total, filter.page, filter.size)
    }

    suspend fun getScheduleForGroup(groupId: Int, filter: ScheduleSearchFilter): ScheduleSearchResult = db.query {
        var q = ScheduleTable.selectAll().where { ScheduleTable.groupId eq groupId }
        if (filter.dayFrom != null) q = q.andWhere { ScheduleTable.day greaterEq LocalDate.parse(filter.dayFrom) }
        if (filter.dayTo != null) q = q.andWhere { ScheduleTable.day lessEq LocalDate.parse(filter.dayTo) }
        val total = q.count()
        val offset = ((if (filter.page > 0) filter.page - 1 else 0) * filter.size).toLong()
        val rows = q.limit(filter.size).offset(offset).map { it.toDto() }
        ScheduleSearchResult(rows, total, filter.page, filter.size)
    }

    private fun ResultRow.toDto(): ScheduleEntryDto =
        ScheduleEntryDto(
            id = this[ScheduleTable.id].value,
            day = this[ScheduleTable.day].toString(),
            time = this[ScheduleTable.time].toString(),
            groupId = this[ScheduleTable.groupId]?.value,
            disciplineId = this[ScheduleTable.disciplineId]?.value,
            teacherId = this[ScheduleTable.teacherId]?.value,
            auditoriumId = this[ScheduleTable.auditoriumId]?.value,
            type = this[ScheduleTable.type]
        )
}
