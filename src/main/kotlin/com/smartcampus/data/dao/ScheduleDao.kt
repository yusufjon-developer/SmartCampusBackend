package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.*
import com.smartcampus.domain.models.ScheduleCreateRequest
import com.smartcampus.domain.models.ScheduleDto
import com.smartcampus.domain.models.ScheduleUpdateRequest
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

    private fun ResultRow.toDto(): ScheduleDto {
        return ScheduleDto(
            id = this[ScheduleTable.id].value,
            workloadId = this[ScheduleTable.workloadId]?.value,
            day = this[ScheduleTable.day].toString(),
            startTime = this[ScheduleTable.startTime].toString(),
            endTime = this[ScheduleTable.endTime].toString(),
            teacherId = this[ScheduleTable.teacherId]?.value,
            groupId = this[ScheduleTable.groupId]?.value,
            disciplineId = this[ScheduleTable.disciplineId]?.value,
            auditoriumId = this[ScheduleTable.auditoriumId]?.value,
            type = this[ScheduleTable.type]
        )
    }

    suspend fun listSchedules(
        day: LocalDate? = null,
        teacherId: Int? = null,
        groupId: Int? = null,
        auditoriumId: Int? = null
    ): List<ScheduleDto> = db.query {
        val q = ScheduleTable.selectAll()
        if (day != null) q.andWhere { ScheduleTable.day eq day }
        if (teacherId != null) q.andWhere { ScheduleTable.teacherId eq EntityID(teacherId, TeachersTable) }
        if (groupId != null) q.andWhere { ScheduleTable.groupId eq EntityID(groupId, GroupsTable) }
        if (auditoriumId != null) q.andWhere { ScheduleTable.auditoriumId eq EntityID(auditoriumId, AuditoriumsTable) }
        q.orderBy(ScheduleTable.day to SortOrder.ASC, ScheduleTable.startTime to SortOrder.ASC)
            .map { it.toDto() }
    }

    suspend fun getById(id: Int): ScheduleDto? = db.query {
        ScheduleTable.selectAll().where { ScheduleTable.id eq id }.singleOrNull()?.toDto()
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
        ScheduleTable.selectAll().where { ScheduleTable.id eq newId }.single().toDto()
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

        ScheduleTable.selectAll().where { ScheduleTable.id eq id }.single().toDto()
    }

    suspend fun delete(id: Int): Boolean = db.query {
        ScheduleTable.deleteWhere { ScheduleTable.id eq id } > 0
    }

    /**
     * Find conflicts:
     * existing.start < newEnd AND existing.end > newStart
     * AND (existing.teacherId == teacherId OR existing.groupId == groupId OR existing.auditoriumId == auditoriumId)
     *
     * excludeId — when updating, exclude own row.
     */
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

        ScheduleTable.selectAll().where { finalCondition }
            .orderBy(ScheduleTable.startTime to SortOrder.ASC)
            .map { it.toDto() }
    }
}
