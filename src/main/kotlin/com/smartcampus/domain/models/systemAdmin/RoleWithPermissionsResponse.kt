package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class RoleWithPermissionsResponse(
    val role: RoleResponse,
    val permissions: List<PermissionResponse>
)