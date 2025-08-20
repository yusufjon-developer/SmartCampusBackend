package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class TeacherWorkloadDto(
    val id: Int,
    val teacherId: Int,
    val disciplineId: Int,
    val type: String?,
    val hours: Int?,
    val academicYear: String?,
    val controlType: String?,
    val groupId: Int?
)

@Serializable
data class TeacherWorkloadCreateRequest(
    val teacherId: Int,
    val disciplineId: Int,
    val type: String?,
    val hours: Int?,
    val academicYear: String?,
    val controlType: String?,
    val groupId: Int?
)

@Serializable
data class TeacherWorkloadUpdateRequest(
    val type: String?,
    val hours: Int?,
    val academicYear: String?,
    val controlType: String?,
    val groupId: Int?
)
