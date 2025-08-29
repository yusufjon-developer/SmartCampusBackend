package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.*
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

class CurriculumsDao(private val db: SmartCampusDb) {
    private val log = LoggerFactory.getLogger(CurriculumsDao::class.java)

    private object SortableFields {
        val CURRICULUMS: Map<String, Column<*>> = mapOf(
            "id" to CurriculumsTable.id,
            "year" to CurriculumsTable.year,
            "profile" to CurriculumsTable.profile
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

    // --- PUBLIC suspend wrappers (open transaction) ---
    suspend fun getCurriculums(params: PageRequestParams): Pair<List<CurriculumDto>, Long> = db.query {
        // We'll LeftJoin curriculum -> curriculum_disciplines -> disciplines -> subjects -> specialities
        val c = CurriculumsTable
        val cd = CurriculumDisciplinesTable
        val d = DisciplinesTable
        val s = SubjectsTable
        val sp = SpecialitiesTable

        val joined = c
            .leftJoin(cd, { c.id }, { cd.curriculumId })
            .leftJoin(d, { cd.disciplineId }, { d.id })
            .leftJoin(s, { d.subjectId }, { s.id })
            .leftJoin(sp, { c.specialityId }, { sp.id })
            .select(
                c.columns +
                        cd.columns +
                        d.columns +
                        s.columns +
                        sp.columns
            )
            .applyPaginationAndSorting(params, SortableFields.CURRICULUMS, c.year)

        val rows = joined.toList()
        val map = linkedMapOf<Int, CurriculumDto>()

        for (row in rows) {
            val curriculumId = runCatching { row[c.id].value }.getOrNull() ?: continue
            val existing = map[curriculumId]
            if (existing == null) {
                val speciality = runCatching {
                    val spId = row[sp.id].value
                    SpecialityDto(id = spId, name = row[sp.name])
                }.getOrNull()

                val curr = CurriculumDto(
                    id = curriculumId,
                    specialityId = row[c.specialityId]?.value,
                    speciality = speciality,
                    year = row[c.year],
                    profile = row[c.profile],
                    educationForm = row[c.educationForm],
                    degree = row[c.degree],
                    duration = row[c.duration],
                    approvedDate = row[c.approvedDate]?.toString(),
                    disciplines = mutableListOf()
                )
                map[curriculumId] = curr
            }

            val cdId = runCatching { row[cd.id].value }.getOrNull()
            if (cdId != null) {
                val disciplineId = runCatching { row[d.id].value }.getOrNull()
                val subj = runCatching {
                    val sid = row[s.id].value
                    SubjectDto(id = sid, name = row[s.name])
                }.getOrNull()

                val specialityForDiscipline = runCatching {
                    val spid = row[d.specialityId]?.value
                    spid?.let { SpecialityDto(it, name = row[sp.name]) }
                }.getOrNull()

                val disciplineDto = disciplineId?.let {
                    DisciplineDto(
                        id = it,
                        subjectId = row[d.subjectId]?.value,
                        subject = subj,
                        semester = row[d.semester],
                        specialityId = row[d.specialityId]?.value,
                        speciality = specialityForDiscipline,
                        course = row[d.course],
                        lecture = row[d.lecture],
                        practice = row[d.practice],
                        lab = row[d.lab],
                        seminar = row[d.seminar],
                        control = row[d.control]
                    )
                }

                val cdDto = CurriculumDisciplineDto(
                    id = cdId,
                    curriculumId = runCatching { row[cd.curriculumId]?.value }.getOrNull() ?: curriculumId,
                    disciplineId = disciplineId,
                    discipline = disciplineDto,
                    semester = row[cd.semester],
                    course = row[cd.course],
                    exam = row[cd.exam] ?: false,
                    credit = row[cd.credit] ?: false,
                    coursework = row[cd.coursework] ?: false,
                    controlType = row[cd.controlType],
                    credits = row[cd.credits]
                )

                (map[curriculumId]?.disciplines as MutableList).add(cdDto)
            }
        }

        val totalItems = CurriculumsTable.selectAll().count()
        val list = map.values.toList()
        Pair(list, totalItems)
    }

    // PUBLIC suspend wrapper that opens transaction and delegates to internal no-tx implementation
    suspend fun getCurriculumById(id: Int, includeDisciplines: Boolean = true): CurriculumDto? = db.query {
        getCurriculumByIdNoTx(id, includeDisciplines)
    }

    suspend fun createCurriculum(request: CurriculumCreateRequest): CurriculumDto = db.query {
        val newId = CurriculumsTable.insertAndGetId {
            it[CurriculumsTable.specialityId] = request.specialityId?.let { EntityID(it, SpecialitiesTable) }
            it[CurriculumsTable.year] = request.year
            it[CurriculumsTable.profile] = request.profile
            it[CurriculumsTable.educationForm] = request.educationForm
            it[CurriculumsTable.degree] = request.degree
            it[CurriculumsTable.duration] = request.duration
            it[CurriculumsTable.approvedDate] = request.approvedDate?.let { LocalDate.parse(it) }
        }

        // insert disciplines if provided
        request.disciplines?.forEach { cdReq ->
            CurriculumDisciplinesTable.insert {
                it[CurriculumDisciplinesTable.curriculumId] = EntityID(newId.value, CurriculumsTable)
                it[CurriculumDisciplinesTable.disciplineId] = cdReq.disciplineId?.let { EntityID(it, DisciplinesTable) }
                it[CurriculumDisciplinesTable.semester] = cdReq.semester
                it[CurriculumDisciplinesTable.course] = cdReq.course
                it[CurriculumDisciplinesTable.exam] = cdReq.exam
                it[CurriculumDisciplinesTable.credit] = cdReq.credit
                it[CurriculumDisciplinesTable.coursework] = cdReq.coursework
                it[CurriculumDisciplinesTable.controlType] = cdReq.controlType
                it[CurriculumDisciplinesTable.credits] = cdReq.credits
            }
        }

        // <-- use internal no-tx version here (we're already inside db.query)
        getCurriculumByIdNoTx(newId.value, includeDisciplines = true)!!
    }

    suspend fun updateCurriculum(id: Int, request: CurriculumUpdateRequest): CurriculumDto? = db.query {
        val exists = CurriculumsTable.selectAll().where { CurriculumsTable.id eq id }.singleOrNull() ?: return@query null

        CurriculumsTable.update({ CurriculumsTable.id eq id }) {
            request.specialityId?.let { v -> it[CurriculumsTable.specialityId] = EntityID(v, SpecialitiesTable) }
            request.year?.let { v -> it[CurriculumsTable.year] = v }
            request.profile?.let { v -> it[CurriculumsTable.profile] = v }
            request.educationForm?.let { v -> it[CurriculumsTable.educationForm] = v }
            request.degree?.let { v -> it[CurriculumsTable.degree] = v }
            request.duration?.let { v -> it[CurriculumsTable.duration] = v }
            request.approvedDate?.let { v -> it[CurriculumsTable.approvedDate] = LocalDate.parse(v) }
        }

        // If disciplines included in update request, synchronize them (simple approach: delete existing and insert new)
        request.disciplines?.let { list ->
            CurriculumDisciplinesTable.deleteWhere { CurriculumDisciplinesTable.curriculumId eq id }
            list.forEach { cdReq ->
                CurriculumDisciplinesTable.insert {
                    it[CurriculumDisciplinesTable.curriculumId] = EntityID(id, CurriculumsTable)
                    it[CurriculumDisciplinesTable.disciplineId] = cdReq.disciplineId?.let { EntityID(it, DisciplinesTable) }
                    it[CurriculumDisciplinesTable.semester] = cdReq.semester
                    it[CurriculumDisciplinesTable.course] = cdReq.course
                    it[CurriculumDisciplinesTable.exam] = cdReq.exam
                    it[CurriculumDisciplinesTable.credit] = cdReq.credit
                    it[CurriculumDisciplinesTable.coursework] = cdReq.coursework
                    it[CurriculumDisciplinesTable.controlType] = cdReq.controlType
                    it[CurriculumDisciplinesTable.credits] = cdReq.credits
                }
            }
        }

        // <-- internal no-tx
        getCurriculumByIdNoTx(id, includeDisciplines = true)
    }

    suspend fun deleteCurriculum(id: Int): Boolean = db.query {
        CurriculumDisciplinesTable.deleteWhere { CurriculumDisciplinesTable.curriculumId eq id }
        CurriculumsTable.deleteWhere { CurriculumsTable.id eq id } > 0
    }

    // helper: add single curriculum discipline
    suspend fun addDisciplineToCurriculum(curriculumId: Int, cdReq: CurriculumDisciplineCreateRequest): CurriculumDisciplineDto? = db.query {
        val newId = CurriculumDisciplinesTable.insertAndGetId {
            it[CurriculumDisciplinesTable.curriculumId] = EntityID(curriculumId, CurriculumsTable)
            it[CurriculumDisciplinesTable.disciplineId] = cdReq.disciplineId?.let { EntityID(it, DisciplinesTable) }
            it[CurriculumDisciplinesTable.semester] = cdReq.semester
            it[CurriculumDisciplinesTable.course] = cdReq.course
            it[CurriculumDisciplinesTable.exam] = cdReq.exam
            it[CurriculumDisciplinesTable.credit] = cdReq.credit
            it[CurriculumDisciplinesTable.coursework] = cdReq.coursework
            it[CurriculumDisciplinesTable.controlType] = cdReq.controlType
            it[CurriculumDisciplinesTable.credits] = cdReq.credits
        }
        // <-- use no-tx reader (we're in same transaction)
        getCurriculumByIdNoTx(curriculumId, includeDisciplines = true)?.disciplines?.find { it.id == newId.value }
    }

    suspend fun updateCurriculumDiscipline(cdId: Int, cdReq: CurriculumDisciplineUpdateRequest): CurriculumDisciplineDto? = db.query {
        val exists = CurriculumDisciplinesTable.selectAll().where { CurriculumDisciplinesTable.id eq cdId }.singleOrNull() ?: return@query null
        CurriculumDisciplinesTable.update({ CurriculumDisciplinesTable.id eq cdId }) {
            cdReq.disciplineId?.let { v -> it[CurriculumDisciplinesTable.disciplineId] = EntityID(v, DisciplinesTable) }
            cdReq.semester?.let { v -> it[CurriculumDisciplinesTable.semester] = v }
            cdReq.course?.let { v -> it[CurriculumDisciplinesTable.course] = v }
            cdReq.exam?.let { v -> it[CurriculumDisciplinesTable.exam] = v }
            cdReq.credit?.let { v -> it[CurriculumDisciplinesTable.credit] = v }
            cdReq.coursework?.let { v -> it[CurriculumDisciplinesTable.coursework] = v }
            cdReq.controlType?.let { v -> it[CurriculumDisciplinesTable.controlType] = v }
            cdReq.credits?.let { v -> it[CurriculumDisciplinesTable.credits] = v }
        }
        val curriculumId = exists[CurriculumDisciplinesTable.curriculumId]?.value
        getCurriculumByIdNoTx(curriculumId!!, includeDisciplines = true)?.disciplines?.find { it.id == cdId }
    }

    suspend fun deleteCurriculumDiscipline(cdId: Int): Boolean = db.query {
        CurriculumDisciplinesTable.deleteWhere { CurriculumDisciplinesTable.id eq cdId } > 0
    }

    // --------------------------------------------------------
    // Private helpers (NO db.query inside) — must be called from inside db.query
    // --------------------------------------------------------
    private fun getCurriculumByIdNoTx(id: Int, includeDisciplines: Boolean = true): CurriculumDto? {
        val c = CurriculumsTable
        val cd = CurriculumDisciplinesTable
        val d = DisciplinesTable
        val s = SubjectsTable
        val sp = SpecialitiesTable

        val rows = c
            .leftJoin(cd, { c.id }, { cd.curriculumId })
            .leftJoin(d, { cd.disciplineId }, { d.id })
            .leftJoin(s, { d.subjectId }, { s.id })
            .leftJoin(sp, { c.specialityId }, { sp.id })
            .select(c.columns + cd.columns + d.columns + s.columns + sp.columns)
            .where { c.id eq id }
            .toList()

        if (rows.isEmpty()) return null

        val first = rows.first()
        val speciality = runCatching {
            val spId = first[sp.id].value
            SpecialityDto(id = spId, name = first[sp.name])
        }.getOrNull()

        val base = CurriculumDto(
            id = first[c.id].value,
            specialityId = first[c.specialityId]?.value,
            speciality = speciality,
            year = first[c.year],
            profile = first[c.profile],
            educationForm = first[c.educationForm],
            degree = first[c.degree],
            duration = first[c.duration],
            approvedDate = first[c.approvedDate]?.toString(),
            disciplines = mutableListOf()
        )

        if (!includeDisciplines) return base

        for (row in rows) {
            val cdId = runCatching { row[cd.id].value }.getOrNull() ?: continue
            val disciplineId = runCatching { row[d.id].value }.getOrNull()
            val subj = runCatching {
                val sid = row[s.id].value
                SubjectDto(id = sid, name = row[s.name])
            }.getOrNull()

            val specialityForDiscipline = runCatching {
                val spid = row[d.specialityId]?.value
                spid?.let { SpecialityDto(it, name = row[sp.name]) }
            }.getOrNull()

            val disciplineDto = disciplineId?.let {
                DisciplineDto(
                    id = it,
                    subjectId = row[d.subjectId]?.value,
                    subject = subj,
                    semester = row[d.semester],
                    specialityId = row[d.specialityId]?.value,
                    speciality = specialityForDiscipline,
                    course = row[d.course],
                    lecture = row[d.lecture],
                    practice = row[d.practice],
                    lab = row[d.lab],
                    seminar = row[d.seminar],
                    control = row[d.control]
                )
            }

            val cdDto = CurriculumDisciplineDto(
                id = cdId,
                curriculumId = runCatching { row[cd.curriculumId]?.value }.getOrNull() ?: id,
                disciplineId = disciplineId,
                discipline = disciplineDto,
                semester = row[cd.semester],
                course = row[cd.course],
                exam = row[cd.exam] ?: false,
                credit = row[cd.credit] ?: false,
                coursework = row[cd.coursework] ?: false,
                controlType = row[cd.controlType],
                credits = row[cd.credits]
            )

            (base.disciplines as MutableList).add(cdDto)
        }

        return base
    }
}
