package com.smartcampus.domain.models.employee

// src/main/kotlin/com/smartcampus/domain/models/employee/EmployeeSignUpResponse.kt

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeSignUpResponse(
    val userId: Int,
    val message: String
)