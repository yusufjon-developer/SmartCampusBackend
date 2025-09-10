package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class GradeRecordDto(
    val id: Int,
    val day: String, // ISO
    val time: String?,
    val studentId: Int,
    val disciplineId: Int,
    val teacherId: Int?,
    val round: String?,
    val mark: String?
)

@Serializable
data class GradeCreateRequest(
    val day: String,
    val time: String?,
    val studentId: Int,
    val disciplineId: Int,
    val teacherId: Int?,
    val round: String?,
    val mark: String?
)
