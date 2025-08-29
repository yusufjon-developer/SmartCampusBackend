package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.GroupsTable
import com.smartcampus.data.database.smartCampus.entities.SpecialitiesTable
import com.smartcampus.domain.models.GroupCreateRequest
import com.smartcampus.domain.models.GroupDto
import com.smartcampus.domain.models.GroupUpdateRequest
import com.smartcampus.domain.models.SpecialityDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.LoggerFactory
import kotlin.math.ceil

class GroupsDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(GroupsDao::class.java)

    private object SortableFields {
        val GROUPS: Map<String, Column<*>> = mapOf(
            "id" to GroupsTable.id,
            "name" to GroupsTable.name
        )
    }

    private fun Query.applyPaginationAndSorting(params: PageRequestParams, sortableFields: Map<String, Column<*>>, defaultSortColumn: Column<*>): Query {
        val sortField = params.sortBy?.lowercase()
        val columnToSort = sortableFields[sortField] ?: defaultSortColumn
        this.orderBy(columnToSort to SortOrder.ASC)
        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }

    private fun ResultRow.toGroupDto(): GroupDto {
        val gId = this[GroupsTable.id].value
        val gName = this[GroupsTable.name]
        val gCourse = this[GroupsTable.course]
        val specId = this[GroupsTable.specId]?.value
        val speciality = specId?.let { SpecialityDto(it, this[SpecialitiesTable.name]) }
        return GroupDto(id = gId, name = gName, course = gCourse, speciality = speciality)
    }

    suspend fun listGroups(params: PageRequestParams): PaginatedResult<GroupDto> = db.query {
        val baseQuery = GroupsTable
            .leftJoin(SpecialitiesTable, { GroupsTable.specId }, { SpecialitiesTable.id })
            .select(GroupsTable.columns + SpecialitiesTable.columns)
            .applyPaginationAndSorting(params, SortableFields.GROUPS, GroupsTable.name)

        val items = baseQuery.map { it.toGroupDto() }
        val totalItems = GroupsTable.selectAll().count()
        val totalPages = if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()

        PaginatedResult(items = items, totalItems = totalItems, totalPages = totalPages, currentPage = params.page, pageSize = params.limit, sortBy = params.sortBy)
    }

    suspend fun getGroupById(id: Int): GroupDto? = db.query {
        val row = GroupsTable
            .leftJoin(SpecialitiesTable, { GroupsTable.specId }, { SpecialitiesTable.id })
            .select(GroupsTable.columns + SpecialitiesTable.columns)
            .where { GroupsTable.id eq id }
            .singleOrNull() ?: return@query null
        row.toGroupDto()
    }

    suspend fun createGroup(req: GroupCreateRequest): GroupDto = db.query {
        val newId = GroupsTable.insertAndGetId {
            it[name] = req.name
            it[GroupsTable.specId] = req.specId?.let { EntityID(it, SpecialitiesTable) }
            it[GroupsTable.course] = req.course
        }
        val row = GroupsTable
            .leftJoin(SpecialitiesTable, { GroupsTable.specId }, { SpecialitiesTable.id })
            .select(GroupsTable.columns + SpecialitiesTable.columns)
            .where { GroupsTable.id eq newId }
            .single()
        row.toGroupDto()
    }

    suspend fun updateGroup(id: Int, req: GroupUpdateRequest): GroupDto? = db.query {
        val exists = GroupsTable.selectAll().where { GroupsTable.id eq id }.singleOrNull() ?: return@query null

        GroupsTable.update({ GroupsTable.id eq id }) {
            req.name?.let { v -> it[GroupsTable.name] = v }
            if (req.specId != null) it[GroupsTable.specId] = EntityID(req.specId, SpecialitiesTable)
            if (req.specId == null && req.specId != exists[GroupsTable.specId]?.value) { /* ignore - keep as-is */ }
            req.course?.let { v -> it[GroupsTable.course] = v }
        }

        val row = GroupsTable
            .leftJoin(SpecialitiesTable, { GroupsTable.specId }, { SpecialitiesTable.id })
            .select(GroupsTable.columns + SpecialitiesTable.columns)
            .where { GroupsTable.id eq id }
            .singleOrNull() ?: return@query null
        row.toGroupDto()
    }

    suspend fun deleteGroup(id: Int): Boolean = db.query {
        GroupsTable.deleteWhere { GroupsTable.id eq id } > 0
    }
}
