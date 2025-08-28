package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.UserDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.systemAdmin.*

interface SystemAdminRepository {
    suspend fun getRoles(params: PageRequestParams): PaginatedResult<RoleResponse>
    suspend fun getRoleById(roleId: Int): RolePermissionDetailsDto?
    suspend fun createRole(role: RoleRequest): RoleResponse
    suspend fun deleteRoleById(id: Int): Boolean
    suspend fun updateRolePermissions(
        roleId: Int,
        request: UpdatePermissionsRequest,
        performingAdminId: Int
    ): Boolean

    suspend fun getPermissions(params: PageRequestParams): PaginatedResult<PermissionResponse>
    suspend fun getPermissionsById(id: Int): PermissionResponse?

    suspend fun getUsers(params: PageRequestParams): PaginatedResult<UserDto>
    suspend fun getUserPermissionsDetails(userId: Int): UserPermissionDetailsDto?
    suspend fun updateUserIndividualPermissions(
        targetUserId: Int,
        request: UpdatePermissionsRequest,
        performingAdminId: Int
    ): Boolean


    suspend fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean
    suspend fun revokePermissionFromRole(roleId: Int, permissionId: Int): Boolean

    suspend fun sendQuery(query: String): String
}