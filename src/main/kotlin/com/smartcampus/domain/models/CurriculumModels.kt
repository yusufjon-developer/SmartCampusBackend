package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class CurriculumListItemDto(
    val id: Int,
    val specialityId: Int,
    val year: Int,
    val profile: String?,
    val educationForm: String?
)

@Serializable
data class CurriculumDetailsDto(
    val id: Int,
    val specialityId: Int,
    val year: Int,
    val profile: String?,
    val educationForm: String?,
    val degree: String?,
    val duration: Int?,
    val approvedDate: String? = null
)

@Serializable
data class CurriculumCreateRequest(
    val specialityId: Int,
    val year: Int,
    val profile: String?,
    val educationForm: String?,
    val degree: String?,
    val duration: Int?
)

@Serializable
data class CurriculumUpdateRequest(
    val profile: String?,
    val educationForm: String?,
    val degree: String?,
    val duration: Int?
)
