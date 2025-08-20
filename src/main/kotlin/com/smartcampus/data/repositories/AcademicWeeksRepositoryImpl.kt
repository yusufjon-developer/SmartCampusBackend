package com.smartcampus.data.repositories

import com.smartcampus.data.dao.AcademicWeeksDao
import com.smartcampus.domain.models.AcademicWeekCreateRequest
import com.smartcampus.domain.models.AcademicWeekDto
import com.smartcampus.domain.models.AcademicWeekUpdateRequest
import com.smartcampus.domain.repositories.AcademicWeeksRepository

class AcademicWeeksRepositoryImpl(private val dao: AcademicWeeksDao) : AcademicWeeksRepository {
    override suspend fun listWeeks(year: Int?): List<AcademicWeekDto> = dao.listWeeks(year)
    override suspend fun getWeekById(id: Int): AcademicWeekDto? = dao.getWeekById(id)
    override suspend fun createWeek(request: AcademicWeekCreateRequest): AcademicWeekDto = dao.createWeek(request)
    override suspend fun updateWeek(id: Int, request: AcademicWeekUpdateRequest): AcademicWeekDto? = dao.updateWeek(id, request)
    override suspend fun deleteWeek(id: Int): Boolean = dao.deleteWeek(id)
}
