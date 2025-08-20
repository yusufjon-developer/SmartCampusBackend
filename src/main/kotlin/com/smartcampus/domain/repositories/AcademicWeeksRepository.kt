package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.AcademicWeekCreateRequest
import com.smartcampus.domain.models.AcademicWeekDto
import com.smartcampus.domain.models.AcademicWeekUpdateRequest

interface AcademicWeeksRepository {
    suspend fun listWeeks(year: Int? = null): List<AcademicWeekDto>
    suspend fun getWeekById(id: Int): AcademicWeekDto?
    suspend fun createWeek(request: AcademicWeekCreateRequest): AcademicWeekDto
    suspend fun updateWeek(id: Int, request: AcademicWeekUpdateRequest): AcademicWeekDto?
    suspend fun deleteWeek(id: Int): Boolean
}
