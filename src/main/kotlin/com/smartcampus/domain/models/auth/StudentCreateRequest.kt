package com.smartcampus.domain.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class StudentCreateRequest(
    val surname: String?,
    val name: String?,
    val lastname: String?,
    val birthday: String?, // ISO date "YYYY-MM-DD" (или Date на стороне сервиса)
    val groupId: Int?,
    val phoneNumber: String?
    // photo omitted for simplicity, add base64 / multipart handling if needed
)