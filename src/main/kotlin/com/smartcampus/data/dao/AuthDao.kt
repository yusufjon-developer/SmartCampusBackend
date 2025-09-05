package com.smartcampus.data.dao

import com.smartcampus.data.database.auth.SmartCampusAuthDb
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.data.database.auth.entities.UserDevicesTable
import com.smartcampus.data.database.auth.entities.UsersTable
import com.smartcampus.data.database.smartCampus.SmartCampusDb
import com.smartcampus.data.database.smartCampus.entities.GroupsTable
import com.smartcampus.data.database.smartCampus.entities.StudentsInfoTable
import com.smartcampus.data.database.smartCampus.entities.StudentsTable
import com.smartcampus.data.database.smartCampus.entities.TeachersInfoTable
import com.smartcampus.data.database.smartCampus.entities.TeachersTable
import com.smartcampus.domain.models.GroupDto
import com.smartcampus.domain.models.StudentDetailsDto
import com.smartcampus.domain.models.StudentSensitiveDto
import com.smartcampus.domain.models.TeacherDetailsDto
import com.smartcampus.domain.models.TeacherSensitiveDto
import com.smartcampus.domain.models.auth.RegisterResponse
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class AuthDao(
    private val authDb: SmartCampusAuthDb,
    private val db: SmartCampusDb
) {
    private val log = LoggerFactory.getLogger(AuthDao::class.java)

    suspend fun findUserByEmailWithRole(email: String): ResultRow? = authDb.query {
        UsersTable
            .join(RolesTable, JoinType.INNER, UsersTable.roleId, RolesTable.id)
            .selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
    }

    suspend fun findUserDevice(userId: Int, deviceUuid: String): ResultRow? = authDb.query {
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
    ): Int = authDb.query {
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

    suspend fun updateUserDeviceLastLogin(deviceId: Int, lastLoginAt: LocalDateTime): Boolean = authDb.query {
        UserDevicesTable.update({ UserDevicesTable.id eq deviceId }) {
            it[UserDevicesTable.lastLoginAt] = lastLoginAt
        } > 0
    }

    suspend fun isUsernameTaken(username: String): Boolean = authDb.query {
        UsersTable.select(UsersTable.username).where { UsersTable.username eq username }.count() > 0
    }

    suspend fun findRoleIdByName(name: String): Int? = authDb.query {
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
    ): RegisterResponse {
        val insertedUserId = authDb.query {
            // === 1. Создание пользователя ===
            UsersTable.insertAndGetId { user ->
                user[UsersTable.username] = username
                user[UsersTable.passwordHash] = passwordHash
                user[UsersTable.email] = email
                user[UsersTable.fullName] = fullName
                user[UsersTable.roleId] = roleId?.let { EntityID(it, RolesTable) }
                user[UsersTable.isActive] = true
                user[UsersTable.studentProfileId] = studentProfileId
                user[UsersTable.teacherProfileId] = teacherProfileId
            }.value


        }

        return db.query {

            // === 2. Загрузка названия роли(ей) ===
            val roleNames: String? = RolesTable
                .selectAll()
                .where { RolesTable.id eq roleId }
                .singleOrNull()
                ?.let { row ->
                    row[RolesTable.name]
                }

            val studentProfile = studentProfileId?.let { id ->
                val s = StudentsTable
                val g = GroupsTable
                s.leftJoin(g, { s.groupId }, { g.id })
                    .selectAll()
                    .where { s.id eq id }
                    .singleOrNull()
                    ?.let { row ->
                        StudentDetailsDto(
                            id = row[s.id].value,
                            email = email,
                            surname = row[s.surname],
                            name = row[s.name],
                            lastname = row[s.lastname],
                            birthday = row[s.birthday]?.toString(),
                            phoneNumber = row[s.phoneNumber],
                            group = GroupDto(id = row[g.id].value, name = row[g.name])
                        )
                    }
            }

            val studentSensitive = studentProfileId?.let { id ->
                StudentsInfoTable
                    .selectAll()
                    .where { StudentsInfoTable.studentId eq id }
                    .singleOrNull()
                    ?.let { row ->
                        StudentSensitiveDto(
                            address = row[StudentsInfoTable.address],
                            passportNumber = row[StudentsInfoTable.passportNumber],
                            school = row[StudentsInfoTable.school],
                            documentNumber = row[StudentsInfoTable.documentNumber],
                            military = row[StudentsInfoTable.military],
                            studentCardNumber = row[StudentsInfoTable.studentCardNumber],
                            studyType = row[StudentsInfoTable.studyType],
                            studyForm = row[StudentsInfoTable.studyForm],
                            status = row[StudentsInfoTable.status],
                            fatherFio = row[StudentsInfoTable.fatherFio],
                            fatherPhone = row[StudentsInfoTable.fatherPhone],
                            fatherAddress = row[StudentsInfoTable.fatherAddress],
                            motherFio = row[StudentsInfoTable.motherFio],
                            motherPhone = row[StudentsInfoTable.motherPhone],
                            motherAddress = row[StudentsInfoTable.motherAddress]
                        )
                    }
            }

            // === 4. Загрузка преподавателя и sensitive данных ===
            val teacherProfile = teacherProfileId?.let { id ->
                val t = TeachersTable
                t.selectAll()
                    .where { t.id eq id }
                    .singleOrNull()
                    ?.let { row ->
                        TeacherDetailsDto(
                            id = row[t.id].value,
                            email = email,
                            surname = row[t.surname],
                            name = row[t.name],
                            lastname = row[t.lastname],
                            birthday = row[t.birthday]?.toString(),
                            phoneNumber = row[t.phoneNumber]
                        )
                    }
            }

            val teacherSensitive = teacherProfileId?.let { id ->
                TeachersInfoTable
                    .selectAll()
                    .where { TeachersInfoTable.teacherId eq id }
                    .singleOrNull()
                    ?.let { row ->
                        TeacherSensitiveDto(
                            address = row[TeachersInfoTable.address],
                            passportNumber = row[TeachersInfoTable.passportNumber],
                            highSchool = row[TeachersInfoTable.highSchool],
                            documentNumber = row[TeachersInfoTable.documentNumber],
                            military = row[TeachersInfoTable.military],
                            degree = row[TeachersInfoTable.degree],
                            title = row[TeachersInfoTable.title],
                            position = row[TeachersInfoTable.position]
                        )
                    }
            }

            // === 5. Формирование ответа ===
            RegisterResponse(
                userId = insertedUserId,
                username = username,
                role = roleNames,
                studentProfile = studentProfile,
                studentSensitive = studentSensitive,
                teacherProfile = teacherProfile,
                teacherSensitive = teacherSensitive
            )
        }
    }
}