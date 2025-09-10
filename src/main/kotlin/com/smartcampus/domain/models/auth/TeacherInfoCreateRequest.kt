package com.smartcampus.domain.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class TeacherInfoCreateRequest(
    val address: String? = null,
    val passportNumber: String? = null,
    val highSchool: String? = null,
    val documentNumber: String? = null,
    val military: String? = null,
    val degree: String? = null,
    val title: String? = null,
    val position: String? = null
)