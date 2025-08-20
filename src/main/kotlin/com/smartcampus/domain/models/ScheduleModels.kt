package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleEntryDto(
    val id: Int,
    val day: String, // ISO date
    val time: String, // "HH:mm" or ISO time
    val groupId: Int?,
    val disciplineId: Int?,
    val teacherId: Int?,
    val auditoriumId: Int?,
    val type: String?
)

@Serializable
data class ScheduleCreateRequest(
    val day: String,
    val time: String,
    val groupId: Int?,
    val disciplineId: Int?,
    val teacherId: Int?,
    val auditoriumId: Int?,
    val type: String?
)

@Serializable
data class ScheduleUpdateRequest(
    val day: String?,
    val time: String?,
    val groupId: Int?,
    val disciplineId: Int?,
    val teacherId: Int?,
    val auditoriumId: Int?,
    val type: String?
)
