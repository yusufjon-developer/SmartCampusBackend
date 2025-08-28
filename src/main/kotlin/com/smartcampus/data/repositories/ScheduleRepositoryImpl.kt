package com.smartcampus.data.repositories

import com.smartcampus.data.dao.ScheduleDao
import com.smartcampus.domain.models.*
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.repositories.ScheduleRepository

class ScheduleRepositoryImpl(private val dao: ScheduleDao) : ScheduleRepository {
    override suspend fun searchSchedule(filter: ScheduleSearchFilter): ScheduleSearchResult =
        dao.searchSchedule(filter)

    override suspend fun getScheduleEntries(params: PageRequestParams): PaginatedResult<ScheduleEntryDto> =
        dao.getScheduleEntries(params)

    override suspend fun getScheduleEntryById(id: Int): ScheduleEntryDto? =
        dao.getScheduleEntryById(id)

    override suspend fun createScheduleEntry(request: ScheduleCreateRequest): ScheduleEntryDto =
        dao.createScheduleEntry(request)

    override suspend fun updateScheduleEntry(
        id: Int,
        request: ScheduleUpdateRequest
    ): ScheduleEntryDto? = dao.updateScheduleEntry(id, request)

    override suspend fun deleteScheduleEntry(id: Int): Boolean = dao.deleteScheduleEntry(id)
    override suspend fun getScheduleForTeacher(teacherId: Int, filter: ScheduleSearchFilter) =
        dao.getScheduleForTeacher(teacherId, filter)

    override suspend fun getScheduleForGroup(groupId: Int, filter: ScheduleSearchFilter) =
        dao.getScheduleForGroup(groupId, filter)
}
