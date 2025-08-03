package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RolePermissionsTable : IntIdTable("Role_Permissions") {
    val roleId = reference("role_id", RolesTable)
    val permissionId = reference("permission_id", PermissionsTable)

    init {
        uniqueIndex("PK_RolePermissions", roleId, permissionId)
    }
}