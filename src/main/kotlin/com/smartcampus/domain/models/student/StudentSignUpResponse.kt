package com.smartcampus.domain.models.student

import com.smartcampus.domain.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class StudentSignUpResponse(
    val userId: Int,
    val message: String,
    val token: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiresAt: LocalDateTime
)