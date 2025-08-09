package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.systemAdmin.PermissionRequest
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse

interface SystemAdminRepository {
    suspend fun getRoles(params: PageRequestParams): PaginatedResult<RoleResponse>
    suspend fun getRoleById(id: Int): Pair<RoleResponse, List<PermissionResponse>>?
    suspend fun createRole(role: RoleRequest): RoleResponse
    suspend fun deleteRoleById(id: Int): Boolean

    suspend fun getPermissions(params: PageRequestParams): PaginatedResult<PermissionResponse>
    suspend fun getPermissionsById(id: Int): PermissionResponse?
    suspend fun createPermissions(permission: PermissionRequest): PermissionResponse
    suspend fun deletePermissionsById(id: Int): Boolean

    suspend fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean
    suspend fun revokePermissionFromRole(roleId: Int, permissionId: Int): Boolean

    suspend fun sendQuery(query: String): String
}