package com.smartcampus.data.database.auth.entities

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object RolePermissionsTable : Table("SmartCampusAuth.dbo.Role_Permissions") {
    val roleId = reference("role_id", RolesTable.id, onDelete = ReferenceOption.CASCADE)
    val permissionId =
        reference("permission_id", PermissionsTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey =
        PrimaryKey(roleId, permissionId, name = "PK_RolePermissions_Composite")

    init {
        uniqueIndex(
            "UQ_RolePermissions_Backup", roleId, permissionId
        )
    }
}