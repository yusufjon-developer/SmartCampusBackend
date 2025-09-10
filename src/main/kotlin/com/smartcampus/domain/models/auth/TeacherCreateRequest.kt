package com.smartcampus.domain.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class TeacherCreateRequest(
    val surname: String?,
    val name: String?,
    val lastname: String?,
    val birthday: String?,
    val phoneNumber: String?
)