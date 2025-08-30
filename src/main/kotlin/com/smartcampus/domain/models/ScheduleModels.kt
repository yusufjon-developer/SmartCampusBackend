package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleDto(
    val id: Int,
    val workloadId: Int? = null,
    val day: String,         // "yyyy-MM-dd"
    val startTime: String,   // "HH:mm"
    val endTime: String,     // "HH:mm"
    val teacherId: Int? = null,
    val groupId: Int? = null,
    val disciplineId: Int? = null,
    val auditoriumId: Int? = null,
    val type: String? = null
)

@Serializable
data class ScheduleCreateRequest(
    val workloadId: Int? = null,
    val day: String,
    val startTime: String,
    val endTime: String,
    val teacherId: Int? = null,
    val groupId: Int? = null,
    val disciplineId: Int? = null,
    val auditoriumId: Int? = null,
    val type: String? = null
)

@Serializable
data class ScheduleUpdateRequest(
    val workloadId: Int? = null,
    val day: String,
    val startTime: String,
    val endTime: String,
    val teacherId: Int? = null,
    val groupId: Int? = null,
    val disciplineId: Int? = null,
    val auditoriumId: Int? = null,
    val type: String? = null
)
