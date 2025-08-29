package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.SubjectsTable
import com.smartcampus.domain.models.SubjectCreateRequest
import com.smartcampus.domain.models.SubjectDto
import com.smartcampus.domain.models.SubjectUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.LoggerFactory
import kotlin.math.ceil

class SubjectsDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(SubjectsDao::class.java)

    private object SortableFields {
        val SUBJECTS: Map<String, Column<*>> = mapOf(
            "id" to SubjectsTable.id,
            "name" to SubjectsTable.name
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

    suspend fun listSubjects(params: PageRequestParams): PaginatedResult<SubjectDto> = db.query {
        val rows = SubjectsTable
            .selectAll()
            .applyPaginationAndSorting(params, SortableFields.SUBJECTS, SubjectsTable.name)
            .map { r -> SubjectDto(id = r[SubjectsTable.id].value, name = r[SubjectsTable.name]) }

        val totalItems = SubjectsTable.selectAll().count()
        val totalPages = if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()

        PaginatedResult(
            items = rows,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = params.page,
            pageSize = params.limit,
            sortBy = params.sortBy
        )
    }

    suspend fun getSubjectById(id: Int): SubjectDto? = db.query {
        SubjectsTable
            .selectAll()
            .where { SubjectsTable.id eq id }
            .singleOrNull()
            ?.let { SubjectDto(it[SubjectsTable.id].value, it[SubjectsTable.name]) }
    }

    suspend fun createSubject(req: SubjectCreateRequest): SubjectDto = db.query {
        val newId = SubjectsTable.insertAndGetId {
            it[name] = req.name
        }
        SubjectDto(newId.value, req.name)
    }

    suspend fun updateSubject(id: Int, req: SubjectUpdateRequest): SubjectDto? = db.query {
        val existing = SubjectsTable.selectAll().where { SubjectsTable.id eq id }.singleOrNull() ?: return@query null
        SubjectsTable.update({ SubjectsTable.id eq id }) {
            req.name?.let { v -> it[SubjectsTable.name] = v }
        }
        // return updated
        SubjectsTable.selectAll().where { SubjectsTable.id eq id }.singleOrNull()?.let {
            SubjectDto(it[SubjectsTable.id].value, it[SubjectsTable.name])
        }
    }

    suspend fun deleteSubject(id: Int): Boolean = db.query {
        SubjectsTable.deleteWhere { SubjectsTable.id eq id } > 0
    }
}
