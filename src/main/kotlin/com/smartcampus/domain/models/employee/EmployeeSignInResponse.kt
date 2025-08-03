package com.smartcampus.domain.models.employee

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeSignInResponse(
    val token: String
)
