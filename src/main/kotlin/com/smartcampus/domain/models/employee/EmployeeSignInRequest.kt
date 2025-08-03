package com.smartcampus.domain.models.employee

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeSignInRequest(
    val email: String,
    val password: String,
    val uuid: String
)
