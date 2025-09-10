package com.smartcampus.data.dao

import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.AcademicWeeksTable
import com.smartcampus.domain.models.AcademicWeekCreateRequest
import com.smartcampus.domain.models.AcademicWeekDto
import com.smartcampus.domain.models.AcademicWeekUpdateRequest
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class AcademicWeeksDao(private val db: SmartCampusDb) {

    suspend fun listWeeks(year: Int? = null): List<AcademicWeekDto> = db.query {
        val q = if (year != null) AcademicWeeksTable.selectAll().where { AcademicWeeksTable.academicYear eq year.toString() } else AcademicWeeksTable.selectAll()
        q.map {
            AcademicWeekDto(
                id = it[AcademicWeeksTable.id].value,
                academicYear = it[AcademicWeeksTable.academicYear],
                weekNumber = it[AcademicWeeksTable.weekNumber],
                startDate = it[AcademicWeeksTable.startDate].toString(),
                endDate = it[AcademicWeeksTable.endDate].toString()
            )
        }
    }

    suspend fun getWeekById(id: Int): AcademicWeekDto? = db.query {
        AcademicWeeksTable.selectAll().where { AcademicWeeksTable.id eq id }.singleOrNull()?.let {
            AcademicWeekDto(
                id = it[AcademicWeeksTable.id].value,
                academicYear = it[AcademicWeeksTable.academicYear],
                weekNumber = it[AcademicWeeksTable.weekNumber],
                startDate = it[AcademicWeeksTable.startDate].toString(),
                endDate = it[AcademicWeeksTable.endDate].toString()
            )
        }
    }

    suspend fun createWeek(req: AcademicWeekCreateRequest): AcademicWeekDto = db.query {
        val newId = AcademicWeeksTable.insertAndGetId {
            it[AcademicWeeksTable.academicYear] = req.academicYear.toString()
            it[AcademicWeeksTable.weekNumber] = req.weekNumber
            it[AcademicWeeksTable.startDate] = java.time.LocalDate.parse(req.startDate)
            it[AcademicWeeksTable.endDate] = java.time.LocalDate.parse(req.endDate)
        }
        AcademicWeekDto(newId.value, req.academicYear, req.weekNumber, req.startDate, req.endDate)
    }

    suspend fun updateWeek(id: Int, req: AcademicWeekUpdateRequest): AcademicWeekDto? = db.query {
        val existing = AcademicWeeksTable.selectAll().where { AcademicWeeksTable.id eq id }.singleOrNull() ?: return@query null
        AcademicWeeksTable.update({ AcademicWeeksTable.id eq id }) {
            if (req.startDate != null) it[AcademicWeeksTable.startDate] = java.time.LocalDate.parse(req.startDate)
            if (req.endDate != null) it[AcademicWeeksTable.endDate] = java.time.LocalDate.parse(req.endDate)
        }
        getWeekById(id)
    }

    suspend fun deleteWeek(id: Int): Boolean = db.query {
        AcademicWeeksTable.deleteWhere { AcademicWeeksTable.id eq id } > 0
    }
}
