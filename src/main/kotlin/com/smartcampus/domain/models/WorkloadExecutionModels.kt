package com.smartcampus.domain.models

data class WorkloadExecutionDto(
    val id: Int,
    val workloadId: Int,
    val executionDate: String,
    val hours: Double,
    val executedBy: Int?,
    val notes: String?,
    val status: String?
)

data class WorkloadExecutionCreateRequest(
    val workloadId: Int,
    val executionDate: String,
    val hours: Double,
    val executedBy: Int?,
    val notes: String?,
    val status: String?
)

data class WorkloadExecutionUpdateRequest(
    val executionDate: String? = null,
    val hours: Double? = null,
    val executedBy: Int? = null,
    val notes: String? = null,
    val status: String? = null
)
