package com.smartcampus.data.repositories

import com.smartcampus.data.database.auth.dao.SystemAdminDao
import com.smartcampus.domain.models.systemAdmin.PermissionRequest
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import com.smartcampus.domain.repositories.SystemAdminRepository
import org.slf4j.LoggerFactory

class SystemAdminRepositoryImpl(
    private val dao: SystemAdminDao
) : SystemAdminRepository {
    private val log = LoggerFactory.getLogger(SystemAdminRepositoryImpl::class.java)

    override suspend fun getRoles(): List<RoleResponse> {
        log.debug("Fetching all roles")
        return dao.getAllRoles()
    }

    override suspend fun getRoleById(id: Int): Pair<RoleResponse, List<PermissionResponse>>? {
        log.debug("Fetching role by id: $id")
        val role = dao.getRoleById(id)
        return if (role != null) {
            val permissions = dao.getPermissionsForRole(id)
            log.debug("Found role: ${role.name} with ${permissions.size} permissions")
            Pair(role, permissions)
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

    override suspend fun getPermissions(): List<PermissionResponse> {
        log.debug("Fetching all permissions")
        return dao.getAllPermissions()
    }

    override suspend fun getPermissionsById(id: Int): PermissionResponse? {
        log.debug("Fetching permission by id: $id")
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

    override suspend fun revokePermissionFromRole(roleId: Int, permissionId: Int): Boolean {
        log.info("Revoking permission $permissionId from role $roleId")
        return dao.revokePermissionFromRole(roleId, permissionId)
    }


    override suspend fun sendQuery(query: String): String {
        log.warn("Executing raw SQL query (use with extreme caution!): $query")
        return dao.executeRawQuery(query)
    }
}