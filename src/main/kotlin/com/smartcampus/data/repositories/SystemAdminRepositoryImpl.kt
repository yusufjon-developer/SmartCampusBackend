package com.smartcampus.data.repositories

import com.smartcampus.data.database.auth.dao.SystemAdminDao
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.systemAdmin.PermissionInfoForUserDto
import com.smartcampus.domain.models.systemAdmin.PermissionRequest
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.PermissionSourceDto
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import com.smartcampus.domain.models.systemAdmin.RoleWithPermissionsResponse
import com.smartcampus.domain.models.systemAdmin.UpdateUserPermissionsRequest
import com.smartcampus.domain.models.systemAdmin.UserPermissionDetailsDto
import com.smartcampus.domain.repositories.SystemAdminRepository
import org.slf4j.LoggerFactory
import kotlin.math.ceil

class SystemAdminRepositoryImpl(
    private val dao: SystemAdminDao
) : SystemAdminRepository {
    private val log = LoggerFactory.getLogger(SystemAdminRepositoryImpl::class.java)

    override suspend fun getRoles(params: PageRequestParams): PaginatedResult<RoleResponse> {
        log.info("Fetching roles with params: $params")
        val items = dao.getAllRoles(params)
        val totalItems = dao.countAllRoles()
        val totalPages = if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()

        return PaginatedResult(
            items = items,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = params.page,
            pageSize = params.limit,
            sortBy = params.sortBy
        )
    }



    override suspend fun getRoleById(id: Int): RoleWithPermissionsResponse? {
        log.info("Fetching role by id: $id")
        val role = dao.getRoleById(id)
        return if (role != null) {
            val permissions = dao.getPermissionsForRole(id)
            log.info("Found role: ${role.name} with ${permissions.size} permissions")
            RoleWithPermissionsResponse(role, permissions)
        } else {
            log.warn("Role with id $id not found")
            null
        }
    }

    override suspend fun createRole(role: RoleRequest): RoleResponse {
        log.info("Creating new role: ${role.name}")
        return dao.createRole(role)
    }

    override suspend fun deleteRoleById(id: Int): Boolean {
        log.info("Deleting role by id: $id")
        return dao.deleteRole(id)
    }

    override suspend fun getPermissions(params: PageRequestParams): PaginatedResult<PermissionResponse> {
        log.info("Fetching permissions with params: $params")
        val items = dao.getAllPermissions(params)
        val totalItems = dao.countAllPermissions()
        val totalPages = if (totalItems == 0L || params.limit <= 0) 0 else ceil(totalItems.toDouble() / params.limit).toInt()

        return PaginatedResult(
            items = items,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = params.page,
            pageSize = params.limit,
            sortBy = params.sortBy
        )
    }

    override suspend fun getPermissionsById(id: Int): PermissionResponse? {
        log.info("Fetching permission by id: $id")
        val permission = dao.getPermissionById(id)
        if (permission == null) {
            log.warn("Permission with id $id not found")
        }
        return permission
    }

    override suspend fun createPermissions(permission: PermissionRequest): PermissionResponse {
        log.info("Creating new permission: ${permission.name}")
        return dao.createPermission(permission)
    }

    override suspend fun deletePermissionsById(id: Int): Boolean {
        log.info("Deleting permission by id: $id")
        return dao.deletePermission(id)
    }

    override suspend fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean {
        log.info("Assigning permission $permissionId to role $roleId")
        val roleExists = dao.getRoleById(roleId) != null
        val permissionExists = dao.getPermissionById(permissionId) != null
        if (!roleExists) {
            log.warn("Cannot assign permission: Role with id $roleId not found.")
            return false
        }
        if (!permissionExists) {
            log.warn("Cannot assign permission: Permission with id $permissionId not found.")
            return false
        }
        return dao.assignPermissionToRole(roleId, permissionId)
    }

    override suspend fun getUserPermissionsDetails(userId: Int): UserPermissionDetailsDto? {
        log.info("Repository: Fetching permission details for user ID: $userId")

        val userInfo = dao.getUserInfoById(userId)
        if (userInfo == null) {
            log.warn("Repository: User with ID $userId not found.")
            return null
        }
        val username = userInfo.first
        val userRoleId = userInfo.third

        var roleName: String? = null
        val rolePermissionIds = mutableSetOf<Int>()

        if (userRoleId != null) {
            val role = dao.getRoleById(userRoleId)
            roleName = role?.name
            if (role != null) {
                rolePermissionIds.addAll(dao.getPermissionsForRole(userRoleId).map { it.id })
            }
        }

        val individualPermissionIds = dao.getIndividualPermissionIdsForUser(userId)
        val allSystemPermissions = dao.getAllPermissionDefinitions()

        val permissionDetailsList = allSystemPermissions.map { permDef ->
            val hasRolePermission = rolePermissionIds.contains(permDef.id)
            val hasIndividualPermission = individualPermissionIds.contains(permDef.id)

            val source = when {
                hasRolePermission && hasIndividualPermission -> PermissionSourceDto.ROLE_AND_INDIVIDUAL
                hasRolePermission -> PermissionSourceDto.ROLE
                hasIndividualPermission -> PermissionSourceDto.INDIVIDUAL
                else -> PermissionSourceDto.NONE
            }
            PermissionInfoForUserDto(
                id = permDef.id,
                name = permDef.name,
                description = permDef.description,
                hasPermission = source != PermissionSourceDto.NONE,
                source = source
            )
        }

        return UserPermissionDetailsDto(
            userId = userId,
            username = username,
            roleId = userRoleId,
            roleName = roleName,
            permissions = permissionDetailsList
        )
    }

    override suspend fun updateUserIndividualPermissions(targetUserId: Int, request: UpdateUserPermissionsRequest, performingAdminId: Int): Boolean {
        log.info("Repository: Updating individual permissions for user ID: $targetUserId by admin ID: $performingAdminId. Request: $request")

        if (dao.getUserInfoById(targetUserId) == null) {
            log.warn("Repository: Target user with ID $targetUserId not found. Cannot update permissions.")
            return false
        }

        var allOperationsSucceeded = true

        for (permissionIdToGrant in request.grantPermissionIds.distinct()) {
            if (dao.getPermissionById(permissionIdToGrant) == null) {
                log.warn("Repository: Permission with ID $permissionIdToGrant not found. Skipping grant for user $targetUserId.")
                allOperationsSucceeded = false
                continue
            }
            val success = dao.grantIndividualPermissionToUser(targetUserId, permissionIdToGrant, performingAdminId)
            if (!success) {
                log.warn("Repository: Failed to grant individual permission $permissionIdToGrant to user $targetUserId.")
                allOperationsSucceeded = false
            }
        }

        for (permissionIdToRevoke in request.revokePermissionIds.distinct()) {
            val success = dao.revokeIndividualPermissionFromUser(targetUserId, permissionIdToRevoke)
            if (!success) {
                log.info("Repository: Individual permission $permissionIdToRevoke was not found for user $targetUserId to revoke (or already revoked).")
            }
        }
        return allOperationsSucceeded
    }

    override suspend fun revokePermissionFromRole(roleId: Int, permissionId: Int): Boolean {
        log.info("Revoking permission $permissionId from role $roleId")
        return dao.revokePermissionFromRole(roleId, permissionId)
    }


    override suspend fun sendQuery(query: String): String {
        log.warn("Executing raw SQL query (use with extreme caution!): $query")
        return dao.executeRawQuery(query)
    }
}