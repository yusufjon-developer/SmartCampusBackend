package com.smartcampus.domain.models.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentSignInRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)