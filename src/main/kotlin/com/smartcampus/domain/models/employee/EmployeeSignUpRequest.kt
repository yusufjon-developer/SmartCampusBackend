package com.smartcampus.domain.models.employee

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeSignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String? = null,
    val roleName: String
)