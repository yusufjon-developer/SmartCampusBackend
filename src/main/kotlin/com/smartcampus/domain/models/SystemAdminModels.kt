package com.smartcampus.domain.models

import com.smartcampus.domain.models.systemAdmin.PermissionInfoForUserDto
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
)

@Serializable
data class UserPermissionDetailsDto(
    val userId: Int,
    val username: String,
    val isActive: Boolean,
    val userDevices: List<UserDevice>,
    val permissions: List<PermissionInfoForUserDto>
)

@Serializable
data class UserDevice(
    val id: Int,
    val deviceUuid: String,
    val isApprove: Boolean,
    val description: String? = null,
    val lastLoginAt: String? = null,
    val registeredAt: String? = null,
    val approvedAt: String? = null,
    val approvedBy: Int? = null,
)

@Serializable
data class UpdatePermissionsRequest(
    val grantPermissionIds: List<Int> = emptyList(),
    val revokePermissionIds: List<Int> = emptyList()
)

@Serializable
data class UpdateUserRequest(
    val isActive: Boolean? = null,
    val deviceId: Set<Int>? = null,
    val updatePermissionsRequest: UpdatePermissionsRequest? = null,
)

@Serializable
data class RawQueryPayload(val query: String)

