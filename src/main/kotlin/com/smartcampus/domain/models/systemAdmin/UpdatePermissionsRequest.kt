package com.smartcampus.domain.models.systemAdmin

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePermissionsRequest(
    val grantPermissionIds: List<Int> = emptyList(),
    val revokePermissionIds: List<Int> = emptyList()
)