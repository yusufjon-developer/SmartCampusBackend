package com.smartcampus.data.dao

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.auth.entities.*
import com.smartcampus.data.utils.toPermissionResponse
import com.smartcampus.data.utils.toRoleResponse
import com.smartcampus.domain.models.UpdatePermissionsRequest
import com.smartcampus.domain.models.UserDevice
import com.smartcampus.domain.models.UserDto
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.systemAdmin.PermissionResponse
import com.smartcampus.domain.models.systemAdmin.RoleRequest
import com.smartcampus.domain.models.systemAdmin.RoleResponse
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
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

    suspend fun getUserDevicesById(userId: Int): List<UserDevice> = authDb.query {
        UserDevicesTable
            .selectAll()
            .where { UserDevicesTable.userId eq userId }
            .map { row ->
                UserDevice(
                    id = row[UserDevicesTable.id].value,
                    deviceUuid = row[UserDevicesTable.deviceUuid],
                    isApprove = row[UserDevicesTable.isApproved],
                    description = row[UserDevicesTable.description],
                    lastLoginAt = row[UserDevicesTable.lastLoginAt].toString(),
                    registeredAt = row[UserDevicesTable.registeredAt].toString(),
                    approvedAt = row[UserDevicesTable.approvedAt].toString(),
                    approvedBy = row[UserDevicesTable.approvedBy]?.value,
                )
            }
    }

    suspend fun updateUserComposite(
        userId: Int,
        isActive: Boolean?,
        deviceId: MutableSet<Int>?,
        permissionsReq: UpdatePermissionsRequest?,
        performingAdminId: Int
    ): Boolean = authDb.query {
        // Проверка существования пользователя
        val userRow = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
        if (userRow == null) {
            // user not found
            return@query false
        }

        var allSucceeded = true

        // 1) Обновление is_active (если передали)
        if (isActive != null) {
            val updated = UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.isActive] = isActive
            }
            if (updated <= 0) allSucceeded = false
        }

        // 2) Device: проверяем принадлежит ли устройство пользователю, и обновляем approval
        if (deviceId == null) {
            // снять approve у всех устройств пользователя
            UserDevicesTable.update({ UserDevicesTable.userId eq userId }) {
                it[UserDevicesTable.isApproved] = false
                it[UserDevicesTable.approvedAt] = null
                it[UserDevicesTable.approvedBy] = null
            }
        } else {
            // проверим, принадлежит ли указанное устройство пользователю
            deviceId.forEach { id ->
                val deviceRow = UserDevicesTable
                    .selectAll().where { (UserDevicesTable.id eq id) and (UserDevicesTable.userId eq userId) }
                    .singleOrNull()
                if (deviceRow == null) {
                    deviceId.remove(id)
                }
            }

            // сначала снять approve у всех устройств пользователя
            UserDevicesTable.update({ UserDevicesTable.userId eq userId }) {
                it[UserDevicesTable.isApproved] = false
                it[UserDevicesTable.approvedAt] = null
                it[UserDevicesTable.approvedBy] = null
            }

            // затем одобрить нужное
            deviceId.forEach { id ->
                val updated = UserDevicesTable.update({ UserDevicesTable.id eq id }) {
                    it[UserDevicesTable.isApproved] = true
                    it[UserDevicesTable.approvedAt] = CurrentDateTime
                    it[UserDevicesTable.approvedBy] = EntityID(performingAdminId, UsersTable)
                }
                if (updated <= 0) allSucceeded = false
            }
        }

        // 3) Permissions: grant/revoke индивидуальных прав
        if (permissionsReq != null) {
            // grants
            for (permId in permissionsReq.grantPermissionIds.distinct()) {
                // проверим что permission существует
                val permExists = PermissionsTable.selectAll().where { PermissionsTable.id eq permId }.singleOrNull() != null
                if (!permExists) {
                    allSucceeded = false
                    continue
                }

                // проверим есть ли уже грант
                val existsGrant = AccessGrantsTable
                    .selectAll().where {
                        (AccessGrantsTable.grantedTo eq userId) and (AccessGrantsTable.permissionId eq permId)
                    }
                    .singleOrNull() != null

                if (!existsGrant) {
                    try {
                        AccessGrantsTable.insert {
                            it[AccessGrantsTable.grantedTo] = userId
                            it[AccessGrantsTable.permissionId] = permId
                            it[AccessGrantsTable.grantedBy] = EntityID(performingAdminId, UsersTable)
                            it[AccessGrantsTable.grantDate] = CurrentDateTime
                        }
                    } catch (_: Exception) {
                        allSucceeded = false
                    }
                }
            }

            // revokes
            for (permId in permissionsReq.revokePermissionIds.distinct()) {
                try {
                    AccessGrantsTable.deleteWhere {
                        (AccessGrantsTable.grantedTo eq userId) and (AccessGrantsTable.permissionId eq permId)
                    }
                } catch (_: Exception) {
                    allSucceeded = false
                }
            }
        }

        allSucceeded
    }


    suspend fun getUserInfoById(userId: Int): Triple<String, Boolean, Int?>? = authDb.query {
        UsersTable
            .select(UsersTable.username, UsersTable.isActive, UsersTable.roleId)
            .where { UsersTable.id eq userId }
            .map {
                Triple(
                    it[UsersTable.username],
                    it[UsersTable.isActive],
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