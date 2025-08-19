package com.smartcampus.domain.models.auth

import kotlinx.serialization.Serializable

/** Ответ после регистрации */
@Serializable
data class RegisterResponse(
    val userId: Int,
    val username: String,
    val roleId: Int?,
    val studentProfileId: Int?,
    val teacherProfileId: Int?
)
