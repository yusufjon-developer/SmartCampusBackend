package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.ScheduleSearchFilter
import com.smartcampus.domain.models.ScheduleSearchResult
import com.smartcampus.domain.models.ScheduleCreateRequest
import com.smartcampus.domain.models.ScheduleEntryDto
import com.smartcampus.domain.models.ScheduleUpdateRequest

interface ScheduleRepository {
    suspend fun searchSchedule(filter: ScheduleSearchFilter): ScheduleSearchResult
    suspend fun getScheduleEntries(params: PageRequestParams): PaginatedResult<ScheduleEntryDto>
    suspend fun getScheduleEntryById(id: Int): ScheduleEntryDto?
    suspend fun createScheduleEntry(request: ScheduleCreateRequest): ScheduleEntryDto
    suspend fun updateScheduleEntry(id: Int, request: ScheduleUpdateRequest): ScheduleEntryDto?
    suspend fun deleteScheduleEntry(id: Int): Boolean

    // helpers
    suspend fun getScheduleForTeacher(teacherId: Int, filter: ScheduleSearchFilter): ScheduleSearchResult
    suspend fun getScheduleForGroup(groupId: Int, filter: ScheduleSearchFilter): ScheduleSearchResult
}
