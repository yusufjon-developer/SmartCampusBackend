package com.smartcampus.data.dao

import com.smartcampus.data.database.auth.entities.UsersTable
import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.GroupsTable
import com.smartcampus.data.database.smartCampus.entities.SpecialitiesTable
import com.smartcampus.data.database.smartCampus.entities.StudentsInfoTable
import com.smartcampus.data.database.smartCampus.entities.StudentsTable
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.math.ceil

class StudentsDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(StudentsDao::class.java)

    private object SortableFields {
        val STUDENTS: Map<String, Column<*>> = mapOf(
            "id" to StudentsTable.id,
            "surname" to StudentsTable.surname,
            "name" to StudentsTable.name,
            "birthday" to StudentsTable.birthday
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

    suspend fun getStudents(params: PageRequestParams): PaginatedResult<StudentListItemDto> = db.query {
        // Left join Groups and Specialities so we can return nested objects (group + speciality)
        val baseQuery = StudentsTable
            .leftJoin(GroupsTable, { StudentsTable.groupId }, { GroupsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .select(StudentsTable.columns + GroupsTable.columns + SpecialitiesTable.columns)
            .applyPaginationAndSorting(params, SortableFields.STUDENTS, StudentsTable.name)

        val rows = baseQuery.map { row -> row.toStudentListItemDto() }

        val totalItems = StudentsTable.selectAll().count()
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

    suspend fun getStudentById(id: Int): StudentDetailsDto? = db.query {
        val row = StudentsTable
            .leftJoin(GroupsTable, { StudentsTable.groupId }, { GroupsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(UsersTable, { StudentsTable.id }, { UsersTable.studentProfileId })
            .select(StudentsTable.columns + GroupsTable.columns + SpecialitiesTable.columns + UsersTable.email)
            .where { StudentsTable.id eq id }
            .singleOrNull() ?: return@query null

        // map to StudentDetailsDto WITHOUT sensitive

        row.toStudentDetailsDto()
    }

    suspend fun getStudentSensitiveById(id: Int): StudentSensitiveDto? = db.query {
        val infoRow = StudentsInfoTable
            .selectAll()
            .where { StudentsInfoTable.studentId eq id }
            .singleOrNull() ?: return@query null

        infoRow.toStudentSensitiveDto()
    }

    suspend fun updateStudent(id: Int, request: StudentUpdateRequest, performingUserId: Int): StudentDetailsDto? = db.query {
        StudentsTable.selectAll().where { StudentsTable.id eq id }.singleOrNull() ?: return@query null

        StudentsTable.update({ StudentsTable.id eq id }) {
            request.surname?.let { v -> it[StudentsTable.surname] = v }
            request.name?.let { v -> it[StudentsTable.name] = v }
            request.lastname?.let { v -> it[StudentsTable.lastname] = v }
            if (request.birthday != null) it[StudentsTable.birthday] = LocalDate.parse(request.birthday) else if (request.birthday == null) { /* keep as-is */ }
            // groupId is reference -> need EntityID
            if (request.groupId != null) it[StudentsTable.groupId] = EntityID(request.groupId, GroupsTable)
            request.phoneNumber?.let { v -> it[StudentsTable.phoneNumber] = v }
            if (request.photo != null) it[StudentsTable.photo] = ExposedBlob(request.photo)
        }

        // Update or insert StudentsInfo
        request.info?.let { infoReq ->
            val infoExists = StudentsInfoTable.selectAll().where { StudentsInfoTable.studentId eq id }.singleOrNull() != null
            if (infoExists) {
                StudentsInfoTable.update({ StudentsInfoTable.studentId eq id }) {
                    infoReq.address?.let { v -> it[StudentsInfoTable.address] = v }
                    infoReq.passportNumber?.let { v -> it[StudentsInfoTable.passportNumber] = v }
                    infoReq.school?.let { v -> it[StudentsInfoTable.school] = v }
                    infoReq.documentNumber?.let { v -> it[StudentsInfoTable.documentNumber] = v }
                    infoReq.military?.let { v -> it[StudentsInfoTable.military] = v }
                    infoReq.studentCardNumber?.let { v -> it[StudentsInfoTable.studentCardNumber] = v }
                    infoReq.studyType?.let { v -> it[StudentsInfoTable.studyType] = v }
                    infoReq.studyForm?.let { v -> it[StudentsInfoTable.studyForm] = v }
                    infoReq.status?.let { v -> it[StudentsInfoTable.status] = v }
                    infoReq.fatherFio?.let { v -> it[StudentsInfoTable.fatherFio] = v }
                    infoReq.fatherPhone?.let { v -> it[StudentsInfoTable.fatherPhone] = v }
                    infoReq.fatherAddress?.let { v -> it[StudentsInfoTable.fatherAddress] = v }
                    infoReq.motherFio?.let { v -> it[StudentsInfoTable.motherFio] = v }
                    infoReq.motherPhone?.let { v -> it[StudentsInfoTable.motherPhone] = v }
                    infoReq.motherAddress?.let { v -> it[StudentsInfoTable.motherAddress] = v }
                }
            } else {
                StudentsInfoTable.insert {
                    it[StudentsInfoTable.studentId] = EntityID(id, StudentsTable)
                    it[StudentsInfoTable.address] = infoReq.address
                    it[StudentsInfoTable.passportNumber] = infoReq.passportNumber
                    it[StudentsInfoTable.school] = infoReq.school
                    it[StudentsInfoTable.documentNumber] = infoReq.documentNumber
                    it[StudentsInfoTable.military] = infoReq.military
                    it[StudentsInfoTable.studentCardNumber] = infoReq.studentCardNumber
                    it[StudentsInfoTable.studyType] = infoReq.studyType
                    it[StudentsInfoTable.studyForm] = infoReq.studyForm
                    it[StudentsInfoTable.status] = infoReq.status
                    it[StudentsInfoTable.fatherFio] = infoReq.fatherFio
                    it[StudentsInfoTable.fatherPhone] = infoReq.fatherPhone
                    it[StudentsInfoTable.fatherAddress] = infoReq.fatherAddress
                    it[StudentsInfoTable.motherFio] = infoReq.motherFio
                    it[StudentsInfoTable.motherPhone] = infoReq.motherPhone
                    it[StudentsInfoTable.motherAddress] = infoReq.motherAddress
                }
            }
        }

        log.info("Student $id updated by user $performingUserId")
        // Return fresh details including sensitive info (if exists)
        val rowAfter = StudentsTable
            .leftJoin(GroupsTable, { StudentsTable.groupId }, { GroupsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(UsersTable, { StudentsTable.id }, { UsersTable.studentProfileId })
            .select(StudentsTable.columns + GroupsTable.columns + SpecialitiesTable.columns + UsersTable.email)
            .where { StudentsTable.id eq id }
            .singleOrNull() ?: return@query null

        val base = rowAfter.toStudentDetailsDto()
        base
    }

    suspend fun deleteStudent(id: Int): Boolean = db.query {
        val deleted = StudentsTable.deleteWhere { StudentsTable.id eq id } > 0
        if (deleted) log.info("Student $id deleted")
        deleted
    }

    // -------------------
    // mappers
    // -------------------

    private fun ResultRow.toStudentListItemDto(): StudentListItemDto =
        StudentListItemDto(
            id = this[StudentsTable.id].value,
            surname = this[StudentsTable.surname],
            name = this[StudentsTable.name],
            lastname = this[StudentsTable.lastname],
            birthday = this[StudentsTable.birthday]?.toString(),
            group = this.toGroupDto(),
            phoneNumber = this[StudentsTable.phoneNumber]
        )

    private fun ResultRow.toStudentDetailsDto(): StudentDetailsDto =
        StudentDetailsDto(
            id = this[StudentsTable.id].value,
            email = this[UsersTable.email],
            surname = this[StudentsTable.surname],
            name = this[StudentsTable.name],
            lastname = this[StudentsTable.lastname],
            birthday = this[StudentsTable.birthday]?.toString(),
            group = this.toGroupDto(),
            phoneNumber = this[StudentsTable.phoneNumber]
        )

    private fun ResultRow.toGroupDto(): GroupDto? {
        // Use runCatching to safely read left-joined columns (they may be absent -> return null)
        val gId = runCatching { this[GroupsTable.id].value }.getOrNull() ?: return null
        val gName = runCatching { this[GroupsTable.name] }.getOrNull()
        val gCourse = runCatching { this[GroupsTable.course] }.getOrNull()
        val specId = runCatching { this[GroupsTable.specialityId]?.value }.getOrNull()

        val speciality = specId?.let {
            val sName = runCatching { this[SpecialitiesTable.name] }.getOrNull()
            SpecialityDto(it, sName)
        }

        return GroupDto(
            id = gId,
            name = gName,
            course = gCourse,
            speciality = speciality
        )
    }

    private fun ResultRow.toStudentSensitiveDto() =
        StudentSensitiveDto(
            address = this[StudentsInfoTable.address],
            passportNumber = this[StudentsInfoTable.passportNumber],
            school = this[StudentsInfoTable.school],
            documentNumber = this[StudentsInfoTable.documentNumber],
            military = this[StudentsInfoTable.military],
            studentCardNumber = this[StudentsInfoTable.studentCardNumber],
            studyType = this[StudentsInfoTable.studyType],
            studyForm = this[StudentsInfoTable.studyForm],
            status = this[StudentsInfoTable.status],
            fatherFio = this[StudentsInfoTable.fatherFio],
            fatherPhone = this[StudentsInfoTable.fatherPhone],
            fatherAddress = this[StudentsInfoTable.fatherAddress],
            motherFio = this[StudentsInfoTable.motherFio],
            motherPhone = this[StudentsInfoTable.motherPhone],
            motherAddress = this[StudentsInfoTable.motherAddress]
        )
}
