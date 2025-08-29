package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.SpecialitiesTable
import com.smartcampus.domain.models.SpecialityCreateRequest
import com.smartcampus.domain.models.SpecialityDto
import com.smartcampus.domain.models.SpecialityUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.LoggerFactory
import kotlin.math.ceil

class SpecialitiesDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(SpecialitiesDao::class.java)

    private object SortableFields {
        val SPECIALITIES: Map<String, Column<*>> = mapOf(
            "id" to SpecialitiesTable.id,
            "name" to SpecialitiesTable.name
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

    suspend fun listSpecialities(params: PageRequestParams): PaginatedResult<SpecialityDto> = db.query {
        val rows = SpecialitiesTable
            .selectAll()
            .applyPaginationAndSorting(params, SortableFields.SPECIALITIES, SpecialitiesTable.name)
            .map { r -> SpecialityDto(id = r[SpecialitiesTable.id].value, name = r[SpecialitiesTable.name]) }

        val totalItems = SpecialitiesTable.selectAll().count()
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

    suspend fun getSpecialityById(id: Int): SpecialityDto? = db.query {
        SpecialitiesTable.selectAll().where { SpecialitiesTable.id eq id }.singleOrNull()?.let {
            SpecialityDto(it[SpecialitiesTable.id].value, it[SpecialitiesTable.name])
        }
    }

    suspend fun createSpeciality(req: SpecialityCreateRequest): SpecialityDto = db.query {
        val id = SpecialitiesTable.insertAndGetId {
            it[name] = req.name
        }
        SpecialityDto(id.value, req.name)
    }

    suspend fun updateSpeciality(id: Int, req: SpecialityUpdateRequest): SpecialityDto? = db.query {

        val exists = SpecialitiesTable
            .selectAll()
            .where { SpecialitiesTable.id eq id }
            .singleOrNull() ?: return@query null

        SpecialitiesTable.update({ SpecialitiesTable.id eq id }) {
            log.info("start update")
            req.name?.let { v -> it[SpecialitiesTable.name] = v }
        }

        SpecialitiesTable
            .selectAll()
            .where { SpecialitiesTable.id eq id }
            .singleOrNull()
            ?.let { SpecialityDto(it[SpecialitiesTable.id].value, it[SpecialitiesTable.name]) }
    }


    suspend fun deleteSpeciality(id: Int): Boolean = db.query {
        SpecialitiesTable.deleteWhere { SpecialitiesTable.id eq id } > 0
    }
}
