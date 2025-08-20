package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class StudentListItemDto(
    val id: Int,
    val surname: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val birthday: String? = null, // ISO date
    val groupId: Int? = null,
    val phoneNumber: String? = null
)

@Serializable
data class StudentSensitiveDto(
    val address: String? = null,
    val passportNumber: String? = null,
    val school: String? = null,
    val documentNumber: String? = null,
    val military: String? = null,
    val studentCardNumber: String? = null,
    val studyType: String? = null,
    val studyForm: String? = null,
    val status: String? = null,
    val fatherFio: String? = null,
    val fatherPhone: String? = null,
    val fatherAddress: String? = null,
    val motherFio: String? = null,
    val motherPhone: String? = null,
    val motherAddress: String? = null
)

@Serializable
data class StudentDetailsDto(
    val id: Int,
    val surname: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val birthday: String? = null,
    val groupId: Int? = null,
    val phoneNumber: String? = null,
    val sensitive: StudentSensitiveDto? = null
)

@Serializable
data class StudentCreateRequest(
    val surname: String?,
    val name: String?,
    val lastname: String?,
    val birthday: String?, // ISO
    val groupId: Int?,
    val phoneNumber: String?
)

@Serializable
data class StudentSensitiveUpdateRequest(
    val address: String? = null,
    val passportNumber: String? = null,
    val school: String? = null,
    val documentNumber: String? = null,
    val military: String? = null,
    val studentCardNumber: String? = null,
    val studyType: String? = null,
    val studyForm: String? = null,
    val status: String? = null,
    val fatherFio: String? = null,
    val fatherPhone: String? = null,
    val fatherAddress: String? = null,
    val motherFio: String? = null,
    val motherPhone: String? = null,
    val motherAddress: String? = null
)

@Serializable
data class StudentUpdateRequest(
    val surname: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val birthday: String? = null, // ISO date yyyy-mm-dd or null
    val groupId: Int? = null,
    val phoneNumber: String? = null,
    val photo: ByteArray? = null,
    val info: StudentSensitiveUpdateRequest? = null
)


