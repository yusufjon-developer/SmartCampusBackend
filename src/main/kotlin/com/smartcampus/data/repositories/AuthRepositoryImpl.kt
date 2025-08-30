package com.smartcampus.data.repositories

import com.smartcampus.data.dao.AuthDao
import com.smartcampus.data.dao.SmartCampusProfileDao
import com.smartcampus.data.database.auth.entities.RolesTable
import com.smartcampus.data.database.auth.entities.UserDevicesTable
import com.smartcampus.data.database.auth.entities.UsersTable
import com.smartcampus.domain.models.EmployeeSignInRequest
import com.smartcampus.domain.models.EmployeeSignInResponse
import com.smartcampus.domain.models.StudentSignInRequest
import com.smartcampus.domain.models.StudentSignInResponse
import com.smartcampus.domain.models.auth.RegisterRequest
import com.smartcampus.domain.models.auth.RegisterResponse
import com.smartcampus.domain.repositories.AuthRepository
import com.smartcampus.domain.security.PasswordHasher
import com.smartcampus.domain.security.TokenUtils
import com.smartcampus.domain.utils.Either
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class AuthRepositoryImpl(
    private val authDao: AuthDao,
    private val smartCampusDao: SmartCampusProfileDao,
    private val tokenUtils: TokenUtils,
    private val passwordHasher: PasswordHasher
) : AuthRepository {
    private val log = LoggerFactory.getLogger(AuthRepositoryImpl::class.java)

    override suspend fun signInStudent(request: StudentSignInRequest): StudentSignInResponse {
        val userRow = authDao.findUserByEmailWithRole(request.email)
            ?: throw SecurityException("Student not found or incorrect email.")

        val roleName = userRow[RolesTable.name]
        if (!roleName.equals("Student", ignoreCase = true)) {
            throw SecurityException("Access denied. User is not a student.")
        }

        if (!passwordHasher.checkPassword(request.password, userRow[UsersTable.passwordHash])) {
            throw SecurityException("Invalid password.")
        }

        if (!userRow[UsersTable.isActive]) {
            throw SecurityException("User account is inactive.")
        }

        val userId = userRow[UsersTable.id].value
        val userRoles = listOf(roleName)

        val token = tokenUtils.generateToken(
            userId = userId,
            username = userRow[UsersTable.username],
            roles = userRoles
        )
        return StudentSignInResponse(token)
    }

    override suspend fun signInEmployee(request: EmployeeSignInRequest): EmployeeSignInResponse {
        val userRow = authDao.findUserByEmailWithRole(request.email)
            ?: throw SecurityException("Employee not found or incorrect email.")

        val roleName = userRow[RolesTable.name]
        if (roleName.equals("Student", ignoreCase = true)) {
            throw SecurityException("Access denied. Students cannot sign in as employees.")
        }

        if (!passwordHasher.checkPassword(request.password, userRow[UsersTable.passwordHash])) {
            throw SecurityException("Invalid password.")
        }

        if (!userRow[UsersTable.isActive]) {
            throw SecurityException("User account is inactive.")
        }

        val userId = userRow[UsersTable.id].value
        val userRoles = listOf(roleName)
        val userDeviceRow = authDao.findUserDevice(userId, request.uuid)

        val currentTime = LocalDateTime.now()

        if (userDeviceRow == null) {
            authDao.registerNewDevice(
                userId = userId,
                deviceUuid = request.uuid,
                isApproved = false,
                registeredAt = currentTime,
                lastLoginAt = null
            )
            throw SecurityException("Device registered but requires administrator approval.")
        }

        if (!userDeviceRow[UserDevicesTable.isApproved]) {
            throw SecurityException("Device is not approved. Please contact administrator.")
        }

        authDao.updateUserDeviceLastLogin(userDeviceRow[UserDevicesTable.id].value, currentTime)

        val token = tokenUtils.generateToken(
            userId = userId,
            username = userRow[UsersTable.username],
            roles = userRoles,
            deviceUuid = request.uuid
        )
        return EmployeeSignInResponse(token)
    }

    override suspend fun registerUser(
        request: RegisterRequest,
        passwordHash: String
    ): Either<String, RegisterResponse> {
        // Validate single profile
        if (request.studentProfile != null && request.teacherProfile != null) {
            return Either.Left("Provide only one profile type: student OR teacher")
        }

        val usernameLower = request.username.trim()

        // Quick check (race still possible -> DB unique constraint in auth should guard)
        if (authDao.isUsernameTaken(usernameLower)) {
            return Either.Left("Username already taken")
        }

        var createdStudentId: Int? = null
        var createdTeacherId: Int? = null

        try {
            // 1) create profile in SmartCampusDb
            if (request.studentProfile != null) {
                createdStudentId = smartCampusDao.createStudentWithOptionalInfo(
                    request.studentProfile,
                    request.studentInfo
                )
            } else if (request.teacherProfile != null) {
                createdTeacherId = smartCampusDao.createTeacherWithOptionalInfo(
                    request.teacherProfile,
                    request.teacherInfo
                )
            }

            // 2) find role id in auth db
            val roleId = request.roleName?.let { authDao.findRoleIdByName(it) }

            // 3) create user in auth db
            val createdUser = authDao.createUserAndReturnResponse(
                username = usernameLower,
                passwordHash = passwordHash,
                email = request.email,
                fullName = request.fullName,
                roleId = roleId,
                studentProfileId = createdStudentId,
                teacherProfileId = createdTeacherId
            )

            return Either.Right(createdUser)
        } catch (e: Exception) {
            log.error("Registration failed; compensating created profiles", e)

            try {
                if (createdStudentId != null) smartCampusDao.deleteStudentCascade(createdStudentId)
                if (createdTeacherId != null) smartCampusDao.deleteTeacherCascade(createdTeacherId)
            } catch (comp: Exception) {
                log.error("Compensation failed after registration error", comp)
            }

            return Either.Left("Registration failed: ${e.message ?: "unknown error"}")
        }
    }

}
