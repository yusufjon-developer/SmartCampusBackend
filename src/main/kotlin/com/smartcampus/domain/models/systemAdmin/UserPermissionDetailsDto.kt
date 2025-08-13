package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class UserPermissionDetailsDto(
    val userId: Int,
    val username: String,
    val roleId: Int?,
    val roleName: String?,
    val permissions: List<PermissionInfoForUserDto>
)

