package com.smartcampus.domain.models.student

import kotlinx.serialization.Serializable

@Serializable
data class StudentSignUpResponse(
    val userId: Int,
    val message: String
)