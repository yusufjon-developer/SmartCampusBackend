package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.AuditoriumsTable
import com.smartcampus.domain.models.AuditoriumCreateRequest
import com.smartcampus.domain.models.AuditoriumDetailsDto
import com.smartcampus.domain.models.AuditoriumListItemDto
import com.smartcampus.domain.models.AuditoriumUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.math.ceil

class AuditoriumsDao(private val db: SmartCampusDb) {

    private object SortableFields {
        val AUD: Map<String, Column<*>> = mapOf(
            "id" to AuditoriumsTable.id,
            "number" to AuditoriumsTable.number
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

    suspend fun getAuditoriums(params: PageRequestParams): PaginatedResult<AuditoriumListItemDto> = db.query {
        val rows = AuditoriumsTable.selectAll().applyPaginationAndSorting(params, SortableFields.AUD, AuditoriumsTable.number).map { it.toListItem() }
        val total = AuditoriumsTable.selectAll().count()
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
        PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getAuditoriumById(id: Int): AuditoriumDetailsDto? = db.query {
        AuditoriumsTable.selectAll().where { AuditoriumsTable.id eq id }.singleOrNull()?.toDetails()
    }

    suspend fun createAuditorium(req: AuditoriumCreateRequest): AuditoriumDetailsDto = db.query {
        val newId = AuditoriumsTable.insertAndGetId {
            it[AuditoriumsTable.number] = req.number
            it[AuditoriumsTable.type] = req.type
        }
        AuditoriumDetailsDto(newId.value, req.number, req.type)
    }

    suspend fun updateAuditorium(id: Int, req: AuditoriumUpdateRequest): AuditoriumDetailsDto? = db.query {
        val existing = AuditoriumsTable.selectAll().where { AuditoriumsTable.id eq id }.singleOrNull() ?: return@query null
        AuditoriumsTable.update({ AuditoriumsTable.id eq id }) {
            if (req.number != null) it[AuditoriumsTable.number] = req.number
            if (req.type != null) it[AuditoriumsTable.type] = req.type
        }
        AuditoriumsTable.selectAll().where { AuditoriumsTable.id eq id }.singleOrNull()?.toDetails()
    }

    suspend fun deleteAuditorium(id: Int): Boolean = db.query {
        AuditoriumsTable.deleteWhere { AuditoriumsTable.id eq id } > 0
    }

    private fun ResultRow.toListItem(): AuditoriumListItemDto =
        AuditoriumListItemDto(id = this[AuditoriumsTable.id].value, number = this[AuditoriumsTable.number], type = this[AuditoriumsTable.type])

    private fun ResultRow.toDetails(): AuditoriumDetailsDto =
        AuditoriumDetailsDto(id = this[AuditoriumsTable.id].value, number = this[AuditoriumsTable.number], type = this[AuditoriumsTable.type])
}
