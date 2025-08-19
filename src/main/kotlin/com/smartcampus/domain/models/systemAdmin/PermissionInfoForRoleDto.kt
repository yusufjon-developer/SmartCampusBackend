package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class PermissionInfoForRoleDto(
    val id: Int,
    val name: String,
    val description: String?,
    val hasPermission: Boolean,
    val source: PermissionSourceDto
)