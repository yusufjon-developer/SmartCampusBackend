package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.*
import com.smartcampus.domain.models.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.greater
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.less
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.neq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleDao(private val db: SmartCampusDb) {

    suspend fun listSchedulesWithDetails(
        day: LocalDate? = null,
        teacherId: Int? = null,
        groupId: Int? = null,
        auditoriumId: Int? = null
    ): List<ScheduleDto> = db.query {
        val query = ScheduleTable
            .leftJoin(TeachersTable, { ScheduleTable.teacherId }, { TeachersTable.id })
            .leftJoin(GroupsTable, { ScheduleTable.groupId }, { GroupsTable.id })
            .leftJoin(DisciplinesTable, { ScheduleTable.disciplineId }, { DisciplinesTable.id })
            .leftJoin(AuditoriumsTable, { ScheduleTable.auditoriumId }, { AuditoriumsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(SubjectsTable, { DisciplinesTable.subjectId }, { SubjectsTable.id })
            .selectAll()

        if (day != null) query.andWhere { ScheduleTable.day eq day }
        if (teacherId != null) query.andWhere { ScheduleTable.teacherId eq EntityID(teacherId, TeachersTable) }
        if (groupId != null) query.andWhere { ScheduleTable.groupId eq EntityID(groupId, GroupsTable) }
        if (auditoriumId != null) query.andWhere { ScheduleTable.auditoriumId eq EntityID(auditoriumId, AuditoriumsTable) }

        query.orderBy(ScheduleTable.day to SortOrder.ASC, ScheduleTable.startTime to SortOrder.ASC)
            .map { it.toDetailedDto() }
    }

    suspend fun listSchedules(
        day: LocalDate? = null,
        teacherId: Int? = null,
        groupId: Int? = null,
        auditoriumId: Int? = null
    ): List<ScheduleDto> = listSchedulesWithDetails(day, teacherId, groupId, auditoriumId)

    suspend fun getById(id: Int): ScheduleDto? = db.query {
        ScheduleTable
            .leftJoin(TeachersTable, { ScheduleTable.teacherId }, { TeachersTable.id })
            .leftJoin(GroupsTable, { ScheduleTable.groupId }, { GroupsTable.id })
            .leftJoin(DisciplinesTable, { ScheduleTable.disciplineId }, { DisciplinesTable.id })
            .leftJoin(AuditoriumsTable, { ScheduleTable.auditoriumId }, { AuditoriumsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(SubjectsTable, { DisciplinesTable.subjectId }, { SubjectsTable.id })
            .selectAll()
            .where { ScheduleTable.id eq id }
            .singleOrNull()?.toDetailedDto()
    }

    suspend fun create(req: ScheduleCreateRequest): ScheduleDto? = db.query {
        val day = LocalDate.parse(req.day)
        val start = day.atTime(LocalTime.parse(req.startTime))
        val end = day.atTime(LocalTime.parse(req.endTime))

        val newId = ScheduleTable.insertAndGetId {
            req.workloadId?.let { w -> it[ScheduleTable.workloadId] = EntityID(w, TeachersWorkloadTable) }
            it[ScheduleTable.day] = day
            it[ScheduleTable.startTime] = start
            it[ScheduleTable.endTime] = end
            req.teacherId?.let { t -> it[ScheduleTable.teacherId] = EntityID(t, TeachersTable) }
            req.groupId?.let { g -> it[ScheduleTable.groupId] = EntityID(g, GroupsTable) }
            req.disciplineId?.let { d -> it[ScheduleTable.disciplineId] = EntityID(d, DisciplinesTable) }
            req.auditoriumId?.let { a -> it[ScheduleTable.auditoriumId] = EntityID(a, AuditoriumsTable) }
            it[ScheduleTable.type] = req.type
        }
        ScheduleTable
            .leftJoin(TeachersTable, { ScheduleTable.teacherId }, { TeachersTable.id })
            .leftJoin(GroupsTable, { ScheduleTable.groupId }, { GroupsTable.id })
            .leftJoin(DisciplinesTable, { ScheduleTable.disciplineId }, { DisciplinesTable.id })
            .leftJoin(AuditoriumsTable, { ScheduleTable.auditoriumId }, { AuditoriumsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(SubjectsTable, { DisciplinesTable.subjectId }, { SubjectsTable.id })
            .selectAll()
            .where { ScheduleTable.id eq newId }
            .single()
            .toDetailedDto()
    }

    suspend fun update(id: Int, req: ScheduleUpdateRequest): ScheduleDto? = db.query {
        ScheduleTable.selectAll().where { ScheduleTable.id eq id }.singleOrNull() ?: return@query null

        ScheduleTable.update({ ScheduleTable.id eq id }) { upd ->
            // update only when provided in request
            req.workloadId?.let { v -> upd[ScheduleTable.workloadId] = EntityID(v, TeachersWorkloadTable) }
            req.day.let { d -> upd[ScheduleTable.day] = LocalDate.parse(d) }
            req.startTime.let { s -> upd[ScheduleTable.startTime] = LocalDateTime.parse(s) }
            req.endTime.let { e -> upd[ScheduleTable.endTime] = LocalDateTime.parse(e) }
            req.teacherId?.let { t -> upd[ScheduleTable.teacherId] = EntityID(t, TeachersTable) }
            req.groupId?.let { g -> upd[ScheduleTable.groupId] = EntityID(g, GroupsTable) }
            req.disciplineId?.let { d -> upd[ScheduleTable.disciplineId] = EntityID(d, DisciplinesTable) }
            req.auditoriumId?.let { a -> upd[ScheduleTable.auditoriumId] = EntityID(a, AuditoriumsTable) }
            req.type?.let { ty -> upd[ScheduleTable.type] = ty }
        }

        ScheduleTable
            .leftJoin(TeachersTable, { ScheduleTable.teacherId }, { TeachersTable.id })
            .leftJoin(GroupsTable, { ScheduleTable.groupId }, { GroupsTable.id })
            .leftJoin(DisciplinesTable, { ScheduleTable.disciplineId }, { DisciplinesTable.id })
            .leftJoin(AuditoriumsTable, { ScheduleTable.auditoriumId }, { AuditoriumsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(SubjectsTable, { DisciplinesTable.subjectId }, { SubjectsTable.id })
            .selectAll()
            .where { ScheduleTable.id eq id }
            .single()
            .toDetailedDto()
    }

    suspend fun delete(id: Int): Boolean = db.query {
        ScheduleTable.deleteWhere { ScheduleTable.id eq id } > 0
    }

    suspend fun findConflicts(
        day: LocalDate,
        start: LocalTime,
        end: LocalTime,
        teacherId: Int? = null,
        groupId: Int? = null,
        auditoriumId: Int? = null,
        excludeId: Int? = null
    ): List<ScheduleDto> = db.query {
        val startDt = day.atTime(start)
        val endDt = day.atTime(end)

        val overlap = (ScheduleTable.day eq day) and
                (ScheduleTable.startTime less endDt) and
                (ScheduleTable.endTime greater startDt)

        val entityConds = mutableListOf<Op<Boolean>>()
        teacherId?.let { entityConds.add(ScheduleTable.teacherId eq EntityID(it, TeachersTable)) }
        groupId?.let { entityConds.add(ScheduleTable.groupId eq EntityID(it, GroupsTable)) }
        auditoriumId?.let { entityConds.add(ScheduleTable.auditoriumId eq EntityID(it, AuditoriumsTable)) }

        if (entityConds.isEmpty()) return@query emptyList()

        var combinedEntityCond: Op<Boolean> = entityConds.first()
        for (i in 1 until entityConds.size) combinedEntityCond = combinedEntityCond or entityConds[i]

        var finalCondition: Op<Boolean> = overlap and combinedEntityCond
        if (excludeId != null) finalCondition = finalCondition and (ScheduleTable.id neq excludeId)

        ScheduleTable
            .leftJoin(TeachersTable, { ScheduleTable.teacherId }, { TeachersTable.id })
            .leftJoin(GroupsTable, { ScheduleTable.groupId }, { GroupsTable.id })
            .leftJoin(DisciplinesTable, { ScheduleTable.disciplineId }, { DisciplinesTable.id })
            .leftJoin(AuditoriumsTable, { ScheduleTable.auditoriumId }, { AuditoriumsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(SubjectsTable, { DisciplinesTable.subjectId }, { SubjectsTable.id })
            .selectAll()
            .where { finalCondition }
            .orderBy(ScheduleTable.startTime to SortOrder.ASC)
            .map { it.toDetailedDto() }
    }

    suspend fun listSchedulesByTeacherAndPeriodWithDetails(
        teacherId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ScheduleDto> = db.query {
        ScheduleTable
            .leftJoin(TeachersTable, { ScheduleTable.teacherId }, { TeachersTable.id })
            .leftJoin(GroupsTable, { ScheduleTable.groupId }, { GroupsTable.id })
            .leftJoin(DisciplinesTable, { ScheduleTable.disciplineId }, { DisciplinesTable.id })
            .leftJoin(AuditoriumsTable, { ScheduleTable.auditoriumId }, { AuditoriumsTable.id })
            .leftJoin(SpecialitiesTable, { GroupsTable.specialityId }, { SpecialitiesTable.id })
            .leftJoin(SubjectsTable, { DisciplinesTable.subjectId }, { SubjectsTable.id })
            .selectAll()
            .where {
                (ScheduleTable.teacherId eq EntityID(teacherId, TeachersTable)) and
                        (ScheduleTable.day greaterEq startDate) and
                        (ScheduleTable.day lessEq endDate)
            }
            .orderBy(ScheduleTable.day to SortOrder.ASC, ScheduleTable.startTime to SortOrder.ASC)
            .map { it.toDetailedDto() }
    }

    private fun ResultRow.toDetailedDto(): ScheduleDto {
        return ScheduleDto(
            id = this[ScheduleTable.id].value,
            workloadId = this[ScheduleTable.workloadId]?.value,
            day = this[ScheduleTable.day]?.toString() ?: "",
            startTime = try {
                this[ScheduleTable.startTime].toLocalTime().toString()
            } catch (e: Exception) {
                ""
            },
            endTime = try {
                this[ScheduleTable.endTime].toLocalTime().toString()
            } catch (e: Exception) {
                ""
            },
            teacher = this[TeachersTable.id]?.let { teacherId ->
                TeacherDetailsDto(
                    id = teacherId.value,
                    surname = this[TeachersTable.surname],
                    name = this[TeachersTable.name],
                    lastname = this[TeachersTable.lastname],
                    birthday = this[TeachersTable.birthday]?.toString(),
                    phoneNumber = this[TeachersTable.phoneNumber]
                )
            },
            group = this[GroupsTable.id]?.let { groupId ->
                GroupDto(
                    id = groupId.value,
                    name = this[GroupsTable.name],
                    course = this[GroupsTable.course],
                    speciality = this[SpecialitiesTable.id]?.let { specialityId ->
                        SpecialityDto(
                            id = specialityId.value,
                            name = this[SpecialitiesTable.name]
                        )
                    }
                )
            },
            discipline = this[DisciplinesTable.id]?.let { disciplineId ->
                DisciplineDto(
                    id = disciplineId.value,
                    subject = this[SubjectsTable.id]?.let { subjectId ->
                        SubjectDto(
                            id = subjectId.value,
                            name = this[SubjectsTable.name]
                        )
                    },
                    semester = this[DisciplinesTable.semester],
                    specialityId = this[DisciplinesTable.specialityId]?.value,
                    course = this[DisciplinesTable.course],
                    lecture = this[DisciplinesTable.lecture],
                    practice = this[DisciplinesTable.practice],
                    lab = this[DisciplinesTable.lab],
                    seminar = this[DisciplinesTable.seminar],
                    control = this[DisciplinesTable.control]
                )
            },
            auditorium = this[AuditoriumsTable.id]?.let { auditoriumId ->
                AuditoriumDetailsDto(
                    id = auditoriumId.value,
                    number = this[AuditoriumsTable.number],
                    type = this[AuditoriumsTable.type]
                )
            },
            type = this[ScheduleTable.type]
        )
    }
}