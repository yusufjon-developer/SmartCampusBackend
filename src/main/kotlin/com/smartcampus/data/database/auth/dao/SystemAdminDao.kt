package com.smartcampus.data.database.auth.dao

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.auth.entities.AccessGrantsTable
import com.smartcampus.data.database.auth.entities.PermissionsTable
import com.smartcampus.data.database.auth.entities.RolePermissionsTable
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.data.utils.toPermissionResponse
import com.smartcampus.data.utils.toRoleResponse
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.systemAdmin.PermissionRequest
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import kotlin.text.lowercase

class SystemAdminDao(private val authDb: SmartCampusAuthDb) {

    object SortableFields {
        val ROLES: Map<String, Column<*>> = mapOf(
            "id" to RolesTable.id,
            "name" to RolesTable.name,
            "description" to RolesTable.description
        )
        val PERMISSIONS: Map<String, Column<*>> = mapOf(
            "id" to PermissionsTable.id,
            "name" to PermissionsTable.name,
            "description" to PermissionsTable.description
        )
    }

    private fun Query.applyPaginationAndSorting(
        params: PageRequestParams,
        sortableFields: Map<String, Column<*>>,
        defaultSortColumn: Column<*>
    ): Query {
        val sortField = params.sortBy?.lowercase()
        val columnToSort = sortableFields[sortField] ?: defaultSortColumn
        this.orderBy(columnToSort to SortOrder.ASC)

        this.offset(params.offset)
        this.limit(params.limit)
        return this
    }


    suspend fun getAllRoles(params: PageRequestParams): List<RoleResponse> = authDb.query {
        RolesTable
            .selectAll()
            .applyPaginationAndSorting(params, SortableFields.ROLES, RolesTable.name)
            .map { it.toRoleResponse() }
    }

    suspend fun countAllRoles(): Long = authDb.query {
        RolesTable.selectAll().count()
    }

    suspend fun getRoleById(id: Int): RoleResponse? = authDb.query {
        RolesTable.selectAll().where { RolesTable.id eq id }.singleOrNull()?.toRoleResponse()
    }

    suspend fun getPermissionsForRole(roleId: Int): List<PermissionResponse> =
        authDb.query {
            val p = PermissionsTable.alias("p")
            (RolePermissionsTable innerJoin p)
                .selectAll()
                .where { RolePermissionsTable.roleId eq roleId }
                .map {
                    PermissionResponse(
                        id = it[p[PermissionsTable.id]].value,
                        name = it[p[PermissionsTable.name]],
                        description = it[p[PermissionsTable.description]]
                    )
                }
        }


    suspend fun createRole(roleRequest: RoleRequest): RoleResponse = authDb.query {
        val newId = RolesTable.insertAndGetId {
            it[name] = roleRequest.name
            it[description] = roleRequest.description
        }
        RoleResponse(newId.value, roleRequest.name, roleRequest.description)
    }

    suspend fun deleteRole(roleId: Int): Boolean = authDb.query {
        RolesTable.deleteWhere { RolesTable.id eq roleId } > 0
    }

    suspend fun getAllPermissions(params: PageRequestParams): List<PermissionResponse> = authDb.query {
        PermissionsTable
            .selectAll()
            .applyPaginationAndSorting(params, SortableFields.PERMISSIONS, PermissionsTable.name)
            .map { it.toPermissionResponse() }
    }

    suspend fun countAllPermissions(): Long = authDb.query {
        PermissionsTable.selectAll().count()
    }

    suspend fun getPermissionById(id: Int): PermissionResponse? = authDb.query {
        PermissionsTable.selectAll().where { PermissionsTable.id eq id }.singleOrNull()?.toPermissionResponse()
    }

    suspend fun createPermission(permissionRequest: PermissionRequest): PermissionResponse =
        authDb.query {
            val newId = PermissionsTable.insertAndGetId {
                it[name] = permissionRequest.name
                it[description] = permissionRequest.description
            }
            PermissionResponse(newId.value, permissionRequest.name, permissionRequest.description)
        }

    suspend fun deletePermission(permissionId: Int): Boolean = authDb.query {
        RolePermissionsTable.deleteWhere { RolePermissionsTable.permissionId eq permissionId }
        AccessGrantsTable.deleteWhere { AccessGrantsTable.permissionId eq permissionId }
        PermissionsTable.deleteWhere { PermissionsTable.id eq permissionId } > 0
    }

    suspend fun assignPermissionToRole(roleId: Int, permissionId: Int): Boolean = authDb.query {
        try {
            RolePermissionsTable.insert {
                it[RolePermissionsTable.roleId] = roleId
                it[RolePermissionsTable.permissionId] = permissionId
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun revokePermissionFromRole(roleId: Int, permissionId: Int): Boolean = authDb.query {
        RolePermissionsTable.deleteWhere {
            (RolePermissionsTable.roleId eq roleId) and (RolePermissionsTable.permissionId eq permissionId)
        } > 0
    }


    suspend fun executeRawQuery(rawQuery: String): String = authDb.query {
        val result = mutableListOf<String>()
        TransactionManager.current().exec(rawQuery) { rs ->
            val meta = rs.metaData
            val columnCount = meta.columnCount
            val header = (1..columnCount).joinToString(", ") { meta.getColumnName(it) }
            result.add(header)

            while (rs.next()) {
                val row = (1..columnCount).joinToString(", ") {
                    val obj = rs.getObject(it)
                    if (obj is ExposedBlob) {
                        "[BLOB_DATA]"
                    } else {
                        obj?.toString() ?: "NULL"
                    }
                }
                result.add(row)
            }
        }
        if (result.isEmpty()) "Query executed, no results or not a SELECT statement." else result.joinToString(
            separator = "\n"
        )
    }
}