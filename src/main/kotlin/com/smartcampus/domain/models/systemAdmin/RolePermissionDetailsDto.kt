package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class RolePermissionDetailsDto(
    val roleId: Int,
    val name: String,
    val description: String?,
    val permissions: List<PermissionInfoForRoleDto>
)