package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class TeacherWorkloadDto(
    val id: Int,
    val teacherId: Int,
    val teacherName: String? = null,
    val disciplineId: Int? = null,
    val disciplineName: String? = null,
    val type: String? = null,
    val hours: Int? = null,
    val academicYear: String? = null,
    val controlType: String? = null,
    val groupId: Int? = null,
    val groupName: String? = null
)

@Serializable
data class TeacherWorkloadCreateRequest(
    val teacherId: Int,
    val disciplineId: Int? = null,
    val type: String? = null,
    val hours: Int? = null,
    val academicYear: String? = null,
    val controlType: String? = null,
    val groupId: Int? = null
)

@Serializable
data class TeacherWorkloadUpdateRequest(
    val teacherId: Int? = null,
    val disciplineId: Int? = null,
    val type: String? = null,
    val hours: Int? = null,
    val academicYear: String? = null,
    val controlType: String? = null,
    val groupId: Int? = null
)

@Serializable
data class WorkloadExecutionDto(
    val id: Int,
    val workloadId: Int,
    val executionDate: String, // "yyyy-MM-dd"
    val hours: Double,
    val executedBy: Int? = null,
    val notes: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)

@Serializable
data class WorkloadExecutionCreateRequest(
    val executionDate: String, // "yyyy-MM-dd"
    val hours: Double,
    val executedBy: Int? = null,
    val notes: String? = null,
    val status: String? = null
)

@Serializable
data class ExecutedHoursResponse(
    val workloadId: Int,
    val executedHours: Double
)
