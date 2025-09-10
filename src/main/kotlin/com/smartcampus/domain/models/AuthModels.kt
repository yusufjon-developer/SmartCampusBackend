package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class StudentSignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class StudentSignInResponse(
    val token: String
)

@Serializable
data class EmployeeSignInRequest(
    val email: String,
    val password: String,
    val uuid: String
)

@Serializable
data class EmployeeSignInResponse(
    val token: String
)