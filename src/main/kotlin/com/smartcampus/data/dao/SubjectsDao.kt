package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.SubjectsTable
import com.smartcampus.domain.models.SubjectDetailsDto
import com.smartcampus.domain.models.SubjectListItemDto
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

class SubjectsDao(private val db: SmartCampusDb) {

    private object SortableFields {
        val SUBJECTS: Map<String, Column<*>> = mapOf(
            "id" to SubjectsTable.id,
            "name" to SubjectsTable.name
        )
    }

    private fun Query.applyPaginationAndSorting(params: PageRequestParams, sortableFields: Map<String, Column<*>>, defaultSort: Column<*>): Query {
        val sf = params.sortBy?.lowercase()
        val col = sortableFields[sf] ?: defaultSort
        this.orderBy(col to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    suspend fun getSubjects(params: PageRequestParams): PaginatedResult<SubjectListItemDto> = db.query {
        val rows = SubjectsTable
            .selectAll()
            .applyPaginationAndSorting(params, SortableFields.SUBJECTS, SubjectsTable.name)
            .map { it.toSubjectListItemDto() }

        val total = SubjectsTable.selectAll().count()
        val totalPages = if (total == 0L || params.limit <= 0) 0 else ceil(total.toDouble() / params.limit).toInt()

        PaginatedResult(items = rows, totalItems = total, totalPages = totalPages, currentPage = params.page, pageSize = params.limit, sortBy = params.sortBy)
    }

    suspend fun getSubjectById(id: Int): SubjectDetailsDto? = db.query {
        SubjectsTable.selectAll().where { SubjectsTable.id eq id }.singleOrNull()?.toSubjectDetailsDto()
    }

    suspend fun createSubject(name: String): SubjectDetailsDto = db.query {
        val newId = SubjectsTable.insertAndGetId {
            it[SubjectsTable.name] = name
        }
        SubjectDetailsDto(newId.value, name)
    }

    suspend fun updateSubject(id: Int, name: String?): SubjectDetailsDto? = db.query {
        val existing = SubjectsTable.selectAll().where { SubjectsTable.id eq id }.singleOrNull() ?: return@query null
        SubjectsTable.update({ SubjectsTable.id eq id }) {
            if (name != null) it[SubjectsTable.name] = name
        }
        SubjectsTable.selectAll().where { SubjectsTable.id eq id }.singleOrNull()?.toSubjectDetailsDto()
    }

    suspend fun deleteSubject(id: Int): Boolean = db.query {
        SubjectsTable.deleteWhere { SubjectsTable.id eq id } > 0
    }

    // mappers
    private fun ResultRow.toSubjectListItemDto(): SubjectListItemDto =
        SubjectListItemDto(id = this[SubjectsTable.id].value, name = this[SubjectsTable.name])

    private fun ResultRow.toSubjectDetailsDto(): SubjectDetailsDto {
        return SubjectDetailsDto(
            id = this[SubjectsTable.id].value,
            name = this[SubjectsTable.name]
        )
    }
}
