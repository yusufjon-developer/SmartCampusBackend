package com.smartcampus.data.repositories

import com.smartcampus.data.dao.SystemAdminDao
import com.smartcampus.domain.models.UpdatePermissionsRequest
import com.smartcampus.domain.models.UserDto
import com.smartcampus.domain.models.UserPermissionDetailsDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.systemAdmin.*
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



    override suspend fun getRoleById(roleId: Int): RolePermissionDetailsDto? {
        log.info("Repository: Fetching permission details for role ID: $roleId")
        val role = dao.getRoleById(roleId)
        if (role == null) {
            log.warn("Repository: Role with ID $roleId not found.")
            return null
        }

        val rolePermissionIds = dao.getPermissionIdsForRole(roleId)
        val allSystemPermissions = dao.getAllPermissionDefinitions()

        val permissionDetailsList = allSystemPermissions.map { permDef ->
            val hasRolePermission = rolePermissionIds.contains(permDef.id)
            val source = if (hasRolePermission) PermissionSourceDto.ROLE else PermissionSourceDto.NONE
            PermissionInfoForRoleDto(
                id = permDef.id,
                name = permDef.name,
                description = permDef.description,
                hasPermission = hasRolePermission,
                source = source
            )
        }

        return RolePermissionDetailsDto(
            roleId = role.id,
            name = role.name,
            description = role.description,
            permissions = permissionDetailsList
        )
    }

    override suspend fun updateRolePermissions(roleId: Int, request: UpdatePermissionsRequest, performingAdminId: Int): Boolean {
        log.info("Repository: Updating permissions for role ID: $roleId by admin ID: $performingAdminId. Request: $request")

        val role = dao.getRoleById(roleId)
        if (role == null) {
            log.warn("Repository: Role with ID $roleId not found.")
            return false
        }

        var allOperationsSucceeded = true

        // Grant permissions (insert into RolePermissionsTable)
        for (permissionIdToGrant in request.grantPermissionIds.distinct()) {
            val perm = dao.getPermissionById(permissionIdToGrant)
            if (perm == null) {
                log.warn("Repository: Permission with ID $permissionIdToGrant not found. Skipping grant.")
                allOperationsSucceeded = false
                continue
            }
            val success = dao.assignPermissionToRole(roleId, permissionIdToGrant)
            if (!success) {
                log.warn("Repository: Failed to assign permission $permissionIdToGrant to role $roleId.")
                allOperationsSucceeded = false
            }
        }

        // Revoke permissions
        for (permissionIdToRevoke in request.revokePermissionIds.distinct()) {
            val success = dao.revokePermissionFromRole(roleId, permissionIdToRevoke)
            if (!success) {
                // Возможно, права и не было — логируем, но не делаем это фатальной ошибкой
                log.info("Repository: Permission $permissionIdToRevoke was not present on role $roleId (or already revoked).")
            }
        }

        return allOperationsSucceeded
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

    override suspend fun getUsers(params: PageRequestParams): PaginatedResult<UserDto> {
        log.info("Fetching users with params: $params")
        val items = dao.getAllUsers(params)
        val totalItems = dao.countAllUsers()
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

    override suspend fun getUserPermissionsDetails(userId: Int): UserPermissionDetailsDto? {
        log.info("Repository: Fetching permission details for user ID: $userId")

        val userInfo = dao.getUserInfoById(userId)
        if (userInfo == null) {
            log.warn("Repository: User with ID $userId not found.")
            return null
        }
        val username = userInfo.first
        val userIsActive = userInfo.second
        val userRoleId = userInfo.third

        val userDevices = dao.getUserDevicesById(userId)

        val rolePermissionIds = mutableSetOf<Int>()

        if (userRoleId != null) {
            val role = dao.getRoleById(userRoleId)
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
            isActive = userIsActive,
            userDevices = userDevices,
            permissions = permissionDetailsList
        )
    }

    override suspend fun updateUserComposite(
        userId: Int,
        isActive: Boolean?,
        deviceId: Set<Int>?,
        permissionsReq: UpdatePermissionsRequest?,
        performingAdminId: Int
    ): Boolean {
        log.info("Repository: updateUserComposite user=$userId isActive=$isActive deviceId=$deviceId by admin=$performingAdminId")
        return dao.updateUserComposite(userId, isActive, deviceId?.toMutableSet(), permissionsReq, performingAdminId)
    }

    override suspend fun sendQuery(query: String): String {
        log.warn("Executing raw SQL query (use with extreme caution!): $query")
        return dao.executeRawQuery(query)
    }
}