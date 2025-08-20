package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.ScheduleSearchFilter
import com.smartcampus.domain.models.ScheduleSearchResult

interface ScheduleSearchRepository {
    suspend fun search(filter: ScheduleSearchFilter): ScheduleSearchResult
}
