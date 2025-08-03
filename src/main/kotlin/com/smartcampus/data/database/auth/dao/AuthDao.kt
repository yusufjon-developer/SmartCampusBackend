package com.smartcampus.data.database.auth.dao

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.data.database.auth.entities.UserDevicesTable
import com.smartcampus.data.database.auth.entities.UsersTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class AuthDao(private val db: SmartCampusAuthDb) {

    suspend fun findUserByEmailWithRole(email: String): ResultRow? = db.query {
        UsersTable
            .join(RolesTable, JoinType.INNER, UsersTable.roleId, RolesTable.id)
            .selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
    }

    suspend fun findUserDevice(userId: Int, deviceUuid: String): ResultRow? = db.query {
        UserDevicesTable.selectAll().where {
            (UserDevicesTable.userId eq userId) and (UserDevicesTable.deviceUuid eq deviceUuid)
        }.singleOrNull()
    }

    suspend fun registerNewDevice(
        userId: Int,
        deviceUuid: String,
        isApproved: Boolean,
        registeredAt: LocalDateTime,
        lastLoginAt: LocalDateTime?
    ): Int = db.query {
        UserDevicesTable.insertAndGetId {
            it[UserDevicesTable.userId] = userId
            it[UserDevicesTable.deviceUuid] = deviceUuid
            it[UserDevicesTable.isApproved] = isApproved
            it[UserDevicesTable.registeredAt] = registeredAt
            if (lastLoginAt != null) {
                it[UserDevicesTable.lastLoginAt] = lastLoginAt
            }
        }.value
    }

    suspend fun updateUserDeviceLastLogin(deviceId: Int, lastLoginAt: LocalDateTime): Boolean = db.query {
        UserDevicesTable.update({ UserDevicesTable.id eq deviceId }) {
            it[UserDevicesTable.lastLoginAt] = lastLoginAt
        } > 0
    }

    suspend fun findUserByUsernameWithRole(username: String): ResultRow? = db.query { // Добавляем, если нет
        UsersTable
            .join(RolesTable, JoinType.INNER, UsersTable.roleId, RolesTable.id)
            .selectAll()
            .where { UsersTable.username eq username }
            .singleOrNull()
    }

    suspend fun findRoleByName(roleName: String): ResultRow? = db.query {
        RolesTable.selectAll().where { RolesTable.name eq roleName }.singleOrNull()
    }

    suspend fun createUser(
        username: String,
        email: String,
        passwordHash: String,
        fullName: String?,
        roleId: Int,
        isActive: Boolean,
        createdAt: LocalDateTime
    ): Int = db.query {
        UsersTable.insertAndGetId {
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.email] = email
            if (fullName != null) it[UsersTable.fullName] = fullName
            it[UsersTable.roleId] = roleId
            it[UsersTable.isActive] = isActive
            it[UsersTable.createdAt] = createdAt
        }.value
    }

}

