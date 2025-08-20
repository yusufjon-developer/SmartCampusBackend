package com.smartcampus.data.dao

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.data.database.auth.entities.UserDevicesTable
import com.smartcampus.data.database.auth.entities.UsersTable
import com.smartcampus.domain.models.auth.RegisterResponse
import com.smartcampus.domain.models.auth.UserCredentialsDto
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class AuthDao(private val db: SmartCampusAuthDb) {
    private val log = LoggerFactory.getLogger(AuthDao::class.java)

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

    suspend fun isUsernameTaken(username: String): Boolean = db.query {
        UsersTable.select(UsersTable.username).where { UsersTable.username eq username }.count() > 0
    }

    suspend fun findRoleIdByName(name: String): Int? = db.query {
        RolesTable.select(RolesTable.id).where { RolesTable.name eq name }.singleOrNull()?.get(RolesTable.id)?.value
    }

    suspend fun createUserAndReturnResponse(
        username: String,
        passwordHash: String,
        email: String?,
        fullName: String?,
        roleId: Int?,
        studentProfileId: Int?,
        teacherProfileId: Int?
    ): RegisterResponse = db.query {
        val inserted = UsersTable.insertAndGetId { r ->
            r[UsersTable.username] = username
            r[UsersTable.passwordHash] = passwordHash
            r[UsersTable.email] = email
            r[UsersTable.fullName] = fullName
            r[UsersTable.roleId] = roleId
            r[UsersTable.isActive] = true
            r[UsersTable.studentProfileId] = studentProfileId
            r[UsersTable.teacherProfileId] = teacherProfileId
        }
        RegisterResponse(inserted.value, username, roleId, studentProfileId, teacherProfileId)
    }

    suspend fun findCredentialsByUsername(username: String): UserCredentialsDto? = db.query {
        UsersTable.selectAll().where { UsersTable.username eq username }.singleOrNull()?.let {
            UserCredentialsDto(
                id = it[UsersTable.id].value,
                username = it[UsersTable.username],
                passwordHash = it[UsersTable.passwordHash],
                roleId = it[UsersTable.roleId]?.value,
                studentProfileId = it[UsersTable.studentProfileId],
                teacherProfileId = it[UsersTable.teacherProfileId]
            )
        }
    }
}