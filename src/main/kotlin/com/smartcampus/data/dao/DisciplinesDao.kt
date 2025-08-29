package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.DisciplinesTable
import com.smartcampus.data.database.smartCampus.entities.SpecialitiesTable
import com.smartcampus.data.database.smartCampus.entities.SubjectsTable
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.math.ceil

class DisciplinesDao(private val db: SmartCampusDb) {

    private object SortableFields {
        val DISCIPLINES: Map<String, Column<*>> = mapOf(
            "id" to DisciplinesTable.id,
            "semester" to DisciplinesTable.semester,
            "course" to DisciplinesTable.course
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

    suspend fun listDisciplines(params: PageRequestParams): PaginatedResult<DisciplineDto> = db.query {
        val d = DisciplinesTable
        val s = SubjectsTable
        val sp = SpecialitiesTable

        val joined = d
            .leftJoin(s, { d.subjectId }, { s.id })
            .leftJoin(sp, { d.specialityId }, { sp.id })
            .select(d.columns + s.columns + sp.columns)
            .applyPaginationAndSorting(params, SortableFields.DISCIPLINES, d.id)

        val items = joined.map { row ->
            val subj = runCatching {
                val sid = row[s.id].value
                SubjectDto(id = sid, name = row[s.name])
            }.getOrNull()

            val spec = runCatching {
                val spid = row[sp.id].value
                SpecialityDto(id = spid, name = row[sp.name])
            }.getOrNull()

            DisciplineDto(
                id = row[d.id].value,
                subjectId = row[d.subjectId]?.value,
                subject = subj,
                semester = row[d.semester],
                specialityId = row[d.specialityId]?.value,
                speciality = spec,
                course = row[d.course],
                lecture = row[d.lecture],
                practice = row[d.practice],
                lab = row[d.lab],
                seminar = row[d.seminar],
                control = row[d.control]
            )
        }

        val totalItems = DisciplinesTable.selectAll().count()
        val totalPages =
            if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()

        PaginatedResult(
            items = items,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = params.page,
            pageSize = params.limit,
            sortBy = params.sortBy
        )
    }

    suspend fun getDisciplineById(id: Int): DisciplineDto? = db.query {
        val d = DisciplinesTable
        val s = SubjectsTable
        val sp = SpecialitiesTable

        d
            .leftJoin(s, { d.subjectId }, { s.id })
            .leftJoin(sp, { d.specialityId }, { sp.id })
            .select(d.columns + s.columns + sp.columns)
            .where { d.id eq id }
            .singleOrNull()
            ?.let { row ->
                val subj = runCatching {
                    val sid = row[s.id].value
                    SubjectDto(id = sid, name = row[s.name])
                }.getOrNull()

                val spec = runCatching {
                    val spid = row[sp.id].value
                    SpecialityDto(id = spid, name = row[sp.name])
                }.getOrNull()

                DisciplineDto(
                    id = row[d.id].value,
                    subjectId = row[d.subjectId]?.value,
                    subject = subj,
                    semester = row[d.semester],
                    specialityId = row[d.specialityId]?.value,
                    speciality = spec,
                    course = row[d.course],
                    lecture = row[d.lecture],
                    practice = row[d.practice],
                    lab = row[d.lab],
                    seminar = row[d.seminar],
                    control = row[d.control]
                )
            }
    }

    suspend fun createDiscipline(request: DisciplineCreateRequest): DisciplineDto {
        val newId = db.query {
            DisciplinesTable.insertAndGetId {
                it[DisciplinesTable.subjectId] = request.subjectId?.let { EntityID(it, SubjectsTable) }
                it[DisciplinesTable.semester] = request.semester
                it[DisciplinesTable.specialityId] = request.specialityId?.let { EntityID(it, SpecialitiesTable) }
                it[DisciplinesTable.course] = request.course
                it[DisciplinesTable.lecture] = request.lecture
                it[DisciplinesTable.practice] = request.practice
                it[DisciplinesTable.lab] = request.lab
                it[DisciplinesTable.seminar] = request.seminar
                it[DisciplinesTable.control] = request.control
            }
        }
        return getDisciplineById(newId.value)!!
    }

    suspend fun updateDiscipline(id: Int, request: DisciplineUpdateRequest): DisciplineDto? {
        db.query {
            val exists =
                DisciplinesTable.selectAll().where { DisciplinesTable.id eq id }.singleOrNull() ?: return@query null

            DisciplinesTable.update({ DisciplinesTable.id eq id }) {
                request.subjectId?.let { v -> it[DisciplinesTable.subjectId] = EntityID(v, SubjectsTable) }
                request.semester?.let { v -> it[DisciplinesTable.semester] = v }
                request.specialityId?.let { v -> it[DisciplinesTable.specialityId] = EntityID(v, SpecialitiesTable) }
                request.course?.let { v -> it[DisciplinesTable.course] = v }
                request.lecture?.let { v -> it[DisciplinesTable.lecture] = v }
                request.practice?.let { v -> it[DisciplinesTable.practice] = v }
                request.lab?.let { v -> it[DisciplinesTable.lab] = v }
                request.seminar?.let { v -> it[DisciplinesTable.seminar] = v }
                request.control?.let { v -> it[DisciplinesTable.control] = v }
            }
        }
        return getDisciplineById(id)
    }

    suspend fun deleteDiscipline(id: Int): Boolean = db.query {
        DisciplinesTable.deleteWhere { DisciplinesTable.id eq id } > 0
    }
}
