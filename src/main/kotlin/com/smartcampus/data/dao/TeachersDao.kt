package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.TeachersInfoTable
import com.smartcampus.data.database.smartCampus.entities.TeachersTable
import com.smartcampus.domain.models.TeacherDetailsDto
import com.smartcampus.domain.models.TeacherListItemDto
import com.smartcampus.domain.models.TeacherSensitiveDto
import com.smartcampus.domain.models.TeacherUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.math.ceil

class TeachersDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(TeachersDao::class.java)

    private object SortableFields {
        val TEACHERS: Map<String, Column<*>> = mapOf(
            "id" to TeachersTable.id,
            "surname" to TeachersTable.surname,
            "name" to TeachersTable.name,
            "birthday" to TeachersTable.birthday
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

    suspend fun getTeachers(params: PageRequestParams): PaginatedResult<TeacherListItemDto> = db.query {
        val rows = TeachersTable.selectAll().applyPaginationAndSorting(params, SortableFields.TEACHERS, TeachersTable.name).map { it.toTeacherListItemDto() }
        val totalItems = TeachersTable.selectAll().count()
        val totalPages = if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()
        PaginatedResult(rows, totalItems, totalPages, params.page, params.limit, params.sortBy)
    }

    suspend fun getTeacherById(id: Int, includeSensitive: Boolean = false): TeacherDetailsDto? = db.query {
        val row = TeachersTable.selectAll().where { TeachersTable.id eq id }.singleOrNull() ?: return@query null
        val base = row.toTeacherDetailsDto()
        if (!includeSensitive) return@query base
        val infoRow = TeachersInfoTable.selectAll().where { TeachersInfoTable.teacherId eq id }.singleOrNull()
        val sensitive = infoRow?.toTeacherSensitiveDto()
        base.copy(sensitive = sensitive)
    }

    suspend fun updateTeacher(id: Int, request: TeacherUpdateRequest, performingUserId: Int): TeacherDetailsDto? = db.query {
        val existing = TeachersTable.selectAll().where { TeachersTable.id eq id }.singleOrNull() ?: return@query null

        TeachersTable.update({ TeachersTable.id eq id }) {
            request.surname?.let { v -> it[TeachersTable.surname] = v }
            request.name?.let { v -> it[TeachersTable.name] = v }
            request.lastname?.let { v -> it[TeachersTable.lastname] = v }
            if (request.birthday != null) it[TeachersTable.birthday] = LocalDate.parse(request.birthday)
            request.phoneNumber?.let { v -> it[TeachersTable.phoneNumber] = v }
            if (request.photo != null) it[TeachersTable.photo] = ExposedBlob(request.photo)
        }

        request.info?.let { infoReq ->
            val infoExists = TeachersInfoTable.selectAll().where { TeachersInfoTable.teacherId eq id }.singleOrNull() != null
            if (infoExists) {
                TeachersInfoTable.update({ TeachersInfoTable.teacherId eq id }) {
                    infoReq.address?.let { v -> it[TeachersInfoTable.address] = v }
                    infoReq.passportNumber?.let { v -> it[TeachersInfoTable.passportNumber] = v }
                    infoReq.highSchool?.let { v -> it[TeachersInfoTable.highSchool] = v }
                    infoReq.documentNumber?.let { v -> it[TeachersInfoTable.documentNumber] = v }
                    infoReq.military?.let { v -> it[TeachersInfoTable.military] = v }
                    infoReq.degree?.let { v -> it[TeachersInfoTable.degree] = v }
                    infoReq.title?.let { v -> it[TeachersInfoTable.title] = v }
                    infoReq.position?.let { v -> it[TeachersInfoTable.position] = v }
                }
            } else {
                TeachersInfoTable.insert {
                    it[TeachersInfoTable.teacherId] = EntityID(id, TeachersTable)
                    it[TeachersInfoTable.address] = infoReq.address
                    it[TeachersInfoTable.passportNumber] = infoReq.passportNumber
                    it[TeachersInfoTable.highSchool] = infoReq.highSchool
                    it[TeachersInfoTable.documentNumber] = infoReq.documentNumber
                    it[TeachersInfoTable.military] = infoReq.military
                    it[TeachersInfoTable.degree] = infoReq.degree
                    it[TeachersInfoTable.title] = infoReq.title
                    it[TeachersInfoTable.position] = infoReq.position
                }
            }
        }

        log.info("Teacher $id updated by user $performingUserId")
        val rowAfter = TeachersTable.selectAll().where { TeachersTable.id eq id }.singleOrNull() ?: return@query null
        val base = rowAfter.toTeacherDetailsDto()
        val infoRow = TeachersInfoTable.selectAll().where { TeachersInfoTable.teacherId eq id }.singleOrNull()
        val sensitive = infoRow?.toTeacherSensitiveDto()
        base.copy(sensitive = sensitive)
    }

    suspend fun deleteTeacher(id: Int): Boolean = db.query {
        val deleted = TeachersTable.deleteWhere { TeachersTable.id eq id } > 0
        if (deleted) log.info("Teacher $id deleted")
        deleted
    }

    private fun ResultRow.toTeacherListItemDto(): TeacherListItemDto =
        TeacherListItemDto(
            id = this[TeachersTable.id].value,
            surname = this[TeachersTable.surname],
            name = this[TeachersTable.name],
            lastname = this[TeachersTable.lastname],
            birthday = this[TeachersTable.birthday]?.toString(),
            phoneNumber = this[TeachersTable.phoneNumber]
        )

    private fun ResultRow.toTeacherDetailsDto() =
        TeacherDetailsDto(
            id = this[TeachersTable.id].value,
            surname = this[TeachersTable.surname],
            name = this[TeachersTable.name],
            lastname = this[TeachersTable.lastname],
            birthday = this[TeachersTable.birthday]?.toString(),
            phoneNumber = this[TeachersTable.phoneNumber],
            sensitive = null
        )

    private fun ResultRow.toTeacherSensitiveDto() =
        TeacherSensitiveDto(
            address = this[TeachersInfoTable.address],
            passportNumber = this[TeachersInfoTable.passportNumber],
            highSchool = this[TeachersInfoTable.highSchool],
            documentNumber = this[TeachersInfoTable.documentNumber],
            military = this[TeachersInfoTable.military],
            degree = this[TeachersInfoTable.degree],
            title = this[TeachersInfoTable.title],
            position = this[TeachersInfoTable.position]
        )
}
