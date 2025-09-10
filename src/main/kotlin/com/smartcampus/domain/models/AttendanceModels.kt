package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecordDto(
    val id: Int,
    val day: String,
    val time: String?,
    val studentId: Int,
    val disciplineId: Int,
    val mark: String?
)

@Serializable
data class AttendanceCreateRequest(
    val day: String,
    val time: String?,
    val studentId: Int,
    val disciplineId: Int,
    val mark: String?
)
