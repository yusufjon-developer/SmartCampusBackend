package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
enum class PermissionSourceDto {
    ROLE,
    INDIVIDUAL,
    ROLE_AND_INDIVIDUAL,
    NONE
}