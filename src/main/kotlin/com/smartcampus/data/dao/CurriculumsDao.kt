package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.CurriculumsTable
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.CurriculumCreateRequest
import com.smartcampus.domain.models.CurriculumDetailsDto
import com.smartcampus.domain.models.CurriculumListItemDto
import com.smartcampus.domain.models.CurriculumUpdateRequest
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.math.ceil

class CurriculumsDao(private val db: SmartCampusDb) {

    private object Sortable {
        val CURS: Map<String, Column<*>> = mapOf(
            "id" to CurriculumsTable.id,
            "year" to CurriculumsTable.year
        )
    }

    private fun Query.applyPaginationAndSorting(
        params: PageRequestParams,
        sortable: Map<String, Column<*>>,
        default: Column<*>
    ): Query {
        val s = params.sortBy?.lowercase()
        val c = sortable[s] ?: default
        this.orderBy(c to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    suspend fun getCurriculums(params: PageRequestParams): PaginatedResult<CurriculumListItemDto> =
        db.query {
            val rows = CurriculumsTable.selectAll()
                .applyPaginationAndSorting(params, Sortable.CURS, CurriculumsTable.year)
                .map { it.toListItem() }
            val total = CurriculumsTable.selectAll().count()
            val totalPages =
                if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()
            PaginatedResult(rows, total, totalPages, params.page, params.limit, params.sortBy)
        }

    suspend fun getCurriculumById(id: Int): CurriculumDetailsDto? = db.query {
        CurriculumsTable.selectAll().where { CurriculumsTable.id eq id }.singleOrNull()?.toDetails()
    }

    suspend fun createCurriculum(req: CurriculumCreateRequest): CurriculumDetailsDto = db.query {
        val id = CurriculumsTable.insertAndGetId {
            it[CurriculumsTable.specialityId] = req.specialityId
            it[CurriculumsTable.year] = req.year
            it[CurriculumsTable.profile] = req.profile
            it[CurriculumsTable.educationForm] = req.educationForm
            it[CurriculumsTable.degree] = req.degree
            it[CurriculumsTable.duration] = req.duration
        }
        getCurriculumById(id.value)!!
    }

    suspend fun updateCurriculum(id: Int, req: CurriculumUpdateRequest): CurriculumDetailsDto? =
        db.query {
            val existing =
                CurriculumsTable.selectAll().where { CurriculumsTable.id eq id }.singleOrNull()
                    ?: return@query null
            CurriculumsTable.update({ CurriculumsTable.id eq id }) {
                if (req.profile != null) it[CurriculumsTable.profile] = req.profile
                if (req.educationForm != null) it[CurriculumsTable.educationForm] =
                    req.educationForm
                if (req.degree != null) it[CurriculumsTable.degree] = req.degree
                if (req.duration != null) it[CurriculumsTable.duration] = req.duration
            }
            getCurriculumById(id)
        }

    suspend fun deleteCurriculum(id: Int): Boolean = db.query {
        CurriculumsTable.deleteWhere { CurriculumsTable.id eq id } > 0
    }

    private fun ResultRow.toListItem(): CurriculumListItemDto =
        CurriculumListItemDto(
            id = this[CurriculumsTable.id].value,
            specialityId = this[CurriculumsTable.specialityId]!!.value,
            year = this[CurriculumsTable.year]!!,
            profile = this[CurriculumsTable.profile],
            educationForm = this[CurriculumsTable.educationForm]
        )

    private fun ResultRow.toDetails(): CurriculumDetailsDto =
        CurriculumDetailsDto(
            id = this[CurriculumsTable.id].value,
            specialityId = this[CurriculumsTable.specialityId]!!.value,
            year = this[CurriculumsTable.year]!!,
            profile = this[CurriculumsTable.profile],
            educationForm = this[CurriculumsTable.educationForm],
            degree = this[CurriculumsTable.degree],
            duration = this[CurriculumsTable.duration],
            approvedDate = this[CurriculumsTable.approvedDate]?.toString()
        )
}
