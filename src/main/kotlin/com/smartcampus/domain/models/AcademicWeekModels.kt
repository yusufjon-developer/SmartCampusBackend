package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AcademicWeekDto(
    val id: Int,
    val academicYear: String,
    val weekNumber: Int,
    val startDate: String, // ISO date
    val endDate: String    // ISO date
)

@Serializable
data class AcademicWeekCreateRequest(
    val academicYear: String,
    val weekNumber: Int,
    val startDate: String,
    val endDate: String
)

@Serializable
data class AcademicWeekUpdateRequest(
    val startDate: String?,
    val endDate: String?
)
