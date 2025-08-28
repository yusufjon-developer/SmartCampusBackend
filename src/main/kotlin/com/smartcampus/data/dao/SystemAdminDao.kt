package com.smartcampus.data.dao

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.auth.entities.*
import com.smartcampus.data.utils.toPermissionResponse
import com.smartcampus.data.utils.toRoleResponse
import com.smartcampus.domain.models.UserDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

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
        val USERS: Map<String, Column<*>> = mapOf(
            "id" to UsersTable.id,
            "username" to UsersTable.username
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

    suspend fun getRoleByName(name: String): RoleResponse? = authDb.query {
        RolesTable
            .selectAll()
            .where { RolesTable.name eq name } // Ищем роль по точному совпадению имени
            .map { it.toRoleResponse() }      // Преобразуем ResultRow в RoleResponse
            .singleOrNull()                   // Ожидаем одну роль или ни одной
    }

    suspend fun getPermissionsForRole(roleId: Int): List<PermissionResponse> =
        authDb.query {
            val p = PermissionsTable.alias("p")
            RolePermissionsTable
                .join(
                    otherTable = p,
                    joinType = JoinType.INNER,
                    onColumn = RolePermissionsTable.permissionId,
                    otherColumn = p[PermissionsTable.id]
                )
                .selectAll().where { RolePermissionsTable.roleId eq roleId }
                .map { resultRow ->
                    PermissionResponse(
                        id = resultRow[p[PermissionsTable.id]].value,
                        name = resultRow[p[PermissionsTable.name]],
                        description = resultRow[p[PermissionsTable.description]]
                    )
                }
        }

    suspend fun getPermissionIdsForRole(roleId: Int): Set<Int> = authDb.query {
        RolePermissionsTable
            .select(RolePermissionsTable.permissionId)
            .where { RolePermissionsTable.roleId eq roleId }
            .map { it[RolePermissionsTable.permissionId].value }
            .toSet()
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

    suspend fun getAllPermissions(params: PageRequestParams): List<PermissionResponse> =
        authDb.query {
            PermissionsTable
                .selectAll()
                .applyPaginationAndSorting(
                    params,
                    SortableFields.PERMISSIONS,
                    PermissionsTable.name
                )
                .map { it.toPermissionResponse() }
        }

    suspend fun countAllPermissions(): Long = authDb.query {
        PermissionsTable.selectAll().count()
    }

    suspend fun getPermissionById(id: Int): PermissionResponse? = authDb.query {
        PermissionsTable.selectAll().where { PermissionsTable.id eq id }.singleOrNull()
            ?.toPermissionResponse()
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

    // Ниже остальных методов в SystemAdminDao добавь:

    suspend fun getAllUsers(params: PageRequestParams): List<UserDto> = authDb.query {
        UsersTable
            .selectAll()
            .applyPaginationAndSorting(params, SortableFields.USERS, UsersTable.username)
            .map { row ->
                UserDto( // <- если пакет другой — поправь
                    id = row[UsersTable.id].value,
                    name = row[UsersTable.fullName] ?: row[UsersTable.username]
                )
            }
    }

    suspend fun countAllUsers(): Long = authDb.query {
        UsersTable.selectAll().count()
    }

    suspend fun getUserInfoById(userId: Int): Triple<String, Int, Int?>? = authDb.query {
        UsersTable
            .select(UsersTable.username, UsersTable.id, UsersTable.roleId)
            .where { UsersTable.id eq userId }
            .map {
                Triple(
                    it[UsersTable.username],
                    it[UsersTable.id].value,
                    it[UsersTable.roleId]?.value
                )
            }
            .singleOrNull()
    }

    suspend fun getIndividualPermissionIdsForUser(userId: Int): Set<Int> = authDb.query {
        AccessGrantsTable
            .select(AccessGrantsTable.permissionId)
            .where { AccessGrantsTable.grantedTo eq userId }
            .map { it[AccessGrantsTable.permissionId].value }
            .toSet()
    }

    suspend fun getPermissionNamesByIds(permissionIds: Set<Int>): Set<String> = authDb.query {
        if (permissionIds.isEmpty()) {
            emptySet()
        } else {
            PermissionsTable
                .select(PermissionsTable.name)
                .where { PermissionsTable.id inList permissionIds }
                .map { it[PermissionsTable.name] }
                .toSet()
        }
    }

    suspend fun grantIndividualPermissionToUser(
        grantedToUserId: Int,
        permissionId: Int,
        grantedByUserId: Int
    ): Boolean = authDb.query {
        try {
            val existingGrantCount = AccessGrantsTable
                .selectAll()
                .where { (AccessGrantsTable.grantedTo eq grantedToUserId) and (AccessGrantsTable.permissionId eq permissionId) }
                .count()

            if (existingGrantCount > 0) {
                return@query true
            }

            AccessGrantsTable.insert {
                it[AccessGrantsTable.grantedTo] = grantedToUserId
                it[AccessGrantsTable.permissionId] = permissionId
                it[AccessGrantsTable.grantedBy] = grantedByUserId
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun revokeIndividualPermissionFromUser(
        grantedToUserId: Int,
        permissionId: Int
    ): Boolean = authDb.query {
        AccessGrantsTable.deleteWhere {
            (AccessGrantsTable.grantedTo eq grantedToUserId) and (AccessGrantsTable.permissionId eq permissionId)
        } > 0
    }

    suspend fun getAllPermissionDefinitions(): List<PermissionResponse> = authDb.query {
        PermissionsTable
            .selectAll()
            .map { it.toPermissionResponse() }
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