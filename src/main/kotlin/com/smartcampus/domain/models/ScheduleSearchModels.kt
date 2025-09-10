package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleSearchFilter(
    val dayFrom: String? = null,
    val dayTo: String? = null,
    val teacherId: Int? = null,
    val groupId: Int? = null,
    val disciplineId: Int? = null,
    val auditoriumId: Int? = null,
    val type: String? = null,
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String? = null
)

@Serializable
data class ScheduleSearchResult(
    val results: List<ScheduleDto>,
    val total: Long,
    val page: Int,
    val size: Int
)
