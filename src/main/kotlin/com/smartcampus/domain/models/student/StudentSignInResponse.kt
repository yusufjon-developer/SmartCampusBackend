package com.smartcampus.domain.models.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentSignInResponse(
    @SerialName("token") val token: String
)