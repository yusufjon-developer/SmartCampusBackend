package com.smartcampus.domain.models.student

import kotlinx.serialization.Serializable

@Serializable
data class StudentSignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String? = null
)