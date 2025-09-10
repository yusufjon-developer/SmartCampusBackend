package com.smartcampus.domain.models.auth

data class UserCredentialsDto(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val roleId: Int?,
    val studentProfileId: Int?,
    val teacherProfileId: Int?
)