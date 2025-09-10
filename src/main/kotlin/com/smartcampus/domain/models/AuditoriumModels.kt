package com.smartcampus.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuditoriumListItemDto(
    val id: Int,
    val number: String?,
    val type: String?
)

@Serializable
data class AuditoriumDetailsDto(
    val id: Int,
    val number: String?,
    val type: String?
)

@Serializable
data class AuditoriumCreateRequest(
    val number: String,
    val type: String?
)

@Serializable
data class AuditoriumUpdateRequest(
    val number: String?,
    val type: String?
)
