package com.smartcampus.data.utils

import com.smartcampus.data.database.auth.entities.PermissionsTable
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toRoleResponse() = RoleResponse(
    id = this[RolesTable.id].value,
    name = this[RolesTable.name],
    description = this[RolesTable.description]!!
)

fun ResultRow.toPermissionResponse(table: Alias<PermissionsTable>) = PermissionResponse(
    id = this[table[PermissionsTable.id]].value,
    name = this[table[PermissionsTable.name]],
    description = this[table[PermissionsTable.description]]
)

fun ResultRow.toPermissionResponse(table: PermissionsTable = PermissionsTable) = PermissionResponse(
    id = this[table.id].value,
    name = this[table.name],
    description = this[table.description]
)