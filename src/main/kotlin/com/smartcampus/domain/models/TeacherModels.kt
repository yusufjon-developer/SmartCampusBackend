package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class TeacherListItemDto(
    val id: Int,
    val surname: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val birthday: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class TeacherSensitiveDto(
    val address: String? = null,
    val passportNumber: String? = null,
    val highSchool: String? = null,
    val documentNumber: String? = null,
    val military: String? = null,
    val degree: String? = null,
    val title: String? = null,
    val position: String? = null
)

@Serializable
data class TeacherDetailsDto(
    val id: Int,
    val email: String? = null,
    val surname: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val birthday: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class TeacherUpdateRequest(
    val surname: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val birthday: String? = null,
    val phoneNumber: String? = null,
    val photo: ByteArray? = null,
    val info: TeacherSensitiveDto? = null
)
